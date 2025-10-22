package ch.dvbern.ebegu.api.resource.betreuung;

import java.util.Objects;

import javax.annotation.Nonnull;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.gesuch.betreuung.JaxBetreuungAnmeldungPlatzConverter;
import ch.dvbern.ebegu.api.dtos.JaxBetreuung;
import ch.dvbern.ebegu.api.resource.util.BetreuungUtil;
import ch.dvbern.ebegu.api.resource.util.ResourceHelper;
import ch.dvbern.ebegu.entities.AbstractAnmeldung;
import ch.dvbern.ebegu.entities.AnmeldungFerieninsel;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.KindService;
import ch.dvbern.ebegu.services.VerfuegungService;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

@Path("anmeldung")
@Stateless
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
@NoArgsConstructor
public class AnmeldungResource {

	private static final String KIND_CONTAINER_ID_INVALID =
		"KindContainerId invalid: ";

	private JaxBetreuungAnmeldungPlatzConverter converter;
	private ResourceHelper resourceHelper;
	private BetreuungService betreuungService;
	private KindService kindService;
	private VerfuegungService verfuegungService;

	@Inject
	public AnmeldungResource(
		JaxBetreuungAnmeldungPlatzConverter converter,
		ResourceHelper resourceHelper,
		BetreuungService betreuungService,
		KindService kindService,
		VerfuegungService verfuegungService
	) {
		this.converter = converter;
		this.resourceHelper = resourceHelper;
		this.betreuungService = betreuungService;
		this.kindService = kindService;
		this.verfuegungService = verfuegungService;
	}

	@Operation(
		summary = "Schulamt-Anmeldung wird durch die Institution abgelehnt")
	@Nonnull
	@PUT
	@Path("/ablehnen")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_TRAEGERSCHAFT,
		SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		SACHBEARBEITER_INSTITUTION, SACHBEARBEITER_TS, ADMIN_TS,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE })
	public JaxBetreuung anmeldungSchulamtAblehnen(
		@Nonnull @NotNull @Valid JaxBetreuung betreuungJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {

		Objects.requireNonNull(betreuungJAXP.getId());
		Objects.requireNonNull(betreuungJAXP.getKindId());

		// Sicherstellen, dass der Status des Server-Objektes genau dem erwarteten Status entspricht
		// Anmeldungen ablehnen kann man entweder im Status SCHULAMT_ANMELDUNG_AUSGELOEST oder
		// SCHULAMT_FALSCHE_INSTITUTION
		resourceHelper.assertBetreuungStatusEqual(
			betreuungJAXP.getId(),
			Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST,
			Betreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION
		);

		checkDuplicatedAnmeldung(betreuungJAXP);

		AbstractAnmeldung convertedBetreuung = converter.platzToStoreableEntity(
			betreuungJAXP
		);
		// Sicherstellen, dass das dazugehoerige Gesuch ueberhaupt noch editiert werden darf fuer meine Rolle
		resourceHelper.assertGesuchStatusForBenutzerRole(
			convertedBetreuung.getKind().getGesuch(),
			convertedBetreuung
		);
		AbstractAnmeldung persistedBetreuung = betreuungService
			.anmeldungSchulamtAblehnen(convertedBetreuung);

		return converter.platzToJAX(persistedBetreuung);
	}

	private void checkDuplicatedAnmeldung(JaxBetreuung betreuungJAXP) {
		KindContainer kindContainer = kindService.findKind(
			Objects.requireNonNull(betreuungJAXP.getKindId())
		)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"checkDuplicatedAnmeldung",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					KIND_CONTAINER_ID_INVALID
						+ betreuungJAXP.getKindId()
				)
			);

		if (BetreuungUtil.hasDuplicateAnmeldungTagesschule(
			betreuungJAXP,
			kindContainer.getAnmeldungenTagesschule()
		)) {
			throw new EbeguRuntimeException(
				KibonLogLevel.NONE,
				"checkDuplicatedAnmeldung",
				ErrorCodeEnum.ERROR_DUPLICATE_BETREUUNG
			);
		}
	}

	@Operation(
		summary = "Schulamt-Anmeldung fuer falsche Institution gestellt")
	@Nonnull
	@PUT
	@Path("/falscheInstitution")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_TRAEGERSCHAFT,
		SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		SACHBEARBEITER_INSTITUTION, SACHBEARBEITER_TS, ADMIN_TS,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE })
	public JaxBetreuung anmeldungSchulamtFalscheInstitution(
		@Nonnull @NotNull @Valid JaxBetreuung betreuungJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {

		Objects.requireNonNull(betreuungJAXP.getId());
		Objects.requireNonNull(betreuungJAXP.getKindId());

		// Sicherstellen, dass der Status des Server-Objektes genau dem erwarteten Status entspricht
		resourceHelper.assertBetreuungStatusEqual(
			betreuungJAXP.getId(),
			Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST
		);

		AbstractAnmeldung convertedBetreuung = converter.platzToStoreableEntity(
			betreuungJAXP
		);
		// Sicherstellen, dass das dazugehoerige Gesuch ueberhaupt noch editiert werden darf fuer meine Rolle
		resourceHelper.assertGesuchStatusForBenutzerRole(
			convertedBetreuung.getKind().getGesuch(),
			convertedBetreuung
		);
		AbstractAnmeldung persistedBetreuung = betreuungService
			.anmeldungSchulamtFalscheInstitution(convertedBetreuung);

		return converter.platzToJAX(persistedBetreuung);
	}

	@Operation(
		summary = "Schulamt-Anmeldung wird durch die Gemeinde storniert")
	@Nonnull
	@PUT
	@Path("/stornieren")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, SACHBEARBEITER_TS, ADMIN_TS, ADMIN_GEMEINDE,
		SACHBEARBEITER_GEMEINDE })
	public JaxBetreuung anmeldungSchulamtStornieren(
		@Nonnull @NotNull @Valid JaxBetreuung betreuungJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {

		Objects.requireNonNull(betreuungJAXP.getId());
		Objects.requireNonNull(betreuungJAXP.getKindId());

		// Sicherstellen, dass der Status des Server-Objektes genau dem erwarteten Status entspricht
		// Anmeldungen stornieren kann man entweder im Status SCHULAMT_ANMELDUNG_AUSGELOEST oder
		// SCHULAMT_FALSCHE_INSTITUTION
		resourceHelper.assertBetreuungStatusEqual(
			betreuungJAXP.getId(),
			Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST,
			Betreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION
		);

		AbstractAnmeldung convertedBetreuung = converter.platzToStoreableEntity(
			betreuungJAXP
		);
		// Sicherstellen, dass das dazugehoerige Gesuch ueberhaupt noch editiert werden darf fuer meine Rolle
		resourceHelper.assertGesuchStatusForBenutzerRole(
			convertedBetreuung.getKind().getGesuch(),
			convertedBetreuung
		);
		AbstractAnmeldung persistedBetreuung = betreuungService
			.anmeldungSchulamtStornieren(convertedBetreuung);

		return converter.platzToJAX(persistedBetreuung);
	}

	@Operation(
		summary = "Schulamt-Anmeldung wird durch die Institution bestätigt aber die Finanzeil Situation ist "
			+ "noch nicht geprueft")
	@Nonnull
	@PUT
	@Path("/akzeptieren")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_TRAEGERSCHAFT,
		SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		SACHBEARBEITER_INSTITUTION, SACHBEARBEITER_TS, ADMIN_TS,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE })
	public JaxBetreuung anmeldungSchulamtModuleAkzeptieren(
		@Nonnull @NotNull @Valid JaxBetreuung betreuungJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {

		Objects.requireNonNull(betreuungJAXP.getId());
		Objects.requireNonNull(betreuungJAXP.getKindId());

		// Sicherstellen, dass der Status des Server-Objektes genau dem erwarteten Status entspricht
		resourceHelper.assertBetreuungStatusEqual(
			betreuungJAXP.getId(),
			Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST
		);

		AbstractAnmeldung convertedBetreuung = converter.platzToStoreableEntity(
			betreuungJAXP
		);
		// Sicherstellen, dass das dazugehoerige Gesuch ueberhaupt noch editiert werden darf fuer meine Rolle
		resourceHelper.assertGesuchStatusForBenutzerRole(
			convertedBetreuung.getKind().getGesuch(),
			convertedBetreuung
		);

		if (convertedBetreuung.getBetreuungsangebotTyp().isTagesschule()) {
			if (betreuungJAXP.getBelegungTagesschule() == null
				|| betreuungJAXP.getBelegungTagesschule()
					.getBelegungTagesschuleModule()
					.isEmpty()) {
				throw new EbeguRuntimeException(
					KibonLogLevel.ERROR,
					betreuungJAXP.getId(),
					ErrorCodeEnum.ERROR_ANMELDUNG_KEINE_MODULE
				);
			}
			return converter.platzToJAX(
				betreuungService.anmeldungSchulamtModuleAkzeptieren(
					convertedBetreuung
				)
			);
		}

		if (betreuungJAXP.getBelegungFerieninsel() == null
			|| betreuungJAXP.getBelegungFerieninsel().getTage().isEmpty()) {
			throw new EbeguRuntimeException(
				KibonLogLevel.ERROR,
				betreuungJAXP.getId(),
				ErrorCodeEnum.ERROR_ANMELDUNG_KEINE_MODULE
			);
		}
		AnmeldungFerieninsel convertedAnmeldungFerieninsel =
			(AnmeldungFerieninsel) convertedBetreuung;
		return converter.platzToJAX(
			verfuegungService.anmeldungFerieninselUebernehmen(
				convertedAnmeldungFerieninsel
			)
		);
	}
}
