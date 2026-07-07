/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.outbox.anmeldung.AnmeldungTagesschuleEventServiceBean;
import ch.dvbern.ebegu.outbox.gemeinde.GemeindeEventServiceBean;
import ch.dvbern.ebegu.outbox.gemeindekennzahlen.GemeindeKennzahlenEventServiceBean;
import ch.dvbern.ebegu.outbox.institution.InstitutionEventServiceBean;
import ch.dvbern.ebegu.outbox.platzbestaetigung.BetreuungAnfrageEventServiceBean;
import ch.dvbern.ebegu.outbox.verfuegung.VerfuegungEventServiceBean;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("exportbatch")
@Stateless
@RolesAllowed(UserRoleName.SUPER_ADMIN)
public class ExportBatchResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(
		ExportBatchResource.class
	);

	@Inject
	private GemeindeEventServiceBean gemeindeEventServiceBean;

	@Inject
	private AnmeldungTagesschuleEventServiceBean anmeldungTagesschuleEventServiceBean;

	@Inject
	private GemeindeKennzahlenEventServiceBean gemeindeKennzahlenEventServiceBean;

	@Inject
	private InstitutionEventServiceBean institutionEventServiceBean;

	@Inject
	private BetreuungAnfrageEventServiceBean betreuungAnfrageEventServiceBean;

	@Inject
	private VerfuegungEventServiceBean verfuegungEventServiceBean;

	@Inject
	private PrincipalBean principalBean;

	@Operation(summary = "Führt den Job publishExistingGemeinden aus.")
	@POST
	@Path("/publishExistingGemeinden")
	@Produces(MediaType.TEXT_PLAIN)
	public Response runBatchPublishExistingGemeinden() {
		gemeindeEventServiceBean
			.publishExistingGemeinden(principalBean.getMandant());
		return Response.ok().build();
	}

	@Operation(summary = "Führt den Job publishWartendeAnmeldungen aus.")
	@POST
	@Path("/publishWartendeAnmeldungen")
	@Produces(MediaType.TEXT_PLAIN)
	public Response runBatchPublishWartendeAnmeldungen() {
		anmeldungTagesschuleEventServiceBean
			.publishExistingAnmeldungTagesschule(principalBean.getMandant());
		return Response.ok().build();
	}

	@Operation(summary = "Führt den Job publishExistingGemeindeKennzahlen aus.")
	@POST
	@Path("/publishExistingGemeindeKennzahlen")
	@Produces(MediaType.TEXT_PLAIN)
	public Response runBatchPublishExistingGemeindeKennzahlen() {
		gemeindeKennzahlenEventServiceBean
			.publishExistingGemeindeKennzahlen(principalBean.getMandant());
		return Response.ok().build();
	}

	@Operation(summary = "Führt den Job publishExistingInstitutionen aus.")
	@POST
	@Path("/publishExistingInstitutionen")
	@Produces(MediaType.TEXT_PLAIN)
	public Response runBatchPublishExistingInstitutionen() {
		institutionEventServiceBean
			.publishExistingInstitutionen(principalBean.getMandant());
		return Response.ok().build();
	}

	@Operation(summary = "Führt den Job publishWartendeBetreuung aus.")
	@POST
	@Path("/publishWartendeBetreuung")
	@Produces(MediaType.TEXT_PLAIN)
	public Response runBatchPublishWartendeBetreuung() {
		betreuungAnfrageEventServiceBean.publishExistingBetreuungAnfrage(
			principalBean.getMandant()
		);
		return Response.ok().build();
	}

	@Operation(summary = "Führt den Job migrateVerfuegung aus.")
	@POST
	@Path("/migrateVerfuegung")
	@Produces(MediaType.TEXT_PLAIN)
	public Response runBatchMigrateVerfuegung() {
		verfuegungEventServiceBean.publishExistingVerfuegungen(
			principalBean.getMandant()
		);
		return Response.ok().build();
	}
}
