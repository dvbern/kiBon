package ch.dvbern.ebegu.entities;


import ch.dvbern.ebegu.util.Constants;
import org.hibernate.envers.Audited;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Objects;

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

	@Nonnull
	private Gesuchsteller copyForMutationOrErneuerung(@Nonnull Gesuchsteller mutation) {
		mutation.setMail(this.getMail());
		mutation.setMobile(this.getMobile());
		mutation.setTelefon(this.getTelefon());
		mutation.setTelefonAusland(this.getTelefonAusland());
		mutation.setEwkPersonId(this.getEwkPersonId());
		mutation.setEwkAbfrageDatum(this.getEwkAbfrageDatum());
		mutation.setDiplomatenstatus(this.isDiplomatenstatus());
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
//			Objects.equals(getEwkAbfrageDatum(), otherGesuchsteller.getEwkAbfrageDatum()) &&
			Objects.equals(isDiplomatenstatus(), otherGesuchsteller.isDiplomatenstatus());
	}

}
