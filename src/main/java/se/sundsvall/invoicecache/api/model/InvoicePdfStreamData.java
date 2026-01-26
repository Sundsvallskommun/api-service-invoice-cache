package se.sundsvall.invoicecache.api.model;

import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public record InvoicePdfStreamData(
	StreamingResponseBody contentStream,
	MediaType contentType,
	ContentDisposition contentDisposition) {}
