package apptest;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.invoicecache.InvoiceCache;

@WireMockAppTestSuite(files = "classpath:/InvoiceCache/", classes = InvoiceCache.class)
@Testcontainers
class InvoiceCacheIT extends AbstractInvoiceCacheAppTest {

	private static final String MARIADB_VERSION = "mariadb:10.6.12";
	private static final String MSSQL_VERSION = "mcr.microsoft.com/mssql/server:2019-latest";

	@Container
	public static MSSQLServerContainer<?> raindanceDb = new MSSQLServerContainer<>(DockerImageName.parse(MSSQL_VERSION))
			.withInitScript("InvoiceCache/sql/init-raindance.sql");

	@Container
	public static MariaDBContainer<?> invoiceDb = new MariaDBContainer<>(DockerImageName.parse(MARIADB_VERSION))
			.withDatabaseName("ms-invoicecache");

	static {
		raindanceDb.start();
		invoiceDb.start();
	}

	@Override
	protected Optional<Duration> getSendRequestAndVerifyResponseDelay() {
		return Optional.of(Duration.ofSeconds(2L));
	}

	/**
	 * get the url, user and password from the container and set them in the context.
	 *
	 * @param registry
	 */
	@DynamicPropertySource
	static void registerProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.raindance-datasource.url", raindanceDb::getJdbcUrl);
		registry.add("spring.raindance-datasource.username", raindanceDb::getUsername);
		registry.add("spring.raindance-datasource.password", raindanceDb::getPassword);
		registry.add("spring.datasource.url", invoiceDb::getJdbcUrl);
		registry.add("spring.datasource.username", invoiceDb::getUsername);
		registry.add("spring.datasource.password", invoiceDb::getPassword);
	}

	@Test
	void test1_findByOrganizationNumber() {
		assertThat(raindanceDb.isRunning()).isTrue();
		assertThat(invoiceDb.isRunning()).isTrue();
		setupCall()
				.withServicePath("/invoices?page=1&limit=100&partyIds=fb2f0290-3820-11ed-a261-0242ac120002&partyIds=fb2f0290-3820-11ed-a261-0242ac120003")
				.withHttpMethod(HttpMethod.GET)
				.withExpectedResponseStatus(HttpStatus.OK)
				.withExpectedResponse("expected.json")
				.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_findByOrgNumberAndDatesWithPaging_shouldFindBetweenDates() {
		assertThat(raindanceDb.isRunning()).isTrue();
		assertThat(invoiceDb.isRunning()).isTrue();
		setupCall()
				.withServicePath("/invoices?page=1&limit=10&invoiceDateFrom=" + now().minusMonths(12) + "&invoiceDateTo=" + now().minusMonths(10) + "&partyIds=fb2f0290-3820-11ed-a261-0242ac120002")
				.withHttpMethod(HttpMethod.GET)
				.withExpectedResponseStatus(HttpStatus.OK)
				.withExpectedResponse("expected.json")
				.sendRequestAndVerifyResponse();
	}

	@Test
	void test3_findSingleInvoiceUsingAllFields_shouldFindByCriterias() {
		assertThat(raindanceDb.isRunning()).isTrue();
		assertThat(invoiceDb.isRunning()).isTrue();
		setupCall()
				.withServicePath("/invoices?page=1&limit=100&invoiceDateFrom=" + now().minusMonths(11) + "&invoiceDateTo=" + now().minusMonths(10) + "&dueDateFrom=" + now().minusMonths(10) +
						"&dueDateTo=" + now().minusMonths(10) + "&partyIds=fb2f0290-3820-11ed-a261-0242ac120002&ocrNumber=34563464&invoiceNumbers=53626804")
				.withHttpMethod(HttpMethod.GET)
				.withExpectedResponseStatus(HttpStatus.OK)
				.withExpectedResponse("expected.json")
				.sendRequestAndVerifyResponse();
	}

	@Test
	void test4_noInvoiceFound_shouldReturnEmptyList() {
		assertThat(raindanceDb.isRunning()).isTrue();
		assertThat(invoiceDb.isRunning()).isTrue();
		setupCall()
				.withServicePath("/invoices?page=1&limit=100&invoiceDateFrom=2022-08-09&invoiceDateTo=2022-08-09&partyIds=fb2f0290-3820-11ed-a261-0242ac120002&ocrNumber=34563464&invoiceNumber=53626804")
				.withHttpMethod(HttpMethod.GET)
				.withExpectedResponseStatus(HttpStatus.OK)
				.withExpectedResponse("expected.json")
				.sendRequestAndVerifyResponse();
	}

	@Test
	void test5_searchInvoice_withoutPartyID_shouldReturnWithoutPartyId() {
		assertThat(raindanceDb.isRunning()).isTrue();
		assertThat(invoiceDb.isRunning()).isTrue();
		setupCall()
				.withServicePath("/invoices?page=1&limit=100&invoiceDateFrom=" + now().minusMonths(12) + "&invoiceDateTo=" + now().minusMonths(11) + "&invoiceNumbers=53626800")
				.withHttpMethod(HttpMethod.GET)
				.withExpectedResponseStatus(HttpStatus.OK)
				.withExpectedResponse("expected.json")
				.sendRequestAndVerifyResponse();
	}
}
