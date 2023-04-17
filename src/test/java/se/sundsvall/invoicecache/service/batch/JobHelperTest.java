package se.sundsvall.invoicecache.service.batch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.NoSuchJobException;
import se.sundsvall.invoicecache.api.batchactuator.JobStatus;
import se.sundsvall.invoicecache.integration.db.InvoiceEntityRepository;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static se.sundsvall.invoicecache.service.batch.invoice.BatchConfig.RAINDANCE_JOB_NAME;

@ExtendWith(MockitoExtension.class)
class JobHelperTest {
    
    @Mock
    private JobExplorer mockJobExplorer;
    
    @Mock
    private InvoiceEntityRepository mockInvoiceRepository;
    
    private JobHelper jobHelper;
    
    @BeforeEach
    void setup() {
        this.jobHelper = new JobHelper(mockJobExplorer, mockInvoiceRepository, Duration.ofMinutes(1L));
    }
    
    @Test
    void areInvoicesOutdated_shouldReturnFalse_whenFoundCompletedJob() throws NoSuchJobException {
        setupGetSuccessfulJobMethod(LocalDateTime.now(), ExitStatus.COMPLETED);
        when(mockInvoiceRepository.count()).thenReturn(100L);
        assertFalse(jobHelper.areInvoicesOutdated());
    }
    
    @Test
    void areInvoicesOutdated_shouldReturnTrue_whenNoCompletedJob() throws NoSuchJobException {
        setupGetSuccessfulJobMethod(LocalDateTime.now().minusDays(7L), ExitStatus.COMPLETED);
        assertTrue(jobHelper.areInvoicesOutdated());
    }
    
    @Test
    void testGetSuccessfulJobWithinTimePeriod_shouldReturnPopulatedExecution_whenWithinGivenTime() throws NoSuchJobException {
        String jobName = setupGetSuccessfulJobMethod(LocalDateTime.now(), ExitStatus.COMPLETED);
    
        final Optional<JobExecution> successfulJobWithinTimePeriod = jobHelper.getSuccessfulJobWithinTimePeriod(jobName);
        assertTrue(successfulJobWithinTimePeriod.isPresent());
    }
    
    @Test
    void testGetSuccessfulJobWithinTimePeriod_shouldReturnEmptyExecution_whenOutsideGivenTime() throws NoSuchJobException {
        String jobName = setupGetSuccessfulJobMethod(LocalDateTime.now().minusDays(7L), ExitStatus.COMPLETED);
        
        final Optional<JobExecution> successfulJobWithinTimePeriod = jobHelper.getSuccessfulJobWithinTimePeriod(jobName);
        assertFalse(successfulJobWithinTimePeriod.isPresent());
    }
    
    @Test
    void testGetSuccessfulJobWithinTimePeriod_shouldReturnEmptyExecution_whenNoCompletedExitStatus() throws NoSuchJobException {
        String jobName = setupGetSuccessfulJobMethod(LocalDateTime.now(), ExitStatus.FAILED);
        
        final Optional<JobExecution> successfulJobWithinTimePeriod = jobHelper.getSuccessfulJobWithinTimePeriod(jobName);
        assertFalse(successfulJobWithinTimePeriod.isPresent());
    }
    
    @Test
    void testGetSuccessfulJobWithinTimePeriod_shouldThrowException_whenNoJobExists() throws NoSuchJobException {
        final String jobName = setupGetSuccessfulJobMethod(LocalDateTime.now(), ExitStatus.FAILED);//Ignore job name, we want to get a job that doesn't exist.
        when(mockJobExplorer.getJobInstanceCount(jobName)).thenThrow(new NoSuchJobException("missing job"));
        final Optional<JobExecution> successfulJobWithinTimePeriod = jobHelper.getSuccessfulJobWithinTimePeriod(jobName);
        assertFalse(successfulJobWithinTimePeriod.isPresent());
    }
    
    @Test
    void testGetJobsShouldReturnListOfLatestJobStatuses() throws NoSuchJobException {
        when(mockJobExplorer.getJobInstanceCount(RAINDANCE_JOB_NAME)).thenReturn(100L);
    
        final JobInstance jobInstance = new JobInstance(1L, RAINDANCE_JOB_NAME);
        List<JobInstance> jobList = List.of(jobInstance);
    
        final JobExecution jobExecution = new JobExecution(jobInstance, null);
        jobExecution.setStatus(BatchStatus.COMPLETED);
        jobExecution.setStartTime(LocalDateTime.of(2022, 8, 10, 1, 10 ,0));
        jobExecution.setEndTime(LocalDateTime.of(2022, 8, 10, 1, 10 ,10));
        jobExecution.setJobInstance(jobInstance);
    
        StepExecution step = new StepExecution("stepName", jobExecution);
        step.setReadCount(15);
        step.setWriteCount(20);
        
        jobExecution.addStepExecutions(List.of(step));
    
        when(mockJobExplorer.getJobInstances(eq(RAINDANCE_JOB_NAME), eq(0), eq(50))).thenReturn(jobList);
        when(mockJobExplorer.getJobExecutions(jobInstance)).thenReturn(List.of(jobExecution));
        
        final List<JobStatus> jobs = jobHelper.getJobs();
        assertEquals(1, jobs.size());
        assertEquals(LocalDateTime.of(2022, 8, 10, 1, 10 ,0), jobs.get(0).getStartTime());
        assertEquals(LocalDateTime.of(2022, 8, 10, 1, 10 ,10), jobs.get(0).getEndTime());
        assertEquals(BatchStatus.COMPLETED.toString(), jobs.get(0).getStatus());
        assertEquals("stepName", jobs.get(0).getStepStatusMap().get("stepName").getStepName());
        assertEquals(15L, jobs.get(0).getStepStatusMap().get("stepName").getStepReadCount());
        assertEquals(20L, jobs.get(0).getStepStatusMap().get("stepName").getStepWriteCount());
    }
    
    @Test
    void testConvertDateToLocalDateTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(2022, Calendar.AUGUST, 11, 12, 1, 1);
        Date date = cal.getTime();
        final LocalDateTime localDateTime = jobHelper.convertDateToLocalDateTime(date);
        
        assertEquals(LocalDateTime.of(2022, Month.AUGUST, 11, 12, 1, 1).truncatedTo(ChronoUnit.SECONDS), localDateTime.truncatedTo(ChronoUnit.SECONDS));
    }
    
    @Test
    void testInvoiceTableHasInvoices_shouldReturnTrueWhenMoreThanZero() {
        when(mockInvoiceRepository.count()).thenReturn(123456L);
        final boolean hasInvoices = jobHelper.invoiceTableHasInvoices();
        assertTrue(hasInvoices);
    }
    
    @Test
    void testInvoiceTableHasInvoices_shouldReturnFalseWhenZero() {
        when(mockInvoiceRepository.count()).thenReturn(0L);
        final boolean hasInvoices = jobHelper.invoiceTableHasInvoices();
        assertFalse(hasInvoices);
    }
    
    /**
     * Set some boilerplate stuff and make the parameters that matters configurable
     *
     * @param endTime    which endtime to set for the last successful job
     * @param exitStatus exitStatus, everything other than "COMPLETED" will be ignored.
     * @return
     * @throws NoSuchJobException
     */
    private String setupGetSuccessfulJobMethod(final LocalDateTime endTime, ExitStatus exitStatus) throws NoSuchJobException {
        //We want to see if there are any successful jobs within the last minute
        final JobInstance jobInstance = new JobInstance(1L, RAINDANCE_JOB_NAME);
        List<JobInstance> jobList = List.of(jobInstance);
        
        final JobExecution jobExecution = new JobExecution(jobInstance, null);
        jobExecution.setExitStatus(exitStatus);
        
        jobExecution.setEndTime(endTime);    //Last successful job is now, which means we should get a successful job.
        List<JobExecution> executionList = List.of(jobExecution);
        
        //Lenient since we want to reuse this method.
        Mockito.lenient().when(mockJobExplorer.getJobInstanceCount(eq(RAINDANCE_JOB_NAME))).thenReturn(1L);
        Mockito.lenient().when(mockJobExplorer.getJobInstances(eq(RAINDANCE_JOB_NAME), eq(0), eq(1))).thenReturn(jobList);
        Mockito.lenient().when(mockJobExplorer.getJobExecutions(jobInstance)).thenReturn(executionList);
        return RAINDANCE_JOB_NAME;
    }
    
    private Date createDate(LocalDateTime localDateTime) {
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }
}