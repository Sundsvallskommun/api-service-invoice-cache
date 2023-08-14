package se.sundsvall.invoicecache.util.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InvoiceCacheExceptionTest {

	@Test
	void testExceptionWithMessage() {
		final var message = "This is the OCR-Exception";

		assertThat(new InvoiceCacheException(message)).hasMessage(message);
	}

	@Test
	void testExceptionWithMessageAndThrowable() {
		final var message = "This is the OCR-Exception";
		final var exception = new RuntimeException("This is an RunTimeException");

		assertThat(new InvoiceCacheException(message, exception))
			.hasMessage(message)
			.hasCause(exception);
	}
}
