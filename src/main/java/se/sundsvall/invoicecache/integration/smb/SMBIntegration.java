package se.sundsvall.invoicecache.integration.smb;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.hibernate.engine.jdbc.BlobProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import se.sundsvall.invoicecache.integration.db.PdfEntityRepository;
import se.sundsvall.invoicecache.integration.db.entity.PdfEntity;

import jcifs.CIFSException;
import jcifs.context.SingletonContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

@Component
@EnableScheduling
public class SMBIntegration {

    private static final Logger logger = LoggerFactory.getLogger(SMBIntegration.class);
    private final PdfEntityRepository pdfRepository;
    private final SMBProperties properties;
    private final String sourceUrl;

    SMBIntegration(PdfEntityRepository pdfRepository, SMBProperties properties) {
        this.pdfRepository = pdfRepository;
        this.properties = properties;
        sourceUrl = "smb://" + properties.getDomain() +
                         properties.getShareAndDir() + properties.getRemoteDir();
    }

    static boolean isAfterYesterday(long lastModified) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(lastModified), TimeZone.getDefault().toZoneId()).isAfter(LocalDateTime.now().minusDays(1));

    }

    public PdfEntity findPdf(String filename) {
        try (var file = createSmbFile(sourceUrl + "/" + filename)) {
            return saveFile(file);
        } catch (MalformedURLException | CIFSException e) {
            logger.warn("Something went wrong when trying to find pdf", e);
            return null;
        }
    }
    
    @Scheduled(initialDelay = 10L, fixedRate = 5000L, timeUnit = TimeUnit.SECONDS)
    void cacheInvoicePdfs() {

        long start = System.currentTimeMillis();
        logger.info("Starting caching of invoice pdfs");

        try (var directory = createSmbFile(sourceUrl)) {
            Arrays.stream(Objects.requireNonNull(directory)
                    .listFiles(file -> isAfterYesterday(file.lastModified()))).forEach(this::saveFile);

        } catch (CIFSException | MalformedURLException e) {
            logger.warn("Something went wrong when trying to cache pdf", e);
        }
        long end = System.currentTimeMillis();
        logger.info("Caching of invoice pdfs completed in {} seconds", (end - start) / 1000);
    }

    private SmbFile createSmbFile(String sourceUrl) throws CIFSException, MalformedURLException {
        var base = SingletonContext.getInstance();
        var cifsContext = base.withCredentials(new NtlmPasswordAuthenticator(properties.getUserDomain(),
            properties.getUser(), properties.getPassword()));
        try (var directory = new SmbFile(sourceUrl, cifsContext)) {
            return directory;
        }
    }

    private PdfEntity saveFile(SmbFile file) {
        try (var inputStream = new SmbFileInputStream(file)) {
            return pdfRepository.save(PdfEntity.builder()
                    .withFilename(file.getName().replace(properties.getRemoteDir(), ""))
                    .withDocument(BlobProxy.generateProxy(inputStream.readAllBytes()))
                    .build());
        } catch (IOException e) {
            logger.warn("Something went wrong when trying to save file", e);
            return null;
        }
    }

}
