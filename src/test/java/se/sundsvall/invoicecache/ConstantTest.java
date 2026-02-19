package se.sundsvall.invoicecache;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConstantTest {

	@Test
	void testRaindanceIssuerLegalId() {
		assertThat(Constant.RAINDANCE_ISSUER_LEGAL_ID).isEqualTo("2120002411");
	}
}
