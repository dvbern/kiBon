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
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EbeguUtil;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

/**
 * Entity fuer gesuchstellerdaten
 */
@Audited
@Entity
public class Gesuchsteller extends AbstractPersonEntity {

	private static final long serialVersionUID = -9032257320578372570L;

	@Pattern(regexp = Constants.REGEX_EMAIL, message = "{validator.constraints.Email.message}")
	@Size(min = 5, max = DB_DEFAULT_MAX_LENGTH)
	@NotNull
	@Column(nullable = false)
	private String mail;

	@Column(nullable = true, length = Constants.DB_DEFAULT_MAX_LENGTH)
	@Pattern(regexp = Constants.REGEX_TELEFON_MOBILE, message = "{error_invalid_mobilenummer}")
	private String mobile;

	@Column(nullable = true, length = Constants.DB_DEFAULT_MAX_LENGTH)
	@Pattern(regexp = Constants.REGEX_TELEFON, message = "{error_invalid_mobilenummer}")
	private String telefon;

	@Column(nullable = true, length = Constants.DB_DEFAULT_MAX_LENGTH)
	private String telefonAusland;

	@Column(nullable = true, length = Constants.DB_DEFAULT_MAX_LENGTH)
	private String ewkPersonId;

	@Column(nullable = true)
	private LocalDate ewkAbfrageDatum;

	@NotNull
	private boolean diplomatenstatus;

	@Nullable
	@ElementCollection(targetClass = Sprache.class, fetch = FetchType.EAGER)
	@Enumerated(EnumType.STRING)
	@CollectionTable(name="gesuchsteller_korrespondenz_sprachen")
	private Set<Sprache> korrespondenzSprachen = new TreeSet<>();


	public Gesuchsteller() {
	}

	public String getMail() {
		return mail;
	}

	public void setMail(final String mail) {
		this.mail = mail;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(final String mobile) {
		this.mobile = mobile;
	}

	public String getTelefon() {
		return telefon;
	}

	public void setTelefon(final String telefon) {
		this.telefon = telefon;
	}

	public String getTelefonAusland() {
		return telefonAusland;
	}

	public void setTelefonAusland(final String telefonAusland) {
		this.telefonAusland = telefonAusland;
	}

	public String getEwkPersonId() {
		return ewkPersonId;
	}

	public void setEwkPersonId(final String ewkPersonId) {
		this.ewkPersonId = ewkPersonId;
	}

	public LocalDate getEwkAbfrageDatum() {
		return ewkAbfrageDatum;
	}

	public void setEwkAbfrageDatum(LocalDate ewkAbfrageDatum) {
		this.ewkAbfrageDatum = ewkAbfrageDatum;
	}

	public boolean isDiplomatenstatus() {
		return diplomatenstatus;
	}

	public void setDiplomatenstatus(final boolean diplomatenstatus) {
		this.diplomatenstatus = diplomatenstatus;
	}

	@Nullable
	public Set<Sprache> getKorrespondenzSprachen() {
		return korrespondenzSprachen;
	}

	public void setKorrespondenzSprachen(@Nullable Set<Sprache> korrespondenzSprachen) {
		this.korrespondenzSprachen = korrespondenzSprachen;
	}

	@Nonnull
	private Gesuchsteller copyForMutationOrErneuerung(@Nonnull Gesuchsteller mutation) {
		mutation.setMail(this.getMail());
		mutation.setMobile(this.getMobile());
		mutation.setTelefon(this.getTelefon());
		mutation.setTelefonAusland(this.getTelefonAusland());
		mutation.setEwkPersonId(this.getEwkPersonId());
		mutation.setEwkAbfrageDatum(this.getEwkAbfrageDatum());
		mutation.setDiplomatenstatus(this.isDiplomatenstatus());
		Set<Sprache> currentSprachen = this.getKorrespondenzSprachen();
		if (currentSprachen != null && !currentSprachen.isEmpty()) {
			mutation.setKorrespondenzSprachen(new TreeSet<>(currentSprachen));
		}
		return mutation;
	}

	@Nonnull
	public Gesuchsteller copyForMutation(@Nonnull Gesuchsteller mutation) {
		super.copyForMutation(mutation);
		return copyForMutationOrErneuerung(mutation);
	}

	@Nonnull
	public Gesuchsteller copyForErneuerung(@Nonnull Gesuchsteller mutation) {
		super.copyForErneuerung(mutation);
		return copyForMutationOrErneuerung(mutation);
	}

	@Override
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
		if (!(other instanceof Gesuchsteller)) {
			return false;
		}
		final Gesuchsteller otherGesuchsteller = (Gesuchsteller) other;
		return Objects.equals(getMail(), otherGesuchsteller.getMail()) &&
			Objects.equals(getMobile(), otherGesuchsteller.getMobile()) &&
			Objects.equals(getTelefon(), otherGesuchsteller.getTelefon()) &&
			Objects.equals(getTelefonAusland(), otherGesuchsteller.getTelefonAusland()) &&
			EbeguUtil.isSameOrNullStrings(getEwkPersonId(), otherGesuchsteller.getEwkPersonId()) &&
			Objects.equals(isDiplomatenstatus(), otherGesuchsteller.isDiplomatenstatus()) &&
			Objects.equals(getKorrespondenzSprachen(), otherGesuchsteller.getKorrespondenzSprachen());
	}
}
