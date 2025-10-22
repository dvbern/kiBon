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

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.abweichungen.AbweichungInitializingUtil;
import ch.dvbern.ebegu.api.converter.gesuch.betreuung.JaxBetreuungAnmeldungPlatzConverter;
import ch.dvbern.ebegu.api.dtos.JaxAnmeldungDTO;
import ch.dvbern.ebegu.api.dtos.JaxBetreuung;
import ch.dvbern.ebegu.api.dtos.JaxBetreuungspensumAbweichung;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.resource.util.BetreuungUtil;
import ch.dvbern.ebegu.api.resource.util.ResourceHelper;
import ch.dvbern.ebegu.einstellung.EinstellungService;
import ch.dvbern.ebegu.entities.AnmeldungFerieninsel;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.BetreuungspensumAbweichung;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.containers.PensumUtil;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.DossierService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.KindService;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.Validate;
import org.eclipse.microprofile.openapi.annotations.Operation;

import static ch.dvbern.ebegu.einstellung.EinstellungKey.OEFFNUNGSTAGE_MITTAGSTISCH;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.JURIST;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * REST Resource fuer Betreuungen. Betreuung = ein Kind in einem Betreuungsangebot bei einer Institution.
 */
@Path("betreuungen")
@Stateless
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
@NoArgsConstructor
public class BetreuungResource {

	private static final String KIND_CONTAINER_ID_INVALID =
		"KindContainerId invalid: ";
	private BetreuungService betreuungService;
	private KindService kindService;
	private DossierService dossierService;
	private JaxBetreuungAnmeldungPlatzConverter converter;
	private ResourceHelper resourceHelper;
	private GesuchService gesuchService;
	private EinstellungService einstellungService;

	@Inject
	public BetreuungResource(
		BetreuungService betreuungService,
		KindService kindService,
		DossierService dossierService,
		JaxBetreuungAnmeldungPlatzConverter converter,
		ResourceHelper resourceHelper,
		GesuchService gesuchService,
		EinstellungService einstellungService
	) {
		this.betreuungService = betreuungService;
		this.kindService = kindService;
		this.dossierService = dossierService;
		this.converter = converter;
		this.resourceHelper = resourceHelper;
		this.gesuchService = gesuchService;
		this.einstellungService = einstellungService;
	}

	@Operation(summary = "Speichert eine Betreuung in der Datenbank")
	@Nonnull
	@PUT
	@Path("/betreuung/{abwesenheit}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(
		{ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
			SACHBEARBEITER_GEMEINDE,
			ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT,
			ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION,
			GESUCHSTELLER, SACHBEARBEITER_TS, ADMIN_TS, ADMIN_SOZIALDIENST,
			SACHBEARBEITER_SOZIALDIENST }
	)
	public JaxBetreuung saveBetreuung(
		@Nonnull @NotNull @Valid JaxBetreuung betreuungJAXP,
		@Nonnull @NotNull @PathParam("abwesenheit") Boolean abwesenheit,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {

		Objects.requireNonNull(betreuungJAXP.getKindId());

		Optional<KindContainer> kind = kindService.findKind(
			betreuungJAXP.getKindId()
		);
		if (kind.isPresent()) {
			KindContainer kindContainer = kind.get();
			BetreuungsangebotTyp betreuungsangebotTyp =
				betreuungJAXP.getInstitutionStammdaten()
					.getBetreuungsangebotTyp();
			switch (betreuungsangebotTyp) {
			case TAGESSCHULE:
				return savePlatzAnmeldungTagesschule(
					betreuungJAXP,
					kindContainer
				);
			case FERIENINSEL:
				return savePlatzAnmeldungFerieninsel(
					betreuungJAXP,
					kindContainer
				);
			default:
				return savePlatzBetreuung(
					betreuungJAXP,
					kindContainer,
					abwesenheit
				);
			}
		}
		throw new EbeguEntityNotFoundException(
			"saveBetreuung",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			KIND_CONTAINER_ID_INVALID + betreuungJAXP.getKindId()
		);
	}

	private JaxBetreuung savePlatzBetreuung(
		@Nonnull JaxBetreuung betreuungJAXP,
		@Nonnull KindContainer kindContainer,
		Boolean abwesenheit
	) {
		if (BetreuungUtil.hasDuplicateBetreuung(
			betreuungJAXP,
			kindContainer.getBetreuungen()
		)) {
			throw new EbeguRuntimeException(
				KibonLogLevel.NONE,
				"savePlatzBetreuung",
				ErrorCodeEnum.ERROR_DUPLICATE_BETREUUNG
			);
		}
		Betreuung convertedBetreuung = converter.betreuungToStoreableEntity(
			betreuungJAXP
		);
		resourceHelper.assertGesuchStatusForBenutzerRole(
			kindContainer.getGesuch(),
			convertedBetreuung
		);
		convertedBetreuung.setKind(kindContainer);

		Betreuung persistedBetreuung = betreuungService.saveBetreuung(
			convertedBetreuung,
			abwesenheit,
			null
		);
		return converter.betreuungToJAX(persistedBetreuung);
	}

	private JaxBetreuung savePlatzAnmeldungTagesschule(
		@Nonnull JaxBetreuung betreuungJAXP,
		@Nonnull KindContainer kindContainer
	) {
		if (BetreuungUtil.hasDuplicateAnmeldungTagesschule(
			betreuungJAXP,
			kindContainer.getAnmeldungenTagesschule()
		)) {
			throw new EbeguRuntimeException(
				KibonLogLevel.NONE,
				"savePlatzAnmeldungTagesschule",
				ErrorCodeEnum.ERROR_DUPLICATE_BETREUUNG
			);
		}
		AnmeldungTagesschule converted = converter
			.anmeldungTagesschuleToStoreableEntity(betreuungJAXP);
		resourceHelper.assertGesuchStatusForBenutzerRole(
			kindContainer.getGesuch(),
			converted
		);

		converted.setKind(kindContainer);
		if (converted.isKeineDetailinformationen()) {
			// eine Anmeldung ohne Detailinformationen muss immer als Uebernommen gespeichert werden
			converted.setBetreuungsstatus(
				Betreuungsstatus.SCHULAMT_ANMELDUNG_UEBERNOMMEN
			);
		}

		AnmeldungTagesschule persisted = betreuungService
			.saveAnmeldungTagesschule(converted);
		return converter.anmeldungTagesschuleToJAX(persisted);
	}

	private JaxBetreuung savePlatzAnmeldungFerieninsel(
		@Nonnull JaxBetreuung betreuungJAXP,
		@Nonnull KindContainer kindContainer
	) {
		if (BetreuungUtil.hasDuplicateAnmeldungFerieninsel(
			betreuungJAXP,
			kindContainer.getAnmeldungenFerieninsel()
		)) {
			throw new EbeguRuntimeException(
				KibonLogLevel.NONE,
				"savePlatzAnmeldungFerieninsel",
				ErrorCodeEnum.ERROR_DUPLICATE_BETREUUNG
			);
		}
		AnmeldungFerieninsel converted = converter
			.anmeldungFerieninselToStoreableEntity(betreuungJAXP);
		resourceHelper.assertGesuchStatusForBenutzerRole(
			kindContainer.getGesuch(),
			converted
		);
		converted.setKind(kindContainer);

		AnmeldungFerieninsel persisted = betreuungService
			.saveAnmeldungFerieninsel(converted);
		return converter.anmeldungFerieninselToJAX(persisted);
	}

	@Operation(summary = "Speichert eine Abwesenheit in der Datenbank.")
	@Nonnull
	@PUT
	@Path("/all/{abwesenheit}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(
		{ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
			SACHBEARBEITER_GEMEINDE,
			ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT,
			ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST,
			ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, GESUCHSTELLER,
			SACHBEARBEITER_TS, ADMIN_TS }
	)
	public List<JaxBetreuung> saveAbwesenheiten(
		List<JaxBetreuung> betreuungenJAXP,
		@Nonnull @PathParam("abwesenheit") Boolean abwesenheit,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		Validate.notNull(abwesenheit, "abwesenheit may not be null");
		Validate.notNull(betreuungenJAXP, "betreuungenJAXP may not be null");
		Validator validator = Validation.byDefaultProvider()
			.configure()
			.buildValidatorFactory()
			.getValidator();
		betreuungenJAXP.forEach(validator::validate);

		if (!betreuungenJAXP.isEmpty()) {
			Optional.ofNullable(betreuungenJAXP.get(0).getGesuchId())
				.map(
					gesuchId -> gesuchService.findGesuch(gesuchId)
						.orElseThrow(
							() -> new EbeguEntityNotFoundException(
								"saveAbwesenheiten",
								gesuchId
							)
						)
				)
				.ifPresent(
					gesuch -> resourceHelper
						.assertGesuchStatusForBenutzerRole(gesuch)
				);
		}

		return betreuungenJAXP.stream()
			.map(betreuungJAXP -> {
				Betreuung convertedBetreuung = converter
					.betreuungToStoreableEntity(betreuungJAXP);
				Betreuung persistedBetreuung = betreuungService
					.saveBetreuung(
						convertedBetreuung,
						abwesenheit,
						null
					);

				return converter.betreuungToJAX(persistedBetreuung);
			})
			.collect(Collectors.toList());
	}

	@Operation(
		summary = "Betreuungsplatzanfrage wird durch die Institution abgelehnt"
	)
	@Nonnull
	@PUT
	@Path("/abweisen")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(
		{ SUPER_ADMIN, ADMIN_TRAEGERSCHAFT,
			SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_INSTITUTION,
			SACHBEARBEITER_INSTITUTION }
	)
	public JaxBetreuung betreuungPlatzAbweisen(
		@Nonnull @NotNull @Valid JaxBetreuung betreuungJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {

		Objects.requireNonNull(betreuungJAXP.getId());
		Objects.requireNonNull(betreuungJAXP.getKindId());

		// Sicherstellen, dass der Status des Server-Objektes genau dem erwarteten Status entspricht
		resourceHelper.assertBetreuungStatusEqual(
			betreuungJAXP.getId(),
			Betreuungsstatus.WARTEN
		);

		Betreuung convertedBetreuung = converter.betreuungToStoreableEntity(
			betreuungJAXP
		);

		// Sicherstellen, dass das dazugehoerige Gesuch ueberhaupt noch editiert werden darf fuer meine Rolle
		resourceHelper.assertGesuchStatusForBenutzerRole(
			convertedBetreuung.getKind().getGesuch()
		);
		Betreuung persistedBetreuung = betreuungService.betreuungPlatzAbweisen(
			convertedBetreuung,
			null
		);

		return converter.betreuungToJAX(persistedBetreuung);
	}

	@Operation(
		summary = "Betreuungsplatzanfrage wird durch die Institution bestätigt"
	)
	@Nonnull
	@PUT
	@Path("/bestaetigen")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(
		{ SUPER_ADMIN, ADMIN_TRAEGERSCHAFT,
			SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_INSTITUTION,
			SACHBEARBEITER_INSTITUTION }
	)
	public JaxBetreuung betreuungPlatzBestaetigen(
		@Nonnull @NotNull @Valid JaxBetreuung betreuungJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {

		Objects.requireNonNull(betreuungJAXP.getId());
		Objects.requireNonNull(betreuungJAXP.getKindId());

		// Sicherstellen, dass der Status des Server-Objektes genau dem erwarteten Status entspricht
		resourceHelper.assertBetreuungStatusEqual(
			betreuungJAXP.getId(),
			Betreuungsstatus.WARTEN
		);

		Betreuung convertedBetreuung = converter.betreuungToStoreableEntity(
			betreuungJAXP
		);

		// Sicherstellen, dass die Institution ist nicht eingeladen zu vermeiden das die Bankkontodaten fehlen
		resourceHelper.assertInstitutionNichtEingeladet(
			convertedBetreuung.getInstitutionStammdaten().getInstitution()
		);

		// Sicherstellen, dass das dazugehoerige Gesuch ueberhaupt noch editiert werden darf fuer meine Rolle
		resourceHelper.assertGesuchStatusForBenutzerRole(
			convertedBetreuung.getKind().getGesuch()
		);
		Betreuung persistedBetreuung = betreuungService
			.betreuungPlatzBestaetigen(convertedBetreuung, null);

		return converter.betreuungToJAX(persistedBetreuung);
	}

	@Operation(
		summary = "Sucht die Betreuung mit der übergebenen Id in der Datenbank. Dabei wird geprüft, ob der "
			+
			"eingeloggte Benutzer für die gesuchte Betreuung berechtigt ist"
	)
	@Nullable
	@GET
	@Path("/{betreuungId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(
		{ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
			SACHBEARBEITER_GEMEINDE, JURIST, REVISOR,
			ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT,
			ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, GESUCHSTELLER,
			ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_TS, SACHBEARBEITER_TS,
			ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST }
	)
	public JaxBetreuung findBetreuung(
		@Nonnull
		@Valid
		@NotNull
		@PathParam("betreuungId") JaxId betreuungJAXPId
	) {
		Betreuung betreuungToReturn = betreuungService.findBetreuung(
			betreuungJAXPId.getId()
		)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"findBetreuung",
					betreuungJAXPId.getId()
				)
			);

		return converter.betreuungToJAX(betreuungToReturn);
	}

	@Operation(
		summary = "Löscht die Betreuung mit der übergebenen Id in der Datenbank. Dabei wird geprüft, ob der "
			+
			"eingeloggte Benutzer für die gesuchte Betreuung berechtigt ist"
	)
	@Nullable
	@DELETE
	@Path("/{betreuungId}")
	@Consumes(MediaType.WILDCARD)
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	@RolesAllowed(
		{ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
			SACHBEARBEITER_GEMEINDE,
			ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT,
			ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION,
			GESUCHSTELLER, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_SOZIALDIENST,
			SACHBEARBEITER_SOZIALDIENST }
	)
	public Response removeBetreuung(
		@Nonnull
		@Valid
		@NotNull
		@PathParam("betreuungId") JaxId betreuungJAXPId,
		@Context HttpServletResponse response
	) {

		String id = betreuungJAXPId.getId();
		Optional<Betreuung> betreuung = betreuungService.findBetreuung(id);

		if (betreuung.isPresent()) {
			// Sicherstellen, dass das dazugehoerige Gesuch ueberhaupt noch editiert werden darf fuer meine Rolle
			resourceHelper.assertGesuchStatusForBenutzerRole(
				betreuung.get().extractGesuch()
			);
			betreuungService.removeBetreuung(
				converter.toEntityId(betreuungJAXPId)
			);
			return Response.ok().build();
		}

		var anmeldung = betreuungService.findAnmeldung(id)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"removeBetreuung",
					id
				)
			);

		resourceHelper.assertGesuchStatusForBenutzerRole(
			anmeldung.extractGesuch()
		);
		betreuungService.removeAnmeldung(converter.toEntityId(betreuungJAXPId));

		return Response.ok().build();
	}

	@Operation(
		summary = "Sucht alle verfügten Betreuungen aus allen Gesuchsperioden, welche zum übergebenen "
			+ "Dossier"
			+
			"vorhanden sind. Es werden nur diejenigen Betreuungen zurückgegeben, für welche der eingeloggte Benutzer "
			+
			"berechtigt ist."
	)
	@GET
	@Path("/alleBetreuungen/{dossierId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(
		{ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
			SACHBEARBEITER_GEMEINDE, JURIST, REVISOR,
			ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT,
			ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, GESUCHSTELLER,
			ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_TS, SACHBEARBEITER_TS,
			ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST }
	)
	public Response findAllBetreuungenWithVerfuegungFromFall(
		@Nonnull @NotNull @Valid @PathParam("dossierId") JaxId jaxDossierId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		String id = converter.toEntityId(jaxDossierId);
		Dossier dossier = dossierService.findDossier(id)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"findAllBetreuungenWithVerfuegungFromFall",
					id
				)
			);

		Collection<Betreuung> betreuungCollection = betreuungService
			.findAllBetreuungenWithVerfuegungForDossier(dossier);
		Collection<JaxBetreuung> jaxBetreuungList = converter
			.betreuungListToJax(betreuungCollection);

		return Response.ok(jaxBetreuungList).build();
	}

	@Operation(
		summary = "Erstelle eine Schulamt Anmeldung vom GS-Dashboard in der Datenbank"
	)
	@Nonnull
	@PUT
	@Path("/anmeldung/create/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(
		{ SUPER_ADMIN, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
			ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT,
			ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST,
			ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, GESUCHSTELLER,
			SACHBEARBEITER_TS, ADMIN_TS }
	)
	public Response createAnmeldung(
		@Nonnull @NotNull @Valid JaxAnmeldungDTO jaxAnmeldungDTO,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {

		KindContainer kindContainer = kindService.findKind(
			jaxAnmeldungDTO.getKindContainerId()
		)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"createAnmeldung",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					KIND_CONTAINER_ID_INVALID
						+ jaxAnmeldungDTO.getKindContainerId()
				)
			);

		if (jaxAnmeldungDTO.getAdditionalKindQuestions()
			&& !kindContainer.getKindJA()
				.getFamilienErgaenzendeBetreuung()) {
			kindContainer.getKindJA().setFamilienErgaenzendeBetreuung(true);
			kindContainer.getKindJA()
				.setEinschulungTyp(jaxAnmeldungDTO.getEinschulungTyp());
			kindContainer.getKindJA()
				.setSprichtAmtssprache(
					jaxAnmeldungDTO.getSprichtAmtssprache()
				);
			kindService.saveKind(kindContainer, null);
		}

		JaxBetreuung jaxBetreuung = jaxAnmeldungDTO.getBetreuung();
		BetreuungsangebotTyp betreuungsangebotTyp = jaxBetreuung
			.getInstitutionStammdaten()
			.getBetreuungsangebotTyp();
		switch (betreuungsangebotTyp) {
		case TAGESSCHULE:
			savePlatzAnmeldungTagesschule(jaxBetreuung, kindContainer);
			break;
		case FERIENINSEL:
			savePlatzAnmeldungFerieninsel(jaxBetreuung, kindContainer);
			break;
		default:
			throw new EbeguRuntimeException(
				"createAnmeldung",
				"CreateAnmeldung ist nur für Tagesschulen und "
					+ "Ferieninseln möglich"
			);
		}

		return Response.ok().build();
	}

	@Operation(
		summary = "Speichert die Abweichungen einer Betreuung in der Datenbank"
	)
	@PUT
	@Path("/betreuung/abweichungen/{betreuungId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(
		{ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
			SACHBEARBEITER_GEMEINDE,
			ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT,
			ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST,
			ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, GESUCHSTELLER,
			SACHBEARBEITER_TS, ADMIN_TS }
	)
	public Collection<JaxBetreuungspensumAbweichung> saveBetreuungspensumAbweichungen(
		@Nonnull
		@NotNull
		@Valid
		@PathParam("betreuungId") JaxId betreuungId,
		@Nonnull @NotNull @Valid JaxBetreuung betreuungJax,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {

		Betreuung betreuung = betreuungService.findBetreuung(
			betreuungId.getId()
		)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"saveBetreuungspensumAbweichungen",
					betreuungId.getId()
				)
			);

		BigDecimal multiplier = betreuungService.getMultiplierForAbweichnungen(
			betreuung
		);
		List<BetreuungspensumAbweichung> trustedAbweichungen =
			AbweichungInitializingUtil.fillAbweichungen(multiplier, betreuung);

		List<JaxBetreuungspensumAbweichung> jaxAbweichungen = betreuungJax
			.getBetreuungspensumAbweichungen();
		Set<BetreuungspensumAbweichung> toStore =
			converter.betreuungspensumAbweichungenToEntity(
				jaxAbweichungen,
				trustedAbweichungen
			);
		converter.addAbweichungenToBetreuung(toStore, betreuung);
		BigDecimal oeffnungstageMittagstisch = einstellungService
			.getEinstellungAsBigDecimal(
				OEFFNUNGSTAGE_MITTAGSTISCH,
				betreuung
			);
		PensumUtil.transformBetreuungsPensumContainers(
			betreuung.asAbweichungPensumContainer(),
			oeffnungstageMittagstisch
		);

		betreuungService.saveBetreuung(betreuung, false, null);
		return converter.betreuungspensumAbweichungenToJax(betreuung);
	}

	@Operation(
		summary = "Gibt für jeden Monat in einer Gesuchsperiode eine BetreuungspensumAbweichung zurück."
	)
	@GET
	@Path("/betreuung/abweichungen/{betreuungId}/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(
		{ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
			SACHBEARBEITER_GEMEINDE, JURIST, REVISOR,
			ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT,
			ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, GESUCHSTELLER,
			ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_TS, SACHBEARBEITER_TS,
			ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST, }
	)
	public Collection<JaxBetreuungspensumAbweichung> findBetreuungspensumAbweichungen(
		@Nonnull @NotNull @PathParam("betreuungId") JaxId betreuungId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {

		Betreuung betreuung = betreuungService.findBetreuung(
			betreuungId.getId()
		)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"findBetreuungspensumAbweichungen",
					betreuungId.getId()
				)
			);

		return converter.betreuungspensumAbweichungenToJax(betreuung);
	}
}
