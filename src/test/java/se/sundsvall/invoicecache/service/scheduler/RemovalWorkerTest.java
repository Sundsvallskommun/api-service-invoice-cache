package se.sundsvall.invoicecache.service.scheduler;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;
import se.sundsvall.invoicecache.integration.db.PdfRepository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.invoicecache.Constant.RAINDANCE_ISSUER_LEGAL_ID;

@ExtendWith(MockitoExtension.class)
class RemovalWorkerTest {

	@Mock
	private PdfRepository pdfRepositoryMock;

	@InjectMocks
	private RemovalWorker removalWorker;

	@AfterEach
	void tearDown() {
		verifyNoMoreInteractions(pdfRepositoryMock);
	}

	@Test
	void removeOldRaindanceInvoices() {
		when(pdfRepositoryMock.countAllByInvoiceIssuerLegalIdAndCreatedIsBefore(eq(RAINDANCE_ISSUER_LEGAL_ID), any(OffsetDateTime.class)))
			.thenReturn(100);
		when(pdfRepositoryMock.deleteAllOldRaindancePdfs(eq(RAINDANCE_ISSUER_LEGAL_ID), any(OffsetDateTime.class)))
			.thenReturn(100);

		removalWorker.removeOldRaindanceInvoices();

		verify(pdfRepositoryMock).countAllByInvoiceIssuerLegalIdAndCreatedIsBefore(eq(RAINDANCE_ISSUER_LEGAL_ID), any(OffsetDateTime.class));
		verify(pdfRepositoryMock).deleteAllOldRaindancePdfs(eq(RAINDANCE_ISSUER_LEGAL_ID), any(OffsetDateTime.class));
	}

	@Test
	void removeOldRaindanceInvoicesThrows() {
		when(pdfRepositoryMock.countAllByInvoiceIssuerLegalIdAndCreatedIsBefore(eq(RAINDANCE_ISSUER_LEGAL_ID), any(OffsetDateTime.class)))
			.thenReturn(100);
		when(pdfRepositoryMock.deleteAllOldRaindancePdfs(eq(RAINDANCE_ISSUER_LEGAL_ID), any(OffsetDateTime.class)))
			.thenThrow(new RuntimeException("Database error"));

		assertThatThrownBy(() -> removalWorker.removeOldRaindanceInvoices())
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Failed to remove old Raindance PDFs.");
	}

}
