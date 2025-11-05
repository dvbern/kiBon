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

package ch.dvbern.ebegu.api.resource;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import ch.dvbern.ebegu.api.converter.gesuch.JaxKindConverter;
import ch.dvbern.ebegu.api.converter.gesuch.JaxVerfuegungConverter;
import ch.dvbern.ebegu.api.converter.gesuch.betreuung.JaxBetreuungAnmeldungPlatzConverter;
import ch.dvbern.ebegu.api.dtos.JaxBetreuung;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxKindContainer;
import ch.dvbern.ebegu.api.dtos.JaxVerfuegung;
import ch.dvbern.ebegu.api.dtos.JaxVerfuegungZeitabschnitt;
import ch.dvbern.ebegu.api.resource.util.ResourceHelper;
import ch.dvbern.ebegu.api.util.RestUtil;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.AbstractAnmeldung;
import ch.dvbern.ebegu.entities.AnmeldungFerieninsel;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.VerfuegungService;
import org.eclipse.microprofile.openapi.annotations.Operation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * REST Resource fuer Verfügungen
 */
@Path("verfuegung")
@Stateless
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class VerfuegungResource {

	@Inject
	private VerfuegungService verfuegungService;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private InstitutionService institutionService;

	@Inject
	private ResourceHelper resourceHelper;

	@Inject
	private JaxVerfuegungConverter converter;
	@Inject
	private JaxKindConverter kindConverter;
	@Inject
	private JaxBetreuungAnmeldungPlatzConverter betreuungAnmeldungPlatzConverter;

	@Inject
	private PrincipalBean principalBean;

	@Operation(
		summary = "Calculates the Verfuegung of the Gesuch with the given id, does nothing if the Gesuch "
			+
			"does not exists. Note: Nothing is stored in the Database")
	@Nullable
	@GET
	@Path("/calculate/{gesuchId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public Response calculateVerfuegung(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchstellerId
	) {
		Optional<Gesuch> gesuchOptional = gesuchService.findGesuch(
			gesuchstellerId.getId()
		);

		if (!gesuchOptional.isPresent()) {
			return null;
		}
		Gesuch gesuch = gesuchOptional.get();
		Gesuch gesuchWithCalcVerfuegung = verfuegungService.calculateVerfuegung(
			gesuch
		);

		// wir muessen nur die kind container mappen nicht das ganze gesuch
		Set<JaxKindContainer> kindContainers = gesuchWithCalcVerfuegung
			.getKindContainers()
			.stream()
			.map(
				kindContainer -> kindConverter.kindContainerToJAX(
					kindContainer
				)
			)
			.collect(Collectors.toSet());
		// Es wird gecheckt ob der Benutzer zu einer Institution/Traegerschaft gehoert. Wenn ja, werden die Kinder
		// gefiltert, damit nur die relevanten Kinder geschickt werden
		if (principalBean.isCallerInAnyOfRole(
			ADMIN_TRAEGERSCHAFT,
			SACHBEARBEITER_TRAEGERSCHAFT,
			ADMIN_INSTITUTION,
			SACHBEARBEITER_INSTITUTION
		)) {
			Collection<Institution> instForCurrBenutzer =
				institutionService
					.getInstitutionenReadableForCurrentBenutzer(false);
			RestUtil.purgeKinderAndBetreuungenOfInstitutionen(
				kindContainers,
				instForCurrBenutzer
			);
		}
		return Response.ok(kindContainers).build();
	}

	@Operation(
		summary = "Generiert eine Verfuegung und speichert diese in der Datenbank")
	@Nullable
	@PUT
	@Path("/verfuegen/{gesuchId}/{betreuungId}/{ignorieren}/{ignorierenMahlzeiten}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG })
	public JaxVerfuegung saveVerfuegung(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchJaxId,
		@Nonnull @NotNull @PathParam("betreuungId") JaxId betreuungJaxId,
		@Nonnull @NotNull @PathParam("ignorieren") Boolean ignorieren,
		@Nonnull
		@NotNull
		@PathParam("ignorierenMahlzeiten") Boolean ignorierenMahlzeiten,
		@Nullable String verfuegungManuelleBemerkungen
	) {
		String gesuchId = converter.toEntityId(gesuchJaxId);
		String betreuungId = converter.toEntityId(betreuungJaxId);

		Verfuegung persistedVerfuegung = this.verfuegungService.verfuegen(
			gesuchId,
			betreuungId,
			verfuegungManuelleBemerkungen,
			ignorieren,
			ignorierenMahlzeiten,
			true
		);
		return converter.verfuegungToJax(persistedVerfuegung);
	}

	@Operation(summary = "Schliesst eine Betreuung ab, ohne sie zu verfuegen")
	@Nullable
	@POST
	@Path("/schliessenOhneVerfuegen/{betreuungId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG })
	public Response verfuegungSchliessenOhneVerfuegen(
		@Nonnull @NotNull @PathParam("betreuungId") JaxId betreuungId
	) {

		Betreuung betreuung = betreuungService.findBetreuung(
			betreuungId.getId()
		)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"verfuegungSchliessenOhneVerfuegen",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					"BetreuungID invalid: " + betreuungId.getId()
				)
			);

		betreuungService.schliessenOhneVerfuegen(betreuung);

		return Response.ok().build();
	}

	@Operation(summary = "Erstellt eine Nichteintretens-Verfuegung")
	@Nullable
	@GET
	@Path("/nichtEintreten/{gesuchId}/{betreuungId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS })
	public JaxVerfuegung schliessenNichtEintreten(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchJaxId,
		@Nonnull @NotNull @PathParam("betreuungId") JaxId betreuungJaxId
	) {
		String gesuchId = converter.toEntityId(gesuchJaxId);
		String betreuungId = converter.toEntityId(betreuungJaxId);

		Verfuegung persistedVerfuegung = this.verfuegungService.nichtEintreten(
			gesuchId,
			betreuungId
		);
		return converter.verfuegungToJax(persistedVerfuegung);
	}

	@Operation(
		summary = "Schulamt-Anmeldung wird durch die Institution bestätigt und die Finanzielle Situation ist "
			+ "geprueft")
	@Nonnull
	@PUT
	@Path("/anmeldung/uebernehmen")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_TRAEGERSCHAFT,
		SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		SACHBEARBEITER_INSTITUTION, SACHBEARBEITER_TS, ADMIN_TS,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE })
	public JaxBetreuung anmeldungUebernehmen(
		@Nonnull @NotNull @Valid JaxBetreuung betreuungJAXP
	) {
		Objects.requireNonNull(betreuungJAXP.getId());
		Objects.requireNonNull(betreuungJAXP.getKindId());

		// Sicherstellen, dass der Status des Server-Objektes genau dem erwarteten Status entspricht
		resourceHelper.assertBetreuungStatusEqual(
			betreuungJAXP.getId(),
			Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST,
			Betreuungsstatus.SCHULAMT_MODULE_AKZEPTIERT
		);

		AbstractAnmeldung convertedBetreuung = betreuungAnmeldungPlatzConverter
			.platzToStoreableEntity(
				betreuungJAXP
			);
		// Sicherstellen, dass das dazugehoerige Gesuch ueberhaupt noch editiert werden darf fuer meine Rolle
		resourceHelper.assertGesuchStatusForBenutzerRole(
			convertedBetreuung.getKind().getGesuch(),
			convertedBetreuung
		);

		if (convertedBetreuung.getBetreuungsangebotTyp().isTagesschule()) {
			AnmeldungTagesschule convertedAnmeldungTagesschule =
				(AnmeldungTagesschule) convertedBetreuung;
			AnmeldungTagesschule persistedBetreuung =
				this.verfuegungService.anmeldungTagesschuleUebernehmen(
					convertedAnmeldungTagesschule
				);
			return betreuungAnmeldungPlatzConverter.platzToJAX(
				persistedBetreuung
			);
		} else {
			AnmeldungFerieninsel convertedAnmeldungFerieninsel =
				(AnmeldungFerieninsel) convertedBetreuung;
			AnmeldungFerieninsel persistedBetreuung =
				this.verfuegungService.anmeldungFerieninselUebernehmen(
					convertedAnmeldungFerieninsel
				);
			return betreuungAnmeldungPlatzConverter.platzToJAX(
				persistedBetreuung
			);
		}
	}

	@Operation(
		summary = "Gibt die Zeitabschnitte der Vorgänger-Verfügung der Betreuung zurück")
	@Nullable
	@GET
	@Path("/betreuung/{betreuungId}/vorgaenger-zeitabschnitte")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_TRAEGERSCHAFT,
		SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		SACHBEARBEITER_INSTITUTION, SACHBEARBEITER_TS, ADMIN_TS,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE })
	public List<JaxVerfuegungZeitabschnitt> getBetreuungVorgaengerZeitabschnitte(
		@Nonnull @NotNull @PathParam("betreuungId") String betreuungId
	) {
		Optional<Betreuung> betreuungOpt = betreuungService.findBetreuung(
			betreuungId
		);
		if (betreuungOpt.isEmpty()) {
			return Collections.emptyList();
		}
		Optional<Verfuegung> vorgaengerVerfuegung = verfuegungService
			.findVorgaengerVerfuegung(
				betreuungOpt.get()
			);
		return vorgaengerVerfuegung.map(Verfuegung::getZeitabschnitte)
			.map(
				zeitabschnitte -> converter.verfuegungZeitabschnitteListToJax(
					zeitabschnitte
				)
			)
			.orElse(Collections.emptyList());
	}
}
