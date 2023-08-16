package se.sundsvall.invoicecache.integration.raindance;

import java.math.BigDecimal;
import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Result from raindance.
 */
@Builder(setterPrefix = "with")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RaindanceQueryResultDto {

	private int nr;                     // numeric(15,0)
	private String kundidText;          // varchar(40)
	private String kundid;              // varchar(16)
	private String orgnr;               // varchar(40)
	private String kundrtyp;            // varchar(2)
	private BigDecimal beloppSek;       // numeric(15,2)
	private BigDecimal betaltSek;       // numeric(15,2)
	private BigDecimal momsVal;         // numeric(15,2)
	private Timestamp fakturadatum;     // datetime
	private Timestamp forfallodatum;    // datetime
	private Timestamp betpamdatum;      // datetime
	private Timestamp utskrdatum;       // datetime
	private String ocrnr;               // varchar(40)
	private String VREF;                // varchar(40)
	private String fakturastatus;       // varchar(40)
	private int kravniva;               // numeric(1,0)
	private String faktstatus;          // varchar(4)
	private String faktstatus2;         // varchar(4)
	private String tabBehand;           // varchar(4)
	private String namn2;               // varchar(40)
	private String adr2;                // varchar(40)
	private String ort;                 // varchar(40)
	private int z21_blopnr;             // int (from ik.BLOPNR)
	private int z21_rpnr;               // int (from ik.RPNR)
	private BigDecimal z21_beabel1;     // decimal(15,2) (from ik.BEABEL1)
	private Timestamp z21_beadat;       // datetime (from ik.BEADAT)
	private int z11_blopnr;             // int (from it.BLOPNR)
	private int z11_sbnr;               // int (from it.SBNR)
	private BigDecimal z11_beasum1;     // decimal(15,2) (from it.BEASUM1)
	private int z11_bearpnr;            // int (from it.BEARPNR)
	private Timestamp z11_beadat;       // datetime (from it.BEADAT)
	private String filnamn;             // varchar(40)

	private boolean isVoid;             // "dynamic", to be able to handle void ("makulerade") invoices
}
