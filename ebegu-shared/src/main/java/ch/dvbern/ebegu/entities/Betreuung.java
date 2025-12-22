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

package ch.dvbern.ebegu.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.persistence.AssociationOverride;
import jakarta.persistence.AssociationOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import ch.dvbern.ebegu.entities.containers.BetreuungAbweichung;
import ch.dvbern.ebegu.entities.containers.BetreuungAndPensumContainer;
import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.enums.betreuung.Bedarfsstufe;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.betreuung.BetreuungspensumAbweichungStatus;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.validationgroups.BetreuungBestaetigenValidationGroup;
import ch.dvbern.ebegu.validators.CheckGrundAblehnung;
import ch.dvbern.ebegu.validators.CheckPlatzAndAngebottyp;
import ch.dvbern.ebegu.validators.betreuungspensum.CheckBetreuungspensum;
import ch.dvbern.ebegu.validators.betreuungspensum.CheckMittagstischPensum;
import ch.dvbern.ebegu.validators.dateranges.CheckAbwesenheitDatesOverlapping;
import ch.dvbern.ebegu.validators.dateranges.CheckBetreuungPensumContainerZeitraumInGesuchsperiode;
import ch.dvbern.ebegu.validators.dateranges.CheckBetreuungZeitraumInstitutionsStammdatenZeitraum;
import ch.dvbern.ebegu.validators.dateranges.CheckGueltigkeiten;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SortNatural;
import org.hibernate.envers.Audited;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

import static ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus.ABGEWIESEN;
import static ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus.BESTAETIGT;
import static ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus.STORNIERT;
import static ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus.WARTEN;

/**
 * Entity fuer Betreuungen.
 */
@Audited
@Entity
@CheckPlatzAndAngebottyp
@CheckGrundAblehnung
@CheckBetreuungspensum
@CheckAbwesenheitDatesOverlapping
@CheckMittagstischPensum
@CheckBetreuungPensumContainerZeitraumInGesuchsperiode(
	groups = BetreuungBestaetigenValidationGroup.class)
@CheckBetreuungZeitraumInstitutionsStammdatenZeitraum(
	groups = BetreuungBestaetigenValidationGroup.class)
// Der ForeignKey-Name wird leider nicht richtig generiert, muss von Hand angepasst werden!
@AssociationOverrides({
	@AssociationOverride(name = "kind",
		joinColumns = @JoinColumn(name = "kind_id"),
		foreignKey = @ForeignKey(name = "FK_betreuung_kind_id")),
	@AssociationOverride(name = "institutionStammdaten",
		joinColumns = @JoinColumn(name = "institutionStammdaten_id"),
		foreignKey = @ForeignKey(
			name = "FK_betreuung_institution_stammdaten_id"))
})
@Table(
	uniqueConstraints = @UniqueConstraint(columnNames = { "betreuungNummer",
		"kind_id" }, name = "UK_betreuung_kind_betreuung_nummer")
)
@Indexed
public class Betreuung extends AbstractPlatz implements
	BetreuungAndPensumContainer {

	private static final long serialVersionUID = -6776987863150835840L;

	/**
	 * Contains the VorgaengerVerfuegung that has already been paid. It can be null even in Mutationen if there was no
	 * Zahlung zet
	 */
	@Transient
	@Nullable
	private Map<ZahlungslaufTyp, Verfuegung> vorgaengerAusbezahlteVerfuegungProAuszahlungstyp =
		new HashMap<>();

	/**
	 * Contains a calculatedVerfuegung that we do not want to store in the database yet
	 */
	@Transient
	@Nullable
	private Verfuegung verfuegungPreview;

	@Transient
	@Getter
	@Setter
	private boolean markedForDeletion = false;

	@OneToMany(cascade = CascadeType.ALL,
		orphanRemoval = true,
		mappedBy = "betreuung")
	@SortNatural
	private @Valid Set<BetreuungspensumContainer> betreuungspensumContainers =
		new TreeSet<>();

	@OneToOne(optional = false,
		cascade = CascadeType.ALL,
		orphanRemoval = true,
		mappedBy = "betreuung")
	private @NotNull
	@Valid ErweiterteBetreuungContainer erweiterteBetreuungContainer =
		new ErweiterteBetreuungContainer(this);

	@OneToMany(cascade = CascadeType.ALL,
		orphanRemoval = true,
		mappedBy = "betreuung")
	private @Valid Set<AbwesenheitContainer> abwesenheitContainers =
		new TreeSet<>();

	@Column(nullable = true, length = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	private @Size(max = Constants.DB_TEXTAREA_LENGTH) String grundAblehnung;

	@OneToOne(optional = true,
		cascade = CascadeType.REMOVE,
		orphanRemoval = true,
		mappedBy = "betreuung")
	@Nullable
	private @Valid Verfuegung verfuegung;

	@Column(nullable = false)
	private @NotNull Boolean vertrag = false;

	@Nullable
	@Column(nullable = true)
	private LocalDate datumAblehnung;

	@Nullable
	@Column(nullable = true)
	private LocalDate datumBestaetigung;

	@Nullable
	@Column(nullable = true)
	private LocalDate datumAngefordert;

	@Nullable
	@Column(nullable = true)
	private Boolean betreuungMutiert;

	@Nullable
	@Column(nullable = true)
	private Boolean abwesenheitMutiert;

	@Nonnull
	@OneToMany(cascade = CascadeType.ALL,
		orphanRemoval = true,
		mappedBy = "betreuung")
	@SortNatural
	private Set<BetreuungspensumAbweichung> betreuungspensumAbweichungen =
		new TreeSet<>();

	@Column(nullable = false)
	private @NotNull boolean eventPublished = true;

	@Column(nullable = false)
	private @NotNull boolean eingewoehnung = false;

	@Column(nullable = false)
	private boolean auszahlungAnEltern = false;

	@Nullable
	@Column(nullable = true)
	private @Size(
		max = Constants.DB_TEXTAREA_LENGTH) String begruendungAuszahlungAnInstitution;

	@Enumerated(EnumType.STRING)
	@Column(nullable = true)
	@Nullable
	private Bedarfsstufe bedarfsstufe;

	public Betreuung() {
	}

	public Set<BetreuungspensumContainer> getBetreuungspensumContainers() {
		return betreuungspensumContainers;
	}

	public void setBetreuungspensumContainers(
		Set<BetreuungspensumContainer> betreuungspensumContainers
	) {
		this.betreuungspensumContainers = betreuungspensumContainers;
	}

	public Set<AbwesenheitContainer> getAbwesenheitContainers() {
		return abwesenheitContainers;
	}

	public void setAbwesenheitContainers(
		Set<AbwesenheitContainer> abwesenheiten
	) {
		this.abwesenheitContainers = abwesenheiten;
	}

	@Nonnull
	public ErweiterteBetreuungContainer getErweiterteBetreuungContainer() {
		return erweiterteBetreuungContainer;
	}

	public void setErweiterteBetreuungContainer(
		@Nonnull ErweiterteBetreuungContainer erweiterteBetreuungContainer
	) {
		this.erweiterteBetreuungContainer = erweiterteBetreuungContainer;
	}

	@Nullable
	public String getGrundAblehnung() {
		return grundAblehnung;
	}

	public void setGrundAblehnung(@Nullable String grundAblehnung) {
		this.grundAblehnung = grundAblehnung;
	}

	@Override
	@Nullable
	public Verfuegung getVerfuegung() {
		return verfuegung;
	}

	@Override
	public void setVerfuegung(@Nullable Verfuegung verfuegung) {
		this.verfuegung = verfuegung;
	}

	@Nonnull
	public Boolean getVertrag() {
		return vertrag;
	}

	public void setVertrag(@Nonnull Boolean vertrag) {
		this.vertrag = vertrag;
	}

	@Nullable
	public LocalDate getDatumAblehnung() {
		return datumAblehnung;
	}

	protected void setDatumAblehnung(@Nullable LocalDate datumAblehnung) {
		this.datumAblehnung = datumAblehnung;
	}

	@Nullable
	public LocalDate getDatumBestaetigung() {
		return datumBestaetigung;
	}

	public void setDatumBestaetigung(@Nullable LocalDate datumBestaetigung) {
		this.datumBestaetigung = datumBestaetigung;
	}

	@Nullable
	public Boolean getBetreuungMutiert() {
		return betreuungMutiert;
	}

	public void setBetreuungMutiert(@Nullable Boolean betreuungMutiert) {
		this.betreuungMutiert = betreuungMutiert;
	}

	@Nullable
	public Boolean getAbwesenheitMutiert() {
		return abwesenheitMutiert;
	}

	public void setAbwesenheitMutiert(@Nullable Boolean abwesenheitMutiert) {
		this.abwesenheitMutiert = abwesenheitMutiert;
	}

	@Nonnull
	public Set<BetreuungspensumAbweichung> getBetreuungspensumAbweichungen() {
		return betreuungspensumAbweichungen;
	}

	public void setBetreuungspensumAbweichungen(
		@Nonnull Set<BetreuungspensumAbweichung> betreuungspensumAbweichungen
	) {
		this.betreuungspensumAbweichungen = betreuungspensumAbweichungen;
	}

	@Override
	@Nullable
	public Verfuegung getVerfuegungPreview() {
		return verfuegungPreview;
	}

	@Override
	public void setVerfuegungPreview(@Nullable Verfuegung verfuegungPreview) {
		this.verfuegungPreview = verfuegungPreview;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		//by default just the fields that belong to the Betreuung itself
		return this.isSame(other, false, false);
	}

	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
	public boolean isSame(
		AbstractEntity other,
		boolean inklAbwesenheiten,
		boolean inklStatus
	) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!super.isSame(other)) {
			return false;
		}
		final Betreuung otherBetreuung = (Betreuung) other;

		boolean pensenSame = this.getBetreuungspensumContainers()
			.stream()
			.allMatch(
				pensCont -> otherBetreuung
					.getBetreuungspensumContainers()
					.stream()
					.anyMatch(
						otherPensenCont -> otherPensenCont
							.isSame(pensCont)
					)
			);

		boolean abwesenheitenSame = true;
		if (inklAbwesenheiten) {
			abwesenheitenSame = this.getAbwesenheitContainers()
				.stream()
				.allMatch(
					abwesenheitCont -> otherBetreuung
						.getAbwesenheitContainers()
						.stream()
						.anyMatch(
							otherAbwesenheitCont -> otherAbwesenheitCont
								.isSame(abwesenheitCont)
						)
				);
		}
		boolean statusSame = true;
		if (inklStatus) {
			statusSame = this.getBetreuungsstatus()
				== otherBetreuung.getBetreuungsstatus();
		}

		boolean sameErweiterteBeduerfnisse =
			getErweiterteBetreuungContainer().isSame(
				otherBetreuung.getErweiterteBetreuungContainer()
			);

		return pensenSame
			&& abwesenheitenSame
			&& statusSame
			&& sameErweiterteBeduerfnisse
			&& this.getBedarfsstufe() == otherBetreuung.bedarfsstufe
			&& this.isAuszahlungAnEltern()
				== otherBetreuung.isAuszahlungAnEltern();
	}

	@Transient
	public boolean isAngebotKita() {
		return BetreuungsangebotTyp.KITA == getBetreuungsangebotTyp();
	}

	@Transient
	public boolean isAngebotMittagstisch() {
		return BetreuungsangebotTyp.MITTAGSTISCH == getBetreuungsangebotTyp();
	}

	@Transient
	public boolean isAngebotAuszuzahlen() {
		return BetreuungsangebotTyp.getBetreuungsgutscheinTypes()
			.contains(getBetreuungsangebotTyp());
	}

	@Transient
	public boolean isAngebotTagesfamilien() {
		return BetreuungsangebotTyp.TAGESFAMILIEN == getBetreuungsangebotTyp();
	}

	/**
	 * Since it is used in email templates we need to pass the language as a String parameter
	 */
	@Transient
	public String getBetreuungsangebotTypTranslated(@Nonnull String sprache) {
		return ServerMessageUtil.translateEnumValue(
			getBetreuungsangebotTyp(),
			Locale.forLanguageTag(sprache),
			Objects.requireNonNull(extractGemeinde().getMandant())
		);
	}

	/**
	 * @return die Verfuegung oder Vorgaengerverfuegung dieser Betreuung
	 */
	@Override
	@Nullable
	public Verfuegung getVerfuegungOrVorgaengerVerfuegung() {
		if (getVerfuegung() != null) {
			return getVerfuegung();
		}
		return getVorgaengerVerfuegung();
	}

	@Nullable
	public Map<ZahlungslaufTyp, Verfuegung> getVorgaengerAusbezahlteVerfuegungProAuszahlungstyp() {
		checkVorgaengerInitialized();
		return vorgaengerAusbezahlteVerfuegungProAuszahlungstyp;
	}

	@Override
	public void initVorgaengerVerfuegungen(
		@Nullable Verfuegung vorgaenger,
		@Nullable Map<ZahlungslaufTyp, Verfuegung> vorgaengerAusbezahlt
	) {
		super.initVorgaengerVerfuegungen(vorgaenger, vorgaengerAusbezahlt);
		this.vorgaengerAusbezahlteVerfuegungProAuszahlungstyp =
			vorgaengerAusbezahlt;
	}

	@Nonnull
	public Betreuung copyBetreuung(
		@Nonnull Betreuung target,
		@Nonnull AntragCopyType copyType,
		@Nonnull KindContainer targetKindContainer,
		@Nonnull Eingangsart targetEingangsart
	) {
		super.copyAbstractPlatz(target, copyType, targetKindContainer);
		switch (copyType) {
		case MUTATION:
			for (BetreuungspensumContainer betreuungspensumContainer : this
				.getBetreuungspensumContainers()) {
				target.getBetreuungspensumContainers()
					.add(
						betreuungspensumContainer
							.copyBetreuungspensumContainer(
								new BetreuungspensumContainer(),
								copyType,
								target
							)
					);
			}

			for (BetreuungspensumAbweichung betreuungspensumAbweichung : this
				.getBetreuungspensumAbweichungen()) {
				if (betreuungspensumAbweichung.getStatus()
					== BetreuungspensumAbweichungStatus.NICHT_FREIGEGEBEN) {
					target.getBetreuungspensumAbweichungen()
						.add(
							betreuungspensumAbweichung
								.copyBetreuungspensumAbweichung(
									new BetreuungspensumAbweichung(),
									copyType,
									target
								)
						);
				}
			}

			for (AbwesenheitContainer abwesenheitContainer : this
				.getAbwesenheitContainers()) {
				target.getAbwesenheitContainers()
					.add(
						abwesenheitContainer.copyAbwesenheitContainer(
							new AbwesenheitContainer(),
							copyType,
							target
						)
					);
			}

			target.setErweiterteBetreuungContainer(
				erweiterteBetreuungContainer
					.copyErweiterteBetreuungContainer(
						new ErweiterteBetreuungContainer(),
						copyType,
						target
					)
			);

			target.setGrundAblehnung(this.getGrundAblehnung());
			target.setVerfuegung(null);
			target.setVertrag(this.getVertrag());
			target.setDatumAblehnung(this.getDatumAblehnung());
			target.setDatumBestaetigung(this.getDatumBestaetigung());
			target.setDatumAngefordert(this.getDatumAngefordert());
			target.setBetreuungMutiert(null);
			target.setAbwesenheitMutiert(null);
			target.setBedarfsstufe(this.bedarfsstufe);
			target.setGueltig(false);
			target.setAuszahlungAnEltern(this.isAuszahlungAnEltern());
			target.setBegruendungAuszahlungAnInstitution(
				this.getBegruendungAuszahlungAnInstitution()
			);
			target.setEingewoehnung(this.isEingewoehnung());
			break;
		case ERNEUERUNG:
		case ERNEUERUNG_AR_2023:
		case MUTATION_NEUES_DOSSIER:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}

	@CheckGueltigkeiten(message = "{invalid_betreuungspensen_dates}")
	@Nonnull
	@Override
	public List<Betreuungspensum> getBetreuungenGS() {
		return betreuungspensumContainers.stream()
			.map(BetreuungspensumContainer::getBetreuungspensumGS)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	@CheckGueltigkeiten(message = "{invalid_betreuungspensen_dates}")
	@Nonnull
	@Override
	public List<Betreuungspensum> getBetreuungenJA() {
		return betreuungspensumContainers.stream()
			.map(BetreuungspensumContainer::getBetreuungspensumJA)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	@CheckMittagstischPensum(message = "{invalid_mittagstisch_pensum}")
	public BetreuungAbweichung asAbweichungPensumContainer() {
		return new BetreuungAbweichung(this, this.betreuungspensumAbweichungen);
	}

	@Nonnull
	@Override
	public Optional<Betreuung> findBetreuung() {
		return Optional.of(this);
	}

	@Override
	protected boolean hasAnyNonZeroPensum() {
		for (BetreuungspensumContainer betreuungspensumContainer : betreuungspensumContainers) {
			if (MathUtil.isPositive(
				betreuungspensumContainer.getBetreuungspensumJA()
					.getPensum()
			)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean isBetreunungNichtAngetreten() {
		return Betreuungsstatus.NICHT_EINGETRETEN == getBetreuungsstatus();
	}

	public boolean hasAnspruch() {
		if (getVerfuegungOrVerfuegungPreview() != null) {
			List<VerfuegungZeitabschnitt> vzList =
				getVerfuegungOrVerfuegungPreview().getZeitabschnitte();
			BigDecimal value = vzList.stream()
				.map(VerfuegungZeitabschnitt::getBgPensum)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
			return MathUtil.isPositive(value);
		}
		return false;
	}

	public boolean hasErweiterteBetreuung() {
		return getErweiterteBetreuungContainer().getErweiterteBetreuungJA()
			!= null
			&& getErweiterteBetreuungContainer().getErweiterteBetreuungJA()
				.getErweiterteBeduerfnisse();
	}

	public boolean isErweiterteBeduerfnisseBestaetigt() {
		return getErweiterteBetreuungContainer().getErweiterteBetreuungJA()
			!= null
			&& getErweiterteBetreuungContainer().getErweiterteBetreuungJA()
				.isErweiterteBeduerfnisseBestaetigt();
	}

	public boolean hasErweiterteBeduerfnisseBetrag() {
		return getErweiterteBetreuungContainer().getErweiterteBetreuungJA()
			!= null
			&& getErweiterteBetreuungContainer().getErweiterteBetreuungJA()
				.getErweitereteBeduerfnisseBetrag()
				!= null;
	}

	@Override
	public String getMessageForAccessException() {
		return "referenzNummer: "
			+ getReferenzNummer()
			+ ", gesuchInfo: "
			+ this.extractGesuch().getMessageForAccessException();
	}

	public boolean isEventPublished() {
		return eventPublished;
	}

	public void setEventPublished(boolean eventPublished) {
		this.eventPublished = eventPublished;
	}

	public boolean isEingewoehnung() {
		return eingewoehnung;
	}

	public void setEingewoehnung(boolean eingewoehnung) {
		this.eingewoehnung = eingewoehnung;
	}

	public boolean isAuszahlungAnEltern() {
		return auszahlungAnEltern;
	}

	public void setAuszahlungAnEltern(boolean auszahlungAnEltern) {
		this.auszahlungAnEltern = auszahlungAnEltern;
	}

	@Nullable
	public String getBegruendungAuszahlungAnInstitution() {
		return begruendungAuszahlungAnInstitution;
	}

	public void setBegruendungAuszahlungAnInstitution(
		@Nullable String begruendungAuszahlungAnInstitution
	) {
		this.begruendungAuszahlungAnInstitution =
			begruendungAuszahlungAnInstitution;
	}

	@Nullable
	public Bedarfsstufe getBedarfsstufe() {
		return bedarfsstufe;
	}

	public void setBedarfsstufe(@Nullable Bedarfsstufe bedarfsstufe) {
		this.bedarfsstufe = bedarfsstufe;
	}

	@Nullable
	public LocalDate getDatumAngefordert() {
		return datumAngefordert;
	}

	protected void setDatumAngefordert(@Nullable LocalDate datumAngefordert) {
		this.datumAngefordert = datumAngefordert;
	}

	@Override
	public void setBetreuungsstatus(
		@NotNull Betreuungsstatus betreuungsstatus
	) {
		Betreuungsstatus oldStatus = this.getBetreuungsstatus();
		super.setBetreuungsstatus(betreuungsstatus);
		if (WARTEN.equals(betreuungsstatus)
			&& this.getDatumAngefordert() == null) {
			this.setDatumAngefordert(LocalDate.now());
		}
		if (ABGEWIESEN.equals(betreuungsstatus)
			&& !ABGEWIESEN.equals(oldStatus)) {
			this.setDatumAblehnung(LocalDate.now());
		}
		if ((BESTAETIGT.equals(betreuungsstatus)
			&& !BESTAETIGT.equals(oldStatus))
			|| (STORNIERT.equals(betreuungsstatus)
				&& !STORNIERT.equals(oldStatus))) {
			this.setDatumBestaetigung(LocalDate.now());
		}
	}
}
