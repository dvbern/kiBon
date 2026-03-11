/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.resource;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.institution.JaxInstitutionConverter;
import ch.dvbern.ebegu.api.converter.institution.JaxInstitutionStammdatenConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxInstitution;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionExternalClientAssignment;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionListDTO;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionStammdaten;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionStammdatenBetreuungsgutscheine;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionUpdate;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.einstellung.ApplicationPropertyKey;
import ch.dvbern.ebegu.einstellung.ApplicationPropertyService;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.ExternalClient;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionExternalClient;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdatenBetreuungsgutscheine;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.InstitutionStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.CreateBenutzerService;
import ch.dvbern.ebegu.services.ExternalClientService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.InstitutionStammdatenInitalizerService;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;
import ch.dvbern.ebegu.services.InstitutionUpdateMailService;
import ch.dvbern.ebegu.services.MandantService;
import ch.dvbern.ebegu.services.MitteilungGueltigkeitChangeService;
import ch.dvbern.ebegu.services.MitteilungService;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.GueltigkeitsUtil;
import ch.dvbern.ebegu.util.InstitutionStammdatenInitalizerVisitor;
import ch.dvbern.ebegu.util.mandant.MandantCookieUtil;
import com.google.common.base.Preconditions;
import org.eclipse.microprofile.openapi.annotations.Operation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;
import static java.util.Objects.requireNonNull;

/**
 * REST Resource fuer Institution
 */
@Path("institutionen")
@Stateless
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class InstitutionResource {

	@Inject
	private InstitutionService institutionService;

	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	@Inject
	private ExternalClientService externalClientService;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private JaxInstitutionStammdatenConverter institutionStammdatenConverter;

	@Inject
	private JaxInstitutionConverter institutionConverter;

	@Inject
	private MandantService mandantService;

	@Inject
	private MitteilungService mitteilungService;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@Inject
	private InstitutionStammdatenInitalizerService institutionStammdatenInitalizerService;

	@Inject
	private InstitutionUpdateMailService instiutionUpdateMailService;

	@Inject
	private CreateBenutzerService createBenutzerService;

	@Inject
	private MitteilungGueltigkeitChangeService mitteilungGueltigkeitChangeService;

	@Operation(summary = "Creates a new Institution in the database.")
	@Nullable
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_TRAEGERSCHAFT,
		ADMIN_GEMEINDE, ADMIN_BG, ADMIN_TS, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_TS })
	public Response createInstitution(
		@Nonnull @NotNull JaxInstitution institutionJAXP,
		@Nonnull
		@NotNull
		@Valid
		@QueryParam("date") String stringDateBeguStart,
		@Nonnull
		@NotNull
		@Valid
		@QueryParam("betreuung") BetreuungsangebotTyp betreuungsangebot,
		@Nonnull @NotNull @Valid @QueryParam("adminMail") String adminMail,
		@Nullable @Valid @QueryParam("gemeindeId") String gemeindeId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {

		requireNonNull(adminMail);
		checkCreatInstitutionAllowed(betreuungsangebot);

		Institution convertedInstitution = institutionConverter
			.institutionToNewEntity(
				institutionJAXP
			);
		Institution persistedInstitution = this.institutionService
			.createInstitution(convertedInstitution);

		LocalDate startDate = LocalDate.parse(
			stringDateBeguStart,
			Constants.SQL_DATE_FORMAT
		);
		initInstitutionStammdaten(
			startDate,
			betreuungsangebot,
			persistedInstitution,
			adminMail,
			gemeindeId
		);

		Mandant mandant = requireNonNull(persistedInstitution.getMandant());

		if (BetreuungsangebotTyp.getBetreuungsgutscheinTypes()
			.contains(betreuungsangebot)) {
			Benutzer benutzer = benutzerService.findBenutzer(adminMail, mandant)
				.map(b -> {
					if ((b.getRole() != UserRole.ADMIN_TRAEGERSCHAFT
						&& b.getRole() != UserRole.GESUCHSTELLER)
						||
						!Objects.equals(
							b.getTraegerschaft(),
							persistedInstitution.getTraegerschaft()
						)) {
						// an existing user cannot be used to create a new Institution
						throw new EbeguRuntimeException(
							KibonLogLevel.INFO,
							"createInstitution",
							ErrorCodeEnum.EXISTING_USER_MAIL,
							adminMail
						);
					}

					return b;
				})
				.orElseGet(
					() -> createBenutzerService.createAdminInstitutionByEmail(
						adminMail,
						persistedInstitution
					)
				);

			benutzerService.einladen(
				Einladung.forInstitution(
					benutzer,
					persistedInstitution,
					startDate
				),
				mandant
			);
		}

		URI uri = uriInfo.getBaseUriBuilder()
			.path(InstitutionResource.class)
			.path('/' + persistedInstitution.getId())
			.build();

		JaxInstitution jaxInstitution = institutionStammdatenConverter
			.institutionToJAX(
				persistedInstitution
			);
		return Response.created(uri).entity(jaxInstitution).build();
	}

	private void checkCreatInstitutionAllowed(
		@Nonnull BetreuungsangebotTyp betreuungsangebot
	) {
		if (betreuungsangebot.isKita() || betreuungsangebot.isTagesfamilien()) {
			boolean institutionenDurchGemeindenEinladen = Boolean.TRUE.equals(
				this.applicationPropertyService
					.findApplicationPropertyAsBoolean(
						ApplicationPropertyKey.INSTITUTIONEN_DURCH_GEMEINDEN_EINLADEN,
						principalBean.getMandant()
					)
			);
			// falls Einstellung deaktiviert, dass Institutionen durch Gemeinden eingeladen werden können, dürfen nur
			// SUPERADMIN und MANDANTROLLEN Institutionen einladen
			if (!institutionenDurchGemeindenEinladen
				&& !principalBean.isCallerInAnyOfRole(
					SUPER_ADMIN,
					ADMIN_MANDANT,
					SACHBEARBEITER_MANDANT
				)) {
				throw new IllegalStateException(
					"Nur ein Superadmin oder Mandant Benutzer kann einen neuen Kita/TFO Benutzer einladen. Dies wurde "
						+ "aber versucht durch: "
						+ principalBean.getBenutzer().getUsername()
				);
			}
		} else if (betreuungsangebot.isSchulamt()
			&& principalBean.isCallerInAnyOfRole(UserRole.ADMIN_BG)) {
			throw new IllegalStateException(
				"Ein Admin BG kann keine Tagesschulen oder Ferieninseln erstellen."
			);
		}
	}

	private void initInstitutionStammdaten(
		@Nonnull LocalDate startDate,
		@Nonnull BetreuungsangebotTyp betreuungsangebot,
		@Nonnull Institution persistedInstitution,
		@Nonnull String adminMail,
		@Nullable String gemeindeId
	) {
		InstitutionStammdaten institutionStammdaten =
			new InstitutionStammdatenInitalizerVisitor(
				institutionStammdatenInitalizerService,
				gemeindeId
			)
				.initalizeInstiutionStammdaten(betreuungsangebot);

		Adresse adresse = new Adresse();
		adresse.setStrasse("");
		adresse.setPlz("");
		adresse.setOrt("");
		institutionStammdaten.setAdresse(adresse);
		institutionStammdaten.setBetreuungsangebotTyp(betreuungsangebot);
		institutionStammdaten.setInstitution(persistedInstitution);
		institutionStammdaten.setMail(adminMail);

		DateRange gueltigkeit = new DateRange(startDate, Constants.END_OF_TIME);
		institutionStammdaten.setGueltigkeit(gueltigkeit);

		institutionStammdatenService.saveInstitutionStammdaten(
			institutionStammdaten
		);
	}

	@Operation(summary = "Update a Institution and Stammdaten in the database.")
	@Nullable
	@PUT
	@Path("/{institutionId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_INSTITUTION, ADMIN_TRAEGERSCHAFT,
		ADMIN_GEMEINDE, ADMIN_BG, ADMIN_TS, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS })
	public JaxInstitutionStammdaten updateInstitutionAndStammdaten(
		@Nonnull
		@NotNull
		@PathParam("institutionId") JaxId institutionJAXPId,
		@Nonnull @NotNull @Valid JaxInstitutionUpdate update
	) {

		Institution institution = institutionService.findInstitution(
			requireNonNull(institutionJAXPId.getId()),
			true
		)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"update",
					institutionJAXPId.getId()
				)
			);

		InstitutionStammdaten stammdaten = Optional.ofNullable(
			update.getStammdaten().getId()
		)
			.flatMap(
				id -> institutionStammdatenService
					.findInstitutionStammdaten(id)
			)
			.orElseGet(() -> new InstitutionStammdaten(institution));

		DateRange oldGueltigkeit = new DateRange(stammdaten.getGueltigkeit());
		boolean auszahlungsdatenChanged =
			hasAuszahlungsdatenChanged(
				stammdaten
					.getInstitutionStammdatenBetreuungsgutscheine(),
				update.getStammdaten()
					.getInstitutionStammdatenBetreuungsgutscheine()
			);
		institutionStammdatenConverter.institutionStammdatenToEntity(
			update.getStammdaten(),
			stammdaten
		);

		Preconditions.checkArgument(
			stammdaten.getInstitution().equals(institution),
			"Stammdaten and Institution must belong together, but %s != %s",
			stammdaten.getInstitution(),
			institution
		);

		if (update.getInstitutionExternalClients() != null) {
			List<InstitutionExternalClient> institutionExternalClients =
				institutionStammdatenConverter
					.institutionExternalClientListToEntity(
						update.getInstitutionExternalClients(),
						institution
					);
			if (checkExternalClientDateOverlapping(
				institutionExternalClients
			)) {
				throw new EbeguRuntimeException(
					"updateInstitutionAndStammdaten",
					ErrorCodeEnum.ERROR_INVALID_EXTERNAL_CLIENT_DATERANGE
				);
			}

			institutionService.saveInstitutionExternalClients(
				institution,
				institutionExternalClients
			);
		}

		boolean institutionUpdated = institutionConverter.institutionToEntity(
			update,
			institution,
			stammdaten
		);

		if (institutionUpdated
			|| update.getInstitutionExternalClients() != null) {
			institutionService.updateInstitution(institution);
		}

		// set the updated institution
		stammdaten.setInstitution(institution);

		InstitutionStammdaten persistedInstData =
			institutionStammdatenService.saveInstitutionStammdaten(
				stammdaten
			);

		if (institutionStammdatenService.isGueltigkeitChanged(
			oldGueltigkeit,
			stammdaten.getGueltigkeit()
		)) {
			mitteilungGueltigkeitChangeService
				.adaptOffeneMutationsmitteilungenToInstiGueltigkeitChange(
					stammdaten.getInstitution(),
					stammdaten.getGueltigkeit()
				);
		}

		institutionStammdatenService.fireStammdatenChangedEvent(
			persistedInstData
		);

		if (auszahlungsdatenChanged) {
			instiutionUpdateMailService.sendAuszahlungsdatenUpdatedInfo(
				stammdaten
			);
		}

		return institutionStammdatenConverter.institutionStammdatenToJAX(
			persistedInstData
		);
	}

	private boolean hasAuszahlungsdatenChanged(
		@Nullable InstitutionStammdatenBetreuungsgutscheine existingStammdaten,
		@Nullable JaxInstitutionStammdatenBetreuungsgutscheine updatedStammdaten
	) {
		if (updatedStammdaten == null) {
			return false;
		}

		if (existingStammdaten == null
			||
			existingStammdaten.getAuszahlungsdaten() == null) {
			//wenn noch keine Stammdaten existieren, aber IBAN oder Kontoinhaber erfasst wurden handelt es sich um ein update
			return updatedStammdaten.getIban() != null
				||
				updatedStammdaten.getKontoinhaber() != null;
		}

		String existingIban = existingStammdaten.getAuszahlungsdaten().getIban()
			== null ?
				null :
				existingStammdaten.getAuszahlungsdaten()
					.getIban()
					.toString();

		return !EbeguUtil.contentEquals(
			existingIban,
			updatedStammdaten.getIban()
		)
			||
			!EbeguUtil.contentEquals(
				existingStammdaten.getAuszahlungsdaten()
					.getKontoinhaber(),
				updatedStammdaten.getKontoinhaber()
			);
	}

	@Operation(
		summary = "Find and return an Institution by his institution id as parameter")
	@Nullable
	@GET
	@Path("/{institutionId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Oeffentliche Daten
	public JaxInstitution findInstitution(
		@Nonnull
		@NotNull
		@PathParam("institutionId") JaxId institutionJAXPId
	) {

		requireNonNull(institutionJAXPId.getId());
		String institutionID = institutionStammdatenConverter.toEntityId(
			institutionJAXPId
		);
		Optional<Institution> optional = institutionService.findInstitution(
			institutionID,
			true
		);

		return optional.map(
			institution -> institutionStammdatenConverter.institutionToJAX(
				institution
			)
		).orElse(null);
	}

	@Operation(
		summary = "Remove an Institution from the DB by its institution-id as parameter")
	@Nullable
	@DELETE
	@Path("/{institutionId}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed(SUPER_ADMIN)
	public Response removeInstitution(
		@Nonnull
		@NotNull
		@PathParam("institutionId") JaxId institutionJAXPId,
		@Context HttpServletResponse response
	) {

		requireNonNull(institutionJAXPId.getId());
		institutionService.removeInstitution(
			institutionStammdatenConverter.toEntityId(institutionJAXPId)
		);
		return Response.ok().build();
	}

	@Operation(summary = "Find and return a list of all Institutionen")
	@Nonnull
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Oeffentliche Daten
	public List<JaxInstitution> getAllInstitutionen(
		@CookieParam(MandantCookieUtil.MANDANT_COOKIE_NAME) Cookie mandantCookie
	) {
		var mandant = mandantService.findMandantByCookie(mandantCookie);

		return institutionService.getAllInstitutionen(mandant)
			.stream()
			.map(inst -> institutionStammdatenConverter.institutionToJAX(inst))
			.collect(Collectors.toList());
	}

	@Operation(summary = "Find and return a list of all BG Institutionen")
	@Nonnull
	@GET
	@Path("/bg")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Oeffentliche Daten
	public List<JaxInstitution> getAllBgInstitutionen(
		@CookieParam(MandantCookieUtil.MANDANT_COOKIE_NAME) Cookie mandantCookie
	) {
		var mandant = mandantService.findMandantByCookie(mandantCookie);

		return institutionService.getAllInstitutionenByType(
			mandant,
			BetreuungsangebotTyp.getBetreuungsgutscheinTypes()
		)
			.stream()
			.map(inst -> institutionStammdatenConverter.institutionToJAX(inst))
			.collect(Collectors.toList());
	}

	@Operation(
		summary = "Find and return a list of all editable Institutionen of the currently logged in Benutzer. "
			+ "Returns all for admins")
	@Nonnull
	@GET
	@Path("/editable/currentuser")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public List<JaxInstitution> getInstitutionenEditableForCurrentBenutzer() {
		return institutionService.getInstitutionenEditableForCurrentBenutzer(
			true
		)
			.stream()
			.map(inst -> institutionStammdatenConverter.institutionToJAX(inst))
			.collect(Collectors.toList());
	}

	@Operation(
		summary = "Find and return a list of all editable Institutionen of the currently logged in Benutzer. "
			+ "Returns all for admins")
	@Nonnull
	@GET
	@Path("/editable/currentuser/listdto")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public List<JaxInstitutionListDTO> getInstitutionenListDTOEditableForCurrentBenutzer() {
		Map<Institution, InstitutionStammdaten> institutionInstitutionStammdatenMap =
			institutionService
				.getInstitutionenInstitutionStammdatenEditableForCurrentBenutzer(
					true
				);

		return institutionInstitutionStammdatenMap.entrySet()
			.stream()
			.map(map -> institutionConverter.institutionListDTOToJAX(map))
			.collect(Collectors.toList());
	}

	@Operation(
		summary = "Find and return a list of all editable BgInstitutionen of the currently logged in Benutzer. "
			+ "Returns all for admins")
	@Nonnull
	@GET
	@Path("/editable/currentuser/bg")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public List<JaxInstitutionListDTO> getAllBgInstitutionenEditableForCurrentBenutzer() {
		Map<Institution, InstitutionStammdaten> institutionInstitutionStammdatenMap =
			institutionService
				.getInstitutionenInstitutionStammdatenEditableForCurrentBenutzer(
					true
				);

		return institutionInstitutionStammdatenMap.entrySet()
			.stream()
			.filter(
				i -> BetreuungsangebotTyp.getBetreuungsgutscheinTypes()
					.contains(i.getValue().getBetreuungsangebotTyp())
			)
			.map(map -> institutionConverter.institutionListDTOToJAX(map))
			.collect(Collectors.toList());
	}

	@Operation(
		summary = "Find and return a list of all readable Institutionen of the currently logged in Benutzer. "
			+ "Returns all for admins")
	@Nonnull
	@GET
	@Path("/readable/currentuser")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public List<JaxInstitution> getInstitutionenReadableForCurrentBenutzer() {
		return institutionService.getInstitutionenReadableForCurrentBenutzer(
			false
		)
			.stream()
			.map(inst -> institutionStammdatenConverter.institutionToJAX(inst))
			.collect(Collectors.toList());
	}

	@Operation(
		summary = "Find and return a list of all readable BgInstitutionen of the currently logged in Benutzer. "
			+ "Returns all for admins")
	@Nonnull
	@GET
	@Path("/readable/currentuser/bg")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public List<JaxInstitution> getAllBgInstitutionenReadableForCurrentBenutzer() {
		return institutionService.getInstitutionenReadableForCurrentBenutzer(
			false
		)
			.stream()
			.filter(
				i -> BetreuungsangebotTyp.getBetreuungsgutscheinTypes()
					.contains(
						institutionStammdatenService
							.fetchInstitutionStammdatenByInstitution(
								i.getId(),
								false
							)
							.getBetreuungsangebotTyp()
					)
			)
			.map(inst -> institutionStammdatenConverter.institutionToJAX(inst))
			.collect(Collectors.toList());
	}

	@Operation(
		summary = "Returns true, if the currently logged in Benutzer has any Institutionen in Status "
			+ "EINGELADEN")
	@Nonnull
	@GET
	@Path("/hasEinladungen/currentuser")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public Response hasInstitutionenInStatusEingeladenForCurrentBenutzer() {
		long anzahl = institutionService
			.getInstitutionenEditableForCurrentBenutzer(true)
			.stream()
			.filter(
				inst -> inst.getStatus() == InstitutionStatus.EINGELADEN
			)
			.count();
		return Response.ok(anzahl > 0).build();
	}

	@Operation(
		summary = "Returns all still available external clients and all assigned external clients")
	@Nonnull
	@GET
	@Path("/{institutionId}/externalclients")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_INSTITUTION, ADMIN_TRAEGERSCHAFT,
		ADMIN_GEMEINDE, ADMIN_BG, ADMIN_TS, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_BG, SACHBEARBEITER_TS })
	public Response getExternalClients(
		@Nonnull
		@NotNull
		@PathParam("institutionId") JaxId institutionJAXPId
	) {

		requireNonNull(institutionJAXPId.getId());
		String institutionID = institutionStammdatenConverter.toEntityId(
			institutionJAXPId
		);
		Institution institution = institutionService.findInstitution(
			institutionID,
			true
		)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"getExternalClients",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					institutionJAXPId.getId()
				)
			);

		Collection<ExternalClient> availableClients = externalClientService
			.getAllForInstitution(institution);

		Collection<InstitutionExternalClient> institutionExternalClients =
			externalClientService
				.getInstitutionExternalClientForInstitution(
					institution
				);

		List<ExternalClient> existingExternalClient = institutionExternalClients
			.stream()
			.map(InstitutionExternalClient::getExternalClient)
			.collect(Collectors.toList());

		availableClients.removeAll(existingExternalClient);

		JaxInstitutionExternalClientAssignment jaxInstitutionExternalClientAssignment =
			new JaxInstitutionExternalClientAssignment();
		jaxInstitutionExternalClientAssignment.getAvailableClients()
			.addAll(
				institutionStammdatenConverter.externalClientsToJAX(
					availableClients
				)
			);

		jaxInstitutionExternalClientAssignment.getAssignedClients()
			.addAll(
				institutionStammdatenConverter.institutionExternalClientsToJAX(
					institutionExternalClients
				)
			);

		return Response.ok(jaxInstitutionExternalClientAssignment).build();
	}

	@Operation(
		summary = "Returns true, if the currently logged in Benutzer has any Institutionen which is Tagesschule")
	@Nonnull
	@GET
	@Path("/istagesschulenutzende/currentuser")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public Response isCurrentUserTageschuleNutzende() {
		boolean isTSNutzende = institutionService
			.isCurrentUserTagesschuleNutzende(false);
		return Response.ok(isTSNutzende).build();
	}

	private boolean checkExternalClientDateOverlapping(
		List<InstitutionExternalClient> institutionExternalClients
	) {
		return GueltigkeitsUtil.hasOverlapingGueltigkeit(
			institutionExternalClients
		);
	}

	@Operation(
		summary = "Find and return a list of all editable Institutionen of the currently logged in Benutzer. "
			+ "Returns all for admins")
	@Nonnull
	@GET
	@Path("/gemeinde/listdto/{gemeindeId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public List<JaxInstitutionListDTO> getInstitutionenForGemeinde(
		@Nonnull @NotNull @PathParam("gemeindeId") JaxId gemeindeJAXPId
	) {
		requireNonNull(gemeindeJAXPId.getId());
		String gemeindeId = institutionStammdatenConverter.toEntityId(
			gemeindeJAXPId
		);
		Gemeinde gemeinde = gemeindeService.findGemeinde(gemeindeId)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"getInstitutionenForGemeinde",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					gemeindeId
				)
			);

		Map<Institution, InstitutionStammdaten> institutionInstitutionStammdatenMap =
			institutionService
				.getInstitutionenInstitutionStammdatenForGemeinde(
					gemeinde
				);

		return institutionInstitutionStammdatenMap.entrySet()
			.stream()
			.map(map -> institutionConverter.institutionListDTOToJAX(map))
			.collect(Collectors.toList());
	}

	@Operation(
		summary = "Gibt alle Institutionen zurück, die mindestens einmal in diesem Dossier verwendet wurden")
	@Nullable
	@GET
	@Path("/findAllInstitutionen/{dossierId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public List<JaxInstitution> findAllInstitutionen(
		@Nonnull @NotNull @PathParam("dossierId") JaxId jaxDossierId
	) {
		Objects.requireNonNull(jaxDossierId.getId());

		Collection<Institution> institutions = institutionService
			.findAllInstitutionen(jaxDossierId.getId());

		return institutions.stream()
			.distinct()
			.map(
				institution -> institutionStammdatenConverter.institutionToJAX(
					institution
				)
			)
			.collect(Collectors.toList());
	}

	@Operation(
		summary = "Setzt eine Institution aus dem Status NUR_LATS in die Konfiguration")
	@Nonnull
	@PUT
	@Path("{institutionId}/nurlatsUmwandeln")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		ADMIN_GEMEINDE, ADMIN_BG,
		ADMIN_TS, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_TS, ADMIN_MANDANT,
		SACHBEARBEITER_MANDANT })
	public JaxInstitution nurLatsInstitutionUmwandeln(
		@Nonnull @PathParam("institutionId") JaxId jaxInstitutionId
	) {
		Objects.requireNonNull(jaxInstitutionId.getId());

		Institution institution = institutionService.findInstitution(
			jaxInstitutionId.getId(),
			true
		)
			.orElseThrow(() -> {
				throw new EbeguEntityNotFoundException(
					"nurLatsInstitutionUmwandeln",
					jaxInstitutionId.getId()
				);
			});

		return institutionStammdatenConverter.institutionToJAX(
			institutionService.nurLatsInstitutionUmwandeln(institution)
		);
	}
}
