package se.sundsvall.invoicecache.integration.raindance.samba;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Base64;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.zalando.problem.Problem;

@ExtendWith({
	MockitoExtension.class, OutputCaptureExtension.class
})
class RaindanceSambaIntegrationTest {

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private RaindanceSambaProperties raindanceSambaProperties;

	@InjectMocks
	private RaindanceSambaIntegration raindanceSambaIntegration;

	@AfterEach
	void tearDown() {
		verifyNoMoreInteractions(raindanceSambaProperties);
	}

	@Test
	void fetchInvoiceByFilename() throws IOException {
		final var filename = "test.pdf";
		final var content = "sample content".getBytes();

		try (final MockedConstruction<SmbFileInputStream> myobjectMockedConstruction = Mockito.mockConstruction(SmbFileInputStream.class,
			(mock, context) -> when(mock.readAllBytes()).thenReturn(content))) {

			final var result = raindanceSambaIntegration.fetchInvoiceByFilename(filename);

			assertThat(result.content()).isEqualTo(Base64.getEncoder().encodeToString(content));
			assertThat(result.name()).isEqualTo(filename);

			assertThat(myobjectMockedConstruction.constructed()).hasSize(1);
			final var mock = myobjectMockedConstruction.constructed().getFirst();
			verify(mock).readAllBytes();
			verify(raindanceSambaProperties).targetUrl();
			verify(raindanceSambaProperties).cifsContext();
		}
	}

	@Test
	void fetchInvoiceByFilename_throws() {
		final var filename = "test.pdf";

		try (var ignored = mockConstruction(SmbFile.class, (mock, context) -> when(mock.exists()).thenThrow(new SmbException("Random error")))) {

			assertThatThrownBy(() -> raindanceSambaIntegration.fetchInvoiceByFilename(filename))
				.isInstanceOf(Problem.class)
				.hasMessageContaining("Internal Server Error: Something went wrong when trying to fetch invoice");

			verify(raindanceSambaProperties).targetUrl();
			verify(raindanceSambaProperties).cifsContext();
		}
	}
}
