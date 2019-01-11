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

package ch.dvbern.ebegu.mail;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.enums.EinladungTyp;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import static java.util.Objects.requireNonNull;

/**
 * Configuration For Freemarker Templates
 */
@Dependent
public class MailTemplateConfiguration {

	public static final String EMPFAENGER_MAIL = "empfaengerMail";
	public static final String ANZAHL_TAGE = "anzahlTage";
	public static final String DATUM_LOESCHUNG = "datumLoeschung";
	public static final String GESUCHSTELLER = "gesuchsteller";
	public static final String GESUCHSPERIODE = "gesuchsperiode";
	public static final String START_DATUM = "startDatum";
	public static final String GESUCH = "gesuch";
	public static final String MITTEILUNG = "mitteilung";
	public static final String TEMPLATES_FOLDER = "/mail/templates";

	private final Configuration freeMarkerConfiguration;

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	public MailTemplateConfiguration() {
		this.freeMarkerConfiguration = new Configuration();
		this.freeMarkerConfiguration.setClassForTemplateLoading(MailTemplateConfiguration.class, TEMPLATES_FOLDER);
		this.freeMarkerConfiguration.setDefaultEncoding("UTF-8");
	}

	public String getInfoBetreuungAbgelehnt(
		@Nonnull Betreuung betreuung,
		@Nonnull Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		@Nonnull Sprache sprache
	) {

		return processTemplateBetreuung(
			"InfoBetreuungAbgelehnt",
			betreuung,
			gesuchsteller,
			paramsWithEmpfaenger(empfaengerMail),
			sprache);
	}

	public String getInfoBetreuungenBestaetigt(
		@Nonnull Gesuch gesuch,
		@Nonnull Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		@Nonnull Sprache sprache
	) {

		return processTemplateGesuch(
			"InfoBetreuungenBestaetigt",
			gesuch,
			gesuchsteller,
			paramsWithEmpfaenger(empfaengerMail),
			sprache);
	}

	public String getInfoSchulamtAnmeldungUebernommen(
		@Nonnull Betreuung betreuung,
		@Nonnull Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		@Nonnull Sprache sprache
	) {

		return processTemplateBetreuung(
			"InfoSchulamtAnmeldungUebernommen",
			betreuung,
			gesuchsteller,
			paramsWithEmpfaenger(empfaengerMail),
			sprache);
	}

	public String getInfoSchulamtAnmeldungAbgelehnt(
		@Nonnull Betreuung betreuung,
		@Nonnull Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		@Nonnull Sprache sprache) {

		return processTemplateBetreuung(
			"InfoSchulamtAnmeldungAbgelehnt",
			betreuung,
			gesuchsteller,
			paramsWithEmpfaenger(empfaengerMail),
			sprache);
	}

	public String getInfoBetreuungGeloescht(
		@Nonnull Betreuung betreuung,
		@Nonnull Fall fall,
		@Nonnull Gesuchsteller gesuchsteller1,
		@Nonnull Kind kind,
		@Nonnull Institution institution,
		@Nonnull String empfaengerMail,
		@Nonnull LocalDate datumErstellung,
		@Nonnull LocalDate birthdayKind,
		@Nonnull Sprache sprache) {

		Map<Object, Object> paramMap = paramsWithEmpfaenger(empfaengerMail);
		paramMap.put("datumErstellung", Constants.DATE_FORMATTER.format(datumErstellung));
		paramMap.put("birthday", Constants.DATE_FORMATTER.format(birthdayKind));

		return processTemplateBetreuungGeloescht(
			betreuung,
			fall,
			kind,
			gesuchsteller1,
			institution,
			paramMap,
			sprache);
	}

	public String getInfoBetreuungVerfuegt(
		@Nonnull Betreuung betreuung,
		@Nonnull Fall fall,
		@Nonnull Gesuchsteller gesuchsteller1,
		@Nonnull Kind kind,
		@Nonnull Institution institution,
		@Nonnull String empfaengerMail,
		@Nonnull LocalDate birthdayKind,
		@Nonnull Sprache sprache) {

		Map<Object, Object> paramMap = paramsWithEmpfaenger(empfaengerMail);
		paramMap.put("birthday", Constants.DATE_FORMATTER.format(birthdayKind));

		return processTemplateBetreuungVerfuegt(
			betreuung,
			fall,
			kind,
			gesuchsteller1,
			institution,
			paramMap,
			sprache);
	}

	public String getInfoMitteilungErhalten(
		@Nonnull Mitteilung mitteilung,
		@Nonnull String empfaengerMail,
		@Nonnull Sprache sprache
	) {
		return processTemplateMitteilung(mitteilung, paramsWithEmpfaenger(empfaengerMail), sprache);
	}

	public String getInfoVerfuegtGesuch(
		@Nonnull Gesuch gesuch,
		Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		@Nonnull Sprache sprache
	) {

		return processTemplateGesuch(
			"InfoVerfuegtGesuch",
			gesuch,
			gesuchsteller,
			paramsWithEmpfaenger(empfaengerMail),
			sprache);
	}

	public String getInfoVerfuegtMutation(
		@Nonnull Gesuch gesuch,
		Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		@Nonnull Sprache sprache) {

		return processTemplateGesuch(
			"InfoVerfuegtMutation",
			gesuch,
			gesuchsteller,
			paramsWithEmpfaenger(empfaengerMail),
			sprache);
	}

	public String getInfoMahnung(
		@Nonnull Gesuch gesuch,
		Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		@Nonnull Sprache sprache
	) {
		return processTemplateGesuch(
			"InfoMahnung",
			gesuch,
			gesuchsteller,
			paramsWithEmpfaenger(empfaengerMail),
			sprache);
	}

	public String getWarnungGesuchNichtFreigegeben(
		@Nonnull Gesuch gesuch,
		Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		int anzahlTage,
		@Nonnull Sprache sprache) {

		Map<Object, Object> paramMap = paramsWithEmpfaenger(empfaengerMail);
		paramMap.put(ANZAHL_TAGE, anzahlTage);

		return processTemplateGesuch(
			"WarnungGesuchNichtFreigegeben",
			gesuch,
			gesuchsteller,
			paramMap,
			sprache);
	}

	public String getWarnungFreigabequittungFehlt(
		@Nonnull Gesuch gesuch,
		Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		int anzahlTage,
		@Nonnull Sprache sprache) {

		LocalDate datumLoeschung = LocalDate.now().plusDays(anzahlTage).minusDays(1);

		Map<Object, Object> paramMap = paramsWithEmpfaenger(empfaengerMail);
		paramMap.put(ANZAHL_TAGE, anzahlTage);
		paramMap.put(DATUM_LOESCHUNG, Constants.DATE_FORMATTER.format(datumLoeschung));

		return processTemplateGesuch(
			"WarnungFreigabequittungFehlt",
			gesuch,
			gesuchsteller,
			paramMap,
			sprache);
	}

	public String getInfoGesuchGeloescht(
		@Nonnull Gesuch gesuch,
		Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		@Nonnull Sprache sprache) {

		return processTemplateGesuch(
			"InfoGesuchGeloescht",
			gesuch,
			gesuchsteller,
			paramsWithEmpfaenger(empfaengerMail),
			sprache);
	}

	public String getInfoFreischaltungGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Gesuchsteller gesuchsteller,
		@Nonnull String empfaengerMail,
		@Nonnull Gesuch gesuch,
		@Nonnull Sprache sprache
	) {

		Map<Object, Object> paramMap = paramsWithEmpfaenger(empfaengerMail);
		paramMap.put(GESUCHSPERIODE, gesuchsperiode);
		paramMap.put(START_DATUM, Constants.DATE_FORMATTER.format(gesuchsperiode.getGueltigkeit().getGueltigAb()));
		paramMap.put(GESUCHSTELLER, gesuchsteller);
		paramMap.put(EMPFAENGER_MAIL, empfaengerMail);
		paramMap.put(GESUCH, gesuch);

		return doProcessTemplate(appendLanguageToTemplateName("InfoFreischaltungGesuchsperiode", sprache), paramMap);
	}

	@Nonnull
	public String getBenutzerEinladung(
		@Nonnull Benutzer einladender,
		@Nonnull Einladung einladung,
		@Nonnull Locale locale
	) {

		Benutzer eingeladener = einladung.getEingeladener();

		Map<Object, Object> paramMap = initParamMap();
		paramMap.put("acceptExpire", Constants.DATE_FORMATTER.format(LocalDate.now().plusDays(10)));
		paramMap.put("acceptLink", createLink(eingeladener, einladung));
		paramMap.put("eingeladener", eingeladener);
		paramMap.put(
			"content",
			ServerMessageUtil.getMessage(
				"EinladungEmail_" + einladung.getEinladungTyp(),
				locale,
				einladender.getFullName(),
				ServerMessageUtil.translateEnumValue(eingeladener.getRole(), locale),
				getRollenZusatz(einladung, eingeladener)
			)
		);
		paramMap.put("footer", ServerMessageUtil.getMessage("EinladungEmail_FOOTER", locale));

		return doProcessTemplate(appendLanguageToTemplateName("BenutzerEinladung", locale), paramMap);
	}

	@Nonnull
	private String getRollenZusatz(@Nonnull Einladung einladung, @Nullable Benutzer eingeladener) {
		if (einladung.getEinladungTyp() == EinladungTyp.MITARBEITER) {
			requireNonNull(eingeladener, "For an Einladung of the type Mitarbeiter a user must be set");
			String abhaengigkeitAsString = eingeladener.extractRollenAbhaengigkeitAsString();

			return abhaengigkeitAsString.isEmpty() ? "" : '(' + abhaengigkeitAsString + ')';
		}

		return einladung.getEinladungObjectName()
			.orElse("");
	}

	private String createLink(
		@Nonnull Benutzer eingeladener,
		@Nonnull Einladung einladung
	) {
		return ebeguConfiguration.isClientUsingHTTPS() ? "https://" : "http://"
			+ ebeguConfiguration.getHostname()
			+ "/einladung?typ=" + einladung.getEinladungTyp()
			+ einladung.getEinladungRelatedObjectId().map(entityId -> "&entityid=" + entityId).orElse("")
			+ "&userid=" + eingeladener.getId();
	}

	private String processTemplateGesuch(
		@Nonnull String nameOfTemplate,
		@Nonnull Gesuch gesuch,
		@Nonnull Gesuchsteller gesuchsteller,
		@Nonnull Map<Object, Object> paramMap,
		@Nonnull Sprache sprache
	) {

		paramMap.put(GESUCH, gesuch);
		paramMap.put(GESUCHSTELLER, gesuchsteller);

		return doProcessTemplate(appendLanguageToTemplateName(nameOfTemplate, sprache), paramMap);
	}

	private String processTemplateBetreuung(
		@Nonnull String nameOfTemplate,
		@Nonnull Betreuung betreuung,
		@Nonnull Gesuchsteller gesuchsteller,
		@Nonnull Map<Object, Object> paramMap,
		@Nonnull Sprache sprache
	) {

		paramMap.put("betreuung", betreuung);
		paramMap.put(GESUCHSTELLER, gesuchsteller);

		return doProcessTemplate(appendLanguageToTemplateName(nameOfTemplate, sprache), paramMap);
	}

	private String processTemplateBetreuungGeloescht(
		Betreuung betreuung,
		Fall fall,
		Kind kind,
		Gesuchsteller gesuchsteller1,
		Institution institution,
		@Nonnull Map<Object, Object> paramMap,
		@Nonnull Sprache sprache
	) {

		return processTemplateBetreuungStatus(
			"InfoBetreuungGeloescht",
			betreuung,
			fall,
			kind,
			gesuchsteller1,
			institution,
			paramMap,
			sprache);
	}

	private String processTemplateBetreuungStatus(
		String nameOfTemplate,
		Betreuung betreuung,
		Fall fall,
		Kind kind,
		Gesuchsteller gesuchsteller1,
		Institution institution,
		@Nonnull Map<Object, Object> paramMap,
		@Nonnull Sprache sprache
	) {

		paramMap.put("betreuung", betreuung);
		paramMap.put("fall", fall);
		paramMap.put("kind", kind);
		paramMap.put(GESUCHSTELLER, gesuchsteller1);
		paramMap.put("institution", institution);

		return doProcessTemplate(appendLanguageToTemplateName(nameOfTemplate, sprache), paramMap);
	}

	private String processTemplateBetreuungVerfuegt(
		Betreuung betreuung,
		Fall fall,
		Kind kind,
		Gesuchsteller gesuchsteller1,
		Institution institution,
		@Nonnull Map<Object, Object> paramMap,
		@Nonnull Sprache sprache
	) {

		return processTemplateBetreuungStatus(
			"InfoBetreuungVerfuegt",
			betreuung,
			fall,
			kind,
			gesuchsteller1,
			institution,
			paramMap,
			sprache);
	}

	private String processTemplateMitteilung(
		@Nonnull Mitteilung mitteilung,
		@Nonnull Map<Object, Object> paramMap,
		@Nonnull Sprache sprache
	) {

		paramMap.put(MITTEILUNG, mitteilung);

		return doProcessTemplate(appendLanguageToTemplateName("InfoMitteilungErhalten", sprache), paramMap);
	}

	/**
	 * Appends the language and the file extension to the given name. Result will look like "name_de.ftl"
	 * WARNING! The name must be passed without an extension
	 */
	private String appendLanguageToTemplateName(@Nonnull final String name, @Nonnull Sprache sprache) {
		return appendLanguageToTemplateName(name, sprache.getLocale());
	}

	private String appendLanguageToTemplateName(@Nonnull final String name, @Nonnull Locale locale) {
		return name + '_' + locale.getLanguage().toLowerCase() + ".ftl";
	}

	private String doProcessTemplate(@Nonnull final String name, final Map<Object, Object> rootMap) {

		try {
			final Template template = freeMarkerConfiguration.getTemplate(name, Constants.DEFAULT_LOCALE);
			final StringWriter out = new StringWriter(50);
			template.process(rootMap, out);

			return out.toString();
		} catch (final IOException e) {
			throw new EbeguRuntimeException(
				"doProcessTemplate()",
				String.format("Failed to load template %s.", name),
				e);
		} catch (final TemplateException e) {
			throw new EbeguRuntimeException(
				"doProcessTemplate()",
				String.format("Failed to process template %s.", name),
				e);
		}
	}

	public String getMailCss() {
		return "<style type=\"text/css\">\n" +
			"        body {\n" +
			"            font-family: \"Open Sans\", Arial, Helvetica, sans-serif;\n" +
			"        }\n" +
			"      .kursInfoHeader {background-color: #bce1ff; font-weight: bold;}" +
			"    </style>";
	}

	@Nonnull
	private Map<Object, Object> initParamMap() {
		Map<Object, Object> paramMap = new HashMap<>();
		paramMap.put("configuration", ebeguConfiguration);
		paramMap.put("templateConfiguration", this);
		paramMap.put("base64Header", new UTF8Base64MailHeaderDirective());

		return paramMap;
	}

	@Nonnull
	private Map<Object, Object> paramsWithEmpfaenger(@Nonnull String empfaenger) {
		Map<Object, Object> paramMap = initParamMap();
		paramMap.put(EMPFAENGER_MAIL, empfaenger);

		return paramMap;
	}
}
