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

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxGesuchsperiodeConverter;
import ch.dvbern.ebegu.api.dtos.JaxAbstractDateRangedDTO;
import ch.dvbern.ebegu.api.dtos.JaxGesuchsperiode;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.util.RestUtil;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.services.MandantService;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.mandant.MandantCookieUtil;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.ejb3.annotation.TransactionTimeout;

import static ch.dvbern.ebegu.api.resource.util.ResourceConstants.DOCX_FILE_EXTENSION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;
import static java.util.Objects.requireNonNull;

/**
 * REST Resource fuer Gesuchsperiode
 */
@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
@Path("gesuchsperioden")
@Stateless
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class GesuchsperiodeResource {

	public static final String APPLICATION_OCTET_STREAM =
		"application/octet-stream";

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private MandantService mandantService;

	@SuppressWarnings("CdiInjectionPointsInspection")
	@Inject
	private JaxGesuchsperiodeConverter converter;

	@Inject
	private PrincipalBean principalBean;

	@Operation(summary = "Erstellt eine neue Gesuchsperiode in der Datenbank")
	@Nonnull
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE })
	@TransactionTimeout(value = Constants.MAX_TIMEOUT_MINUTES,
		unit = TimeUnit.MINUTES)
	public JaxGesuchsperiode saveGesuchsperiode(
		@Nonnull @NotNull @Valid JaxGesuchsperiode gesuchsperiodeJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {

		Gesuchsperiode gesuchsperiode = new Gesuchsperiode();
		if (gesuchsperiodeJAXP.getId() != null) {
			Optional<Gesuchsperiode> optional = gesuchsperiodeService
				.findGesuchsperiode(gesuchsperiodeJAXP.getId());
			gesuchsperiode = optional.orElseGet(Gesuchsperiode::new);
		}
		// Überprüfen, ob der Statusübergang zulässig ist
		GesuchsperiodeStatus gesuchsperiodeStatusBisher = gesuchsperiode
			.getStatus();

		Gesuchsperiode convertedGesuchsperiode = converter
			.gesuchsperiodeToEntity(gesuchsperiodeJAXP, gesuchsperiode);
		Gesuchsperiode persistedGesuchsperiode =
			this.gesuchsperiodeService.saveGesuchsperiode(
				convertedGesuchsperiode,
				gesuchsperiodeStatusBisher
			);

		return converter.gesuchsperiodeToJAX(persistedGesuchsperiode);
	}

	@Operation(
		summary = "Sucht die Gesuchsperiode mit der uebergebenen Id in der Datenbank")
	@Nullable
	@GET
	@Path("/gesuchsperiode/{gesuchsperiodeId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Oeffentliche Daten
	public JaxGesuchsperiode findGesuchsperiode(
		@Nonnull
		@NotNull
		@PathParam("gesuchsperiodeId") JaxId gesuchsperiodeJAXPId
	) {

		requireNonNull(gesuchsperiodeJAXPId.getId());
		String gesuchsperiodeID = converter.toEntityId(gesuchsperiodeJAXPId);
		Optional<Gesuchsperiode> optional = gesuchsperiodeService
			.findGesuchsperiode(gesuchsperiodeID);

		return optional.map(
			gesuchsperiode -> converter.gesuchsperiodeToJAX(gesuchsperiode)
		).orElse(null);
	}

	@Operation(
		summary = "Gibt die neuste Gesuchsperiode zurueck anhand des Datums gueltigBis")
	@Nullable
	@GET
	@Path("/newestGesuchsperiode/")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Oeffentliche Daten für eingeloggte User
	public JaxGesuchsperiode findNewestGesuchsperiode() {
		Optional<Gesuchsperiode> optional = gesuchsperiodeService
			.findNewestGesuchsperiode(
				requireNonNull(principalBean.getMandant())
			);
		return optional.map(
			gesuchsperiode -> converter.gesuchsperiodeToJAX(gesuchsperiode)
		).orElse(null);
	}

	@Operation(
		summary = "Loescht die Gesuchsperiode mit der uebergebenen Id in der Datenbank")
	@Nullable
	@DELETE
	@Path("/{gesuchsperiodeId}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed(SUPER_ADMIN)
	public Response removeGesuchsperiode(
		@Nonnull
		@NotNull
		@PathParam("gesuchsperiodeId") JaxId gesuchsperiodeJAXPId,
		@Context HttpServletResponse response
	) {

		requireNonNull(gesuchsperiodeJAXPId.getId());
		gesuchsperiodeService.removeGesuchsperiode(
			converter.toEntityId(gesuchsperiodeJAXPId)
		);
		return Response.ok().build();
	}

	@Operation(
		summary = "Gibt alle in der Datenbank vorhandenen Gesuchsperioden zurueck.")
	@Nonnull
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Oeffentliche Daten
	public List<JaxGesuchsperiode> getAllGesuchsperioden(
		@Context HttpServletRequest request
	) {
		MandantIdentifier mandantIdentifier =
			MandantCookieUtil.getMandantFromCookie(request);
		var mandant = mandantService.findMandantByIdentifier(mandantIdentifier)
			.orElseThrow();

		return gesuchsperiodeService.getAllGesuchsperioden(mandant)
			.stream()
			.map(
				gesuchsperiode -> converter.gesuchsperiodeToJAX(
					gesuchsperiode
				)
			)
			.filter(periode -> periode.getGueltigAb() != null)
			.sorted(
				Comparator.comparing(
					JaxAbstractDateRangedDTO::getGueltigAb
				).reversed()
			)
			.collect(Collectors.toList());
	}

	@Operation(
		summary = "Gibt alle in der Datenbank vorhandenen Gesuchsperioden zurueck, welche im Status AKTIV "
			+ "sind")
	@Nonnull
	@GET
	@Path("/active")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Oeffentliche Daten
	public List<JaxGesuchsperiode> getAllActiveGesuchsperioden(
		@CookieParam(MandantCookieUtil.MANDANT_COOKIE_NAME) Cookie mandantCookie
	) {
		return gesuchsperiodeService.getAllActiveGesuchsperioden()
			.stream()
			.map(
				gesuchsperiode -> converter.gesuchsperiodeToJAX(gesuchsperiode)
			)
			.collect(Collectors.toList());
	}

	@Operation(
		summary = "Gibt alle in der Datenbank vorhandenen Gesuchsperioden zurueck, welche im Status AKTIV "
			+
			"oder INAKTIV sind")
	@Nonnull
	@GET
	@Path("/unclosed")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Oeffentliche Daten
	public List<JaxGesuchsperiode> getAllAktivUndInaktivGesuchsperioden() {
		return gesuchsperiodeService.getAllAktivUndInaktivGesuchsperioden()
			.stream()
			.map(
				gesuchsperiode -> converter.gesuchsperiodeToJAX(
					gesuchsperiode
				)
			)
			.filter(periode -> periode.getGueltigAb() != null)
			.sorted(
				Comparator.comparing(
					JaxAbstractDateRangedDTO::getGueltigAb
				).reversed()
			)
			.collect(Collectors.toList());
	}

	@Operation(
		summary = "Gibt alle Gesuchsperioden zurueck, die im Status AKTIV oder INAKTIV sind und für die der "
			+
			"angegebene Fall noch kein Gesuch freigegeben hat.")
	@SuppressWarnings("InstanceMethodNamingConvention")
	@Nonnull
	@GET
	@Path("/unclosed/{dossierId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Oeffentliche Daten
	public List<JaxGesuchsperiode> getAllAktivInaktivNichtVerwendeteGesuchsperioden(
		@Nonnull @PathParam("dossierId") String dossierId
	) {
		return gesuchsperiodeService
			.getAllAktivInaktivNichtVerwendeteGesuchsperioden(dossierId)
			.stream()
			.map(
				gesuchsperiode -> converter.gesuchsperiodeToJAX(
					gesuchsperiode
				)
			)
			.filter(periode -> periode.getGueltigAb() != null)
			.sorted(
				Comparator.comparing(
					JaxAbstractDateRangedDTO::getGueltigAb
				).reversed()
			)
			.collect(Collectors.toList());
	}

	@Operation(
		summary = "Gibt alle Gesuchsperioden zurück, welche AKTIV oder INAKTIV sind und nach dem "
			+
			"BetreuungsgutscheineStartdatum und vor Ende der Gültigkeit der Gemeinde liegen.")
	@Nonnull
	@GET
	@Path("/gemeinde/{gemeindeId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Oeffentliche Daten
	public List<JaxGesuchsperiode> getAllPeriodenForGemeinde(
		@Nonnull @PathParam("gemeindeId") String gemeindeId,
		@Nullable @QueryParam("dossierId") String dossierId,
		@CookieParam(MandantCookieUtil.MANDANT_COOKIE_NAME) Cookie mandantCookie
	) {

		Collection<Gesuchsperiode> perioden = dossierId == null ?
			gesuchsperiodeService.getAllAktivUndInaktivGesuchsperioden() :
			gesuchsperiodeService
				.getAllAktivInaktivNichtVerwendeteGesuchsperioden(
					dossierId
				);

		return extractValidGesuchsperiodenForGemeinde(gemeindeId, perioden);
	}

	@Operation(
		summary = "Gibt alle Gesuchsperioden zurück, welche AKTIV sind und nach dem "
			+
			"BetreuungsgutscheineStartdatum und vor Ende der Gültigkeit der Gemeinde liegen.")
	@Nonnull
	@GET
	@Path("/aktive/gemeinde/{gemeindeId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Oeffentliche Daten
	public List<JaxGesuchsperiode> getAllAktivePeriodenForGemeinde(
		@Nonnull @PathParam("gemeindeId") String gemeindeId,
		@Nullable @QueryParam("dossierId") String dossierId,
		@CookieParam(MandantCookieUtil.MANDANT_COOKIE_NAME) Cookie mandantCookie
	) {

		Collection<Gesuchsperiode> perioden = dossierId == null ?
			gesuchsperiodeService.getAllActiveGesuchsperioden() :
			gesuchsperiodeService
				.getAllAktiveNichtVerwendeteGesuchsperioden(dossierId);

		return extractValidGesuchsperiodenForGemeinde(gemeindeId, perioden);
	}

	@Nullable
	@DELETE
	@Path("/gesuchsperiodeDokument/{gesuchsperiodeId}/{sprache}/{dokumentTyp}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed(SUPER_ADMIN)
	public Response removeGesuchsperiodeDokument(
		@Nonnull @PathParam("gesuchsperiodeId") String gesuchsperiodeId,
		@Nonnull @PathParam("sprache") Sprache sprache,
		@Nonnull @PathParam("dokumentTyp") DokumentTyp dokumentTyp,
		@Context HttpServletResponse response
	) {

		requireNonNull(gesuchsperiodeId);
		gesuchsperiodeService.removeGesuchsperiodeDokument(
			gesuchsperiodeId,
			sprache,
			dokumentTyp
		);
		return Response.ok().build();

	}

	@Operation(
		summary = "retuns true id the VerfuegungErlaeuterung exists for the given language")
	@GET
	@Path("/existDokument/{gesuchsperiodeId}/{sprache}/{dokumentTyp}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE,
		SACHBEARBEITER_BG, SACHBEARBEITER_TS,
		SACHBEARBEITER_GEMEINDE, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		GESUCHSTELLER, SACHBEARBEITER_SOZIALDIENST, ADMIN_SOZIALDIENST

	})
	public boolean existDokument(
		@Nonnull @PathParam("gesuchsperiodeId") String gesuchsperiodeId,
		@Nonnull @PathParam("sprache") Sprache sprache,
		@Nonnull @PathParam("dokumentTyp") DokumentTyp dokumentTyp,
		@Context HttpServletResponse response
	) {
		requireNonNull(gesuchsperiodeId);
		requireNonNull(sprache);
		requireNonNull(dokumentTyp);
		return gesuchsperiodeService.existDokument(
			gesuchsperiodeId,
			sprache,
			dokumentTyp
		);
	}

	@Operation(
		summary = "return the VerfuegungErlaeuterung for the given language")
	@GET
	@Path("/downloadGesuchsperiodeDokument/{gesuchsperiodeId}/{sprache}/{dokumentTyp}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Oeffentliche Daten
	public Response downloadGesuchsperiodeDokument(
		@Nonnull @PathParam("gesuchsperiodeId") String gesuchsperiodeId,
		@Nonnull @PathParam("sprache") Sprache sprache,
		@Nonnull @PathParam("dokumentTyp") DokumentTyp dokumentTyp,
		@Context HttpServletResponse response
	) {
		requireNonNull(gesuchsperiodeId);
		requireNonNull(sprache);
		requireNonNull(dokumentTyp);

		final byte[] content = gesuchsperiodeService
			.downloadGesuchsperiodeDokument(
				gesuchsperiodeId,
				sprache,
				dokumentTyp
			);

		if (content != null && content.length > 0) {
			try {
				if (dokumentTyp == DokumentTyp.ERLAUTERUNG_ZUR_VERFUEGUNG) {
					//noinspection StringConcatenationMissingWhitespace
					return RestUtil.buildDownloadResponse(
						true,
						"erlaeuterung" + sprache + ".pdf",
						APPLICATION_OCTET_STREAM,
						content
					);
				} else if (dokumentTyp == DokumentTyp.VORLAGE_MERKBLATT_TS) {
					//noinspection StringConcatenationMissingWhitespace
					return RestUtil.buildDownloadResponse(
						true,
						"vorlageMerkblattTS"
							+ sprache
							+ DOCX_FILE_EXTENSION,
						APPLICATION_OCTET_STREAM,
						content
					);
				} else if (dokumentTyp == DokumentTyp.VORLAGE_VERFUEGUNG_LATS) {
					//noinspection StringConcatenationMissingWhitespace
					return RestUtil.buildDownloadResponse(
						true,
						"vorlageVerfuegungLats"
							+ sprache
							+ DOCX_FILE_EXTENSION,
						APPLICATION_OCTET_STREAM,
						content
					);
				} else if (dokumentTyp
					== DokumentTyp.VORLAGE_VERFUEGUNG_FERIENBETREUUNG) {
					//noinspection StringConcatenationMissingWhitespace
					return RestUtil.buildDownloadResponse(
						true,
						"vorlageVerfuegungFerienbetreuung"
							+ sprache
							+ DOCX_FILE_EXTENSION,
						APPLICATION_OCTET_STREAM,
						content
					);
				}
			} catch (IOException e) {
				return Response.status(Status.NOT_FOUND)
					.entity(
						"Gesuchsperiode Dokument: "
							+ dokumentTyp
							+ " kann nicht gelesen werden"
					)
					.build();
			}
		}

		return Response.status(Status.NO_CONTENT).build();
	}

	private List<JaxGesuchsperiode> extractValidGesuchsperiodenForGemeinde(
		@Nonnull String gemeindeId,
		@Nonnull Collection<Gesuchsperiode> perioden
	) {
		Gemeinde gemeinde = gemeindeService.findGemeinde(gemeindeId)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"extractValidGesuchsperiodenForGemeinde",
					String.format(
						"Keine Gemeinde für ID %s",
						gemeindeId
					)
				)
			);

		return perioden.stream()
			.filter(gemeinde::isGesuchsperiodeRelevantForGemeinde)
			.map(periode -> converter.gesuchsperiodeToJAX(periode))
			.filter(periode -> periode.getGueltigAb() != null)
			.sorted(
				Comparator.comparing(
					JaxAbstractDateRangedDTO::getGueltigAb
				).reversed()
			)
			.collect(Collectors.toList());
	}

}
