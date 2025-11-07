package se.sundsvall.invoicecache.integration.storage;

import jcifs.CIFSContext;
import jcifs.context.SingletonContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "integration.storage.samba")
public record StorageSambaProperties(

	String user,
	String password,
	// Domain in AD (Active Directory).
	String userDomain,
	String hostname,

	// Base directory on the Samba server where service directories are located.
	String baseDirectory,

	// Directory within the baseDirectory where files are stored. Defaults to 'invoice-cache' if no value is set.
	@DefaultValue("invoice-cache") String serviceDirectory,

	// Determines which directory to use within the serviceDirectory either [test, prod]. Defaults to 'test' if no value is
	// set.
	@DefaultValue("test") String environment) {

	public String targetUrl() {
		return "smb://%s/%s/%s/%s".formatted(hostname, baseDirectory, serviceDirectory, environment);
	}

	public CIFSContext cifsContext() {
		var base = SingletonContext.getInstance();
		return base.withCredentials(new NtlmPasswordAuthenticator(userDomain, user, password));
	}
}
