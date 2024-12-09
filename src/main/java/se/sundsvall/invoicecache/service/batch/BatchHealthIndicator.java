package se.sundsvall.invoicecache.service.batch;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public abstract class BatchHealthIndicator implements HealthIndicator {

	public static final String DESCRIPTION = "description";

	public static final String STATUS_UNKNOWN_MESSAGE = "%s has never been executed";
	public static final String STATUS_UP_MESSAGE = "%s executed successfully";
	public static final String STATUS_DOWN_MESSAGE = "%s failed";

	private final String name;
	private Health health;

	protected BatchHealthIndicator(final String name) {
		this.name = name;

		health = Health.unknown()
			.withDetail(DESCRIPTION, STATUS_UNKNOWN_MESSAGE.formatted(name))
			.build();
	}

	public final void setHealthy() {
		health = Health.up()
			.withDetail(DESCRIPTION, STATUS_UP_MESSAGE.formatted(name))
			.build();
	}

	public final void setUnhealthy() {
		health = Health.down()
			.withDetail(DESCRIPTION, STATUS_DOWN_MESSAGE.formatted(name))
			.build();
	}

	@Override
	public final Health health() {
		return health;
	}

	@Override
	public final Health getHealth(boolean includeDetails) {
		return HealthIndicator.super.getHealth(includeDetails);
	}
}
