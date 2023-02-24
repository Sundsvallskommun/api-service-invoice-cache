package se.sundsvall.invoicecache.service.batch.backup;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import se.sundsvall.invoicecache.integration.db.BackupInvoiceRepository;
import se.sundsvall.invoicecache.integration.db.InvoiceEntityRepository;
import se.sundsvall.invoicecache.integration.db.entity.BackupInvoiceEntity;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * All configuration for backup and restore of backups.
 */
@Configuration
public class BackupBatchConfig {
    
    private static final int CHUNK_SIZE = 2000;
    public static final String BACKUP_JOB_NAME = "backupJob";
    public static final String RESTORE_BACKUP_JOB_NAME = "restoreBackupJob";
    
    private final InvoiceEntityRepository invoiceRepository;
    private final BackupInvoiceRepository backupRepository;
    private final StepBuilderFactory stepBuilderFactory;
    private final BackupProcessor backupProcessor;
    private final JobBuilderFactory jobBuilderFactory;
    
    private final RestoreBackupProcessor restoreBackupProcessor;
    private final BackupListener backupListener;
    private final RestoreBackupListener restoreBackupListener;
    
    public BackupBatchConfig(final InvoiceEntityRepository invoiceRepository, final BackupInvoiceRepository backupRepository, final StepBuilderFactory stepBuilderFactory,
            final BackupProcessor backupProcessor, final RestoreBackupProcessor restoreBackupProcessor, final JobBuilderFactory jobBuilderFactory, final BackupListener backupListener,
            final RestoreBackupListener restoreBackupListener) {
        this.invoiceRepository = invoiceRepository;
        this.backupRepository = backupRepository;
        this.stepBuilderFactory = stepBuilderFactory;
        this.backupProcessor = backupProcessor;
        this.restoreBackupProcessor = restoreBackupProcessor;
        this.jobBuilderFactory = jobBuilderFactory;
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
    
    public Step backupStep() {
        return stepBuilderFactory.get("backupStep")
                .<InvoiceEntity, BackupInvoiceEntity> chunk(CHUNK_SIZE)
                .reader(invoiceReader())
                .processor(backupProcessor)
                .writer(invoiceBackupWriter())
                .listener(backupListener)
                .build();
    }
    
    @Bean(name = BACKUP_JOB_NAME)
    public Job backupJob() {
        return jobBuilderFactory.get(BACKUP_JOB_NAME)
                .start(backupStep())
                .build();
    }
    
    /////////////////////////////////////
    // Restore backup bacth config below
    /////////////////////////////////////
    
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
    
    public Step restoreBackupStep() {
        return stepBuilderFactory.get("restoreBackupStep")
                .<BackupInvoiceEntity, InvoiceEntity> chunk(CHUNK_SIZE)
                .reader(invoiceBackupReader())
                .processor(restoreBackupProcessor)
                .writer(backupToInvoiceEntityWriter())
                .listener(restoreBackupListener)
                .build();
    }
    
    @Bean(name = RESTORE_BACKUP_JOB_NAME)
    public Job restoreBackupJob() {
        return jobBuilderFactory.get(RESTORE_BACKUP_JOB_NAME)
                .start(restoreBackupStep())
                .build();
    }
    
    /**
     * Sort by invoicenumber when reading from our own database.
     * @return sorting strategy
     */
    private Map<String, Sort.Direction> getSorting() {
        Map<String, Sort.Direction> sorting = new HashMap<>();
        sorting.put("invoiceNumber", Sort.Direction.ASC);
        
        return sorting;
    }
}
