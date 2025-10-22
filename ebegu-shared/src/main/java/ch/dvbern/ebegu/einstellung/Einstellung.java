/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.einstellung;

import java.math.BigDecimal;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.ebegu.einstellung.validation.ValidEinstellungValueType;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.HasMandant;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.util.Constants;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.envers.Audited;

/**
 * Entitaet zum Speichern von zeitabhängigen Einstellungen auf Stufe System, Mandant oder Gemeinde in Ki-Tax
 */
@ValidEinstellungValueType
@Audited
@Entity
public class Einstellung extends AbstractEntity implements HasMandant {

	private static final long serialVersionUID = 8704632842261673111L;

	@NotNull
	@Column(name = "einstellung_key",
		nullable = false,
		updatable = false,
		length = Constants.DB_DEFAULT_MAX_LENGTH)
	@Enumerated(EnumType.STRING)
	private EinstellungKey key;

	@NotNull
	@Column(nullable = false, length = Constants.DB_DEFAULT_MAX_LENGTH)
	private String value;

	@Nullable
	@Column(nullable = true, length = Constants.DB_TEXTAREA_LENGTH)
	private String erklaerung;

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(updatable = false,
		foreignKey = @ForeignKey(name = "FK_einstellung_mandant_id"))
	private Mandant mandant;

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(updatable = false,
		foreignKey = @ForeignKey(name = "FK_einstellung_gemeinde_id"))
	private Gemeinde gemeinde;

	@NotNull
	@Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(updatable = false,
		foreignKey = @ForeignKey(name = "FK_einstellung_gesuchsperiode_id"))
	private Gesuchsperiode gesuchsperiode;

	public Einstellung() {
	}

	public Einstellung(
		@Nonnull EinstellungKey key,
		@Nonnull String value,
		@Nonnull Gesuchsperiode gesuchsperiode
	) {
		this.key = key;
		this.value = value;
		this.gesuchsperiode = gesuchsperiode;
	}

	public Einstellung(
		@Nonnull EinstellungKey key,
		@Nonnull String value,
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Mandant mandant
	) {
		this(key, value, gesuchsperiode);
		this.mandant = mandant;
	}

	public Einstellung(
		@Nonnull EinstellungKey key,
		@Nonnull String value,
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nullable Mandant mandant,
		@Nullable Gemeinde gemeinde,
		@Nullable String erklaerung
	) {
		this(key, value, gesuchsperiode);
		this.gemeinde = gemeinde;
		this.erklaerung = erklaerung;
		if (gemeinde != null) {
			this.mandant = gemeinde.getMandant();
		} else {
			this.mandant = mandant;
		}
	}

	public EinstellungKey getKey() {
		return key;
	}

	public void setKey(EinstellungKey key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Nullable
	public Mandant getMandant() {
		return mandant;
	}

	public void setMandant(@Nullable Mandant mandant) {
		this.mandant = mandant;
	}

	@Nullable
	public Gemeinde getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(@Nullable Gemeinde gemeinde) {
		this.gemeinde = gemeinde;
	}

	@Nonnull
	public Gesuchsperiode getGesuchsperiode() {
		return gesuchsperiode;
	}

	public void setGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	@Nullable
	public String getErklaerung() {
		return erklaerung;
	}

	public void setErklaerung(@Nullable final String erklaerung) {
		this.erklaerung = erklaerung;
	}

	/**
	 * Erstellt eine Kopie der Einstellung für eine neue Gesuchsperiode
	 */
	public Einstellung copyGesuchsperiode(
		@Nonnull Gesuchsperiode newGesuchsperiode
	) {
		return new Einstellung(
			this.getKey(),
			this.getValue(),
			newGesuchsperiode,
			this.getMandant(),
			this.getGemeinde(),
			this.getErklaerung()
		);
	}

	public BigDecimal getValueAsBigDecimal() {
		return new BigDecimal(value);
	}

	public Integer getValueAsInteger() {
		return Integer.valueOf(value);
	}

	public Boolean getValueAsBoolean() {
		return Boolean.parseBoolean(value);
	}

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		final Einstellung otherEinstellung = (Einstellung) other;
		return getKey() == otherEinstellung.getKey()
			&&
			Objects.equals(getValue(), otherEinstellung.getValue())
			&&
			Objects.equals(
				getGesuchsperiode(),
				otherEinstellung.getGesuchsperiode()
			)
			&&
			Objects.equals(getMandant(), otherEinstellung.getMandant())
			&&
			Objects.equals(getGemeinde(), otherEinstellung.getGemeinde())
			&&
			StringUtils.equals(
				getErklaerung(),
				otherEinstellung.getErklaerung()
			);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("key", key)
			.append("value", value)
			.append("erklärung", erklaerung)
			.append("mandant", mandant != null ? mandant.getName() : "null")
			.append(
				"gemeinde",
				gemeinde != null ? gemeinde.getName() : "null"
			)
			.append(
				"gesuchsperiode",
				gesuchsperiode.getGesuchsperiodeString()
			)
			.toString();
	}
}
