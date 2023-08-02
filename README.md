A service which fetches and caches invoices from raindance.

# Mini-SAD
This service uses Spring Batch (and its meta data tables) together with Springs scheduler to fetch, store and in the case of a failure, restore backups.
For each successful/not succsessful batch run, meta data will be stored and this data is used to determine if and when to fetch new invoices.

A scheduler will run on a rather tight interval to continuously check the meta data table to see if the invoices are outdated (24h in production). 
It does this by checking if the last successful job is older than 24h, otherwise it does nothing.

When the service determines that the invoices are outdated it will:
- Copy the entire invoice table to a backup-table
- Fetch new invoices 
  - If successful:
    - overwrite the backup table with the newly fetched data
  - If it failed:
    - Insert the backup-data back into the "real" database table

## Spring batch:
Reading invoices, storing them, saving backups and restoring them are configuread as spring batch jobs.
These jobs are started by the scheduler and will never run "by themselves".
- `BackupBatchConfig`: Handles both saving and restoring backups
- `BatchConfig`: Handles fetching and storing of invoices

Each job has (`StepExecution`)-Listeners that will perform tasks before each job runs.

- `InvoiceListener`: Deletes the invoice-database before running the job.
- `BackupListener`: Deletes old backups before performing a backup.
- `RestoreBackupListener`: Deletes any invoices before restoring backup

Each job has a sequence where data is:
- Read: E.g fetch invoices from raindance and store them as dto:s.
- Processed: E.g convert read invoices to internal entities
- Written: E.g Write entities to the local database

Since reading thousands of invoices is best done in "batches" each batch is configured to 
read, process and write them in chunks of 1000.

Each job contains a `Step` which setups which readers, listeners, processors and writers to use.
Finally it is defined by a `Job` that starts the step.

## Running as a local service.
A docker compose-file whith needed database-ddl:s is stored in `src/main/docker` and can be
run by first building a docker image: `mvn clean spring-boot:build-image` in the root directory.

Followed by: 
`docker-compose -f src/main/docker/docker-compose-local-sandbox.yaml up --remove-orphans` to start the container and the needed databases.

## Status

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-invoice-cache&metric=alert_status)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-invoice-cache)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-invoice-cache&metric=reliability_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-invoice-cache)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-invoice-cache&metric=security_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-invoice-cache)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-invoice-cache&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-invoice-cache)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-invoice-cache&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-invoice-cache)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-invoice-cache&metric=bugs)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-invoice-cache)

## 
Copyright (c) 2021 Sundsvalls kommun
