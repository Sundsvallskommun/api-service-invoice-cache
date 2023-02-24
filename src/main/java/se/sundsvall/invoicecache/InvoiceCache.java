package se.sundsvall.invoicecache;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

import se.sundsvall.dept44.ServiceApplication;

@EnableFeignClients
@ServiceApplication
@EnableCaching
@EnableBatchProcessing
public class InvoiceCache {
    public static void main(String[] args) {
        SpringApplication.run(InvoiceCache.class, args);
    }
}
