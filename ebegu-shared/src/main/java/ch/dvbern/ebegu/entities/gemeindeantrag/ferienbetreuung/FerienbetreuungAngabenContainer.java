/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.entities.gemeindeantrag.ferienbetreuung;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.gemeindeantrag.GemeindeAntrag;
import ch.dvbern.ebegu.entities.gemeindeantrag.WithEinreichedatum;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungAngabenStatus;
import ch.dvbern.ebegu.enums.gemeindeantrag.GemeindeAntragTyp;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_TEXTAREA_LENGTH;

@Entity
@Audited
@Getter
@Setter
public class FerienbetreuungAngabenContainer extends AbstractEntity implements
	GemeindeAntrag,
	WithEinreichedatum {

	private static final long serialVersionUID = -3872331984799085800L;

	@NotNull
	@Nonnull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private FerienbetreuungAngabenStatus status;

	@NotNull
	@Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(
		name = "FK_ferienbetreuung_container_gemeinde_id"),
		nullable = false,
		updatable = false)
	private Gemeinde gemeinde;

	@NotNull
	@Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(
		name = "FK_ferienbetreuung_container_gesuchsperiode_id"),
		nullable = false,
		updatable = false)
	private Gesuchsperiode gesuchsperiode;

	@Nonnull
	@Valid
	@OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(
		name = "FK_ferienbetreuung_container_deklaration_id"),
		nullable = true)
	private FerienbetreuungAngaben angabenDeklaration;

	@Nullable
	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(
		name = "FK_ferienbetreuung_container_korrektur_id"),
		nullable = true)
	private FerienbetreuungAngaben angabenKorrektur;

	@Nullable
	@Size(max = DB_TEXTAREA_LENGTH)
	@Column(nullable = true)
	private String internerKommentar;

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(
		name = "FK_ferienbetreuung_container_verantwortlicher_id"))
	private Benutzer verantwortlicher = null;

	@Nullable
	@Valid
	@OneToMany(mappedBy = "ferienbetreuungAngabenContainer")
	private Set<FerienbetreuungDokument> dokumente;

	@Nullable
	@Column()
	private LocalDate einreichedatum;

	@Nonnull
	@Override
	public GemeindeAntragTyp getGemeindeAntragTyp() {
		return GemeindeAntragTyp.FERIENBETREUUNG;
	}

	@Nonnull
	@Override
	public String getStatusString() {
		return status.toString();
	}

	@Override
	public boolean isAntragAbgeschlossen() {
		return status == FerienbetreuungAngabenStatus.ABGESCHLOSSEN
			|| status == FerienbetreuungAngabenStatus.ABGELEHNT;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		return getId().equals(other.getId());
	}

	public boolean isAtLeastInPruefungKantonOrZurueckAnGemeinde() {
		return status == FerienbetreuungAngabenStatus.IN_PRUEFUNG_KANTON
			||
			status == FerienbetreuungAngabenStatus.GEPRUEFT
			||
			status == FerienbetreuungAngabenStatus.ABGESCHLOSSEN
			||
			status == FerienbetreuungAngabenStatus.ABGELEHNT
			||
			status == FerienbetreuungAngabenStatus.ZURUECK_AN_GEMEINDE
			||
			status == FerienbetreuungAngabenStatus.ZWEITPRUEFUNG;
	}

	public boolean isInPruefungKantonOrZweitpruefung() {
		return status == FerienbetreuungAngabenStatus.IN_PRUEFUNG_KANTON
			|| status == FerienbetreuungAngabenStatus.ZWEITPRUEFUNG;
	}

	public boolean isAtLeastGeprueft() {
		return status == FerienbetreuungAngabenStatus.GEPRUEFT
			||
			status == FerienbetreuungAngabenStatus.ABGESCHLOSSEN
			||
			status == FerienbetreuungAngabenStatus.ABGELEHNT;
	}

	public boolean isAbgeschlossen() {
		return status == FerienbetreuungAngabenStatus.ABGELEHNT
			||
			status == FerienbetreuungAngabenStatus.ABGESCHLOSSEN;
	}

	public void copyForFreigabe() {
		// Nur moeglich, wenn noch nicht freigegeben und ueberhaupt Daten zum kopieren vorhanden
		// falls der Antrag zurück an die Gemeinde gegeben wurde, werden durch die Gemeinde direkt die
		// angabenkorrektur bearbeitet. In diesem Fall muss nicht kopiert werden.
		if (status == FerienbetreuungAngabenStatus.IN_BEARBEITUNG_GEMEINDE) {
			angabenKorrektur = new FerienbetreuungAngaben(angabenDeklaration);
		}
	}

	public boolean isInBearbeitungGemeinde() {
		return status == FerienbetreuungAngabenStatus.IN_BEARBEITUNG_GEMEINDE;
	}

	public boolean isInBearbeitungOrZurueckAnGemeinde() {
		return this.isInBearbeitungGemeinde()
			|| status == FerienbetreuungAngabenStatus.ZURUECK_AN_GEMEINDE;
	}

	public void copyForErneuerung(FerienbetreuungAngabenContainer target) {
		final FerienbetreuungAngaben angabenVorjahr =
			isAtLeastInPruefungKantonOrZurueckAnGemeinde() ?
				getAngabenKorrektur() :
				getAngabenDeklaration();
		Objects.requireNonNull(angabenVorjahr);

		angabenVorjahr.copyForErneuerung(target.getAngabenDeklaration());
		copyDokumenteForErneuerung(target);
	}

	private void copyDokumenteForErneuerung(
		FerienbetreuungAngabenContainer target
	) {
		Set<FerienbetreuungDokument> dokumentCopies = new HashSet<>();
		if (getDokumente() != null && !getDokumente().isEmpty()) {
			dokumentCopies.addAll(
				getDokumente()
					.stream()
					.map(
						ferienbetreuungDokument -> ferienbetreuungDokument
							.copyDokument(
								new FerienbetreuungDokument(),
								target
							)
					)
					.collect(Collectors.toSet())
			);
		}
		target.setDokumente(dokumentCopies);
	}

	@Nullable
	@Override
	public Benutzer getVerantwortlicher() {
		return verantwortlicher;
	}

	public void setVerantwortlicher(@Nullable Benutzer verantwortlicher) {
		this.verantwortlicher = verantwortlicher;
	}

	public boolean isInZweitpruefung() {
		return status
			== FerienbetreuungAngabenStatus.ZWEITPRUEFUNG;
	}
}
