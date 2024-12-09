package se.sundsvall.invoicecache.integration.smb;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "integration.smb")
class SMBProperties {

	private String domain;

	private String userDomain;

	private String user;

	private String password;

	private String shareAndDir;

	private String remoteDir;

}
