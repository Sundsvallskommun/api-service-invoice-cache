package se.sundsvall.invoicecache;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

import se.sundsvall.dept44.ServiceApplication;

@EnableFeignClients
@ServiceApplication
@EnableCaching
@EnableBatchProcessing
@EnableAsync
@EnableSchedulerLock(defaultLockAtMostFor = "PT10M")
public class InvoiceCache {
    public static void main(String[] args) {
        SpringApplication.run(InvoiceCache.class, args);
    }
}
