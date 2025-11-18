package se.sundsvall.invoicecache.integration.storage;

import java.util.Properties;
import jcifs.CIFSContext;
import jcifs.CIFSException;
import jcifs.config.PropertyConfiguration;
import jcifs.context.BaseContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "integration.storage.samba")
public record StorageSambaProperties(

	String user,
	String password,
	// Domain in AD (Active Directory).
	String userDomain,
	String host,
	@DefaultValue("445") Integer port,

	// The name of the share, basically the top-level directory in the Samba server.
	String baseDirectory,

	// Directory within the baseDirectory where files are stored. Defaults to 'invoice-cache' if no value is set.
	@DefaultValue("invoice-cache") String serviceDirectory,

	// Determines which directory to use within the serviceDirectory either [test, prod]. Defaults to 'test' if no value is
	// set.
	@DefaultValue("test") String environment) {

	public String targetUrl() {
		return "smb://%s:%d/%s/%s/%s".formatted(host, port, baseDirectory, serviceDirectory, environment);
	}

	public CIFSContext cifsContext() throws CIFSException {
		return new BaseContext(new PropertyConfiguration(jcifsProperties()))
			.withCredentials(new NtlmPasswordAuthenticator(userDomain, user, password));
	}

	private Properties jcifsProperties() {
		var jcifsProperties = new Properties();
		jcifsProperties.setProperty("jcifs.smb.client.connTimeout", Long.toString(3000L));
		jcifsProperties.setProperty("jcifs.smb.client.responseTimeout", Long.toString(3000L));
		jcifsProperties.setProperty("jcifs.smb.client.minVersion", "SMB300");
		jcifsProperties.setProperty("jcifs.smb.client.maxVersion", "SMB311");
		return jcifsProperties;
	}
}
