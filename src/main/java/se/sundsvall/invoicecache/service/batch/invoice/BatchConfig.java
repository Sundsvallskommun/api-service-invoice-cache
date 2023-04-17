package se.sundsvall.invoicecache.service.batch.invoice;


import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
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
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import se.sundsvall.dept44.util.ResourceUtils;
import se.sundsvall.invoicecache.integration.db.InvoiceEntityRepository;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;
import se.sundsvall.invoicecache.integration.raindance.RaindanceQueryResultDto;
import se.sundsvall.invoicecache.integration.raindance.RaindanceRowMapper;

import jakarta.activation.DataSource;

@Configuration
public class BatchConfig {
    
    private static final int CHUNK_SIZE = 2000;
    
    public static final String RAINDANCE_JOB_NAME = "raindanceInvoiceJob";
    
    private final String sqlString;

    private final RaindanceEntityProcessor raindanceEntityProcessor;
    private final InvoiceEntityRepository invoiceRepository;
    private final InvoiceListener invoiceListener;
    
    public BatchConfig(@Value("classpath:${raindance.sql.filename}") Resource sqlResource,
            final RaindanceEntityProcessor raindanceEntityProcessor,
            final InvoiceEntityRepository invoiceRepository,
            final InvoiceListener invoiceListener) {
        this.raindanceEntityProcessor = raindanceEntityProcessor;
        this.invoiceRepository = invoiceRepository;
        this.sqlString = ResourceUtils.asString(sqlResource);
        this.invoiceListener = invoiceListener;
    }
    
    /**
     * Read invoices from the raindance-DB
     * @param dataSource
     * @return
     */
    public JdbcCursorItemReader<RaindanceQueryResultDto> raindanceReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<RaindanceQueryResultDto>()
                .dataSource(dataSource)
                .name("raindanceItemReader")
                .sql(sqlString)
                .saveState(false)   //No need to restart the job
                .fetchSize(CHUNK_SIZE)
                .verifyCursorPosition(false)    //For some reason it fails without this, even with a "clean" database.
                .rowMapper(new RaindanceRowMapper())
                .build();
    }
    
    /**
     * Defines which steps to use, they are:
     * - Read from raindance
     * - "Process" the received data, basically turn them into entities
     * - Write it to our own database.
     * @param dataSource
     * @return
     */
    public Step step1(DataSource dataSource, JobRepository jobRepository) {
        return new StepBuilder("invoiceStep", jobRepository)//get("invoiceStep")
                .<RaindanceQueryResultDto, InvoiceEntity> chunk(CHUNK_SIZE, new DataSourceTransactionManager(dataSource))
                .faultTolerant()
                .reader(raindanceReader(dataSource))
                .processor(raindanceEntityProcessor)
                .writer(invoiceEntityWriter())
                .listener(invoiceListener)
                .build();
    }
    
    /**
     * Writes items to our DB.
     * No save method specified which implies to use "saveAll"-method
     *
     * @return
     */
    public RepositoryItemWriter<InvoiceEntity> invoiceEntityWriter() {
        return new RepositoryItemWriterBuilder<InvoiceEntity>()
                .repository(invoiceRepository)
                .build();
    }
    
    /**
     * Defines the job and which step to start in the job.
     * @param dataSource
     * @return the job
     */
    @Bean(name = RAINDANCE_JOB_NAME)
    public Job startJob(@Qualifier("raindanceDataSource") DataSource dataSource, JobRepository jobRepository) {
        return new JobBuilder(RAINDANCE_JOB_NAME, jobRepository)
                .start(step1(dataSource, jobRepository))
                .build();
    }
}
