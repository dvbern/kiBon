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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import ch.dvbern.ebegu.api.converter.institution.JaxInstitutionStammdatenConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionStammdaten;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionStammdatenSummary;
import ch.dvbern.ebegu.api.dtos.admin.institution.JaxModuleGroupAnmeldungDTO;
import ch.dvbern.ebegu.dto.filter.InstitutionNameStammdatenIdDto;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.ModulTagesschuleGroup;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;
import ch.dvbern.ebegu.services.tagesschule.ModulTagesschuleGroupService;
import org.eclipse.microprofile.openapi.annotations.Operation;

/**
 * REST Resource fuer InstitutionStammdaten
 */
@Path("institutionstammdaten")
@Stateless
@PermitAll // Grundsaetzliche fuer alle Rollen (nur Lesend): Datenabhaengig. -> Authorizer
public class InstitutionStammdatenResource {

	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	@Inject
	private ModulTagesschuleGroupService modulTagesschuleGroupService;

	@Inject
	private JaxInstitutionStammdatenConverter converter;

	@Operation(
		summary = "Sucht die InstitutionsStammdaten mit der uebergebenen Id in der Datenbank")
	@Nullable
	@GET
	@Path("/id/{institutionStammdatenId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxInstitutionStammdaten findInstitutionStammdaten(
		@Nonnull
		@NotNull
		@PathParam("institutionStammdatenId") JaxId institutionStammdatenJAXPId
	) {

		Objects.requireNonNull(institutionStammdatenJAXPId.getId());
		String institutionStammdatenID = converter.toEntityId(
			institutionStammdatenJAXPId
		);
		Optional<InstitutionStammdaten> optional =
			institutionStammdatenService.findInstitutionStammdaten(
				institutionStammdatenID
			);

		return optional.map(
			institutionStammdaten -> converter.institutionStammdatenToJAX(
				institutionStammdaten
			)
		)
			.orElse(null);
	}

	/**
	 * Sucht in der DB alle aktiven InstitutionStammdaten, deren Gueltigkeit zwischen DatumVon und DatumBis
	 * der Gesuchsperiode liegt
	 *
	 * @param gesuchsperiodeJaxId id der Gesuchsperiode fuer die Stammdaten gesucht werden sollen
	 * @return Liste mit allen InstitutionStammdaten die den Bedingungen folgen
	 */
	@Operation(
		summary = "Gibt alle Institutionsstammdaten zurueck, welche am angegebenen Datum existieren und aktiv "
			+ "sind und welche (falls TS oder FI oder BG mit Filterung durch Gemeinde) zur angegebenen Gemeinde gehören")
	@Nonnull
	@GET
	@Path("/gesuchsperiode/gemeinde/active")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxInstitutionStammdatenSummary> getAllActiveInstitutionStammdatenByGesuchsperiodeAndGemeinde(
		@Nonnull
		@NotNull
		@QueryParam("gesuchsperiodeId") JaxId gesuchsperiodeJaxId,
		@Nonnull @NotNull @QueryParam("gemeindeId") JaxId gemeindeJaxId
	) {

		Objects.requireNonNull(gesuchsperiodeJaxId);
		Objects.requireNonNull(gesuchsperiodeJaxId.getId());
		Objects.requireNonNull(gemeindeJaxId);
		Objects.requireNonNull(gemeindeJaxId.getId());

		String gesuchsperiodeId = converter.toEntityId(gesuchsperiodeJaxId);
		String gemeindeId = converter.toEntityId(gemeindeJaxId);

		return institutionStammdatenService
			.getAllActiveInstitutionStammdatenByGesuchsperiodeAndGemeinde(
				gesuchsperiodeId,
				gemeindeId
			)
			.stream()
			.map(
				institutionStammdaten -> converter
					.institutionStammdatenSummaryToJAX(
						institutionStammdaten,
						new JaxInstitutionStammdatenSummary()
					)
			)
			.collect(Collectors.toList());
	}

	/**
	 * Sucht in der DB alle InstitutionStammdaten, bei welchen die Institutions-id dem übergabeparameter entspricht.
	 * Falls die Institution keine Stammdaten hat gibt sie null zurück, dabei wird keine Ausnahme geworfen.
	 *
	 * @param institutionJAXPId ID der gesuchten Institution
	 * @return Die InstitutionStammdaten dieser Institution
	 */
	@Operation(
		summary = "Gibt alle Institutionsstammdaten der uebergebenen Institution zurueck, null falls keine "
			+ "vorhanden.")
	@Nullable
	@GET
	@Path("/institutionornull/{institutionId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxInstitutionStammdaten fetchInstitutionStammdatenByInstitution(
		@Nonnull
		@NotNull
		@PathParam("institutionId") JaxId institutionJAXPId
	) {

		Objects.requireNonNull(institutionJAXPId.getId());
		String institutionID = converter.toEntityId(institutionJAXPId);
		InstitutionStammdaten stammdaten =
			institutionStammdatenService
				.fetchInstitutionStammdatenByInstitution(
					institutionID,
					true
				);
		return null == stammdaten ?
			null :
			converter.institutionStammdatenToJAX(stammdaten);
	}

	/**
	 * Gibt alle BetreuungsangebotsTypen zurueck, welche die Institutionen des eingeloggten Benutzers anbieten
	 */
	@Operation(
		summary = "Gibt alle BetreuungsangebotTypen aller Institutionen zurueck, zu welchen der eingeloggte "
			+
			"Benutzer zugeordnet ist")
	@SuppressWarnings("InstanceMethodNamingConvention")
	@Nonnull
	@GET
	@Path("/currentuser")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<BetreuungsangebotTyp> getBetreuungsangeboteForInstitutionenOfCurrentBenutzer() {
		List<BetreuungsangebotTyp> result =
			new ArrayList<>(
				institutionStammdatenService
					.getBetreuungsangeboteForInstitutionenOfCurrentBenutzer()
			);
		return result;
	}

	@Operation(
		summary = "Findet alle Tagesschulinstitutionen und Stammdaten für den momentan eingeloggten Benutzer."
			+ "Gibt alle zurück für Administratoren.")
	@Nonnull
	@GET
	@Path("/tagesschulen/currentuser")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxInstitutionStammdatenSummary> getTagesschulenForCurrentBenutzer() {
		return institutionStammdatenService.getTagesschulenForCurrentBenutzer()
			.stream()
			.map(
				stammdaten -> converter
					.institutionStammdatenSummaryToJAX(
						stammdaten,
						new JaxInstitutionStammdatenSummary()
					)
			)
			.collect(Collectors.toList());
	}

	@Operation(
		summary = "Findet alle TagesschuleinstitutionenStammdaten Id und Institution Name für den momentan eingeloggten Benutzer."
			+ "Nur die Tagesschulen, die nicht nur für LATS Aktiv sind und Module anbieten, sind enthalten.")
	@Nonnull
	@GET
	@Path("/filter/tagesschulen/currentuser")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<InstitutionNameStammdatenIdDto> getTagesschulenFilterListForCurrentBenutzer() {
		return institutionStammdatenService
			.getTagesschulenFilterListForCurrentBenutzer()
			.stream()
			.toList();
	}

	@Operation(
		summary = "Gibt die TagesschulEinstellungsmodule mit der Information, ob es Anmeldungen gibt, zurück"
	)
	@Nonnull
	@POST
	@Path("/tagesschulen/einstellungen-angemeldet")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxModuleGroupAnmeldungDTO> getModulTagesschuleGroupWithAnmeldung(
		@Nonnull List<String> modulesToSearchAnmeldungenFor
	) {
		final Collection<ModulTagesschuleGroup> modulTagesschuleGroupWithAnmeldung =
			modulTagesschuleGroupService.getModulTagesschuleGroupWithAnmeldung(
				modulesToSearchAnmeldungenFor
			);

		return modulesToSearchAnmeldungenFor
			.stream()
			.map(
				idToSearchFor -> JaxModuleGroupAnmeldungDTO.builder()
					.groupId(idToSearchFor)
					.hasAnmeldung(
						modulTagesschuleGroupWithAnmeldung.stream()
							.anyMatch(
								withAnmeldung -> withAnmeldung.getId()
									.equals(idToSearchFor)
							)
					)
					.build()
			)
			.collect(Collectors.toList());
	}
}
