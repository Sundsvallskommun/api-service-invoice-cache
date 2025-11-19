package se.sundsvall.invoicecache.integration.storage.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class HashUtil {

	private static final String SHA256 = "SHA-256";

	private HashUtil() {}

	/**
	 * Calculates the SHA-256 hash of the provided InputStream.
	 *
	 * @param  inputStream the InputStream to calculate the hash for
	 * @return             the SHA-256 hash as a hexadecimal string
	 * @throws IOException if an I/O error occurs
	 */
	public static String SHA256(final InputStream inputStream) throws IOException {
		try (inputStream) {
			final var messageDigest = MessageDigest.getInstance(SHA256);
			// Sets a buffer size of 64KB.
			final var buffer = new byte[64 * 1024];
			int numberOfBytes;

			// Reads 64KB at a time and updates the message digest until end of stream is reached.
			while ((numberOfBytes = inputStream.read(buffer)) != -1) {
				messageDigest.update(buffer, 0, numberOfBytes);
			}
			return HexFormat.of().formatHex(messageDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			// Should never happen since SHA-256 is a standard algorithm.
			throw new IllegalStateException("%s not available".formatted(SHA256), e);
		}
	}
}
