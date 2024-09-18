package se.sundsvall.invoicecache;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

import se.sundsvall.dept44.ServiceApplication;
import se.sundsvall.dept44.util.jacoco.ExcludeFromJacocoGeneratedCoverageReport;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;

@EnableFeignClients
@ServiceApplication
@EnableCaching
@EnableBatchProcessing
@EnableAsync
@EnableSchedulerLock(defaultLockAtMostFor = "PT10M")
@ExcludeFromJacocoGeneratedCoverageReport
public class Application {

	public static void main(final String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
