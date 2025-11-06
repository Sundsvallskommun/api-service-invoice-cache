package se.sundsvall.invoicecache.service.batch.invoice;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;
import se.sundsvall.dept44.util.ResourceUtils;
import se.sundsvall.invoicecache.integration.db.InvoiceRepository;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;
import se.sundsvall.invoicecache.integration.raindance.RaindanceQueryResultDto;
import se.sundsvall.invoicecache.integration.raindance.mapper.RaindanceRowMapper;

@Configuration
@EnableBatchProcessing(dataSourceRef = "raindanceDataSource")
public class BatchConfig {

	private static final Logger LOG = LoggerFactory.getLogger(BatchConfig.class);
	private static final int CHUNK_SIZE = 2000;

	public static final String RAINDANCE_JOB_NAME = "raindanceInvoiceJob";

	private final String sqlString;

	private final RaindanceEntityProcessor raindanceEntityProcessor;
	private final InvoiceRepository invoiceRepository;
	private final InvoiceListener invoiceListener;

	public BatchConfig(@Value("classpath:${raindance.sql.filename}") final Resource sqlResource,
		final RaindanceEntityProcessor raindanceEntityProcessor,
		final InvoiceRepository invoiceRepository,
		final InvoiceListener invoiceListener) {
		this.raindanceEntityProcessor = raindanceEntityProcessor;
		this.invoiceRepository = invoiceRepository;
		this.sqlString = ResourceUtils.asString(sqlResource);
		this.invoiceListener = invoiceListener;
	}

	/**
	 * Read invoices from the raindance-DB
	 */
	public JdbcCursorItemReader<RaindanceQueryResultDto> raindanceReader(final DataSource dataSource) {
		return new JdbcCursorItemReaderBuilder<RaindanceQueryResultDto>()
			.dataSource(dataSource)
			.name("raindanceItemReader")
			.sql(sqlString)
			.saveState(false)   // No need to restart the job
			.fetchSize(CHUNK_SIZE)
			.verifyCursorPosition(false)    // For some reason it fails without this, even with a "clean" database.
			.rowMapper(new RaindanceRowMapper())  // Calls mapRow(ResultSet resultSet, int rowNum) for each row which maps each row to a RaindanceQueryResultDto
			.build();
	}

	/**
	 * Defines which steps to use, they are: - Read from raindance - "Process" the received data, basically turn them into
	 * entities - Write it to our own database.
	 */
	public Step step1(final DataSource dataSource, final JobRepository jobRepository, final PlatformTransactionManager transactionManager) {
		LOG.info("Creating step1 for reading, processing and writing invoices.");
		return new StepBuilder("invoiceStep", jobRepository)
			.<RaindanceQueryResultDto, InvoiceEntity>chunk(CHUNK_SIZE, transactionManager)
			.faultTolerant()
			.reader(raindanceReader(dataSource))
			.processor(raindanceEntityProcessor)
			.writer(invoiceEntityWriter())
			.listener(invoiceListener)
			.build();
	}

	/**
	 * Writes items to our DB. No save method specified which implies to use "saveAll"-method
	 */
	public RepositoryItemWriter<InvoiceEntity> invoiceEntityWriter() {
		return new RepositoryItemWriterBuilder<InvoiceEntity>()
			.repository(invoiceRepository)
			.build();
	}

	/**
	 * Defines the job and which step to start in the job.
	 *
	 * @param  dataSource the datasource to use
	 * @return            the job
	 */
	@Bean(name = RAINDANCE_JOB_NAME)
	Job startJob(@Qualifier("raindanceDataSource") final DataSource dataSource, final JobRepository jobRepository, final PlatformTransactionManager transactionManager) {
		return new JobBuilder(RAINDANCE_JOB_NAME, jobRepository)
			.start(step1(dataSource, jobRepository, transactionManager))
			.build();
	}
}
