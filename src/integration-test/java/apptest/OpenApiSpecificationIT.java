package apptest;

import static apptest.AbstractInvoiceCacheAppTest.MARIADB_VERSION;
import static apptest.AbstractInvoiceCacheAppTest.MSSQL_VERSION;
import static java.nio.file.Files.writeString;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import net.javacrumbs.jsonunit.core.Option;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import se.sundsvall.dept44.util.ResourceUtils;
import se.sundsvall.invoicecache.Application;

@ActiveProfiles("it")
@SpringBootTest(
	webEnvironment = WebEnvironment.RANDOM_PORT,
	classes = Application.class,
	properties = {
		"spring.main.banner-mode=off",
		"logging.level.se.sundsvall.dept44.payload=OFF",
		"wiremock.server.port=10101"
	})
@Testcontainers
class OpenApiSpecificationIT {

	private static final YAMLMapper YAML_MAPPER = new YAMLMapper();

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

	@Value("${openapi.name}")
	private String openApiName;

	@Value("${openapi.version}")
	private String openApiVersion;

	@Value("classpath:/openapi.yaml")
	private Resource openApiResource;

	@Autowired
	private TestRestTemplate restTemplate;

	/**
	 * get the url, user and password from the container and set them in the context.
	 *
	 * @param registry
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

	@Test
	void compareOpenApiSpecifications() throws IOException {
		final String existingOpenApiSpecification = ResourceUtils.asString(openApiResource);
		final String currentOpenApiSpecification = getCurrentOpenApiSpecification();

		writeString(Path.of("target/openapi.yml"), currentOpenApiSpecification);

		assertThatJson(toJson(currentOpenApiSpecification))
			.withOptions(List.of(Option.IGNORING_ARRAY_ORDER))
			.whenIgnoringPaths("servers")
			.isEqualTo(toJson(existingOpenApiSpecification));
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
