package se.sundsvall.invoicecache.integration.storage.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HashUtilTest {

	@ParameterizedTest
	@MethodSource("sha256ArgumentProvider")
	void SHA256_test(final String value, final String expectedHash) throws IOException {
		var inputStream = new ByteArrayInputStream(value.getBytes());

		var hash = HashUtil.SHA256(inputStream);

		assertThat(hash).isEqualTo(expectedHash);
	}

	@Test
	void SHA256_notAvailable() {
		var inputStream = new ByteArrayInputStream("TestString".getBytes());

		try (var mockedStatic = mockStatic(MessageDigest.class)) {
			mockedStatic.when(() -> MessageDigest.getInstance("SHA-256"))
				.thenThrow(new NoSuchAlgorithmException("SHA-256 not available"));

			assertThatThrownBy(() -> HashUtil.SHA256(inputStream))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("SHA-256 not available");
		}
	}

	private static Stream<Arguments> sha256ArgumentProvider() {
		return Stream.of(
			Arguments.of("TestString", "6dd79f2770a0bb38073b814a5ff000647b37be5abbde71ec9176c6ce0cb32a27"),
			Arguments.of("AnotherTestString", "8df77a731fb9cacafb233b32ace2fa58ebbbfbd48849a83c48e2d717bd333acd"),
			Arguments.of("ThirdString", "9eade98be98408fa03c656457076018866d8ac1f78c9fe9fea1b6f4efe75c2ae"));
	}

}
