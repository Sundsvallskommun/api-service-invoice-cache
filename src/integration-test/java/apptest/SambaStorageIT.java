package apptest;

import static apptest.AbstractInvoiceCacheAppTest.MARIADB_VERSION;
import static apptest.AbstractInvoiceCacheAppTest.MSSQL_VERSION;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;
import java.util.Map;
import jcifs.CIFSContext;
import jcifs.smb.SmbFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.invoicecache.Application;
import se.sundsvall.invoicecache.integration.db.PdfRepository;
import se.sundsvall.invoicecache.integration.storage.StorageSambaProperties;

@WireMockAppTestSuite(files = "classpath:/SambaStorageIT", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
@Testcontainers
class SambaStorageIT extends AbstractAppTest {

	private static final String SAMBA_FILE_PATH = "smb://localhost:%d/%s/%s/%s/%s/%s.pdf";

	private static int port;
	private CIFSContext cifsContext;

	@Autowired
	private PdfRepository pdfRepository;

	@Autowired
	private StorageSambaProperties storageSambaProperties;

	/**
	 * The SMB container for storing transferred invoice PDFs. This is used by the StorageScheduler to transfer files from the database to the SMB share.
	 */
	@Container
	public static GenericContainer<?> smbContainer = new GenericContainer<>("dockurr/samba")
		.withExposedPorts(445)
		.withEnv(Map.of(
			"NAME", "ocp",
			"USER", "user",
			"PASS", "password"))

		.withFileSystemBind(
			Paths.get("src/integration-test/resources/test-directory")
				.toAbsolutePath()
				.toString(),
			"/storage",
			BindMode.READ_WRITE);

	/**
	 * The MariaDB container for InvoiceCache. This is used for storing invoice PDFs.
	 */
	@Container
	public static MariaDBContainer<?> invoiceDb = new MariaDBContainer<>(DockerImageName.parse(MARIADB_VERSION))
		.withDatabaseName("ms-invoicecache");

	/**
	 * The MSSQL container with Raindance initialization script. This is not used directly in the tests but is required to start the application context successfully.
	 */
	@Container
	public static MSSQLServerContainer<?> raindanceDb = new MSSQLServerContainer<>(DockerImageName.parse(MSSQL_VERSION))
		.withInitScript("InvoiceCache/sql/init-raindance.sql");

	/**
	 * Get the url, user and password from the container and set them in the context.
	 */
	@DynamicPropertySource
	static void registerProperties(final DynamicPropertyRegistry registry) {
		registry.add("spring.raindance-datasource.url", raindanceDb::getJdbcUrl);
		registry.add("spring.raindance-datasource.username", raindanceDb::getUsername);
		registry.add("spring.raindance-datasource.password", raindanceDb::getPassword);
		registry.add("spring.datasource.url", invoiceDb::getJdbcUrl);
		registry.add("spring.datasource.username", invoiceDb::getUsername);
		registry.add("spring.datasource.password", invoiceDb::getPassword);

		port = smbContainer.getMappedPort(445);
		registry.add("samba.port", () -> port);

		registry.add("invoice.scheduled.cron", () -> "-");
	}

	@BeforeEach
	void setUp() throws Exception {
		// Initialize the JCIFS context, if needed
		if (cifsContext == null) {
			cifsContext = storageSambaProperties.cifsContext();
		}
	}

	@Test
	void test01_transferFile() throws Exception {

		// Fetches an existing entity from the repository that has not been moved or truncated
		var entity = pdfRepository.findByInvoiceNumberAndInvoiceIdAndMunicipalityId("1", "1", "2281").orElseThrow();
		assertThat(entity.getMovedAt()).isNull();
		assertThat(entity.getTruncatedAt()).isNull();
		assertThat(entity.getDocument()).isNotNull();

		setupCall()
			.withServicePath("/2281/scheduler/transfer")
			.withHttpMethod(HttpMethod.POST)
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		var transferredEntity = pdfRepository.findByInvoiceNumberAndInvoiceIdAndMunicipalityId("1", "1", "2281").orElseThrow();
		assertThat(transferredEntity.getMovedAt()).isNotNull();
		assertThat(transferredEntity.getTruncatedAt()).isNull();
		assertThat(transferredEntity.getDocument()).isNotNull();

		var fileDirectory = transferredEntity.getFileHash().substring(0, 2);

		try (var transferredFile = new SmbFile(SAMBA_FILE_PATH.formatted(
			port, storageSambaProperties.share(),
			storageSambaProperties.serviceDirectory(),
			storageSambaProperties.environment(), fileDirectory,
			transferredEntity.getFileHash()), storageSambaProperties.cifsContext())) {

			assertThat(transferredFile.exists()).isTrue();
			assertThat(transferredFile.isFile()).isTrue();
			assertThat(transferredFile.getInputStream().readAllBytes()).isEqualTo(transferredEntity.getDocument().getBinaryStream().readAllBytes());
		}

	}

	@Test
	void test02_truncateFile() {
		// Fetches an existing entity from the repository that has been moved but not truncated
		var entity = pdfRepository.findByInvoiceNumberAndInvoiceIdAndMunicipalityId("2", "2", "2281").orElseThrow();
		assertThat(entity.getMovedAt()).isNotNull();
		assertThat(entity.getTruncatedAt()).isNull();
		assertThat(entity.getDocument()).isNotNull();

		setupCall()
			.withServicePath("/2281/scheduler/truncate")
			.withHttpMethod(HttpMethod.POST)
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		var truncatedEntity = pdfRepository.findByInvoiceNumberAndInvoiceIdAndMunicipalityId("2", "2", "2281").orElseThrow();
		assertThat(truncatedEntity.getMovedAt()).isNotNull();
		assertThat(truncatedEntity.getTruncatedAt()).isNotNull();
		assertThat(truncatedEntity.getDocument()).isNull(); // Verify that the document has been truncated

	}

	@Test
	void test03_getTruncatedFileByFilename() {
		setupCall()
			.withServicePath("/2281/invoices/my-invoice.pdf")
			.withHttpMethod(HttpMethod.GET)
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_getTruncatedFileByIssuerLegalIdAndInvoiceNumber() {
		setupCall()
			.withServicePath("/2281/invoices/issuerLegalId/invoiceNumber/pdf")
			.withHttpMethod(HttpMethod.GET)
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

}
