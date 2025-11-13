package se.sundsvall.invoicecache.integration.storage.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.invoicecache.TestObjectFactory;
import se.sundsvall.invoicecache.integration.db.PdfRepository;
import se.sundsvall.invoicecache.integration.db.entity.PdfEntity;
import se.sundsvall.invoicecache.integration.storage.StorageSambaIntegration;

@ExtendWith(MockitoExtension.class)
class StorageSchedulerWorkerTest {

	@Mock
	private PdfRepository pdfRepositoryMock;

	@Mock
	private StorageSambaIntegration storageSambaIntegrationMock;

	@Captor
	private ArgumentCaptor<PdfEntity> pdfEntityCaptor;

	@InjectMocks
	private StorageSchedulerWorker storageSchedulerWorker;

	@AfterEach
	void tearDown() {
		verifyNoMoreInteractions(pdfRepositoryMock, storageSambaIntegrationMock);
	}

	@Test
	void transferFile() {
		var pdfEntity = TestObjectFactory.generatePdfEntity();
		var fileHash = "someFileHash";
		when(pdfRepositoryMock.findPdfToTransfer(any(), any()))
			.thenReturn(Optional.of(pdfEntity));
		when(storageSambaIntegrationMock.writeFile(pdfEntity.getDocument()))
			.thenReturn(fileHash);

		storageSchedulerWorker.transferFile();

		verify(pdfRepositoryMock).findPdfToTransfer(any(), any());
		verify(pdfRepositoryMock).save(pdfEntityCaptor.capture());
		var capturedPdfEntity = pdfEntityCaptor.getValue();
		assertThat(capturedPdfEntity.getFileHash()).isEqualTo(fileHash);
		assertThat(capturedPdfEntity.getMovedAt()).isNotNull();

		verify(storageSambaIntegrationMock).writeFile(capturedPdfEntity.getDocument());
	}

	@Test
	void truncateFile_verification_OK() {
		var fileHash = "someFileHash";
		var pdfEntity = TestObjectFactory.generatePdfEntity();
		pdfEntity.setFileHash(fileHash);

		when(pdfRepositoryMock.findPdfToTruncate())
			.thenReturn(Optional.of(pdfEntity));
		when(storageSambaIntegrationMock.verifyBlobIntegrity(fileHash))
			.thenReturn(fileHash);

		storageSchedulerWorker.truncateFile();

		assertThat(pdfEntity.getDocument()).isNull();
		assertThat(pdfEntity.getTruncatedAt()).isNotNull();
		verify(pdfRepositoryMock).findPdfToTruncate();
		verify(pdfRepositoryMock).save(pdfEntity);
		verify(storageSambaIntegrationMock).verifyBlobIntegrity(fileHash);
	}

	@Test
	void truncateFile_verification_failed() {
		var fileHash = "someFileHash";
		var pdfEntity = TestObjectFactory.generatePdfEntity();
		pdfEntity.setFileHash(fileHash);

		when(pdfRepositoryMock.findPdfToTruncate())
			.thenReturn(Optional.of(pdfEntity));
		when(storageSambaIntegrationMock.verifyBlobIntegrity(fileHash))
			.thenReturn("differentFileHash");

		storageSchedulerWorker.truncateFile();

		assertThat(pdfEntity.getDocument()).isNotNull();
		assertThat(pdfEntity.getTruncatedAt()).isNull();
		verify(pdfRepositoryMock).findPdfToTruncate();
		verify(pdfRepositoryMock, never()).save(pdfEntity);
		verify(storageSambaIntegrationMock).verifyBlobIntegrity(fileHash);
	}

}
