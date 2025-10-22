/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.property.resource;

import java.net.URI;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.dtos.JaxPublicAppConfig;
import ch.dvbern.ebegu.api.property.converter.JaxConfigurationConverter;
import ch.dvbern.ebegu.api.property.dto.JaxApplicationProperties;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.einstellung.ApplicationProperty;
import ch.dvbern.ebegu.einstellung.ApplicationPropertyKey;
import ch.dvbern.ebegu.einstellung.ApplicationPropertyService;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.MandantService;
import ch.dvbern.ebegu.util.mandant.MandantCookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Resource fuer ApplicationProperties
 */
@Path("application-properties")
@Stateless
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class ApplicationPropertyResource {

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@Inject
	private JaxConfigurationConverter converter;

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private MandantService mandantService;

	private static final Logger LOG = LoggerFactory.getLogger(
		ApplicationPropertyResource.class.getSimpleName()
	);

	@Nonnull
	private String readWhitelistAsString(@Nonnull Mandant mandant) {
		final Collection<String> whitelist = this.applicationPropertyService
			.readMimeTypeWhitelist(mandant);
		MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
		final List<String> extensions = whitelist.stream().map(mimetype -> {
			try {
				return allTypes.forName(mimetype).getExtension();
			} catch (MimeTypeException e) {
				LOG.error(
					"Could not find extension for mime type {}",
					mimetype
				);
				return "";
			}
		}).filter(Objects::nonNull).collect(Collectors.toList());
		return StringUtils.join(extensions, ",");
	}

	@Nonnull
	private JaxApplicationProperties getSentryEnvName(Mandant mandant) {
		Optional<ApplicationProperty> propertyFromDB =
			this.applicationPropertyService
				.readApplicationProperty(
					ApplicationPropertyKey.SENTRY_ENV,
					mandant
				);

		ApplicationProperty prop = propertyFromDB.orElseGet(() -> {
			String sentryEnv = ebeguConfiguration.getSentryEnv();
			return new ApplicationProperty(
				ApplicationPropertyKey.SENTRY_ENV,
				sentryEnv
			);
		});
		return converter.applicationPropertyToJAX(prop);
	}

	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	@Operation(summary = "Returns background Color for the current System")
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/public/background")
	@PermitAll
	public JaxApplicationProperties getBackgroundColor(
		@CookieParam(MandantCookieUtil.MANDANT_COOKIE_NAME) Cookie mandantCookie
	) {
		// getBackgroundColor muss auch erlaubt sein, wenn kein Mandant gesetzt ist. Wir brauchen dies auf der Verteiler-
		// seite der Mandanten, um herauszufinden, ob die Mandantenfähigkeit überhaupt aktiv ist
		Mandant mandant;
		if (mandantCookie == null) {
			mandant = mandantService.getMandantBern();
		} else {
			mandant = mandantService.findMandantByCookie(mandantCookie);
		}

		ApplicationProperty prop = getBackgroundColorProperty(mandant);
		return converter.applicationPropertyToJAX(prop);
	}

	@Nonnull
	private ApplicationProperty getBackgroundColorProperty(Mandant mandant) {
		Optional<ApplicationProperty> propertyFromDB =
			this.applicationPropertyService.readApplicationProperty(
				ApplicationPropertyKey.BACKGROUND_COLOR,
				mandant
			);
		ApplicationProperty prop =
			propertyFromDB.orElse(
				new ApplicationProperty(
					ApplicationPropertyKey.BACKGROUND_COLOR,
					"#FFFFFF"
				)
			);
		return prop;
	}

	@Operation(summary = "Returns all application properties")
	@Nonnull
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(SUPER_ADMIN)
	public List<JaxApplicationProperties> getAllApplicationProperties(
		@CookieParam(MandantCookieUtil.MANDANT_COOKIE_NAME) Cookie mandantCookie
	) {
		var mandant = mandantService.findMandantByCookie(mandantCookie);

		return applicationPropertyService.getAllApplicationProperties(mandant)
			.stream()
			.sorted(Comparator.comparing(o -> o.getName().name()))
			.map(ap -> converter.applicationPropertyToJAX(ap))
			.collect(Collectors.toList());
	}

	@Operation(
		summary = "Create a new ApplicationProperty with the given key and value")
	@Nullable
	@POST
	@Path("/{key}")
	@Consumes(MediaType.TEXT_PLAIN)
	@RolesAllowed(SUPER_ADMIN)
	public Response create(
		@Nonnull @NotNull @PathParam("key") String key,
		@Nonnull @NotNull String value,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response,
		@CookieParam(MandantCookieUtil.MANDANT_COOKIE_NAME) Cookie mandantCookie
	) {

		var mandant = mandantService.findMandantByCookie(mandantCookie);

		ApplicationProperty modifiedProperty =
			this.applicationPropertyService.saveOrUpdateApplicationProperty(
				Enum.valueOf(
					ApplicationPropertyKey.class,
					key
				),
				value,
				mandant
			);

		URI uri = uriInfo.getBaseUriBuilder()
			.path(ApplicationPropertyResource.class)
			.path("/" + modifiedProperty.getName())
			.build();

		return Response.created(uri)
			.entity(converter.applicationPropertyToJAX(modifiedProperty))
			.build();
	}

	@Operation(summary = "Aktualisiert ein bestehendes ApplicationProperty")
	@Nullable
	@PUT
	@Path("/{key}")
	@Consumes(MediaType.TEXT_PLAIN)
	@RolesAllowed(SUPER_ADMIN)
	public JaxApplicationProperties update(
		@Nonnull @PathParam("key") String key,
		@Nonnull @NotNull String value,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response,
		@CookieParam(MandantCookieUtil.MANDANT_COOKIE_NAME) Cookie mandantCookie
	) {
		var mandant = mandantService.findMandantByCookie(mandantCookie);

		ApplicationProperty modifiedProperty =
			this.applicationPropertyService.saveOrUpdateApplicationProperty(
				Enum.valueOf(
					ApplicationPropertyKey.class,
					key
				),
				value,
				mandant
			);

		return converter.applicationPropertyToJAX(modifiedProperty);
	}

	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	@Operation(summary = "Removes an application property")
	@Nullable
	@DELETE
	@Path("/{key}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed({ ADMIN_BG, ADMIN_GEMEINDE, SUPER_ADMIN })
	public Response remove(
		@Nonnull @PathParam("key") String keyParam,
		@Context HttpServletResponse response
	) {
		applicationPropertyService.removeApplicationProperty(
			Enum.valueOf(ApplicationPropertyKey.class, keyParam),
			principalBean.getMandant()
		);
		return Response.ok().build();
	}

	@RolesAllowed(SUPER_ADMIN)
	@Operation(summary = "Gibt den Wert des Properties zurück")
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.WILDCARD)
	@Path("/property/{key}")
	public Response getProperty(
		@Nonnull @PathParam("key") String keyParam,
		@Context HttpServletResponse response
	) {
		if (keyParam.startsWith("ebegu")) {
			return Response.ok(System.getProperty(keyParam)).build();
		}
		return Response.noContent().build();
	}

	@Operation(summary = "Single request to load public config")
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/public/all")
	@PermitAll
	public Response getPublicProperties(
		@Context HttpServletResponse response,
		@CookieParam(MandantCookieUtil.MANDANT_COOKIE_NAME) Cookie mandantCookie
	) {
		ApplicationPropertyContext applicationPropertyContext =
			new ApplicationPropertyContext();
		// getPublicProperties muss auch erlaubt sein, wenn kein Mandant gesetzt ist. Wir brauchen dies auf der Verteiler-
		// seite der Mandanten, um herauszufinden, ob die Mandantenfähigkeit überhaupt aktiv ist
		Mandant mandant;
		if (mandantCookie == null) {
			mandant = mandantService.getMandantBern();
		} else {
			mandant = mandantService.findMandantByCookie(mandantCookie);
		}

		applicationPropertyContext.setDevmode(
			ebeguConfiguration.getIsDevmode()
		);
		applicationPropertyContext.setWhitelist(readWhitelistAsString(mandant));
		applicationPropertyContext.setDummyMode(
			ebeguConfiguration.isDummyLoginEnabled(mandant)
		);
		applicationPropertyContext.setSentryEnvName(
			getSentryEnvName(mandant).getValue()
		);
		applicationPropertyContext.setBackground(
			getBackgroundColorProperty(mandant).getValue()
		);
		applicationPropertyContext.setZahlungentestmode(
			ebeguConfiguration.getIsZahlungenTestMode()
		);
		applicationPropertyContext.setPersonenSucheDisabled(
			ebeguConfiguration
				.isPersonenSucheDisabled()
		);
		applicationPropertyContext.setKitaxHost(
			ebeguConfiguration.getKitaxHost()
		);
		applicationPropertyContext.setKitaxendpoint(
			ebeguConfiguration.getKitaxEndpoint()
		);
		applicationPropertyContext.setMultimandantEnabled(
			ebeguConfiguration
				.getMultimandantEnabled()
		);
		applicationPropertyContext.setEbeguKibonAnfrageTestGuiEnabled(
			ebeguConfiguration
				.getEbeguKibonAnfrageTestGuiEnabled()
		);
		applicationPropertyContext.setTestfaelleEnabled(
			ebeguConfiguration.isTestfaelleEnabled()
		);

		applicationPropertyContext.setFerienbetreuungAktiv(
			getApplicationProperty(
				mandant,
				ApplicationPropertyKey.FERIENBETREUUNG_AKTIV
			)
		);
		applicationPropertyContext.setLastenausgleichAktiv(
			getApplicationProperty(
				mandant,
				ApplicationPropertyKey.LASTENAUSGLEICH_AKTIV
			)
		);
		applicationPropertyContext.setLastenausgleichTagesschulenAktiv(
			getApplicationProperty(
				mandant,
				ApplicationPropertyKey.LASTENAUSGLEICH_TAGESSCHULEN_AKTIV
			)
		);
		applicationPropertyContext.setGemeindeKennzahlenAktiv(
			getApplicationProperty(
				mandant,
				ApplicationPropertyKey.GEMEINDE_KENNZAHLEN_AKTIV
			)
		);
		applicationPropertyContext
			.setLastenausgleichTagesschulenAnteilZweitpruefungDe(
				getApplicationProperty(
					mandant,
					ApplicationPropertyKey.LASTENAUSGLEICH_TAGESSCHULEN_ANTEIL_ZWEITPRUEFUNG_DE
				)
			);
		applicationPropertyContext
			.setLastenausgleichTagesschulenAnteilZweitpruefungFr(
				getApplicationProperty(
					mandant,
					ApplicationPropertyKey.LASTENAUSGLEICH_TAGESSCHULEN_ANTEIL_ZWEITPRUEFUNG_FR
				)
			);
		applicationPropertyContext
			.setLastenausgleichTagesschulenAutoZweitpruefungDe(
				getApplicationProperty(
					mandant,
					ApplicationPropertyKey.LASTENAUSGLEICH_TAGESSCHULEN_AUTO_ZWEITPRUEFUNG_DE
				)
			);
		applicationPropertyContext
			.setLastenausgleichTagesschulenAutoZweitpruefungFr(
				getApplicationProperty(
					mandant,
					ApplicationPropertyKey.LASTENAUSGLEICH_TAGESSCHULEN_AUTO_ZWEITPRUEFUNG_FR
				)
			);
		applicationPropertyContext.setFerienbetreuungAnteilZweitpruefungDe(
			getApplicationProperty(
				mandant,
				ApplicationPropertyKey.FERIENBETREUUNG_ANTEIL_ZWEITPRUEFUNG_DE
			)
		);
		applicationPropertyContext.setFerienbetreuungAnteilZweitpruefungFr(
			getApplicationProperty(
				mandant,
				ApplicationPropertyKey.FERIENBETREUUNG_ANTEIL_ZWEITPRUEFUNG_FR
			)
		);
		applicationPropertyContext.setFerienbetreuungAutoZweitpruefungDe(
			getApplicationProperty(
				mandant,
				ApplicationPropertyKey.FERIENBETREUUNG_AUTO_ZWEITPRUEFUNG_DE
			)
		);
		applicationPropertyContext.setFerienbetreuungAutoZweitpruefungFr(
			getApplicationProperty(
				mandant,
				ApplicationPropertyKey.FERIENBETREUUNG_AUTO_ZWEITPRUEFUNG_FR
			)
		);
		applicationPropertyContext.setPrimaryColor(
			getApplicationProperty(
				mandant,
				ApplicationPropertyKey.PRIMARY_COLOR
			)
		);
		applicationPropertyContext.setPrimaryColorDark(
			getApplicationProperty(
				mandant,
				ApplicationPropertyKey.PRIMARY_COLOR_DARK
			)
		);
		applicationPropertyContext.setPrimaryColorLight(
			getApplicationProperty(
				mandant,
				ApplicationPropertyKey.PRIMARY_COLOR_LIGHT
			)
		);
		applicationPropertyContext.setInfomaZahlungen(
			getApplicationProperty(
				mandant,
				ApplicationPropertyKey.INFOMA_ZAHLUNGEN
			)
		);
		applicationPropertyContext.setAuszahlungAnEltern(
			getApplicationProperty(
				mandant,
				ApplicationPropertyKey.AUSZAHLUNGEN_AN_ELTERN
			)
		);
		applicationPropertyContext.setFrenchEnabled(
			getApplicationProperty(
				mandant,
				ApplicationPropertyKey.FRENCH_ENABLED
			)
		);
		applicationPropertyContext.setGeresEnabledForMandant(
			getApplicationProperty(
				mandant,
				ApplicationPropertyKey.GERES_ENABLED_FOR_MANDANT
			)
		);
		applicationPropertyContext.setSteuerschnittstelleAktivAb(
			getApplicationProperty(
				mandant,
				ApplicationPropertyKey.SCHNITTSTELLE_STEUERSYSTEME_AKTIV_AB
			)
		);
		applicationPropertyContext.setZusatzinformationenInstitution(
			getApplicationProperty(
				mandant,
				ApplicationPropertyKey.ZUSATZINFORMATIONEN_INSTITUTION
			)
		);
		applicationPropertyContext.setActivatedDemoFeatures(
			getApplicationProperty(
				mandant,
				ApplicationPropertyKey.ACTIVATED_DEMO_FEATURES
			)
		);
		applicationPropertyContext.setCheckboxAuszahlungInZukunft(
			getApplicationProperty(
				mandant,
				ApplicationPropertyKey.CHECKBOX_AUSZAHLEN_IN_ZUKUNFT
			)
		);
		applicationPropertyContext.setInstitutionenDurchGemeindenEinladen(
			getApplicationProperty(
				mandant,
				ApplicationPropertyKey.INSTITUTIONEN_DURCH_GEMEINDEN_EINLADEN
			)
		);

		applicationPropertyContext.setErlaubenInstitutionenZuWaehlen(
			getApplicationProperty(
				mandant,
				ApplicationPropertyKey.ERLAUBEN_INSTITUTIONEN_ZU_WAEHLEN
			)
		);
		applicationPropertyContext.setAngebotTSEnabled(
			getApplicationProperty(
				mandant,
				ApplicationPropertyKey.ANGEBOT_TS_ENABLED
			)
		);
		applicationPropertyContext.setAngebotFIEnabled(
			getApplicationProperty(
				mandant,
				ApplicationPropertyKey.ANGEBOT_FI_ENABLED
			)
		);
		applicationPropertyContext.setAngebotMittagstischEnabled(
			getApplicationProperty(
				mandant,
				ApplicationPropertyKey.ANGEBOT_MITTAGSTISCH_ENABLED
			)
		);
		applicationPropertyContext.setAngebotTFOEnabled(
			getApplicationProperty(
				mandant,
				ApplicationPropertyKey.ANGEBOT_TFO_ENABLED
			)
		);
		applicationPropertyContext.setGemeindeVereinfachteKonfigAktiv(
			getApplicationProperty(
				mandant,
				ApplicationPropertyKey.GEMEINDE_VEREINFACHTE_KONFIG_AKTIV
			)
		);

		applicationPropertyContext.setAbgeloesteViewEnabled(
			getApplicationProperty(
				mandant,
				ApplicationPropertyKey.ABGELOESTE_VIEW
			)
		);

		applicationPropertyContext.setGemeindeKennzahlenReminderActivated(
			getApplicationProperty(
				mandant,
				ApplicationPropertyKey.GEMEINDE_KENNZAHLEN_REMINDER_ACTIVATED
			)
		);

		JaxPublicAppConfig pubAppConf = applicationPropertyContext
			.buildJaxPublicAppConfig();
		return Response.ok(pubAppConf).build();
	}

	private ApplicationProperty getApplicationProperty(
		Mandant mandant,
		ApplicationPropertyKey applicationPropertyKey
	) {
		return this.applicationPropertyService.readApplicationProperty(
			applicationPropertyKey,
			mandant
		)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"getPublicProperties",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND
				)
			);
	}
}
