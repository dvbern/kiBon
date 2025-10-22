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

import java.time.LocalDate;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 * Entity fuer Gesuchsperiode.
 */
@Audited
@Entity
public class Gesuchsperiode extends AbstractDateRangedEntity implements
	HasMandant {

	private static final long serialVersionUID = -9132257370971574570L;
	public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	@NotNull
	@Nonnull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private GesuchsperiodeStatus status = GesuchsperiodeStatus.ENTWURF;

	@NotNull
	@Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gesuchsperiode_mandant_id"),
		updatable = false)
	private Mandant mandant;

	// Wir merken uns, wann die Periode aktiv geschalten wurde, damit z.B. die Mails nicht 2 mal verschickt werden
	@Column(nullable = true)
	private LocalDate datumAktiviert;

	@Nullable
	@OneToOne(fetch = FetchType.LAZY,
		cascade = CascadeType.ALL,
		orphanRemoval = true)
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	private Vorlage verfuegungErlaeuterungenDe;

	@Nullable
	@OneToOne(fetch = FetchType.LAZY,
		cascade = CascadeType.ALL,
		orphanRemoval = true)
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	private Vorlage verfuegungErlaeuterungenFr;

	@Nullable
	@OneToOne(fetch = FetchType.LAZY,
		cascade = CascadeType.ALL,
		orphanRemoval = true)
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	private Vorlage vorlageMerkblattTsDe;

	@Nullable
	@OneToOne(fetch = FetchType.LAZY,
		cascade = CascadeType.ALL,
		orphanRemoval = true)
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	private Vorlage vorlageMerkblattTsFr;

	@Nullable
	@OneToOne(fetch = FetchType.LAZY,
		cascade = CascadeType.ALL,
		orphanRemoval = true)
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	private Vorlage vorlageVerfuegungLatsDe;

	@Nullable
	@OneToOne(fetch = FetchType.LAZY,
		cascade = CascadeType.ALL,
		orphanRemoval = true)
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	private Vorlage vorlageVerfuegungLatsFr;

	@Nullable
	@OneToOne(fetch = FetchType.LAZY,
		cascade = CascadeType.ALL,
		orphanRemoval = true)
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	private Vorlage vorlageVerfuegungFerienbetreuungDe;

	@Nullable
	@OneToOne(fetch = FetchType.LAZY,
		cascade = CascadeType.ALL,
		orphanRemoval = true)
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	private Vorlage vorlageVerfuegungFerienbetreuungFr;

	@Nonnull
	public GesuchsperiodeStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull GesuchsperiodeStatus status) {
		this.status = status;
	}

	public int getBasisJahr() {
		return getGueltigkeit().getGueltigAb().getYear() - 1;
	}

	public int getBasisJahrPlus1() {
		return getBasisJahr() + 1;
	}

	public int getBasisJahrPlus2() {
		return getBasisJahr() + 2;
	}

	public String getBasisJahrPlus1AsString() {
		return String.valueOf(getBasisJahrPlus1());
	}

	public LocalDate getDatumAktiviert() {
		return datumAktiviert;
	}

	public void setDatumAktiviert(LocalDate datumAktiviert) {
		this.datumAktiviert = datumAktiviert;
	}

	@Nonnull
	public byte[] getVerfuegungErlaeuterungenDe() {
		if (this.verfuegungErlaeuterungenDe == null) {
			return EMPTY_BYTE_ARRAY;
		}
		return this.verfuegungErlaeuterungenDe.getVorlageDokument();
	}

	public void setVerfuegungErlaeuterungenDe(
		@Nullable byte[] verfuegungErlaeuterungenDe
	) {
		if (verfuegungErlaeuterungenDe == null) {
			this.verfuegungErlaeuterungenDe = null;
		} else {
			if (this.verfuegungErlaeuterungenDe == null) {
				this.verfuegungErlaeuterungenDe = new Vorlage(this.mandant);
			}
			this.verfuegungErlaeuterungenDe.setVorlageDokument(
				verfuegungErlaeuterungenDe
			);
		}
	}

	@Nonnull
	public byte[] getVerfuegungErlaeuterungenFr() {
		if (this.verfuegungErlaeuterungenFr == null) {
			return EMPTY_BYTE_ARRAY;
		}
		return this.verfuegungErlaeuterungenFr.getVorlageDokument();
	}

	public void setVerfuegungErlaeuterungenFr(
		@Nullable byte[] verfuegungErlaeuterungenFr
	) {
		if (verfuegungErlaeuterungenFr == null) {
			this.verfuegungErlaeuterungenFr = null;
		} else {
			if (this.verfuegungErlaeuterungenFr == null) {
				this.verfuegungErlaeuterungenFr = new Vorlage(this.mandant);
			}
			this.verfuegungErlaeuterungenFr.setVorlageDokument(
				verfuegungErlaeuterungenFr
			);
		}
	}

	/**
	 * Returns the correct VerfuegungErlaeuterung for the given language
	 */
	@Nonnull
	public byte[] getVerfuegungErlaeuterungWithSprache(
		@Nonnull Sprache sprache
	) {
		switch (sprache) {
		case DEUTSCH:
			return this.getVerfuegungErlaeuterungenDe();
		case FRANZOESISCH:
			return this.getVerfuegungErlaeuterungenFr();
		default:
			return EMPTY_BYTE_ARRAY;
		}
	}

	@Nonnull
	public byte[] getVorlageMerkblattTsDe() {
		if (this.vorlageMerkblattTsDe == null) {
			return EMPTY_BYTE_ARRAY;
		}
		return this.vorlageMerkblattTsDe.getVorlageDokument();
	}

	public void setVorlageMerkblattTsDe(@Nullable byte[] vorlageMerkblattTsDe) {
		if (vorlageMerkblattTsDe == null) {
			this.vorlageMerkblattTsDe = null;
		} else {
			if (this.vorlageMerkblattTsDe == null) {
				this.vorlageMerkblattTsDe = new Vorlage(this.mandant);
			}
			this.vorlageMerkblattTsDe.setVorlageDokument(vorlageMerkblattTsDe);
		}
	}

	@Nonnull
	public byte[] getVorlageMerkblattTsFr() {
		if (this.vorlageMerkblattTsFr == null) {
			return EMPTY_BYTE_ARRAY;
		}
		return this.vorlageMerkblattTsFr.getVorlageDokument();
	}

	public void setVorlageMerkblattTsFr(@Nullable byte[] vorlageMerkblattTsFr) {
		if (vorlageMerkblattTsFr == null) {
			this.vorlageMerkblattTsFr = null;
		} else {
			if (this.vorlageMerkblattTsFr == null) {
				this.vorlageMerkblattTsFr = new Vorlage(this.mandant);
			}
			this.vorlageMerkblattTsFr.setVorlageDokument(vorlageMerkblattTsFr);
		}
	}

	/**
	 * Returns the correct VerfuegungErlaeuterung for the given language
	 */
	@Nonnull
	public byte[] getVorlageMerkblattTsWithSprache(
		@Nonnull Sprache sprache
	) {
		switch (sprache) {
		case DEUTSCH:
			return this.getVorlageMerkblattTsDe();
		case FRANZOESISCH:
			return this.getVorlageMerkblattTsFr();
		default:
			return EMPTY_BYTE_ARRAY;
		}
	}

	@Nonnull
	public byte[] getVorlageVerfuegungLatsDe() {
		if (this.vorlageVerfuegungLatsDe == null) {
			return EMPTY_BYTE_ARRAY;
		}
		return this.vorlageVerfuegungLatsDe.getVorlageDokument();
	}

	public void setVorlageVerfuegungLatsDe(
		@Nullable byte[] vorlageVerfuegungLatsDe
	) {
		if (vorlageVerfuegungLatsDe == null) {
			this.vorlageVerfuegungLatsDe = null;
		} else {
			if (this.vorlageVerfuegungLatsDe == null) {
				this.vorlageVerfuegungLatsDe = new Vorlage(this.mandant);
			}
			this.vorlageVerfuegungLatsDe.setVorlageDokument(
				vorlageVerfuegungLatsDe
			);
		}
	}

	@Nonnull
	public byte[] getVorlageVerfuegungLatsFr() {
		if (this.vorlageVerfuegungLatsFr == null) {
			return EMPTY_BYTE_ARRAY;
		}
		return this.vorlageVerfuegungLatsFr.getVorlageDokument();
	}

	public void setVorlageVerfuegungLatsFr(
		@Nullable byte[] vorlageVerfuegungLatsFr
	) {
		if (vorlageVerfuegungLatsFr == null) {
			this.vorlageVerfuegungLatsFr = null;
		} else {
			if (this.vorlageVerfuegungLatsFr == null) {
				this.vorlageVerfuegungLatsFr = new Vorlage(this.mandant);
			}
			this.vorlageVerfuegungLatsFr.setVorlageDokument(
				vorlageVerfuegungLatsFr
			);
		}
	}

	@Nonnull
	public byte[] getVorlageVerfuegungFerienbetreuungDe() {
		if (this.vorlageVerfuegungFerienbetreuungDe == null) {
			return EMPTY_BYTE_ARRAY;
		}
		return this.vorlageVerfuegungFerienbetreuungDe.getVorlageDokument();
	}

	public void setVorlageVerfuegungFerienbetreuungDe(
		@Nullable byte[] vorlageVerfuegungFerienbetreuungDe
	) {
		if (vorlageVerfuegungFerienbetreuungDe == null) {
			this.vorlageVerfuegungFerienbetreuungDe = null;
		} else {
			if (this.vorlageVerfuegungFerienbetreuungDe == null) {
				this.vorlageVerfuegungFerienbetreuungDe = new Vorlage(
					this.mandant
				);
			}
			this.vorlageVerfuegungFerienbetreuungDe.setVorlageDokument(
				vorlageVerfuegungFerienbetreuungDe
			);
		}
	}

	@Nonnull
	public byte[] getVorlageVerfuegungFerienbetreuungFr() {
		if (this.vorlageVerfuegungFerienbetreuungFr == null) {
			return EMPTY_BYTE_ARRAY;
		}
		return this.vorlageVerfuegungFerienbetreuungFr.getVorlageDokument();
	}

	public void setVorlageVerfuegungFerienbetreuungFr(
		@Nullable byte[] vorlageVerfuegungFerienbetreuungFr
	) {
		if (vorlageVerfuegungFerienbetreuungFr == null) {
			this.vorlageVerfuegungFerienbetreuungFr = null;
		} else {
			if (this.vorlageVerfuegungFerienbetreuungFr == null) {
				this.vorlageVerfuegungFerienbetreuungFr = new Vorlage(
					this.mandant
				);
			}
			this.vorlageVerfuegungFerienbetreuungFr.setVorlageDokument(
				vorlageVerfuegungFerienbetreuungFr
			);
		}
	}

	/**
	 * Returns the correct VerfuegungErlaeuterung for the given language
	 */
	@Nonnull
	public byte[] getVorlageVerfuegungLatsWithSprache(
		@Nonnull Sprache sprache
	) {
		switch (sprache) {
		case DEUTSCH:
			return this.getVorlageVerfuegungLatsDe();
		case FRANZOESISCH:
			return this.getVorlageVerfuegungLatsFr();
		default:
			throw new EbeguRuntimeException(
				"getVorlageVerfuegungLatsWithSprache",
				"Sprache not defined",
				sprache
			);
		}
	}

	/**
	 * Returns the correct VorlageVerfuegungFerienbetreuung for the given language
	 */
	@Nonnull
	public byte[] getVorlageVerfuegungFerienbetreuungWithSprache(
		@Nonnull Sprache sprache
	) {
		switch (sprache) {
		case DEUTSCH:
			return this.getVorlageVerfuegungFerienbetreuungDe();
		case FRANZOESISCH:
			return this.getVorlageVerfuegungFerienbetreuungFr();
		default:
			throw new EbeguRuntimeException(
				"getVorlageVerfuegungFerienbetreuungWithSprache",
				"Sprache not defined",
				sprache
			);
		}
	}

	@Override
	@SuppressWarnings({ "OverlyComplexBooleanExpression",
		"PMD.CompareObjectsWithEquals" })
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
	public boolean isSame(AbstractEntity other) {
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
		final Gesuchsperiode otherGesuchsperiode = (Gesuchsperiode) other;
		return this.getStatus() == otherGesuchsperiode.getStatus();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("gueltigkeit", getGueltigkeit().toString())
			.append("status", status.name())
			.toString();
	}

	public String getGesuchsperiodeString() {
		DateRange gueltigkeit = this.getGueltigkeit();
		return gueltigkeit.getGueltigAb().getYear()
			+ "/"
			+ gueltigkeit.getGueltigBis().getYear();
	}

	/**
	 * Gibt den GesuchsperiodenString im Format 2022/23 zurück
	 */
	public String getGesuchsperiodeStringShort() {
		DateRange gueltigkeit = this.getGueltigkeit();
		int year2000 = 2000;
		return gueltigkeit.getGueltigAb().getYear()
			+ "/"
			+ (gueltigkeit.getGueltigBis().getYear() - year2000);
	}

	public String getGesuchsperiodeDisplayName(@Nonnull Locale locale) {
		DateRange gueltigkeit = this.getGueltigkeit();

		return Constants.DATE_FORMATTER.format(gueltigkeit.getGueltigAb())
			+ " - "
			+ Constants.DATE_FORMATTER.format(gueltigkeit.getGueltigBis());
	}

	public String getGesuchsperiodeStatusName(@Nonnull Locale locale) {
		return "("
			+ ServerMessageUtil.translateEnumValue(status, locale, mandant)
			+ ')';
	}

	@Nonnull
	@Override
	public Mandant getMandant() {
		return mandant;
	}

	@Override
	public void setMandant(@Nonnull Mandant mandant) {
		this.mandant = mandant;
	}
}
