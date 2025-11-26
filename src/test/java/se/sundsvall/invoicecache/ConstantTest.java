package se.sundsvall.invoicecache;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ConstantTest {

	@Test
	void testRaindanceIssuerLegalId() {
		assertThat(Constant.RAINDANCE_ISSUER_LEGAL_ID).isEqualTo("2120002411");
	}
}
