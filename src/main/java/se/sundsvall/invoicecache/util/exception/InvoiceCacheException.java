package se.sundsvall.invoicecache.util.exception;

public class InvoiceCacheException extends RuntimeException {

	private static final long serialVersionUID = 5953569821735155103L;

	public InvoiceCacheException(String message) {
		super(message);
	}

	public InvoiceCacheException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
