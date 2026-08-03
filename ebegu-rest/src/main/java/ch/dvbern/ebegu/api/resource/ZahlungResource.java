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
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.activation.MimeTypeParseException;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxZahlungConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxPaginationDTO;
import ch.dvbern.ebegu.api.dtos.JaxZahlung;
import ch.dvbern.ebegu.api.dtos.JaxZahlungsauftrag;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.dto.ZahlungenSearchParamsDTO;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Workjob;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.WorkJobType;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.enums.reporting.BatchJobStatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GeneratedDokumentService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.WorkjobService;
import ch.dvbern.ebegu.services.ZahlungService;
import ch.dvbern.ebegu.services.zahlungen.WorkjobZahlungslaufService;
import ch.dvbern.ebegu.util.DateUtil;
import org.apache.commons.collections.CollectionUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.JURIST;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;
import static java.util.Objects.requireNonNull;

/**
 * Resource fuer Zahlungen
 */
@Path("zahlungen")
@Stateless
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class ZahlungResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(
		ZahlungResource.class
	);

	@Inject
	private ZahlungService zahlungService;

	@Inject
	private JaxZahlungConverter converter;

	@Inject
	private GeneratedDokumentService generatedDokumentService;

	@Inject
	private InstitutionService institutionService;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private WorkjobZahlungslaufService workjobZahlungslaufService;

	@Inject
	private Authorizer authorizer;

	@Inject
	private WorkjobService workjobService;

	@Operation(summary = "Gibt alle Zahlungsauftraege zurueck.")
	@Nullable
	@GET
	@Path("/all")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
		SACHBEARBEITER_GEMEINDE, JURIST, REVISOR,
		ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public JaxPaginationDTO<JaxZahlungsauftrag> getAllZahlungsauftraege(
		@Nullable @QueryParam("gemeinde") String filterGemeinde,
		@Nullable @QueryParam("sortPredicate") String sortPredicate,
		@Nullable @QueryParam("sortReverse") String sortReverseParam,
		@Nonnull @QueryParam("page") String pageParam,
		@Nonnull @QueryParam("pageSize") String pageSizeParam,
		@Nonnull @QueryParam("zahlungslaufTyp") String zahlungslaufTyp
	) {
		ZahlungenSearchParamsDTO zahlungenSearchParamsDTO =
			toZahlungenSearchParamsDTO(
				filterGemeinde,
				sortPredicate,
				sortReverseParam,
				pageParam,
				pageSizeParam,
				ZahlungslaufTyp.valueOf(zahlungslaufTyp),
				null
			);

		List<JaxZahlungsauftrag> zahlungsauftraege = zahlungService
			.getAllZahlungsauftraege(zahlungenSearchParamsDTO)
			.stream()
			.map(
				zahlungsauftrag -> converter.zahlungsauftragToJAX(
					zahlungsauftrag,
					false
				)
			)
			.collect(Collectors.toList());
		Long count = zahlungService.countAllZahlungsauftraege(
			zahlungenSearchParamsDTO
		);

		JaxPaginationDTO<JaxZahlungsauftrag> jaxPaginationDTO =
			new JaxPaginationDTO<>();
		jaxPaginationDTO.setResultList(zahlungsauftraege);
		jaxPaginationDTO.setTotalCount(count);
		return jaxPaginationDTO;
	}

	@Operation(
		summary = "Gibt alle Zahlungsauftraege aller Institutionen zurueck, fuer welche der eingeloggte "
			+
			"Benutzer zustaendig ist.")
	@Nullable
	@GET
	@Path("/institution")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_TRAEGERSCHAFT,
		SACHBEARBEITER_INSTITUTION, ADMIN_INSTITUTION,
		SACHBEARBEITER_TRAEGERSCHAFT })
	public JaxPaginationDTO<JaxZahlungsauftrag> getAllZahlungsauftraegeInstitution(
		@Nullable @QueryParam("gemeinde") String filterGemeinde,
		@Nullable @QueryParam("sortPredicate") String sortPredicate,
		@Nullable @QueryParam("sortReverse") String sortReverseParam,
		@Nonnull @QueryParam("page") String pageParam,
		@Nonnull @QueryParam("pageSize") String pageSizeParam
	) {

		JaxPaginationDTO<JaxZahlungsauftrag> jaxPaginationDTO =
			new JaxPaginationDTO<>();
		Collection<Institution> allowedInst = institutionService
			.getInstitutionenReadableForCurrentBenutzer(false);

		if (CollectionUtils.isEmpty(allowedInst)) {
			return jaxPaginationDTO;
		}

		ZahlungenSearchParamsDTO zahlungenSearchParamsDTO =
			toZahlungenSearchParamsDTO(
				filterGemeinde,
				sortPredicate,
				sortReverseParam,
				pageParam,
				pageSizeParam,
				ZahlungslaufTyp.GEMEINDE_INSTITUTION,
				allowedInst
			);

		List<JaxZahlungsauftrag> zahlungenList = zahlungService
			.getAllZahlungsauftraege(zahlungenSearchParamsDTO)
			.stream()
			.map(
				zahlungsauftrag -> converter.zahlungsauftragToJAX(
					zahlungsauftrag,
					principalBean.discoverMostPrivilegedRole(),
					allowedInst
				)
			)
			.collect(Collectors.toList());

		Long count = zahlungService.countAllZahlungsauftraege(
			zahlungenSearchParamsDTO
		);

		jaxPaginationDTO.setResultList(zahlungenList);
		jaxPaginationDTO.setTotalCount(count);
		return jaxPaginationDTO;
	}

	private ZahlungenSearchParamsDTO toZahlungenSearchParamsDTO(
		@Nullable String filterGemeindeParam,
		@Nullable String sortPredicate,
		@Nullable String sortReverseParam,
		@Nonnull String pageParam,
		@Nonnull String pageSizeParam,
		@Nonnull ZahlungslaufTyp zahlungslaufTyp,
		@Nullable Collection<Institution> allowedInst
	) {
		String message = "invalid param: ";
		int page;
		int pageSize;
		try {
			page = Integer.parseInt(pageParam);
		} catch (NumberFormatException e) {
			throw new BadRequestException(message + "page", e);
		}

		try {
			pageSize = Integer.parseInt(pageSizeParam);
		} catch (NumberFormatException e) {
			throw new BadRequestException(message + "pageSize", e);
		}
		ZahlungenSearchParamsDTO zahlungenParams = new ZahlungenSearchParamsDTO(
			page,
			pageSize
		);

		if (filterGemeindeParam != null) {
			Gemeinde gemeinde = gemeindeService.findGemeinde(
				filterGemeindeParam
			)
				.orElseThrow(
					() -> new EbeguEntityNotFoundException(
						"toZahlungenSearchParamsDTO",
						filterGemeindeParam
					)
				);
			zahlungenParams.setGemeinde(gemeinde);
		}
		if (sortReverseParam == null
			|| sortReverseParam.equals("true")
			|| sortReverseParam.equals("false")) {
			zahlungenParams.setSortPredicate(sortPredicate);
			zahlungenParams.setSortReverse(
				Boolean.parseBoolean(sortReverseParam)
			);
		} else {
			throw new BadRequestException(message + "sortReverse");
		}

		zahlungenParams.setZahlungslaufTyp(zahlungslaufTyp);

		if (allowedInst != null) {
			if (allowedInst.isEmpty()) {
				throw new BadRequestException(message + "allowedInst");
			}

			List<String> allowedInstIds = allowedInst.stream()
				.map(AbstractEntity::getId)
				.collect(Collectors.toList());
			zahlungenParams.setAllowedInstitutionIds(allowedInstIds);
		}
		return zahlungenParams;
	}

	@Operation(
		summary = "Gibt den Zahlungsauftrag mit der uebergebenen Id zurueck.")
	@Nullable
	@GET
	@Path("/zahlungsauftrag/{zahlungsauftragId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
		SACHBEARBEITER_GEMEINDE, JURIST, REVISOR,
		ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public JaxZahlungsauftrag findZahlungsauftrag(
		@Nonnull
		@NotNull
		@PathParam("zahlungsauftragId") JaxId zahlungsauftragJAXPId
	) {

		requireNonNull(zahlungsauftragJAXPId.getId());
		String zahlungsauftragId = converter.toEntityId(zahlungsauftragJAXPId);
		Optional<Zahlungsauftrag> optional = zahlungService.findZahlungsauftrag(
			zahlungsauftragId
		);

		return optional
			.map(
				zahlungsauftrag -> converter.zahlungsauftragToJAX(
					zahlungsauftrag,
					true
				)
			)
			.orElse(null);
	}

	@Operation(
		summary = "Gibt den Zahlungsauftrag mit der uebebergebenen Id zurueck, jedoch nur mit den Eintraegen "
			+
			"derjenigen Institutionen, fuer welche der eingeloggte Benutzer zustaendig ist")
	@Nullable
	@GET
	@Path("/zahlungsauftraginstitution/{zahlungsauftragId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION,
		ADMIN_TRAEGERSCHAFT,
		SACHBEARBEITER_TRAEGERSCHAFT })
	public JaxZahlungsauftrag findZahlungsauftraginstitution(
		@Nonnull
		@NotNull
		@PathParam("zahlungsauftragId") JaxId zahlungsauftragJAXPId
	) {

		requireNonNull(zahlungsauftragJAXPId.getId());
		String zahlungsauftragId = converter.toEntityId(zahlungsauftragJAXPId);
		Optional<Zahlungsauftrag> optional = zahlungService.findZahlungsauftrag(
			zahlungsauftragId
		);

		return optional
			.filter(
				zahlungsauftrag -> zahlungsauftrag.getZahlungslaufTyp()
					== ZahlungslaufTyp.GEMEINDE_INSTITUTION
			)
			.map(
				zahlungsauftrag -> converter.zahlungsauftragToJAX(
					zahlungsauftrag,
					principalBean.discoverMostPrivilegedRole(),
					institutionService
						.getInstitutionenReadableForCurrentBenutzer(
							false
						)
				)
			)
			.orElse(null);
	}

	@Operation(
		summary = "Setzt den Status des Zahlungsautrags auf ausgeloest. Danach kann er nicht mehr veraendert "
			+
			"werden")
	@Nullable
	@PUT
	@Path("/ausloesen/{zahlungsauftragId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE })
	public JaxZahlungsauftrag zahlungsauftragAusloesen(
		@Nonnull
		@NotNull
		@PathParam("zahlungsauftragId") JaxId zahlungsauftragJAXPId
	) throws MimeTypeParseException {

		requireNonNull(zahlungsauftragJAXPId.getId());
		String zahlungsauftragId = converter.toEntityId(zahlungsauftragJAXPId);

		final Zahlungsauftrag zahlungsauftrag = zahlungService
			.zahlungsauftragAusloesen(zahlungsauftragId);

		//Force creation and saving of ZahlungsFile Pain001
		generatedDokumentService.createZahlungsFiles(zahlungsauftrag);

		return converter.zahlungsauftragToJAX(zahlungsauftrag, false);
	}

	@Operation(summary = "Erstellt einen neue Zahlungsauftrag")
	@Nullable
	@GET
	@Path("/create")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
		SACHBEARBEITER_GEMEINDE })
	public Response createZahlung(
		@QueryParam("zahlungslaufTyp") String sZahlungslaufTyp,
		@QueryParam("gemeindeId") String gemeindeId,
		@QueryParam("faelligkeitsdatum") String stringFaelligkeitsdatum,
		@QueryParam("beschrieb") String beschrieb,
		@QueryParam("auszahlungInZukunft") Boolean auszahlungInZukunft,
		@Nullable @QueryParam("datumGeneriert") String stringDatumGeneriert,
		@Context UriInfo uriInfo
	) throws EbeguRuntimeException {

		LOGGER.info(
			"Zahlungsauftrag abgefordert für zahlungslaufTyp: {}, gemeindeId: {}, faelligkeitsdatum: {}, auszahlungInZukunft: {}, "
				+ "datumGeneriert: {}",
			sZahlungslaufTyp,
			gemeindeId,
			stringFaelligkeitsdatum,
			auszahlungInZukunft,
			stringDatumGeneriert
		);

		ZahlungslaufTyp zahlungslaufTyp = ZahlungslaufTyp.valueOf(
			sZahlungslaufTyp
		);
		LocalDate faelligkeitsdatum = DateUtil.parseStringToDateOrReturnNow(
			stringFaelligkeitsdatum
		);

		Gemeinde gemeinde = gemeindeService.findGemeinde(gemeindeId)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"createZahlung",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					gemeindeId
				)
			);
		// Validation before to start Job
		authorizer.checkWriteAuthorization(gemeinde);

		// Es darf nur ein Zahlungsauftrag per Gemeinde in erstellung sein
		checkNoWorkjobStartedWithGivenGemeindeId(gemeindeId);

		// Es darf immer nur ein Zahlungsauftrag im Status ENTWURF sein
		Optional<Zahlungsauftrag> lastZahlungsauftragOptional =
			zahlungService.findLastZahlungsauftrag(zahlungslaufTyp, gemeinde);
		if (lastZahlungsauftragOptional.isPresent()) {
			if (lastZahlungsauftragOptional.get().getStatus().isEntwurf()) {
				throw new EbeguRuntimeException(
					KibonLogLevel.DEBUG,
					"createZahlung called from zahlungResource",
					ErrorCodeEnum.ERROR_ZAHLUNG_ERSTELLEN
				);
			}
			// in extrem Situation kann einer Zahlungslauf blockiert sein (Server reboot, out of memory)
			// der Workjob lauft nicht mehr, der Zahlungslauf ist im Status in Erstellung und wird nicht mehr bearbeitet sein
			if (lastZahlungsauftragOptional.get().getStatus().isAngefragt()) {
				LOGGER.info(
					"Zahlungslauf mit id {} würde untergebrochen wegen einen externen Faktor, wir setzen es als Failed",
					lastZahlungsauftragOptional.get().getId()
				);
				zahlungService.setZahlungsauftragstatusHasFailed(
					lastZahlungsauftragOptional.get().getId()
				);
			}
		}

		if (stringDatumGeneriert != null) {
			validateDatumGeneriert(
				DateUtil.parseStringToDateOrReturnNow(
					stringDatumGeneriert
				).atStartOfDay()
			);
		}
		LocalDateTime datumGeneriert;
		if (stringDatumGeneriert != null) {
			datumGeneriert = DateUtil.parseStringToDateOrReturnNow(
				stringDatumGeneriert
			).atStartOfDay();
		} else {
			datumGeneriert = LocalDateTime.now();
		}
		Zahlungsauftrag zahlungsauftrag = zahlungService
			.createEmptyZahlungsauftrag(
				zahlungslaufTyp,
				gemeindeId,
				faelligkeitsdatum,
				beschrieb,
				auszahlungInZukunft,
				datumGeneriert,
				principalBean.getMandant()
			);

		Workjob workjob = createWorkjobForZahlungslauf(uriInfo, gemeindeId);
		workjob = workjobZahlungslaufService.startZahlungslaufWorkjob(
			zahlungslaufTyp,
			gemeindeId,
			auszahlungInZukunft,
			zahlungsauftrag.getId(),
			workjob
		);
		LOGGER.info(
			"Zahlungsauftrag erstellung gestartet für gemeinde: {}",
			gemeinde.getName()
		);

		return Response.accepted(Map.of("workjobId", workjob.getId())).build();
	}

	private void checkNoWorkjobStartedWithGivenGemeindeId(
		@Nonnull String gemeindeId
	) throws EbeguRuntimeException {
		List<Workjob> workjobs = workjobService.getWorkjobsWithParams(
			gemeindeId
		);
		if (!workjobs.isEmpty()) {
			Optional<Workjob> workjobOpt = workjobs.stream()
				.filter(
					workjob -> workjob.getStatus()
						.equals(BatchJobStatus.RUNNING)
						|| workjob.getStatus().equals(BatchJobStatus.REQUESTED)
				)
				.findAny();
			if (workjobOpt.isPresent()) {
				throw new EbeguRuntimeException(
					KibonLogLevel.DEBUG,
					"createZahlung called from zahlungResource",
					ErrorCodeEnum.ERROR_ZAHLUNG_ALREADY_REQUESTED
				);
			}
		}
	}

	@Nonnull
	private Workjob createWorkjobForZahlungslauf(
		UriInfo uriInfo,
		String gemeindeId
	) {
		Workjob workJob = new Workjob();
		workJob.setWorkJobType(WorkJobType.ZAHLUNGSLAUF);
		workJob.setStartinguser(principalBean.getPrincipal().getName());
		workJob.setRequestURI(uriInfo.getRequestUri().toString());
		String param = gemeindeId;
		workJob.setParams(param);
		return workJob;
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/status/{id}")
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
		SACHBEARBEITER_GEMEINDE })
	public Response status(@PathParam("id") String id) {
		Workjob workjob = workjobService.findById(id);

		return Response.ok(
			Map.of(
				"status",
				workjob != null ? workjob.getStatus().toString() : "FAILED"
			)
		).build();
	}

	private void validateDatumGeneriert(LocalDateTime datumGeneriert) {
		if (LocalDate.now().atStartOfDay().isAfter(datumGeneriert)) {
			throw new EbeguRuntimeException(
				"validateDatumGeneriert",
				ErrorCodeEnum.ERROR_GENERIERT_DATUM_MUSS_IN_ZUKUNFT_LIEGEN
			);
		}
	}

	@Operation(summary = "Aktualisiert einen Zahlungsauftrag")
	@Nullable
	@GET
	@Path("/update")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE })
	public JaxZahlungsauftrag updateZahlung(
		@QueryParam("beschrieb") String beschrieb,
		@QueryParam("faelligkeitsdatum") String stringFaelligkeitsdatum,
		@QueryParam("id") String id
	) throws EbeguRuntimeException {

		LocalDate faelligkeitsdatum = DateUtil.parseStringToDateOrReturnNow(
			stringFaelligkeitsdatum
		);
		final Zahlungsauftrag zahlungsauftragUpdated =
			zahlungService.zahlungsauftragAktualisieren(
				id,
				faelligkeitsdatum,
				beschrieb
			);
		return converter.zahlungsauftragToJAX(zahlungsauftragUpdated, false);
	}

	@Operation(
		summary = "Setzt eine Zahlung eines Zahlungsauftrags auf bestaetigt")
	@Nullable
	@PUT
	@Path("/bestaetigen/{zahlungId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION,
		ADMIN_TRAEGERSCHAFT,
		SACHBEARBEITER_TRAEGERSCHAFT })
	public JaxZahlung zahlungBestaetigen(
		@Nonnull @NotNull @PathParam("zahlungId") JaxId zahlungJAXPId
	) {

		requireNonNull(zahlungJAXPId.getId());
		String zahlungId = converter.toEntityId(zahlungJAXPId);

		final Zahlung zahlung = zahlungService.zahlungBestaetigen(zahlungId);
		return converter.zahlungToJAX(zahlung);
	}
}
