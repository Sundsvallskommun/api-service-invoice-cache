package apptest;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;

import java.time.Duration;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.Resource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.dept44.util.ResourceUtils;
import se.sundsvall.invoicecache.InvoiceCache;

@WireMockAppTestSuite(files = "classpath:/InvoiceCache/", classes = InvoiceCache.class)
@Testcontainers
class OpenApiSpecificationIT extends AbstractInvoiceCacheAppTest {

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

	private static final YAMLMapper YAML_MAPPER = new YAMLMapper();

	@Value("${openapi.name}")
	private String openApiName;
	@Value("${openapi.version}")
	private String openApiVersion;

	@Value("classpath:/openapi.yaml")
	private Resource openApiResource;

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void compareOpenApiSpecifications() {
		final String existingOpenApiSpecification = ResourceUtils.asString(openApiResource);
		final String currentOpenApiSpecification = getCurrentOpenApiSpecification();

		assertThatJson(toJson(existingOpenApiSpecification))
			.withOptions(IGNORING_ARRAY_ORDER)
			.whenIgnoringPaths("servers")
			.isEqualTo(toJson(currentOpenApiSpecification));
	}

	/**
	 * Fetches and returns the current OpenAPI specification in YAML format.
	 *
	 * @return the current OpenAPI specification
	 */
	private String getCurrentOpenApiSpecification() {
		final var uri = UriComponentsBuilder.fromPath("/api-docs.yaml")
			.buildAndExpand(openApiName, openApiVersion)
			.toUri();

		return restTemplate.getForObject(uri, String.class);
	}

	/**
	 * Attempts to convert the given YAML (no YAML-check...) to JSON.
	 *
	 * @param  yaml the YAML to convert
	 * @return      a JSON string
	 */
	private String toJson(final String yaml) {
		try {
			return YAML_MAPPER.readTree(yaml).toString();
		} catch (final JsonProcessingException e) {
			throw new IllegalStateException("Unable to convert YAML to JSON", e);
		}
	}
}
