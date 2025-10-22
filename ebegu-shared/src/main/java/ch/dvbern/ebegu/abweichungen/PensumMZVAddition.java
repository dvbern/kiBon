package ch.dvbern.ebegu.abweichungen;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PensumMZVAddition {
	private BigDecimal anteil = BigDecimal.ZERO;
	private BigDecimal anzahlHauptmahlzeiten = BigDecimal.ZERO;
	private BigDecimal anzahlNebenmahlzeiten = BigDecimal.ZERO;
	private BigDecimal tarifHauptmahlzeiten = BigDecimal.ZERO;
	private BigDecimal tarifNebenmahlzeiten = BigDecimal.ZERO;
}
