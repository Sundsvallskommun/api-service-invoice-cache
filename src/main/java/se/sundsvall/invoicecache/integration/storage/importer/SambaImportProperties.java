package se.sundsvall.invoicecache.integration.storage.importer;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "integration.storage.samba.import")
public record SambaImportProperties(

	String sourceDirectory,

	@DefaultValue("PT4H") Duration lockAtMostFor,

	@DefaultValue("PT30S") Duration lockAtLeastFor) {
}
