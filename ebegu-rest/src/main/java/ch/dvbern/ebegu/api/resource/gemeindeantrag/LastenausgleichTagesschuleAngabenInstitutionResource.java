/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.resource.gemeindeantrag;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.gemeindeantrag.JaxLastenausgleichTagesschuleAngabenInstitutionConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxLastenausgleichTagesschuleAngabenInstitutionContainer;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.gemeindeantrag.lastenausgleichtagesschulen.LastenausgleichTagesschuleAngabenInstitutionContainer;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitutionService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.hibernate.StaleObjectStateException;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * REST Resource fuer den Lastenausgleich der Tagesschulen, Angaben der Institution
 */
@Path("lats/institution")
@Stateless
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class LastenausgleichTagesschuleAngabenInstitutionResource {

	@Inject
	private LastenausgleichTagesschuleAngabenInstitutionService angabenInstitutionService;

	@Inject
	private JaxLastenausgleichTagesschuleAngabenInstitutionConverter converter;

	@Inject
	private PrincipalBean principal;

	@Inject
	private Authorizer authorizer;

	@Inject
	private InstitutionService institutionService;

	@Operation(
		summary = "Gibt den LastenausgleichTagesschuleAngabenInstitutionContainer mit der uebergebenen Id zurueck")
	@Nullable
	@GET
	@Path("/find/{latsInstitutionAngabenJaxId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS,
		SACHBEARBEITER_TS,
		ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT,
		ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION })
	public JaxLastenausgleichTagesschuleAngabenInstitutionContainer findLastenausgleichTagesschuleAngabenInstitutionContainer(
		@Nonnull
		@NotNull
		@PathParam("latsInstitutionAngabenJaxId") JaxId latsInstitutionAngabenJaxId
	) {
		Objects.requireNonNull(latsInstitutionAngabenJaxId);
		Objects.requireNonNull(latsInstitutionAngabenJaxId.getId());

		final Optional<LastenausgleichTagesschuleAngabenInstitutionContainer> latsInstitutionContainerOptional =
			angabenInstitutionService
				.findLastenausgleichTagesschuleAngabenInstitutionContainer(
					converter.toEntityId(
						latsInstitutionAngabenJaxId
					)
				);

		return latsInstitutionContainerOptional
			.map(
				container -> converter
					.lastenausgleichTagesschuleAngabenInstitutionContainerToJax(
						container
					)
			)
			.orElse(null);
	}

	@Operation(
		summary = "Speichert einen LastenausgleichTagesschuleAngabenInstitutionContainer in der Datenbank")
	@Nonnull
	@PUT
	@Path("/save")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS,
		SACHBEARBEITER_TS,
		ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT,
		ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION })
	public JaxLastenausgleichTagesschuleAngabenInstitutionContainer saveLastenausgleichTagesschuleInstitution(
		@Nonnull
		@NotNull
		@Valid JaxLastenausgleichTagesschuleAngabenInstitutionContainer latsInstitutionContainerJax,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		final LastenausgleichTagesschuleAngabenInstitutionContainer converted =
			getConvertedLastenausgleichTagesschuleAngabenInstitutionContainer(
				latsInstitutionContainerJax
			);

		if (converted.getAngabenGemeinde().getStatus()
			== LastenausgleichTagesschuleAngabenGemeindeStatus.NEU) {
			throw new EbeguRuntimeException(
				"saveLastenausgleichTagesschuleInstitution",
				ErrorCodeEnum.ERROR_GEMEINDE_ANTRAG_NEU
			);
		}

		final LastenausgleichTagesschuleAngabenInstitutionContainer saved =
			angabenInstitutionService
				.saveLastenausgleichTagesschuleInstitution(converted);

		return converter
			.lastenausgleichTagesschuleAngabenInstitutionContainerToJax(
				saved
			);
	}

	@Operation(
		summary = "Gibt den LastenausgleichTagesschuleAngabenInstitutionContainer frei fuer die Prüfung durch die Gemeinde")
	@Nonnull
	@PUT
	@Path("/freigeben")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS,
		SACHBEARBEITER_TS,
		ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT,
		ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION })
	public JaxLastenausgleichTagesschuleAngabenInstitutionContainer lastenausgleichTagesschuleInstitutionFreigeben(
		@Nonnull
		@NotNull
		@Valid JaxLastenausgleichTagesschuleAngabenInstitutionContainer latsInstitutionContainerJax,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		final LastenausgleichTagesschuleAngabenInstitutionContainer converted =
			getConvertedLastenausgleichTagesschuleAngabenInstitutionContainer(
				latsInstitutionContainerJax
			);

		final LastenausgleichTagesschuleAngabenInstitutionContainer saved =
			angabenInstitutionService
				.lastenausgleichTagesschuleInstitutionFreigeben(
					converted
				);

		return converter
			.lastenausgleichTagesschuleAngabenInstitutionContainerToJax(
				saved
			);
	}

	@Operation(
		summary = "Setzt den LastenausgleichTagesschuleAngabenInstitutionContainer frei für die Lesbarkeit durch die Kantone")
	@Nonnull
	@PUT
	@Path("/geprueft")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS,
		SACHBEARBEITER_TS })
	public JaxLastenausgleichTagesschuleAngabenInstitutionContainer lastenausgleichTagesschuleInstitutionGeprueft(
		@Nonnull
		@NotNull
		@Valid JaxLastenausgleichTagesschuleAngabenInstitutionContainer latsInstitutionContainerJax
	) {
		final LastenausgleichTagesschuleAngabenInstitutionContainer converted =
			getConvertedLastenausgleichTagesschuleAngabenInstitutionContainer(
				latsInstitutionContainerJax
			);

		final LastenausgleichTagesschuleAngabenInstitutionContainer saved =
			angabenInstitutionService
				.lastenausgleichTagesschuleInstitutionGeprueft(
					converted
				);

		return converter
			.lastenausgleichTagesschuleAngabenInstitutionContainerToJax(
				saved
			);
	}

	@Operation(
		summary = "Setzt den LastenausgleichTagesschuleAngabenInstitutionContainer zurück in den Status Prüfung durch Gemeinde")
	@Nonnull
	@PUT
	@Path("/gemeinde-falsche-angaben")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_BG,
		SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS })
	public JaxLastenausgleichTagesschuleAngabenInstitutionContainer falscheAngabenGemeinde(
		@Nonnull
		@NotNull
		@Valid JaxLastenausgleichTagesschuleAngabenInstitutionContainer latsInstitutionContainerJax
	) {
		final LastenausgleichTagesschuleAngabenInstitutionContainer converted =
			getConvertedLastenausgleichTagesschuleAngabenInstitutionContainer(
				latsInstitutionContainerJax
			);

		final LastenausgleichTagesschuleAngabenInstitutionContainer saved =
			angabenInstitutionService
				.latsAngabenInstitutionContainerWiederOeffnenGemeinde(
					converted
				);

		return converter
			.lastenausgleichTagesschuleAngabenInstitutionContainerToJax(
				saved
			);
	}

	@Operation(
		summary = "Setzt den LastenausgleichTagesschuleAngabenInstitutionContainer zurück in den Status Prüfung durch Gemeinde")
	@Nonnull
	@PUT
	@Path("/ts-falsche-angaben")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_BG,
		SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS,
		SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_TRAEGERSCHAFT,
		ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION })
	public JaxLastenausgleichTagesschuleAngabenInstitutionContainer falscheAngaben(
		@Nonnull
		@NotNull
		@Valid JaxLastenausgleichTagesschuleAngabenInstitutionContainer latsInstitutionContainerJax
	) {
		final LastenausgleichTagesschuleAngabenInstitutionContainer converted =
			getConvertedLastenausgleichTagesschuleAngabenInstitutionContainer(
				latsInstitutionContainerJax
			);

		final LastenausgleichTagesschuleAngabenInstitutionContainer saved =
			angabenInstitutionService
				.latsAngabenInstitutionContainerWiederOeffnenTS(
					converted
				);

		return converter
			.lastenausgleichTagesschuleAngabenInstitutionContainerToJax(
				saved
			);
	}

	@Nonnull
	private LastenausgleichTagesschuleAngabenInstitutionContainer getConvertedLastenausgleichTagesschuleAngabenInstitutionContainer(
		@Nonnull JaxLastenausgleichTagesschuleAngabenInstitutionContainer latsInstitutionContainerJax
	) {
		Objects.requireNonNull(latsInstitutionContainerJax);
		Objects.requireNonNull(latsInstitutionContainerJax.getId());

		// Das Objekt muss in der DB schon vorhanden sein, da die Erstellung immer ueber den GemeindeAntragService
		// geschieht
		final LastenausgleichTagesschuleAngabenInstitutionContainer latsInstitutionContainer =
			angabenInstitutionService
				.findLastenausgleichTagesschuleAngabenInstitutionContainer(
					latsInstitutionContainerJax.getId()
				)
				.orElseThrow(
					() -> new EbeguEntityNotFoundException(
						"getConvertedLastenausgleichTagesschuleAngabenInstitutionContainer",
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						latsInstitutionContainerJax.getId()
					)
				);

		try {
			final LastenausgleichTagesschuleAngabenInstitutionContainer converted =
				converter
					.lastenausgleichTagesschuleAngabenInstitutionContainerToEntity(
						latsInstitutionContainerJax,
						latsInstitutionContainer,
						true
					);
			return converted;
		} catch (StaleObjectStateException e) {
			throw new WebApplicationException(e, Status.CONFLICT);
		}
	}

	@Operation(
		summary = "Berechnet die Anzahl eingeschriebener Kinder pro Stufe (overall, vorschulalter, kindergarten, primarschule")
	@Nonnull
	@GET
	@Path("/anzahl-eingeschriebene-kinder/{containerJaxId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS,
		SACHBEARBEITER_TS, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION,
		ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT })
	public Map<String, Integer> calculateAnzahlEingeschriebeneKinder(
		@Nonnull
		@NotNull
		@PathParam("containerJaxId") JaxId latsInstitutionAngabenJaxId
	) {
		Objects.requireNonNull(latsInstitutionAngabenJaxId);
		Objects.requireNonNull(latsInstitutionAngabenJaxId.getId());

		LastenausgleichTagesschuleAngabenInstitutionContainer container =
			angabenInstitutionService
				.findLastenausgleichTagesschuleAngabenInstitutionContainer(
					converter.toEntityId(
						latsInstitutionAngabenJaxId
					)
				)
				.orElseThrow(
					() -> new EbeguEntityNotFoundException(
						"calculateAnzahlEingeschriebeneKinder",
						latsInstitutionAngabenJaxId.getId()
					)
				);

		return angabenInstitutionService.calculateAnzahlEingeschriebeneKinder(
			container
		);

	}

	@Operation(
		summary = "Berechnet den Durchschnitt der Kinder pro Modulgruppe (Frühbetreuung, Mittagsbetreuung, Nachmittagsbetreuung 1, Nachmittagsbetreuung 2")
	@Nonnull
	@GET
	@Path("/durchschnitt-kinder-pro-tag/{containerJaxId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS,
		SACHBEARBEITER_TS, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION,
		ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT })
	public Map<String, BigDecimal> calculateDurchschnitKinderProTag(
		@Nonnull
		@NotNull
		@PathParam("containerJaxId") JaxId latsInstitutionAngabenJaxId
	) {
		Objects.requireNonNull(latsInstitutionAngabenJaxId);
		Objects.requireNonNull(latsInstitutionAngabenJaxId.getId());

		LastenausgleichTagesschuleAngabenInstitutionContainer container =
			angabenInstitutionService
				.findLastenausgleichTagesschuleAngabenInstitutionContainer(
					converter.toEntityId(
						latsInstitutionAngabenJaxId
					)
				)
				.orElseThrow(
					() -> new EbeguEntityNotFoundException(
						"calculateDurchschnitKinderProTag",
						latsInstitutionAngabenJaxId.getId()
					)
				);

		return angabenInstitutionService.calculateDurchschnittKinderProTag(
			container
		);

	}

	@Operation(
		summary = "Gibt alle Tagesschuleanträge des Gemeinde-Antrags zurück, die für die Benutzerin sichtbar sind")
	@GET
	@Path("{gemeindeAntragId}/tagesschulenantraege")
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION,
		ADMIN_GEMEINDE, SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_TRAEGERSCHAFT,
		ADMIN_TS, SACHBEARBEITER_TS, SACHBEARBEITER_GEMEINDE })
	public List<JaxLastenausgleichTagesschuleAngabenInstitutionContainer> getTagesschuleAntraegeFuerGemeinedAntrag(
		@Nonnull @Valid @PathParam("gemeindeAntragId") String gemeindeAntragId
	) {
		authorizer.checkReadAuthorizationLATSGemeindeAntrag(gemeindeAntragId);

		return angabenInstitutionService
			.findLastenausgleichTagesschuleAngabenInstitutionByGemeindeAntragId(
				gemeindeAntragId
			)
			.stream()
			// does user belong to institution or is mandant
			.filter(
				lastenausgleichTagesschuleAngabenInstitutionContainer -> principal
					.isCallerInAnyOfRole(UserRole.getMandantSuperadminRoles())
					|| institutionService
						.getInstitutionenReadableForCurrentBenutzer(
							false
						)
						.stream()
						.anyMatch(
							institution -> institution.getId()
								.equals(
									lastenausgleichTagesschuleAngabenInstitutionContainer
										.getInstitution()
										.getId()
								)
						)
			)
			.filter(lastenausgleichTagesschuleAngabenInstitutionContainer -> {
				switch (lastenausgleichTagesschuleAngabenInstitutionContainer
					.getStatus()) {
				case OFFEN:
				case IN_PRUEFUNG_GEMEINDE:
					return principal.isCallerInAnyOfRole(
						UserRole.SUPER_ADMIN,
						UserRole.ADMIN_MANDANT,
						UserRole.SACHBEARBEITER_MANDANT,
						UserRole.ADMIN_GEMEINDE,
						UserRole.SACHBEARBEITER_GEMEINDE,
						UserRole.ADMIN_TS,
						UserRole.SACHBEARBEITER_TS,
						UserRole.ADMIN_INSTITUTION,
						UserRole.SACHBEARBEITER_INSTITUTION,
						UserRole.SACHBEARBEITER_TRAEGERSCHAFT,
						UserRole.ADMIN_TRAEGERSCHAFT
					);
				case GEPRUEFT:
				default:
					return true;
				}
			})
			.map(
				l -> converter
					.lastenausgleichTagesschuleAngabenInstitutionContainerToJax(
						l
					)
			)
			.collect(Collectors.toList());

	}
}
