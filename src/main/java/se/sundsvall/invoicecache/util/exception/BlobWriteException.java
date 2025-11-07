package se.sundsvall.invoicecache.util.exception;

public class BlobWriteException extends RuntimeException {

	public BlobWriteException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
