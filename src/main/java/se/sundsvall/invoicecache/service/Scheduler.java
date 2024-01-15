package se.sundsvall.invoicecache.service;

import static se.sundsvall.invoicecache.service.batch.backup.BackupBatchConfig.BACKUP_JOB_NAME;
import static se.sundsvall.invoicecache.service.batch.backup.BackupBatchConfig.RESTORE_BACKUP_JOB_NAME;
import static se.sundsvall.invoicecache.service.batch.invoice.BatchConfig.RAINDANCE_JOB_NAME;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import se.sundsvall.invoicecache.service.batch.JobHelper;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Configuration
@EnableScheduling
public class Scheduler {

	private static final Logger LOG = LoggerFactory.getLogger(Scheduler.class);

	private final JobLauncher jobLauncher;
	private final Job backupJob;
	private final Job invoiceJob;
	private final Job restoreBackupJob;
	private final JobHelper jobHelper;
	private final boolean schedulingIsEnabled;

	public Scheduler(final JobLauncher jobLauncher,
		@Qualifier(RAINDANCE_JOB_NAME) Job invoiceJob,
		@Qualifier(BACKUP_JOB_NAME) Job backupJob,
		@Qualifier(RESTORE_BACKUP_JOB_NAME) Job restoreBackupJob,
		final JobHelper jobHelper,
		@Value("${invoices.scheduling.enabled:true}") boolean schedulingIsEnabled) {
		this.jobLauncher = jobLauncher;
		this.backupJob = backupJob;
		this.invoiceJob = invoiceJob;
		this.jobHelper = jobHelper;
		this.restoreBackupJob = restoreBackupJob;
		this.schedulingIsEnabled = schedulingIsEnabled;
	}

	/**
	 * Run with an initial delay of 5 seconds when the application starts.
	 * Then run every 10 minutes.
	 *
	 * @throws JobInstanceAlreadyCompleteException
	 * @throws JobExecutionAlreadyRunningException
	 * @throws JobParametersInvalidException
	 * @throws JobRestartException
	 */
	@Scheduled(initialDelayString = "${invoice.scheduled.initialdelay}", fixedRateString = "${invoice.scheduled.fixedrate}")
	@SchedulerLock(name = "invoiceLaunchJob", lockAtMostFor = "${invoice.scheduled.shedlock-lock-at-most-for}")
	public void launchJob() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
		// Only run if scheduling is enabled
		if (schedulingIsEnabled) {
			if (jobHelper.areInvoicesOutdated()) {
				// Always try to run the job that fetches invoices
				startFetchingInvoices();
			} else {
				LOG.info("Invoices are not outdated.");
			}
		} else {
			LOG.info("Scheduling is disabled.");
		}
	}

	private void startFetchingInvoices() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
		final JobExecution executionResult = fetchInvoices();

		// If the invoice job ended with a successful exitstatus, and we actually have any invoices, backup the fetched
		// invoices.
		// TODO, maybe check the age of the backup, if it's less than a day old, don't run a backup
		if (executionResult.getExitStatus().equals(ExitStatus.COMPLETED) && jobHelper.invoiceTableHasInvoices()) {
			runBackup();
		} else {
			// We cannot know if fetching invoices went ok, since we always delete the whole invoices table beforehand we always
			// need to restore it.
			restoreBackup();
		}
	}

	/**
	 * Uses a StepListener to handle the steps.
	 * "before" step will clean the DB.
	 * "after" step will only print some info.
	 *
	 * @return
	 * @throws JobInstanceAlreadyCompleteException
	 * @throws JobExecutionAlreadyRunningException
	 * @throws JobParametersInvalidException
	 * @throws JobRestartException
	 */
	public JobExecution fetchInvoices() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
		final JobExecution executionResult = jobLauncher.run(this.invoiceJob, new JobParametersBuilder().addDate(RAINDANCE_JOB_NAME + "_key", new Date()).toJobParameters());
		LOG.info("Invoice job ended with status: {} at: {}", executionResult.getExitStatus(), executionResult.getEndTime());
		return executionResult;
	}

	/**
	 * Uses a StepListener to handle cleaning of old backups before running a new backup.
	 *
	 * @throws JobInstanceAlreadyCompleteException
	 * @throws JobExecutionAlreadyRunningException
	 * @throws JobParametersInvalidException
	 * @throws JobRestartException
	 */
	public void runBackup() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
		final JobExecution executionResult = jobLauncher.run(this.backupJob, new JobParametersBuilder().addDate(BACKUP_JOB_NAME + "_key", new Date()).toJobParameters());
		LOG.info("Backup job ended with status: {} at: {}", executionResult.getExitStatus(), executionResult.getEndTime());
	}

	/**
	 * Restores a backup and replaces the invoices in case reading of invoices was not successful
	 * Uses a StepListener to delete old invoices before restoring them from a backup.
	 *
	 * @throws JobInstanceAlreadyCompleteException
	 * @throws JobExecutionAlreadyRunningException
	 * @throws JobParametersInvalidException
	 * @throws JobRestartException
	 */
	public void restoreBackup() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
		final JobExecution executionResult = jobLauncher.run(this.restoreBackupJob, new JobParametersBuilder().addDate(RESTORE_BACKUP_JOB_NAME + "_key", new Date()).toJobParameters());
		LOG.info("RestoreBackup job ended with status: {} at: {}", executionResult.getExitStatus(), executionResult.getEndTime());
	}
}
