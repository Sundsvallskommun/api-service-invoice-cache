# InvoiceCache

A service for caching invoice information from Raindance and storing them in a local database.
It also serves as a storage for invoices in pdf format.

See the [Mini-SAD](#Mini-sad) for more detailed information.

## Getting Started

### Prerequisites

- **Java 21 or higher**
- **Git**
- **Maven**
- **MariaDB**
- **Microsoft SQL Server**
- **[Dependent Microservices](#dependencies)**

### Installation

1. **Clone the repository:**

   ```bash
   git https://github.com/Sundsvallskommun/api-service-invoice-cache.git
   cd api-service-invoice-cache
   ```
2. **Configure the application:**

   Before running the application, you need to set up configuration settings.
   See [Configuration](#Configuration)

   **Note:** Ensure all required configurations are set; otherwise, the application may fail to start.

3. **Ensure dependent services are running:**

   If this microservice depends on other services, make sure they are up and accessible.
   See [Dependencies](#dependencies) for more details.

4. **Build and run the application:**

   - Using Maven:

     ```bash
     mvn spring-boot:run
     ```

## Dependencies

This microservice depends on the following services:

- **Party**
  - **Purpose:** Used for translating between party id and legal id.
  - **Repository:** [https://github.com/Sundsvallskommun/api-service-party](https://github.com/Sundsvallskommun/api-service-party)
  - **Setup Instructions:** See documentation in repository above for installation and configuration steps.
  - **Configuration**: See [Key Configuration Parameters](#key-configuration-parameters) for configuration regarding the Party service.

Ensure that these services are running and properly configured before starting this microservice.

## API Documentation

Access the API documentation via Swagger UI:

- **Swagger UI:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

### API Endpoints

Refer to the [API Documentation](#api-documentation) for detailed information on available endpoints.

### Example Request

Example request for fetching an invoice in PDF format:

```bash
curl -X 'GET' 'http://localhost:8080/2281/invoices/filename.pdf' -H 'accept: application/json'
```

## Configuration

Configuration is crucial for the application to run successfully. Ensure all necessary settings are configured in
`application.yml`.

- **Samba Configuration**

  ```yaml
  integration:
    smb:
      cron: 0 5 * * * MON-FRI
      domain: smb-domain
      password: smb-password
      remoteDir: smb-remote-dir
      shareAndDir: smb-share-and-dir
      user: smb-user
  ```

## Database Initialization

The project is set up with [Flyway](https://github.com/flyway/flyway) for database migrations for the "internal" database.

As the service doesn't own the Raindance database there are no flyway scripts for it.
Use the scripts provided in /src/integration-test/resources/InvoiceCache/sql/init-raindance.sql to initialize a local
instance of the Raindance database to create tables and insert test data into it.

### Key Configuration Parameters

- **Server Port:**

  ```yaml
  server:
    port: 8080
  ```
- **Database Settings:**

  ```yaml
  spring:
    datasource:
      driver-class-name: org.mariadb.jdbc.Driver
      url: jdbc:mysql://database_url:3306/your_database
      username: your_db_username
      password: your_db_password
    raindance-datasource:
      driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
      username: sa
      password: raindance_password
      url: jdbc:sqlserver://raindance_url:1433;databaseName=master;encrypt=false
  ```
- **Party Service Configuration:**

  ```yaml
  integration:
    party:
      url: http://party_service_url
      oauth2-token-url: http://token_service_url
      oauth2-client-id: some-client-id
      oauth2-client-secret: some-client-secret
  ```

### Additional Notes

- **Application Profiles:**

  Use Spring profiles (`dev`, `prod`, etc.) to manage different configurations for different environments.

- **Logging Configuration:**

  Adjust logging levels if necessary.

# Mini-SAD

This service uses Spring Batch (and its meta data tables) together with Springs scheduler to fetch, store and in the
case of a failure, restore backups.
For each successful/not succsessful batch run, meta data will be stored and this data is used to determine if and when
to fetch new invoices.

A scheduler will run on a rather tight interval to continuously check the meta data table to see if the invoices are
outdated (24h in production).
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

## Contributing

Contributions are welcome! Please
see [CONTRIBUTING.md](https://github.com/Sundsvallskommun/.github/blob/main/.github/CONTRIBUTING.md) for guidelines.

## License

This project is licensed under the [MIT License](LICENSE).

## Code status

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-invoice-cache&metric=alert_status)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-invoice-cache)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-invoice-cache&metric=reliability_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-invoice-cache)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-invoice-cache&metric=security_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-invoice-cache)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-invoice-cache&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-invoice-cache)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-invoice-cache&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-invoice-cache)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-invoice-cache&metric=bugs)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-invoice-cache)

---

© 2024 Sundsvalls kommun
