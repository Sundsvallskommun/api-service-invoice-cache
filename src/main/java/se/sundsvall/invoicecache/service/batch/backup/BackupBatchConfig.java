package se.sundsvall.invoicecache.service.batch.backup;

import java.util.HashMap;
import java.util.Map;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import se.sundsvall.invoicecache.integration.db.BackupInvoiceRepository;
import se.sundsvall.invoicecache.integration.db.InvoiceRepository;
import se.sundsvall.invoicecache.integration.db.entity.BackupInvoiceEntity;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;

/**
 * All configuration for backup and restore of backups.
 */
@Configuration
@EnableBatchProcessing(dataSourceRef = "batchDataSource")
public class BackupBatchConfig {

	public static final String BACKUP_JOB_NAME = "backupJob";
	public static final String RESTORE_BACKUP_JOB_NAME = "restoreBackupJob";
	private static final int CHUNK_SIZE = 2000;
	private static final String SORT_ON = "invoiceNumber";
	private final InvoiceRepository invoiceRepository;
	private final BackupInvoiceRepository backupRepository;
	private final BackupProcessor backupProcessor;

	private final RestoreBackupProcessor restoreBackupProcessor;
	private final BackupListener backupListener;
	private final RestoreBackupListener restoreBackupListener;

	public BackupBatchConfig(
		final InvoiceRepository invoiceRepository,
		final BackupInvoiceRepository backupRepository,
		final BackupProcessor backupProcessor,
		final RestoreBackupProcessor restoreBackupProcessor,
		final BackupListener backupListener,
		final RestoreBackupListener restoreBackupListener) {
		this.invoiceRepository = invoiceRepository;
		this.backupRepository = backupRepository;
		this.backupProcessor = backupProcessor;
		this.restoreBackupProcessor = restoreBackupProcessor;
		this.backupListener = backupListener;
		this.restoreBackupListener = restoreBackupListener;
	}

	public RepositoryItemReader<InvoiceEntity> invoiceReader() {
		return new RepositoryItemReaderBuilder<InvoiceEntity>()
			.repository(invoiceRepository)
			.sorts(getSorting())
			.saveState(false)
			.name("invoiceToBackupReader")
			.methodName("findAll")
			.pageSize(CHUNK_SIZE)
			.build();
	}

	public RepositoryItemWriter<BackupInvoiceEntity> invoiceBackupWriter() {
		return new RepositoryItemWriterBuilder<BackupInvoiceEntity>()
			.repository(backupRepository)
			.build();
	}

	public Step backupStep(final JobRepository jobRepository, final PlatformTransactionManager transactionManager) {
		return new StepBuilder("backupStep", jobRepository)
			.<InvoiceEntity, BackupInvoiceEntity>chunk(CHUNK_SIZE, transactionManager)
			.reader(invoiceReader())
			.processor(backupProcessor)
			.writer(invoiceBackupWriter())
			.listener(backupListener)
			.build();
	}

	@Bean(name = BACKUP_JOB_NAME)
	Job backupJob(final JobRepository jobRepository, final PlatformTransactionManager transactionManager) {
		return new JobBuilder(BACKUP_JOB_NAME, jobRepository)
			.start(backupStep(jobRepository, transactionManager))
			.build();
	}

	// --------------------------------//
	// Restore backup batch config below
	// --------------------------------//

	public RepositoryItemReader<BackupInvoiceEntity> invoiceBackupReader() {
		return new RepositoryItemReaderBuilder<BackupInvoiceEntity>()
			.repository(backupRepository)
			.name("backupToInvoiceReader")
			.sorts(getSorting())
			.methodName("findAll")
			.pageSize(CHUNK_SIZE)
			.build();
	}

	public RepositoryItemWriter<InvoiceEntity> backupToInvoiceEntityWriter() {
		return new RepositoryItemWriterBuilder<InvoiceEntity>()
			.repository(invoiceRepository)
			.build();
	}

	public Step restoreBackupStep(final JobRepository jobRepository, final PlatformTransactionManager transactionManager) {
		return new StepBuilder("restoreBackupStep", jobRepository)
			.<BackupInvoiceEntity, InvoiceEntity>chunk(CHUNK_SIZE, transactionManager)
			.reader(invoiceBackupReader())
			.processor(restoreBackupProcessor)
			.writer(backupToInvoiceEntityWriter())
			.listener(restoreBackupListener)
			.build();
	}

	@Bean(name = RESTORE_BACKUP_JOB_NAME)
	Job restoreBackupJob(final JobRepository jobRepository, final PlatformTransactionManager transactionManager) {
		return new JobBuilder(RESTORE_BACKUP_JOB_NAME, jobRepository)
			.start(restoreBackupStep(jobRepository, transactionManager))
			.build();
	}

	/**
	 * Sort by invoice number when reading from our own database.
	 *
	 * @return sorting strategy
	 */
	private Map<String, Sort.Direction> getSorting() {
		final Map<String, Sort.Direction> sorting = new HashMap<>();
		sorting.put(SORT_ON, Sort.Direction.ASC);

		return sorting;
	}
}
