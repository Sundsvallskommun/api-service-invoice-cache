package apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.invoicecache.Application;

/**
 * Test for checking that backup invoices are read when we fail to read invoices from raindance. Only initializes the DB from raindance, no data is inserted which fakes that we couldn't fetch anything. The "local" DB only has the backup table populated,
 * which should be transferred to the invoice table and then read by the test.
 */
@WireMockAppTestSuite(files = "classpath:/InvoiceCacheBackup/",
	classes = Application.class)
@Testcontainers
@Sql({
	"/InvoiceCacheBackup/sql/data-local.sql"
})
public class InvoiceCacheBackupIT extends AbstractInvoiceCacheAppTest {

	private static final String PATH = "/2281/invoices";

	@Container
	public static MSSQLServerContainer<?> raindanceDb = new MSSQLServerContainer<>(DockerImageName.parse(MSSQL_VERSION))
		.withInitScript("InvoiceCacheBackup/sql/init-raindance.sql");

	@Container
	public static MariaDBContainer<?> invoiceDb = new MariaDBContainer<>(DockerImageName.parse(MARIADB_VERSION))
		.withDatabaseName("ms-invoicecache");

	static {
		raindanceDb.start();
		invoiceDb.start();
	}

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
	}

	@Override
	protected Optional<Duration> getSendRequestAndVerifyResponseDelay() {
		return Optional.of(Duration.ofSeconds(2L));
	}

	@Test
	void test1_testBackupsAreRead_whenCouldNotFetchInvoicesFromRaindance() {
		assertThat(raindanceDb.isRunning()).isTrue();
		assertThat(invoiceDb.isRunning()).isTrue();
		setupCall()
			.withServicePath(PATH + "?page=1&limit=100&partyIds=fb2f0290-3820-11ed-a261-0242ac120002")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("expected.json")
			.sendRequestAndVerifyResponse();
	}

}
