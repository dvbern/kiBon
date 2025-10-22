/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import javax.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.AntragStatusDTO;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.GesuchBetreuungenStatus;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import io.github.threetenjaxb.core.LocalDateTimeXmlAdapter;
import io.github.threetenjaxb.core.LocalDateXmlAdapter;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO fuer Pendenzen
 */
@XmlRootElement(name = "pendenz")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class JaxAntragDTO extends JaxAbstractAntragDTO {

	private static final long serialVersionUID = -1277026654764135397L;

	//probably unused
	public JaxAntragDTO(
		String antragId,
		LocalDate gesuchsperiodeGueltigAb,
		LocalDate gesuchsperiodeGueltigBis,
		@Nullable LocalDate eingangsdatum,
		@Nullable LocalDate eingangsdatumSTV,
		AntragTyp antragTyp,
		int laufnummer,
		Eingangsart eingangsart
	) {
		this();
		this.antragId = antragId;
		this.gesuchsperiodeGueltigAb = gesuchsperiodeGueltigAb;
		this.gesuchsperiodeGueltigBis = gesuchsperiodeGueltigBis;
		this.eingangsdatum = eingangsdatum;
		this.eingangsdatumSTV = eingangsdatumSTV;
		this.antragTyp = antragTyp;
		this.laufnummer = laufnummer;
		this.eingangsart = eingangsart;
	}

	//constructor fuer query
	public JaxAntragDTO(
		String antragId,
		LocalDate gesuchsperiodeGueltigAb,
		LocalDate gesuchsperiodeGueltigBis,
		@Nullable LocalDate eingangsdatum,
		@Nullable LocalDate eingangsdatumSTV,
		AntragTyp antragTyp,
		AntragStatus antragStatus,
		int laufnummer,
		Eingangsart eingangsart,
		@Nullable String besitzerUsername
	) {
		this();
		this.antragId = antragId;
		this.gesuchsperiodeGueltigAb = gesuchsperiodeGueltigAb;
		this.gesuchsperiodeGueltigBis = gesuchsperiodeGueltigBis;
		this.eingangsdatum = eingangsdatum;
		this.eingangsdatumSTV = eingangsdatumSTV;
		this.antragTyp = antragTyp;
		this.verfuegt = antragStatus.isAnyStatusOfVerfuegt();
		this.beschwerdeHaengig = antragStatus
			== AntragStatus.BESCHWERDE_HAENGIG;
		this.laufnummer = laufnummer;
		this.eingangsart = eingangsart;
		this.besitzerUsername = besitzerUsername;
	}

	public JaxAntragDTO() {
		super(JaxAntragDTO.class.getSimpleName());
	}

	@NotNull
	private String antragId;

	@NotNull
	private Eingangsart eingangsart;

	@Nullable
	private String besitzerUsername;

	@NotNull
	private AntragTyp antragTyp;

	@NotNull
	@XmlJavaTypeAdapter(LocalDateXmlAdapter.class)
	private LocalDate gesuchsperiodeGueltigAb = null;

	@NotNull
	@XmlJavaTypeAdapter(LocalDateXmlAdapter.class)
	private LocalDate gesuchsperiodeGueltigBis = null;

	@NotNull
	private String verantwortlicherBG;    // Name Vorname

	@Nullable
	private String verantwortlicherTS; // Name Vorname

	@Nullable
	private String verantwortlicherUsernameBG;    // Wird fuer Freigabe gebraucht

	@Nullable
	private String verantwortlicherUsernameTS; // Wird fuer Freigabe gebraucht

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXmlAdapter.class)
	private LocalDate eingangsdatum = null;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXmlAdapter.class)
	private LocalDate regelnGueltigAb = null;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXmlAdapter.class)
	private LocalDate eingangsdatumSTV = null;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateTimeXmlAdapter.class)
	private LocalDateTime aenderungsdatum = null;

	@NotNull
	private Set<BetreuungsangebotTyp> angebote;

	@NotNull
	private Set<String> kinder;

	@NotNull
	private Set<String> institutionen;

	@NotNull
	private AntragStatusDTO status;

	@NotNull
	private int laufnummer;

	private boolean verfuegt;

	private boolean beschwerdeHaengig;

	@NotNull
	private GesuchBetreuungenStatus gesuchBetreuungenStatus;

	private boolean dokumenteHochgeladen;

	@Nullable
	private FinSitStatus finSitStatus;

	@NotNull
	private String gemeinde;

	@NotNull
	private String gemeindeId;

	private boolean isSozialdienst = false;

	private boolean internePendenz = false;

	private Boolean internePendenzAbgelaufen = false;

	@Nullable
	private String begruendungMutation;

	@Nullable
	private String gesuchsperiodeString;

}
