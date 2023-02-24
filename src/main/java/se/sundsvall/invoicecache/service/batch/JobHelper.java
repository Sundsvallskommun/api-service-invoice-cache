package se.sundsvall.invoicecache.service.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.sundsvall.invoicecache.api.batchactuator.JobStatus;
import se.sundsvall.invoicecache.integration.db.InvoiceEntityRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static se.sundsvall.invoicecache.service.batch.invoice.BatchConfig.RAINDANCE_JOB_NAME;

/**
 * Helper class for everything related to fetching statuses for batches and determining whether to run or not.
 */
@Component
public class JobHelper {
    
    private static final Logger LOG = LoggerFactory.getLogger(JobHelper.class);
    
    private final Duration successfulWithin;  //Check for successful jobs withing this Duration
    
    private final JobExplorer jobExplorer;
    private final InvoiceEntityRepository invoiceRepository;
    
    public JobHelper(final JobExplorer jobExplorer, final InvoiceEntityRepository invoiceRepository,
            @Value("${raindance.invoice.outdated}") Duration timeToWait) {
        this.jobExplorer = jobExplorer;
        this.invoiceRepository = invoiceRepository;
        this.successfulWithin = timeToWait;
    }
    
    public boolean invoiceTableHasInvoices() {
        return invoiceRepository.count() > 0;
    }
    
    /**
     * Check if there is a successful job within the last "minutesToCheck" minutes, if not, we should fetch invoices.
     * @return true if we should update, false if not.
     */
    public boolean areInvoicesOutdated() {
        boolean isOutdated = getSuccessfulJobWithinTimePeriod(RAINDANCE_JOB_NAME).isEmpty();
        
        if(isOutdated) {
            LOG.info("No successful job within the last {} minutes were found.", successfulWithin.toMinutes());
        } else if(invoiceRepository.count() == 0) {
            LOG.info("No invoices found in local DB, get them.");
            isOutdated = true;
        } else {
            if(getSuccessfulJobWithinTimePeriod(RAINDANCE_JOB_NAME).isPresent()) {
                LOG.debug("Found a completed successful job from: {}, not running a new one.", getSuccessfulJobWithinTimePeriod(RAINDANCE_JOB_NAME).get().getEndTime());
            }
        }
        
        return isOutdated;
    }
    
    /**
     * Check if there are any successful job within the last 24 hrs.
     * @param jobName name of the job to check
     * @return
     */
    Optional<JobExecution> getSuccessfulJobWithinTimePeriod(String jobName) {
        
        Optional<JobExecution> possibleJob = Optional.empty();
        
        try {
            final int jobInstanceCount = jobExplorer.getJobInstanceCount(jobName);
            
            //See of there are any successful jobs done within the last 24 hours.
            possibleJob = jobExplorer.getJobInstances(jobName, 0, jobInstanceCount)
                    .stream()
                    .map(jobExplorer::getJobExecutions)
                    .flatMap(List<JobExecution>::stream)
                    .filter(jobExecution -> jobExecution.getExitStatus().equals(ExitStatus.COMPLETED))
                    .filter(jobExecution -> Objects.nonNull(jobExecution.getEndTime())
                            && convertDateToLocalDateTime(jobExecution.getEndTime()).isAfter(LocalDateTime.now().minusMinutes(successfulWithin.toMinutes())))
                    .findFirst();
            
        } catch (NoSuchJobException e) {
            //If we can't find any job, we don't care, run a new one.
            LOG.info("Couldn't find any job with name: {}", jobName);
        }
        
        return possibleJob;
    }
    
    /**
     * Mainly used for actuator
     * @return list of the 50 latest jobs
     */
    public List<JobStatus> getJobs() {
        List<JobStatus> listOfJobs = new ArrayList<>();
        int jobsToFetch = 50;
        try {
            int jobInstanceCount = jobExplorer.getJobInstanceCount(RAINDANCE_JOB_NAME);
            int mostRecentInstances = jobInstanceCount;
            //Only get the latest 50
            if(jobInstanceCount > jobsToFetch) {
                mostRecentInstances = jobInstanceCount - (jobInstanceCount - jobsToFetch);
            }
    
            //The latest job has index 0. If there are a total of 20 execution, fetching #20 will get the first, which is why we get from 0.
            listOfJobs = jobExplorer.getJobInstances(RAINDANCE_JOB_NAME, 0, mostRecentInstances)
                    .stream()
                    .map(jobExplorer::getJobExecutions)
                    .flatMap(List<JobExecution>::stream)
                    .map(this::mapJobExecutionToJobStatus)
                    .toList();
    
        } catch (NoSuchJobException e) {
            //If we can't find any job, we don't care, run a new one.
            LOG.info("Couldn't find any job with name: {}", RAINDANCE_JOB_NAME);
        }
        
        return listOfJobs;
    }
    
    private JobStatus mapJobExecutionToJobStatus(JobExecution jobExecution) {
        final JobStatus jobStatus = JobStatus.builder()
                .withStatus(jobExecution.getStatus().getBatchStatus().toString())
                .withStartTime(convertDateToLocalDateTime(jobExecution.getStartTime()))
                .withEndTime(convertDateToLocalDateTime(jobExecution.getEndTime()))
                .build();
        
        //We also want to know how many rows we read and wrote.
        jobExecution.getStepExecutions()
                .stream()
                .map(stepExecution -> new JobStatus.StepStatus(stepExecution.getStepName(), stepExecution.getReadCount(), stepExecution.getWriteCount()))
                .forEach(jobStatus::addStepStatus);
        
        return jobStatus;
    }
    
    public LocalDateTime convertDateToLocalDateTime(Date dateToConvert) {
        if(dateToConvert != null) {
            return dateToConvert
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        } else {
            return LocalDateTime.now();
        }
    }
}
