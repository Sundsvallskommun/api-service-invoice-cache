package se.sundsvall.invoicecache;

import static org.springframework.boot.SpringApplication.run;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import se.sundsvall.dept44.ServiceApplication;
import se.sundsvall.dept44.util.jacoco.ExcludeFromJacocoGeneratedCoverageReport;

@EnableFeignClients
@ServiceApplication
@EnableCaching
@EnableBatchProcessing
@EnableAsync
@ExcludeFromJacocoGeneratedCoverageReport
public class Application {

	public static void main(final String... args) {
		run(Application.class, args);
	}

}
