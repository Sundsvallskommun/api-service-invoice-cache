package se.sundsvall.invoicecache.integration.db.entity;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class that asserts that the {@link se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity} and
 * {@link se.sundsvall.invoicecache.integration.db.entity.BackupInvoiceEntity} has the same fields and annotations,
 * since they must always be the
 * same.
 */
class EntityEqualsTest {

	@Test
	void testAnnotationsAreTheSame() {
		final Map<String, Annotation[]> invoiceEntityMap = Arrays.stream(InvoiceEntity.class.getDeclaredFields())
			.collect(Collectors.toMap(Field::getName, Field::getDeclaredAnnotations));

		final Map<String, Annotation[]> backupInvoiceEntityMap = Arrays.stream(BackupInvoiceEntity.class.getDeclaredFields())
			.collect(Collectors.toMap(Field::getName, Field::getDeclaredAnnotations));

		invoiceEntityMap.forEach((s, annotations) -> {
			final List<Annotation> backupAnno = Arrays.stream(backupInvoiceEntityMap.get(s)).toList();
			final List<Annotation> invoiceAnno = Arrays.stream(annotations).toList();
			assertThat(backupAnno).isEqualTo(invoiceAnno);
		});
	}

	@Test
	void testFieldNamesAreTheSame() {

		final List<String> backupFields = Arrays.stream(BackupInvoiceEntity.class.getDeclaredFields())
			.toList()
			.stream()
			.map(Field::getName)
			.toList();
		final List<String> invoiceFields = Arrays.stream(InvoiceEntity.class.getDeclaredFields())
			.toList()
			.stream()
			.map(Field::getName)
			.toList();

		assertThat(backupFields).isEqualTo(invoiceFields);
	}

}
