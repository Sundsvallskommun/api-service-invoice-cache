package apptest;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import jcifs.smb.SmbFile;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;
import org.testcontainers.mssqlserver.MSSQLServerContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.invoicecache.Application;
import se.sundsvall.invoicecache.integration.db.PdfRepository;
import se.sundsvall.invoicecache.integration.storage.StorageSambaProperties;

import static apptest.AbstractInvoiceCacheAppTest.MARIADB_VERSION;
import static apptest.AbstractInvoiceCacheAppTest.MSSQL_VERSION;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockAppTestSuite(files = "classpath:/SambaImportIT", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
@Testcontainers
class SambaImportIT extends AbstractAppTest {

	@Container
	public static final MSSQLServerContainer raindanceDb = new MSSQLServerContainer(DockerImageName.parse(MSSQL_VERSION))
		.withInitScript("InvoiceCache/sql/init-raindance.sql");
	@Container
	public static final MariaDBContainer invoiceDb = new MariaDBContainer(DockerImageName.parse(MARIADB_VERSION))
		.withDatabaseName("ms-invoicecache");
	@Container
	public static final GenericContainer<?> smbContainer = new GenericContainer<>("dockurr/samba:4.22.6")
		.withExposedPorts(445)
		.withEnv(Map.of(
			"NAME", "ocp",
			"USER", "user",
			"PASS", "password"))
		.withCopyFileToContainer(
			MountableFile.forClasspathResource("test-directory"),
			"/storage");
	private static final String MUNICIPALITY_ID = "2281";
	private static final String IMPORT_URL = "smb://localhost:%d/%s/import/%s";
	private static final String SAMBA_FILE_PATH = "smb://localhost:%d/%s/%s/%s/%s/%s.pdf";
	private static int port;

	@Autowired
	private PdfRepository pdfRepository;

	@Autowired
	private StorageSambaProperties storageSambaProperties;

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

	@BeforeAll
	static void init() throws IOException, InterruptedException {
		smbContainer.execInContainer("sh", "-c", "chmod -R 777 /storage");
	}

	@Test
	void test01_importSambaZips_happyPathAndDuplicateAndMalformed() throws Exception {
		setupCall()
			.withServicePath("/" + MUNICIPALITY_ID + "/import/samba-zips")
			.withHttpMethod(HttpMethod.POST)
			.withExpectedResponseStatus(HttpStatus.ACCEPTED)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		Awaitility.await()
			.atMost(Duration.ofSeconds(30))
			.pollInterval(Duration.ofSeconds(1))
			.untilAsserted(() -> {
				assertThat(pdfRepository.findByFilenameAndMunicipalityId("happyOne.pdf", MUNICIPALITY_ID)).isPresent();
				assertThat(pdfRepository.findByFilenameAndMunicipalityId("happyTwo.pdf", MUNICIPALITY_ID)).isPresent();
				assertThat(pdfRepository.findByFilenameAndMunicipalityId("freshOne.pdf", MUNICIPALITY_ID)).isPresent();
				assertThat(happyZipDeleted()).isTrue();
			});

		// happy-path rows look right
		final var imported = pdfRepository.findByFilenameAndMunicipalityId("happyOne.pdf", MUNICIPALITY_ID).orElseThrow();
		assertThat(imported.getDocument()).isNull();
		assertThat(imported.getMovedAt()).isNotNull();
		assertThat(imported.getTruncatedAt()).isNotNull();
		assertThat(imported.getFileHash()).isNotBlank();
		assertThat(imported.getInvoiceNumber()).isEqualTo("HAPPY-1");
		assertThat(imported.getInvoiceId()).isEqualTo("HAPPY-1");
		assertThat(imported.getInvoiceDebtorLegalId()).isEqualTo("CUST-100");
		assertThat(imported.getInvoiceIssuerLegalId()).isEqualTo("5565027223");

		// PDF is actually on Samba at the hash path
		assertSambaFileExists(imported.getFileHash());
		assertSambaFileExists(pdfRepository.findByFilenameAndMunicipalityId("happyTwo.pdf", MUNICIPALITY_ID).orElseThrow().getFileHash());
		assertSambaFileExists(pdfRepository.findByFilenameAndMunicipalityId("freshOne.pdf", MUNICIPALITY_ID).orElseThrow().getFileHash());

		// Duplicate pre-seeded row wasn't overwritten (file hash stayed as-is, document stays null)
		final var preseeded = pdfRepository.findByFilenameAndMunicipalityId("dup.pdf", MUNICIPALITY_ID).orElseThrow();
		assertThat(preseeded.getFileHash()).isEqualTo("preseeded-hash");

		// Malformed zip still sits in the import folder
		assertThat(malformedZipStillPresent()).isTrue();

		// Duplicate zip (789.zip) is NOT deleted either because the duplicate is treated as skipped; since the
		// non-duplicate sibling imported, failed=0 and the zip gets deleted. Verify it was deleted.
		assertThat(duplicateZipDeleted()).isTrue();
	}

	private boolean happyZipDeleted() throws IOException {
		try (final var file = new SmbFile(IMPORT_URL.formatted(port, storageSambaProperties.share(), "123.zip"), storageSambaProperties.cifsContext())) {
			return !file.exists();
		}
	}

	private boolean malformedZipStillPresent() throws IOException {
		try (final var file = new SmbFile(IMPORT_URL.formatted(port, storageSambaProperties.share(), "456.zip"), storageSambaProperties.cifsContext())) {
			return file.exists();
		}
	}

	private boolean duplicateZipDeleted() throws IOException {
		try (final var file = new SmbFile(IMPORT_URL.formatted(port, storageSambaProperties.share(), "789.zip"), storageSambaProperties.cifsContext())) {
			return !file.exists();
		}
	}

	private void assertSambaFileExists(final String fileHash) throws IOException {
		final var filePath = SAMBA_FILE_PATH.formatted(
			port, storageSambaProperties.share(),
			storageSambaProperties.serviceDirectory(),
			storageSambaProperties.environment(),
			fileHash.substring(0, 2),
			fileHash);
		try (final var file = new SmbFile(filePath, storageSambaProperties.cifsContext())) {
			assertThat(file.exists()).as("samba file %s", filePath).isTrue();
			assertThat(file.isFile()).isTrue();
		}
	}
}
