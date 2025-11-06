package se.sundsvall.invoicecache.integration.raindance.samba;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.raindance.samba")
record RaindanceSambaProperties(
	String domain,
	String userDomain,
	String user,
	String password,
	String shareAndDir,
	String remoteDir) {
}
