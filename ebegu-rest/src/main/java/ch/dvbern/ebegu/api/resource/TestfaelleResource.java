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

import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import ch.dvbern.ebegu.api.dtos.JaxGemeindeAntraegeFBTestdatenDTO;
import ch.dvbern.ebegu.api.dtos.JaxGemeindeAntraegeLATSTestdatenDTO;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.gemeindeantrag.ferienbetreuung.FerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.lastenausgleichtagesschulen.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.SchulungService;
import ch.dvbern.ebegu.services.TestfaelleService;
import ch.dvbern.ebegu.util.DateUtil;
import ch.dvbern.ebegu.validators.CheckEmail;
import org.eclipse.microprofile.openapi.annotations.Operation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * REST Resource zur Erstellung von (vordefinierten) Testfaellen.
 * Alle Testfaelle erstellen:
 * http://localhost:8080/ebegu/api/v1/testfaelle/testfall/all
 */
@Path("testfaelle")
@Stateless
@RolesAllowed(SUPER_ADMIN)
public class TestfaelleResource {

	private static final String FALL = "Fall ";

	@Inject
	private TestfaelleService testfaelleService;

	@Inject
	private SchulungService schulungService;

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	@Inject
	private PrincipalBean principal;

	@Inject
	private GemeindeService gemeindeService;

	@Operation(
		summary = "Erstellt einen Testfall aus mehreren vordefinierten Testfaellen. Folgende Einstellungen "
			+
			"sind moeglich: Gesuchsperiode, Gemeinde, Status der Betreuungen, Gesuch verfuegen")
	@GET
	@Path("/testfall/{fallid}/{gesuchsperiodeId}/{gemeindeId}/{betreuungenBestaetigt}/{verfuegen}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public Response getTestFall(
		@PathParam("fallid") String fallid,
		@PathParam("gesuchsperiodeId") String gesuchsperiodeId,
		@PathParam("gemeindeId") String gemeindeId,
		@PathParam("betreuungenBestaetigt") boolean betreuungenBestaetigt,
		@PathParam("verfuegen") boolean verfuegen
	) {

		assertTestfaelleAccessAllowed();
		StringBuilder responseString = testfaelleService
			.createAndSaveTestfaelle(
				Objects.requireNonNull(principal.getMandant()),
				fallid,
				betreuungenBestaetigt,
				verfuegen,
				gesuchsperiodeId,
				gemeindeId
			);
		return Response.ok(responseString.toString()).build();
	}

	@Operation(
		summary = "Erstellt einen Testfall aus mehreren vordefinierten Testfaellen fuer einen Gesuchsteller "
			+
			"(Online Gesuch). Folgende Einstellungen sind moeglich: Gesuchsperiode, Gemeinde, Status der Betreuungen, "
			+ "Gesuch "
			+
			"verfuegen, gewuenschter Gesuchsteller")
	@GET
	@Path("/testfallgs/{fallid}/{gesuchsperiodeId}/{gemeindeId}/{betreuungenBestaetigt}/{verfuegen}/{username}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public Response getTestFallGS(
		@PathParam("fallid") String fallid,
		@PathParam("gesuchsperiodeId") String gesuchsperiodeId,
		@PathParam("gemeindeId") String gemeindeId,
		@PathParam("betreuungenBestaetigt") boolean betreuungenBestaetigt,
		@PathParam("verfuegen") boolean verfuegen,
		@PathParam("username") String username
	) {

		assertTestfaelleAccessAllowed();
		StringBuilder responseString = testfaelleService
			.createAndSaveAsOnlineGesuch(
				fallid,
				betreuungenBestaetigt,
				verfuegen,
				username,
				gesuchsperiodeId,
				gemeindeId,
				Objects.requireNonNull(principal.getMandant())
			);
		return Response.ok(responseString.toString()).build();
	}

	@Operation(
		summary = "Loescht alle Antraege des uebergebenen Gesuchstellers.")
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	@DELETE
	@Path("/testfallgs/{username}")
	@Consumes(MediaType.WILDCARD)
	public Response removeFaelleOfGS(
		@PathParam("username") String username
	) {

		assertTestfaelleAccessAllowed();
		testfaelleService.removeGesucheOfGS(
			username,
			Objects.requireNonNull(principal.getMandant())
		);
		return Response.ok().build();
	}

	@Operation(summary = "Simuliert fuer den uebergebenen Testfall eine Heirat")
	@GET
	@Path("/mutationHeirat/{dossierId}/{gesuchsperiodeid}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public Response mutationHeirat(
		@PathParam("dossierId") String dossierId,
		@PathParam("gesuchsperiodeid") String gesuchsperiodeid,
		@Nullable @QueryParam("mutationsdatum") String stringMutationsdatum,
		@Nullable @QueryParam("aenderungper") String stringAenderungPer
	) {

		assertTestfaelleAccessAllowed();
		LocalDate mutationsdatum = DateUtil.parseStringToDateOrReturnNow(
			stringMutationsdatum
		);
		LocalDate aenderungPer = DateUtil.parseStringToDateOrReturnNow(
			stringAenderungPer
		);

		final Gesuch gesuch =
			testfaelleService.mutierenHeirat(
				dossierId,
				gesuchsperiodeid,
				mutationsdatum,
				aenderungPer,
				false
			);
		if (gesuch != null) {
			return Response.ok(
				FALL
					+ gesuch.getFall().getFallNummer()
					+ " mutiert zu heirat"
			).build();
		}
		return Response.ok(FALL + dossierId + " konnte nicht mutiert").build();
	}

	@Operation(
		summary = "Simuliert fuer den uebergebenen Testfall eine Scheidung")
	@GET
	@Path("/mutationScheidung/{dossierId}/{gesuchsperiodeid}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public Response mutierenScheidung(
		@PathParam("dossierId") String dossierId,
		@PathParam("gesuchsperiodeid") String gesuchsperiodeid,
		@Nullable @QueryParam("mutationsdatum") String stringMutationsdatum,
		@Nullable @QueryParam("aenderungper") String stringAenderungPer
	) {

		assertTestfaelleAccessAllowed();
		LocalDate mutationsdatum = DateUtil.parseStringToDateOrReturnNow(
			stringMutationsdatum
		);
		LocalDate aenderungPer = DateUtil.parseStringToDateOrReturnNow(
			stringAenderungPer
		);

		final Gesuch gesuch =
			testfaelleService.mutierenScheidung(
				dossierId,
				gesuchsperiodeid,
				mutationsdatum,
				aenderungPer,
				false
			);
		if (gesuch != null) {
			return Response.ok(
				FALL
					+ gesuch.getFall().getFallNummer()
					+ " mutiert zu scheidung"
			).build();
		}
		return Response.ok(FALL + dossierId + " konnte nicht mutiert").build();
	}

	@Operation(
		summary = "Setzt die Tutorialdaten zurueck. Gemeinde und Institution")
	@GET
	@Path("/schulung/tutorial/create")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE })
	public Response createTutorialdaten() {
		assertTestfaelleAccessAllowed();
		schulungService.createTutorialdaten(
			Objects.requireNonNull(principal.getMandant())
		);
		return Response.ok("Tutorialdaten erstellt").build();
	}

	@Operation(
		summary = "Sendet ein Beispiel aller Mails an die uebergebene Adresse")
	@GET
	@Path("/mailtest")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public Response testAllMails(
		@QueryParam("mailadresse") @CheckEmail String mailadresse,
		@QueryParam("gemeindeId") String gemeindeId
	) {

		assertTestfaelleAccessAllowed();

		Gemeinde gemeinde = gemeindeService.findGemeinde(gemeindeId)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"testAllMails",
					gemeindeId
				)
			);

		testfaelleService.testAllMails(
			mailadresse,
			Objects.requireNonNull(principal.getMandant()),
			gemeinde
		);
		return Response.ok().build();
	}

	@Operation(summary = "Erstellt LATS testdaten")
	@POST
	@Path("/gemeinde-antraege/LASTENAUSGLEICH_TAGESSCHULEN")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public Response createTestdatenLATS(
		@Nonnull
		@NotNull
		@Valid JaxGemeindeAntraegeLATSTestdatenDTO jaxGemeindeAntraegeTestdatenDTO
	) {
		assertTestfaelleAccessAllowed();
		final String gemeindeId = jaxGemeindeAntraegeTestdatenDTO.getGemeinde()
			!= null ?
				jaxGemeindeAntraegeTestdatenDTO.getGemeinde().getId() :
				null;
		final Collection<LastenausgleichTagesschuleAngabenGemeindeContainer> latsContainers =
			testfaelleService.createAndSaveLATSTestdaten(
				Objects.requireNonNull(
					jaxGemeindeAntraegeTestdatenDTO
						.getGesuchsperiode()
						.getId()
				),
				gemeindeId,
				jaxGemeindeAntraegeTestdatenDTO.getStatus()
			);
		return Response.ok(
			latsContainers.stream()
				.map(AbstractEntity::getId)
				.collect(Collectors.joining(","))
		).build();
	}

	@Operation(summary = "Erstellt FB testdaten")
	@POST
	@Path("/gemeinde-antraege/FERIENBETREUUNG")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public Response createTestdatenFerienbetreuung(
		@Nonnull
		@NotNull
		@Valid JaxGemeindeAntraegeFBTestdatenDTO jaxGemeindeAntraegeTestdatenDTO
	) {
		assertTestfaelleAccessAllowed();
		final FerienbetreuungAngabenContainer ferienbetreuungContainer =
			testfaelleService.createAndSaveFerienbetreuungTestdaten(
				Objects.requireNonNull(
					jaxGemeindeAntraegeTestdatenDTO
						.getGesuchsperiode()
						.getId()
				),
				Objects.requireNonNull(
					jaxGemeindeAntraegeTestdatenDTO.getGemeinde()
						.getId()
				),
				jaxGemeindeAntraegeTestdatenDTO.getStatus()
			);
		return Response.ok(ferienbetreuungContainer.getId()).build();
	}

	private void assertTestfaelleAccessAllowed() {
		// Testfaelle duerfen nur erstellt werden, wenn das Flag gesetzt ist und das Dummy Login eingeschaltet ist
		if (!ebeguConfiguration.isDummyLoginEnabled(principal.getMandant())) {
			throw new EbeguRuntimeException(
				"assertTestfaelleAccessAllowed",
				ErrorCodeEnum.ERROR_TESTFAELLE_DISABLED,
				"Testfaelle duerfen nur verwendet werden,"
					+ " wenn das DummyLogin fuer diese Umgebung eingeschaltet ist"
			);
		}
		if (!ebeguConfiguration.isTestfaelleEnabled()) {
			throw new EbeguRuntimeException(
				"assertTestfaelleAccessAllowed",
				ErrorCodeEnum.ERROR_TESTFAELLE_DISABLED,
				"Testfaelle duerfen nur verwendet "
					+ "werden, wenn diese ueber ein SystemProperty eingeschaltet sind"
			);
		}
	}
}
