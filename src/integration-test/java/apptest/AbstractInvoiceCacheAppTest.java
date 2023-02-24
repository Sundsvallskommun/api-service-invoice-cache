package apptest;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sundsvall.dept44.test.AbstractAppTest;

public class AbstractInvoiceCacheAppTest extends AbstractAppTest {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractInvoiceCacheAppTest.class);
    
    protected Optional<Duration> getSendRequestAndVerifyResponseDelay() {
        return Optional.empty();
    }
    
    @Override
    public AbstractAppTest sendRequestAndVerifyResponse() {
        getSendRequestAndVerifyResponseDelay().ifPresent(delay -> {
            LOG.info("Sleeping {} seconds before sending request", delay.getSeconds());
    
            try {
                TimeUnit.SECONDS.sleep(delay.getSeconds());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
    
        });
        
        return super.sendRequestAndVerifyResponse();
    }
}
