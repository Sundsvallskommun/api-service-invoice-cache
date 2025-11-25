package se.sundsvall.invoicecache.integration.raindance.samba;

import jcifs.CIFSContext;
import jcifs.context.SingletonContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.raindance.samba")
record RaindanceSambaProperties(
	String domain,
	String userDomain,
	String user,
	String password,
	String shareAndDir,
	String remoteDir) {

	public String targetUrl() {
		return "smb://%s%s%s".formatted(domain, shareAndDir, remoteDir);
	}

	public CIFSContext cifsContext() {
		var base = SingletonContext.getInstance();
		return base.withCredentials(new NtlmPasswordAuthenticator(userDomain, user, password));
	}
}
