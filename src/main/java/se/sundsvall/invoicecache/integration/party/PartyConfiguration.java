package se.sundsvall.invoicecache.integration.party;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import feign.Request;
import feign.codec.ErrorDecoder;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

@Import(FeignConfiguration.class)
public class PartyConfiguration {

	private static final String REGISTRATION_ID = "party";

	private final PartyProperties partyProperties;

	public PartyConfiguration(PartyProperties partyProperties) {
		this.partyProperties = partyProperties;
	}

	@Bean
	public FeignBuilderCustomizer feignBuilderCustomizer() {
		return FeignMultiCustomizer.create()
			.withErrorDecoder(errorDecoder())
			.withRequestOptions(feignOptions())
			.withRetryableOAuth2InterceptorForClientRegistration(clientRegistration())
			.composeCustomizersToOne();
	}

	private ClientRegistration clientRegistration() {
		return ClientRegistration.withRegistrationId(REGISTRATION_ID)
			.tokenUri(partyProperties.getOauth2TokenUrl())
			.clientId(partyProperties.getOauth2ClientId())
			.clientSecret(partyProperties.getOauth2ClientSecret())
			.authorizationGrantType(new AuthorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS.getValue()))
			.build();
	}

	ErrorDecoder errorDecoder() {
		// We want to return 404 as a 404.
		return new ProblemErrorDecoder(REGISTRATION_ID, List.of(HttpStatus.NOT_FOUND.value()));
	}

	Request.Options feignOptions() {
		return new Request.Options(
			TimeUnit.SECONDS.convert(partyProperties.getConnectTimeout()), TimeUnit.SECONDS,
			TimeUnit.SECONDS.convert(partyProperties.getReadTimeout()), TimeUnit.SECONDS,
			false);
	}
}
