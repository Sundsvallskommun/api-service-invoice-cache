package se.sundsvall.invoicecache.integration.storage.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
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

	@Mock
	private Consumer<String> transferFileUnhealthyConsumerMock;

	@Mock
	private Consumer<String> truncateFileUnhealthyConsumerMock;

	@Captor
	private ArgumentCaptor<PdfEntity> pdfEntityCaptor;

	@InjectMocks
	private StorageSchedulerWorker storageSchedulerWorker;

	@BeforeEach
	void setUp() {
		// Set transfer threshold to 6 months for testing
		ReflectionTestUtils.setField(storageSchedulerWorker, "transferThresholdMonths", 6);
	}

	@AfterEach
	void tearDown() {
		verifyNoMoreInteractions(pdfRepositoryMock, storageSambaIntegrationMock, transferFileUnhealthyConsumerMock, truncateFileUnhealthyConsumerMock);
	}

	@Test
	void transferFiles() {
		final var pdfEntity = TestObjectFactory.generatePdfEntity();
		final var fileHash = "someFileHash";
		when(pdfRepositoryMock.findPdfIdsToTransfer(any(), any()))
			.thenReturn(List.of(pdfEntity.getId()));
		when(pdfRepositoryMock.findById(pdfEntity.getId()))
			.thenReturn(java.util.Optional.of(pdfEntity));
		when(storageSambaIntegrationMock.writeFile(pdfEntity.getDocument()))
			.thenReturn(fileHash);

		storageSchedulerWorker.transferFiles(transferFileUnhealthyConsumerMock);

		verify(pdfRepositoryMock).findPdfIdsToTransfer(any(), any());
		verify(pdfRepositoryMock).findById(pdfEntity.getId());
		verify(pdfRepositoryMock).save(pdfEntityCaptor.capture());
		final var capturedPdfEntity = pdfEntityCaptor.getValue();
		assertThat(capturedPdfEntity.getFileHash()).isEqualTo(fileHash);
		assertThat(capturedPdfEntity.getMovedAt()).isNotNull();

		verify(storageSambaIntegrationMock).writeFile(capturedPdfEntity.getDocument());
	}

	@Test
	void truncateFiles_verification_OK() {
		final var fileHash1 = "someFileHash1";
		final var fileHash2 = "someFileHash2";
		final var pdfEntity1 = TestObjectFactory.generatePdfEntity();
		pdfEntity1.setFileHash(fileHash1);
		final var pdfEntity2 = TestObjectFactory.generatePdfEntity();
		pdfEntity2.setId(2);
		pdfEntity2.setFileHash(fileHash2);

		when(pdfRepositoryMock.findPdfIdsToTruncate())
			.thenReturn(List.of(pdfEntity1.getId(), pdfEntity2.getId()));
		when(pdfRepositoryMock.findById(pdfEntity1.getId()))
			.thenReturn(Optional.of(pdfEntity1));
		when(pdfRepositoryMock.findById(pdfEntity2.getId()))
			.thenReturn(Optional.of(pdfEntity2));
		when(storageSambaIntegrationMock.verifyBlobIntegrity(fileHash1))
			.thenReturn(fileHash1);
		when(storageSambaIntegrationMock.verifyBlobIntegrity(fileHash2))
			.thenReturn(fileHash2);

		final var fileIds = storageSchedulerWorker.getFileIdsToTruncate();
		fileIds.forEach(fileId -> storageSchedulerWorker.truncateFile(fileId, truncateFileUnhealthyConsumerMock));

		assertThat(pdfEntity1.getDocument()).isNull();
		assertThat(pdfEntity1.getTruncatedAt()).isNotNull();
		assertThat(pdfEntity2.getDocument()).isNull();
		assertThat(pdfEntity2.getTruncatedAt()).isNotNull();
		verify(pdfRepositoryMock).findPdfIdsToTruncate();
		verify(pdfRepositoryMock).findById(pdfEntity1.getId());
		verify(pdfRepositoryMock).findById(pdfEntity2.getId());
		verify(pdfRepositoryMock).save(pdfEntity1);
		verify(pdfRepositoryMock).save(pdfEntity2);
		verify(storageSambaIntegrationMock).verifyBlobIntegrity(fileHash1);
		verify(storageSambaIntegrationMock).verifyBlobIntegrity(fileHash2);
	}

	@Test
	void truncateFiles_verification_failed() {
		final var fileHash = "someFileHash";
		final var pdfEntity = TestObjectFactory.generatePdfEntity();
		pdfEntity.setFileHash(fileHash);

		when(pdfRepositoryMock.findPdfIdsToTruncate())
			.thenReturn(List.of(pdfEntity.getId()));
		when(pdfRepositoryMock.findById(pdfEntity.getId()))
			.thenReturn(Optional.of(pdfEntity));
		when(storageSambaIntegrationMock.verifyBlobIntegrity(fileHash))
			.thenReturn("differentFileHash");

		final var fileIds = storageSchedulerWorker.getFileIdsToTruncate();
		fileIds.forEach(fileId -> storageSchedulerWorker.truncateFile(fileId, truncateFileUnhealthyConsumerMock));

		assertThat(pdfEntity.getDocument()).isNotNull();
		assertThat(pdfEntity.getTruncatedAt()).isNull();
		verify(pdfRepositoryMock).findPdfIdsToTruncate();
		verify(pdfRepositoryMock).findById(pdfEntity.getId());
		verify(pdfRepositoryMock, never()).save(pdfEntity);
		verify(storageSambaIntegrationMock).verifyBlobIntegrity(fileHash);
		verify(truncateFileUnhealthyConsumerMock).accept(any());
	}

	@Test
	void transferFiles_noFilesEligible() {
		when(pdfRepositoryMock.findPdfIdsToTransfer(any(), any()))
			.thenReturn(List.of());

		storageSchedulerWorker.transferFiles(transferFileUnhealthyConsumerMock);

		verify(pdfRepositoryMock).findPdfIdsToTransfer(any(), any());
		verify(pdfRepositoryMock, never()).findById(any());
		verify(pdfRepositoryMock, never()).save(any());
		verify(storageSambaIntegrationMock, never()).writeFile(any());
	}

	@Test
	void transferFiles_fileNotFound() {
		final var pdfId = 123;
		when(pdfRepositoryMock.findPdfIdsToTransfer(any(), any()))
			.thenReturn(List.of(pdfId));
		when(pdfRepositoryMock.findById(pdfId))
			.thenReturn(java.util.Optional.empty());

		storageSchedulerWorker.transferFiles(transferFileUnhealthyConsumerMock);

		verify(pdfRepositoryMock).findPdfIdsToTransfer(any(), any());
		verify(pdfRepositoryMock).findById(pdfId);
		verify(pdfRepositoryMock, never()).save(any());
		verify(storageSambaIntegrationMock, never()).writeFile(any());
	}

	@Test
	void transferFiles_exceptionDuringTransfer() {
		final var pdfEntity = TestObjectFactory.generatePdfEntity();
		when(pdfRepositoryMock.findPdfIdsToTransfer(any(), any()))
			.thenReturn(List.of(pdfEntity.getId()));
		when(pdfRepositoryMock.findById(pdfEntity.getId()))
			.thenReturn(java.util.Optional.of(pdfEntity));
		when(storageSambaIntegrationMock.writeFile(pdfEntity.getDocument()))
			.thenThrow(new RuntimeException("Transfer failed"));

		storageSchedulerWorker.transferFiles(transferFileUnhealthyConsumerMock);

		verify(pdfRepositoryMock).findPdfIdsToTransfer(any(), any());
		verify(pdfRepositoryMock).findById(pdfEntity.getId());
		verify(pdfRepositoryMock, never()).save(any());
		verify(storageSambaIntegrationMock).writeFile(pdfEntity.getDocument());
		verify(transferFileUnhealthyConsumerMock).accept(any());
	}

	@Test
	void truncateFiles_noFilesEligible() {
		when(pdfRepositoryMock.findPdfIdsToTruncate())
			.thenReturn(List.of());

		final var result = storageSchedulerWorker.getFileIdsToTruncate();

		assertThat(result).isEmpty();
		verify(pdfRepositoryMock).findPdfIdsToTruncate();
		verify(pdfRepositoryMock, never()).save(any());
		verify(storageSambaIntegrationMock, never()).verifyBlobIntegrity(any());
	}

}
