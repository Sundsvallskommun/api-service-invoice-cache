package apptest;

import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sundsvall.dept44.test.AbstractAppTest;

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
