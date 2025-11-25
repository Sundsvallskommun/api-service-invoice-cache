package se.sundsvall.invoicecache.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;

@CircuitBreaker(name = "invoiceRepository")
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Long>, JpaSpecificationExecutor<InvoiceEntity> {

	Optional<InvoiceEntity> findFirstByInvoiceNumberAndMunicipalityId(String invoiceNumber, String municipalityId);

	Optional<InvoiceEntity> findByFileNameAndMunicipalityId(String fileName, String municipalityId);

	@Modifying
	@Transactional
	@Query(value = "truncate table invoice", nativeQuery = true)
	void truncateTable();

}
