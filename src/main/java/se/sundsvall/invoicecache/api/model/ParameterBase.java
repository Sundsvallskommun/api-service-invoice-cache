package se.sundsvall.invoicecache.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class ParameterBase {

	@Schema(description = "Page number", examples = "1", defaultValue = "1")
	@Min(1)
	protected int page = 1;

	@Schema(description = "Result size per page", examples = "100", defaultValue = "100")
	@Min(1)
	@Max(1000)
	protected int limit = 100;
}
