package se.sundsvall.invoicecache.service.batch.invoice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.invoicecache.service.batch.BatchHealthIndicator.DESCRIPTION;
import static se.sundsvall.invoicecache.service.batch.BatchHealthIndicator.STATUS_DOWN_MESSAGE;
import static se.sundsvall.invoicecache.service.batch.BatchHealthIndicator.STATUS_UNKNOWN_MESSAGE;
import static se.sundsvall.invoicecache.service.batch.BatchHealthIndicator.STATUS_UP_MESSAGE;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;

@ExtendWith(MockitoExtension.class)
class FetchInvoicesJobHealthIndicatorTest {

	@Mock
	private Health.Builder mockHealthBuilder;

	private FetchInvoicesJobHealthIndicator healthIndicator;

	@Test
	void initialState() {
		try (var mockStaticHealth = mockStatic(Health.class)) {
			mockStaticHealth.when(Health::unknown).thenReturn(mockHealthBuilder);

			when(mockHealthBuilder.withDetail(any(), any())).thenReturn(mockHealthBuilder);

			healthIndicator = new FetchInvoicesJobHealthIndicator();

			verify(mockHealthBuilder).withDetail(DESCRIPTION, STATUS_UNKNOWN_MESSAGE.formatted(FetchInvoicesJobHealthIndicator.NAME));
			verify(mockHealthBuilder).build();
			verifyNoMoreInteractions(mockHealthBuilder);

			mockStaticHealth.verify(Health::unknown);
			mockStaticHealth.verifyNoMoreInteractions();
		}
	}

	@Test
	void setHealthy() {
		healthIndicator = new FetchInvoicesJobHealthIndicator();

		try (var mockStaticHealth = mockStatic(Health.class)) {
			mockStaticHealth.when(Health::up).thenReturn(mockHealthBuilder);

			when(mockHealthBuilder.withDetail(any(), any())).thenReturn(mockHealthBuilder);

			healthIndicator.setHealthy();

			verify(mockHealthBuilder).withDetail(DESCRIPTION, STATUS_UP_MESSAGE.formatted(FetchInvoicesJobHealthIndicator.NAME));
			verify(mockHealthBuilder).build();
			verifyNoMoreInteractions(mockHealthBuilder);

			mockStaticHealth.verify(Health::up);
			mockStaticHealth.verifyNoMoreInteractions();
		}
	}

	@Test
	void setUnhealthy() {
		healthIndicator = new FetchInvoicesJobHealthIndicator();

		try (var mockStaticHealth = mockStatic(Health.class)) {
			mockStaticHealth.when(Health::down).thenReturn(mockHealthBuilder);

			when(mockHealthBuilder.withDetail(any(), any())).thenReturn(mockHealthBuilder);

			healthIndicator.setUnhealthy();

			verify(mockHealthBuilder).withDetail(DESCRIPTION, STATUS_DOWN_MESSAGE.formatted(FetchInvoicesJobHealthIndicator.NAME));
			verify(mockHealthBuilder).build();
			verifyNoMoreInteractions(mockHealthBuilder);

			mockStaticHealth.verify(Health::down);
			mockStaticHealth.verifyNoMoreInteractions();
		}
	}
}
