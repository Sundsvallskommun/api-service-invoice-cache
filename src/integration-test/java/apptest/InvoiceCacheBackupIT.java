package apptest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.invoicecache.InvoiceCache;

/**
 * Test for checking that backup invoices are read when we fail to read invoices from raindance.
 * Only initializes the DB from raindance, no data is inserted which fakes that we couldn't fetch anything.
 * The "local" DB only has the backup table populated, which should be transferred to the invoice table and then read by the test.
 */
@WireMockAppTestSuite(files = "classpath:/InvoiceCacheBackup/",
        classes = InvoiceCache.class)
@Testcontainers
public class InvoiceCacheBackupIT extends AbstractInvoiceCacheAppTest {
    
    @Container
    public static MariaDBContainer<?> raindanceDb = new MariaDBContainer<>(DockerImageName.parse("mariadb:10.9.2"))
            .withDatabaseName("raindance")
            .withUsername("root")
            .withPassword("")
            .withInitScript("InvoiceCacheBackup/sql/init-raindance.sql");
    
    @Container
    public static MariaDBContainer<?> invoiceDb = new MariaDBContainer<>(DockerImageName.parse("mariadb:10.9.2"))
            .withDatabaseName("ms-invoicecache")
            .withUsername("root")
            .withPassword("")
            .withInitScript("InvoiceCacheBackup/sql/init-local.sql");
    
    static {
        Stream.of(raindanceDb, invoiceDb).parallel().forEach(MariaDBContainer::start);
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
    void test1_testBackupsAreRead_whenCouldntFetchInvoicesFromRaindance() {
        assertThat(raindanceDb.isRunning()).isTrue();
        assertThat(invoiceDb.isRunning()).isTrue();
        setupCall()
                .withServicePath("/invoices?page=1&limit=100&partyIds=fb2f0290-3820-11ed-a261-0242ac120002")
                .withHttpMethod(HttpMethod.GET)
                .withExpectedResponseStatus(HttpStatus.OK)
                .withExpectedResponse("expected.json")
                .sendRequestAndVerifyResponse();
    }
}
