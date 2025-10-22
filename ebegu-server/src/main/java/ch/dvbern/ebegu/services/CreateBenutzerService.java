/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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
 *
 */

package ch.dvbern.ebegu.services;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.authentication.KibonJwt;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Berechtigung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.entities.sozialdienst.Sozialdienst;
import ch.dvbern.ebegu.enums.BenutzerStatus;
import ch.dvbern.ebegu.enums.RollenAbhaengigkeit;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.authentication.KeycloakApi;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;

import static ch.dvbern.ebegu.enums.UserRole.GESUCHSTELLER;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@ApplicationScoped
public class CreateBenutzerService {

	@Inject
	private KeycloakApi keycloakApi;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private MandantService mandantService;

	@Inject
	private Persistence persistence;

	@Inject
	private KibonJwt kibonJwt;

	@Inject
	private PasswordGenerator passwordGenerator;

	@Nonnull
	public Benutzer createAdminGemeindeByEmail(
		@Nonnull String adminMail,
		@Nonnull UserRole userRole,
		@Nonnull Gemeinde gemeinde
	) {
		requireNonNull(gemeinde);
		requireNonNull(gemeinde.getMandant());
		return createBenutzerFromEmail(
			adminMail,
			userRole,
			gemeinde.getMandant(),
			gemeinde,
			b -> b.getGemeindeList().add(gemeinde)
		);
	}

	@Nonnull
	public Benutzer createAdminInstitutionByEmail(
		@Nonnull String adminMail,
		@Nonnull Institution institution
	) {
		requireNonNull(institution);
		requireNonNull(institution.getMandant());
		return createBenutzerFromEmail(
			adminMail,
			UserRole.ADMIN_INSTITUTION,
			institution.getMandant(),
			institution,
			b -> b.setInstitution(institution)
		);
	}

	@Nonnull
	public Benutzer createAdminTraegerschaftByEmail(
		@Nonnull String adminMail,
		@Nonnull Traegerschaft traegerschaft
	) {
		requireNonNull(traegerschaft);
		requireNonNull(traegerschaft.getMandant());
		Benutzer admin = createBenutzerFromEmail(
			adminMail,
			UserRole.ADMIN_TRAEGERSCHAFT,
			traegerschaft.getMandant(),
			traegerschaft,
			b -> b.setTraegerschaft(traegerschaft)
		);

		return admin;
	}

	@Nonnull
	public Benutzer createAdminSozialdienstByEmail(
		@Nonnull String adminMail,
		@Nonnull Sozialdienst sozialdienst
	) {
		requireNonNull(sozialdienst);
		return createBenutzerFromEmail(
			adminMail,
			UserRole.ADMIN_SOZIALDIENST,
			sozialdienst.getMandant(),
			sozialdienst,
			b -> b.setSozialdienst(sozialdienst)
		);
	}

	@Nonnull
	public Benutzer createBenutzerFromEmail(
		@Nonnull String email,
		@Nonnull UserRole role,
		@Nonnull Mandant mandant
	) {
		Mandant associatedEntity = role.getRollenAbhaengigkeit()
			== RollenAbhaengigkeit.NONE ? null : mandant;
		return createBenutzerFromEmail(
			email,
			role,
			mandant,
			associatedEntity,
			b -> {
			}
		);
	}

	@Nonnull
	public Benutzer createKeycloakAccount(Benutzer benutzer) {
		benutzer.setInitialPassword(
			passwordGenerator.createRandomPassword()
		);

		String externalUuid = keycloakApi.create(benutzer);
		benutzer.setExternalUUID(externalUuid);
		if (GESUCHSTELLER != benutzer.getRole()) {
			// Gesuchsteller dürfen keinen Mitarbeiter-Zugang haben, alle andere schon
			// (auch die Test-Benutzer nicht)
			keycloakApi.addMitarbeiterAccessBenutzerRole(benutzer);
		}
		return benutzerService.saveBenutzer(benutzer);
	}

	@Nonnull
	private <T extends AbstractEntity> Benutzer createBenutzerFromEmail(
		@Nonnull String adminMail,
		@Nonnull UserRole role,
		@Nonnull Mandant mandant,
		@Nullable T associatedEntity,
		@Nonnull Consumer<Berechtigung> appender
	) {
		requireNonNull(adminMail);
		requireNonNull(mandant);

		checkArgument(
			role.getRollenAbhaengigkeit()
				.getAssociatedEntityClass()
				.map(clazz -> clazz.isInstance(associatedEntity))
				.orElseGet(() -> associatedEntity == null),
			MessageFormat.format(
				"Rollenabhaengikeit type {0} does not match {1}",
				role.getRollenAbhaengigkeit()
					.getAssociatedEntityClass(),
				associatedEntity
			)
		);

		final Benutzer benutzer = new Benutzer();
		benutzer.setEmail(adminMail);
		benutzer.setNachname(Constants.UNKNOWN);
		benutzer.setVorname(Constants.UNKNOWN);
		benutzer.setUsername(adminMail);
		benutzer.setStatus(BenutzerStatus.EINGELADEN);
		benutzer.setMandant(mandant);

		final Berechtigung berechtigung = new Berechtigung();
		berechtigung.setRole(role);
		berechtigung.setBenutzer(benutzer);
		berechtigung.setGueltigkeit(
			new DateRange(LocalDate.now(), Constants.END_OF_TIME)
		);
		benutzer.getBerechtigungen().add(berechtigung);

		appender.accept(berechtigung);
		return createKeycloakAccount(benutzer);
	}

	public void createNewBenutzerFromJwt() {
		var benutzer = new Benutzer();
		benutzer.setEmail(kibonJwt.getEmail());
		benutzer.setUsername(kibonJwt.getEmail());
		benutzer.setVorname(kibonJwt.getVorname());
		benutzer.setNachname(kibonJwt.getNachname());
		benutzer.setMandant(
			mandantService.findMandant(
				kibonJwt.getMandantUuid()
			).orElseThrow()
		);
		benutzer.setExternalUUID(kibonJwt.getExternalUUID());
		Berechtigung berechtigung = new Berechtigung();
		berechtigung.setRole(GESUCHSTELLER);
		berechtigung.setInstitution(null);
		berechtigung.setTraegerschaft(null);
		berechtigung.setBenutzer(benutzer);
		benutzer.getBerechtigungen().add(berechtigung);
		benutzer.setZpvNummer(kibonJwt.getZpvNummer());
		persistence.persist(benutzer);
	}

}
