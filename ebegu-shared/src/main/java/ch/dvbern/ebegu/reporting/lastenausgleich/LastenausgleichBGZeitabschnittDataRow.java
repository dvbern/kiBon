/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.reporting.lastenausgleich;

import java.math.BigDecimal;
import java.time.LocalDate;

import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO für den Lastenausgleich von KiBon
 */
@Getter
@Setter
public class LastenausgleichBGZeitabschnittDataRow {
	private String referenzNummer;
	private long bfsNummer;
	private String nameGemeinde;
	private String nachname;
	private String vorname;
	private LocalDate geburtsdatum;
	private LocalDate von;
	private LocalDate bis;
	private String institution;
	private BetreuungsangebotTyp betreuungsangebotTyp;
	private String betreuungsangebotTypTranslated;
	private BigDecimal bgPensum;
	private Boolean keinSelbstbehaltDurchGemeinde;
	private BigDecimal gutschein;
	private Boolean isKorrektur;

	public LastenausgleichBGZeitabschnittDataRow(
		String referenzNummer,
		String nachname,
		String vorname,
		LocalDate geburtsdatum,
		DateRange gueltigkeit,
		String institution,
		BetreuungsangebotTyp betreuungsangebotTyp,
		BigDecimal betreuungspensumProzent,
		int anspruchspensumProzent,
		Boolean keinSelbstbehaltDurchGemeinde,
		BigDecimal gutschein
	) {
		this.referenzNummer = referenzNummer;
		this.nachname = nachname;
		this.vorname = vorname;
		this.geburtsdatum = geburtsdatum;
		this.von = gueltigkeit.getGueltigAb();
		this.bis = gueltigkeit.getGueltigBis();
		this.institution = institution;
		this.betreuungsangebotTyp = betreuungsangebotTyp;
		this.bgPensum = betreuungspensumProzent.min(
			MathUtil.DEFAULT.from(anspruchspensumProzent)
		);
		this.keinSelbstbehaltDurchGemeinde = keinSelbstbehaltDurchGemeinde;
		this.gutschein = gutschein;
	}

	public LastenausgleichBGZeitabschnittDataRow() {
	}
}
