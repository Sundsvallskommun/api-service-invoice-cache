package apptest;

import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import se.sundsvall.dept44.test.AbstractAppTest;

/**
 * Each concrete app test binds its Spring context to its own Testcontainers databases via {@code @DynamicPropertySource},
 * so contexts are never reusable across classes. Closing the context when the class finishes shuts down its Hikari pool
 * and every-second {@code @Dept44Scheduled} invoice job at that point; otherwise the cached context lingers and keeps
 * hitting a container Testcontainers has already stopped, spamming "Connection refused". Capping the context-cache size
 * is not enough here: eviction is lazy (only when the next class's context loads, seconds later, after its containers
 * have booted), leaving a long window in which the previous context's scheduler keeps firing.
 */
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public abstract class AbstractInvoiceCacheAppTest extends AbstractAppTest {

	public static final String MARIADB_VERSION = "mariadb:10.6.12";
	public static final String MSSQL_VERSION = "mcr.microsoft.com/mssql/server:2019-latest";

	private static final Logger LOG = LoggerFactory.getLogger(AbstractInvoiceCacheAppTest.class);

	protected Optional<Duration> getSendRequestAndVerifyResponseDelay() {
		return Optional.empty();
	}

	@Override
	public AbstractAppTest sendRequestAndVerifyResponse() {
		getSendRequestAndVerifyResponseDelay().ifPresent(delay -> {
			LOG.info("Sleeping {} seconds before sending request", delay.getSeconds());

			await().pollDelay(delay).untilAsserted(() -> {
				// Delay completed
			});

		});

		return super.sendRequestAndVerifyResponse();
	}
}
