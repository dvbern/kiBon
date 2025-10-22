/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.dto.pendenz;

import java.time.LocalDate;

import javax.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.types.DateRange;
import io.github.threetenjaxb.core.LocalDateXmlAdapter;
import lombok.Getter;
import lombok.Setter;

@XmlRootElement(name = "pendenz")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class PendenzBetreuungDTO {

	private static final long serialVersionUID = -1277026654764135397L;

	@NotNull
	private String betreuungsNummer;

	@NotNull
	private String gemeindeName;

	@NotNull
	private String betreuungsId;

	@NotNull
	private String gesuchId;

	@NotNull
	private String kindId;

	@NotNull
	private String name;

	@NotNull
	private String vorname;

	@NotNull
	@XmlJavaTypeAdapter(LocalDateXmlAdapter.class)
	private LocalDate geburtsdatum;

	@NotNull
	private String typ;

	@NotNull
	private String gesuchsperiodeString;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXmlAdapter.class)
	private LocalDate eingangsdatum = null;

	@NotNull
	private BetreuungsangebotTyp betreuungsangebotTyp;

	@NotNull
	private String institutionName;

	@NotNull
	private String institutionId;

	@NotNull
	private String gemeindeId;

	@NotNull
	private AntragStatus antragStatus;

	public PendenzBetreuungDTO(
		String betreuungsNummer,
		String betreuungsId,
		Betreuungsstatus betreuungsstatus,
		String betreuungsVorgaengerId,
		String gesuchId,
		String kindId,
		String name,
		String vorname,
		LocalDate geburtsdatum,
		DateRange gesuchsperiodeGueltigkeit,
		@Nullable LocalDate eingangsdatum,
		BetreuungsangebotTyp betreuungsangebotTyp,
		String institutionName,
		String gemeindeName,
		String institutionId,
		String gemeindeId,
		AntragStatus antragStatus
	) {
		this.betreuungsNummer = betreuungsNummer;
		this.gemeindeName = gemeindeName;
		this.betreuungsId = betreuungsId;
		this.gesuchId = gesuchId;
		this.kindId = kindId;
		this.name = name;
		this.vorname = vorname;
		this.geburtsdatum = geburtsdatum;
		this.gesuchsperiodeString = gesuchsperiodeGueltigkeit.getGueltigAb()
			.getYear()
			+ "/"
			+ (gesuchsperiodeGueltigkeit.getGueltigBis().getYear() - 2000);
		this.eingangsdatum = eingangsdatum;
		this.betreuungsangebotTyp = betreuungsangebotTyp;
		this.institutionName = institutionName;
		if (betreuungsstatus == Betreuungsstatus.WARTEN) {
			if (betreuungsVorgaengerId == null) {
				this.typ = "PLATZBESTAETIGUNG";
			} else {
				//Wenn die Betreung eine VorgängerID hat ist sie mutiert
				this.typ = "PLATZBESTAETIGUNG_MUTATION";
			}
		} else {
			this.typ = betreuungsstatus.name();
		}
		this.gemeindeId = gemeindeId;
		this.institutionId = institutionId;
		this.antragStatus = antragStatus;
	}
}
