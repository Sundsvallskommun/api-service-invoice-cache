package se.sundsvall.invoicecache.util.exception;

import java.io.Serial;

public class InvoiceCacheException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 5953569821735155103L;

	public InvoiceCacheException(String message) {
		super(message);
	}

	public InvoiceCacheException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
