package se.sundsvall.invoicecache.util.exception;

public class BlobIntegrityException extends RuntimeException {

	public BlobIntegrityException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
