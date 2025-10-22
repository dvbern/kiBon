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

package ch.dvbern.ebegu.locallogin;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.RunAs;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import ch.dvbern.ebegu.authentication.ExternalUUIDUtil;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.entities.sozialdienst.Sozialdienst;
import ch.dvbern.ebegu.enums.BenutzerQueries_;
import ch.dvbern.ebegu.enums.BenutzerStatus;
import ch.dvbern.ebegu.enums.RollenAbhaengigkeit;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.locallogin.UserTemplates.UserTemplate;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.CreateBenutzerService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.MandantService;
import ch.dvbern.ebegu.services.SozialdienstService;
import ch.dvbern.ebegu.services.TraegerschaftService;
import ch.dvbern.ebegu.services.authentication.KeycloakApi;
import ch.dvbern.ebegu.util.TutorialConstants;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jboss.ejb3.annotation.RunAsPrincipal;

import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

@RunAs(UserRoleName.SUPER_ADMIN)
@RunAsPrincipal(PrincipalBean.KIBON_SERVICE_ACCOUNT)
@Stateless
public class LocalLoginService {
	@Inject
	private KeycloakApi keycloakApi;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private TraegerschaftService traegerschaftService;

	@Inject
	private InstitutionService institutionService;

	@Inject
	private MandantService mandantService;

	@Inject
	private CreateBenutzerService createBenutzerService;

	@Inject
	private SozialdienstService sozialdienstService;

	@Inject
	private Persistence persistence;

	@Inject
	private LocalLoginConfig localLoginConfig;

	record LocalLoginBenutzer(String name, String vorname, String email,
							  UserRole role, String info) {
		static LocalLoginBenutzer from(Benutzer benutzer) {
			return new LocalLoginBenutzer(
				benutzer.getNachname(),
				benutzer.getVorname(),
				benutzer.getEmail(),
				benutzer.getCurrentBerechtigung().getRole(),
				getInfoText(benutzer)
			);
		}

		@JsonProperty
		public String group() {
			RollenAbhaengigkeit rollenAbhaengigkeit =
				role.getRollenAbhaengigkeit();

			if (role == UserRole.GESUCHSTELLER) {
				return role.name();
			}

			return switch (rollenAbhaengigkeit) {
			case SOZIALDIENST, KANTON, TRAEGERSCHAFT, GEMEINDE, INSTITUTION ->
				rollenAbhaengigkeit.name();
			case NONE -> "MANDANT";
			};
		}

		static String getInfoText(Benutzer benutzer) {
			return switch (benutzer.getCurrentBerechtigung()
				.getRole()
				.getRollenAbhaengigkeit()) {
			case GEMEINDE -> benutzer.getCurrentBerechtigung()
				.extractGemeindenForBerechtigungAsString();
			case INSTITUTION -> benutzer.getCurrentBerechtigung()
				.getInstitution()
				.getName();
			case TRAEGERSCHAFT -> benutzer.getCurrentBerechtigung()
				.getTraegerschaft()
				.getName();
			case KANTON -> benutzer.getMandant().getName();
			case SOZIALDIENST -> benutzer.getCurrentBerechtigung()
				.getSozialdienst()
				.getName();
			case NONE -> "";
			};
		}
	}

	public record Result(String email, boolean ok, String message) {
		static Result successful(String email) {
			return new Result(email, true, "");
		}

		static Result failed(String email, String message) {
			return new Result(email, false, message);
		}
	}

	@PermitAll
	@Transactional
	public List<Result> createPersonaUsers(
		MandantIdentifier mandantIdentifier
	) {
		var mandant = mandantService.findMandantByIdentifier(mandantIdentifier)
			.orElseThrow();

		List<Result> results = new ArrayList<>();

		for (var template : UserTemplates.getMandantsUserTemplates(
			mandantIdentifier
		)) {
			results.add(
				createLocalLoginUser(
					template,
					mandant,
					this::createBenutzerWithoutAbhaengigkeit
				)
			);
		}

		for (var template : UserTemplates.getGesuchstellende(
			mandantIdentifier
		)) {
			results.add(
				createLocalLoginUser(
					template,
					mandant,
					this::createBenutzerWithoutAbhaengigkeit
				)
			);
		}

		for (var template : UserTemplates.getTagesschuleUserTemplates(
			mandantIdentifier
		)) {
			createLocalLoginUser(
				template,
				mandant,
				this::createInstitutionsUser
			);
		}

		for (var template : UserTemplates.getInstitutionsUserTemplates(
			mandantIdentifier
		)) {
			createLocalLoginUser(
				template,
				mandant,
				this::createInstitutionsUser
			);
		}

		for (var template : UserTemplates.getTraegerschaftsUserTemplates(
			mandantIdentifier
		)) {
			createLocalLoginUser(
				template,
				mandant,
				this::createTraegerschaftUser
			);
		}

		for (var template : UserTemplates.getSozialdienstUserTemplates(
			mandantIdentifier
		)) {
			results.add(
				createLocalLoginUser(
					template,
					mandant,
					this::createSozialdienstUser
				)
			);
		}

		for (var template : UserTemplates.getDefaultGemeindeUserTemplates(
			mandantIdentifier
		)) {
			results.add(
				createLocalLoginUser(
					template,
					mandant,
					this::createGemeindeUser
				)
			);
		}

		for (var template : UserTemplates.getSecondGemeindeUserTemplates(
			mandantIdentifier
		)) {
			results.add(
				createLocalLoginUser(
					template,
					mandant,
					this::createGemeindeUser
				)
			);
		}

		for (var template : UserTemplates.getCombinedGemeindeUserTemplates(
			mandantIdentifier
		)) {
			results.add(
				createLocalLoginUser(
					template,
					mandant,
					this::createGemeindeUser
				)
			);
		}

		return results;
	}

	@RolesAllowed(SUPER_ADMIN)
	@Transactional
	public List<Result> createTutorialBenutzer(
		TutorialConstants constants,
		Mandant mandant
	) {

		List<Result> results = new ArrayList<>();

		UserTemplate sbInstitution = UserTemplate.builder()
			.vorname("Sophie")
			.name("Tutorial")
			.role(UserRole.SACHBEARBEITER_INSTITUTION)
			.mandantIdentifier(mandant.getMandantIdentifier())
			.institutionId(constants.getInstitutionTutorialId())
			.build();
		results.add(
			createLocalLoginUser(
				sbInstitution,
				mandant,
				this::createInstitutionsUser
			)
		);

		UserTemplate adminGemeinde = UserTemplate.builder()
			.vorname("Gerlinde")
			.name("Tutorial")
			.role(UserRole.ADMIN_BG)
			.mandantIdentifier(mandant.getMandantIdentifier())
			.gemeindeIds(Set.of(constants.getGemeindeTutorialId()))
			.build();

		results.add(
			createLocalLoginUser(
				adminGemeinde,
				mandant,
				this::createGemeindeUser
			)
		);
		return results;
	}

	private Result createLocalLoginUser(
		UserTemplate userTemplate,
		Mandant mandant,
		Function<UserTemplate, Benutzer> roleSpecificCreateBenutzer
	) {
		try {
			var mandantIdentifier = mandant.getMandantIdentifier();

			Optional<Benutzer> existingKibonBenutzer = benutzerService
				.findByEmail(
					userTemplate.email(localLoginConfig.getEmailTemplate()),
					mandant
				);
			Optional<String> existingKeycloakUuid = keycloakApi
				.findByEmail(
					userTemplate.email(localLoginConfig.getEmailTemplate()),
					mandantIdentifier
				);

			if (existingKibonBenutzer.isPresent()) {
				Benutzer benutzer = existingKibonBenutzer.get();
				benutzer
					.getCurrentBerechtigung()
					.setRole(userTemplate.role());
				benutzer.setVorname(userTemplate.vorname());
				benutzer.setNachname(userTemplate.name());
			}

			if (existingKibonBenutzer.isPresent()
				&& existingKeycloakUuid.isPresent()
				&& ExternalUUIDUtil.equals(
					existingKibonBenutzer.get()
						.getExternalUUID(),
					existingKeycloakUuid.get()
				)
				&& existingKibonBenutzer.get()
					.getCurrentBerechtigung()
					.getRole()
					== userTemplate.role()) {
				return Result.successful(
					userTemplate.email(localLoginConfig.getEmailTemplate())
				); // User exists both in Kibon and Keycloak and they are linked -> nothing more to do
			}

			if (existingKibonBenutzer.isEmpty()
				&& existingKeycloakUuid.isPresent()) {
				if (localLoginConfig.isDeleteKeycloakUsers()) {
					deleteKeycloakUser(
						userTemplate,
						mandantIdentifier
					);
				} else {
					return Result.failed(
						userTemplate.email(localLoginConfig.getEmailTemplate()),
						MessageFormat.format(
							"User only exists in keycloak, manual clean-up required: {0}",
							existingKeycloakUuid
						)
					);
				}
			} else if (existingKibonBenutzer.isPresent()
				&& existingKeycloakUuid.isEmpty()) {
				return Result.failed(
					userTemplate.email(localLoginConfig.getEmailTemplate()),
					MessageFormat.format(
						"User only exists in kibon, manual clean-up required: {0}}",
						existingKibonBenutzer
					)
				);
			}

			Benutzer benutzer = roleSpecificCreateBenutzer.apply(
				userTemplate
			);

			benutzer.setVorname(userTemplate.vorname());
			benutzer.setNachname(userTemplate.name());
			benutzer.getCurrentBerechtigung().setRole(userTemplate.role());
			benutzer.setStatus(BenutzerStatus.AKTIV);

			keycloakApi.configureForLocalLogin(
				benutzer,
				userTemplate
			);
			return Result.successful(
				userTemplate.email(localLoginConfig.getEmailTemplate())
			);
		} catch (Exception e) {
			return Result.failed(
				userTemplate.email(localLoginConfig.getEmailTemplate()),
				e.getMessage()
			);
		}
	}

	private void deleteKeycloakUser(
		UserTemplate userTemplate,
		MandantIdentifier mandantIdentifier
	) {
		keycloakApi.findByEmail(
			userTemplate.email(localLoginConfig.getEmailTemplate()),
			mandantIdentifier
		)
			.ifPresent(
				uuid -> keycloakApi.deleteByExternalUUID(
					uuid,
					mandantIdentifier
				)
			);

	}

	private Benutzer createBenutzerWithoutAbhaengigkeit(
		UserTemplate userTemplate
	) {
		var mandant = mandantService.findMandantByIdentifier(
			userTemplate.mandantIdentifier()
		).orElseThrow();

		return createBenutzerService
			.createBenutzerFromEmail(
				userTemplate.email(localLoginConfig.getEmailTemplate()),
				userTemplate.role(),
				mandant
			);
	}

	private Benutzer createTraegerschaftUser(UserTemplate userTemplate) {
		Traegerschaft traegerschaft = traegerschaftService
			.findTraegerschaft(
				userTemplate.traegerschaftId()
			)
			.orElseThrow();

		return createBenutzerService
			.createAdminTraegerschaftByEmail(
				userTemplate.email(localLoginConfig.getEmailTemplate()),
				traegerschaft
			);

	}

	private Benutzer createInstitutionsUser(UserTemplate userTemplate) {
		Institution institution = institutionService
			.findInstitution(
				userTemplate.institutionId(),
				false
			)
			.orElseThrow();

		return createBenutzerService
			.createAdminInstitutionByEmail(
				userTemplate.email(localLoginConfig.getEmailTemplate()),
				institution
			);

	}

	private Benutzer createSozialdienstUser(UserTemplate userTemplate) {
		Sozialdienst sozialdienst = sozialdienstService
			.findSozialdienst(
				userTemplate.sozialdienstId()
			)
			.orElseThrow();

		return createBenutzerService
			.createAdminSozialdienstByEmail(
				userTemplate.email(localLoginConfig.getEmailTemplate()),
				sozialdienst
			);
	}

	private Benutzer createGemeindeUser(UserTemplate userTemplate) {
		List<Gemeinde> gemeinden = userTemplate.gemeindeIds()
			.stream()
			.map(gemeindeService::findGemeinde)
			.map(Optional::orElseThrow)
			.toList();

		var firstGemeinde = gemeinden.get(0);

		Benutzer benutzer = createBenutzerService
			.createAdminGemeindeByEmail(
				userTemplate.email(localLoginConfig.getEmailTemplate()),
				userTemplate.role(),
				firstGemeinde
			);

		gemeinden.forEach(
			gemeinde -> benutzer.getCurrentBerechtigung()
				.getGemeindeList()
				.add(gemeinde)
		);

		return benutzer;
	}

	@PermitAll
	public List<LocalLoginBenutzer> getLocalLoginUsers(
		MandantIdentifier mandantIdentifier
	) {
		var mandant = mandantService.findMandantByIdentifier(mandantIdentifier)
			.orElseThrow();
		return BenutzerQueries_.findLocalLoginUsers(
			persistence.getEntityManager(),
			mandant
		).stream().map(LocalLoginBenutzer::from).toList();
	}
}
