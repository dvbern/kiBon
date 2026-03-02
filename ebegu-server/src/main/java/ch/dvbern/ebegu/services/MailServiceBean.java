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

package ch.dvbern.ebegu.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import jakarta.ejb.AsyncResult;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.dto.SupportAnfrageDTO;
import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.entities.AbstractAnmeldung;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.BenutzerStatus;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.GemeindeAngebotTyp;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.mail.MailTemplateConfiguration;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.EnumUtil;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.util.Constants.NEW_LINE_CHAR_PATTERN;
import static ch.dvbern.ebegu.util.logging.LogUtil.logExceptionAccordingToEnvironment;
import static java.util.Objects.requireNonNull;

/**
 * Service fuer Senden von E-Mails
 */
@Stateless
@Local(MailService.class)
public class MailServiceBean extends AbstractMailServiceBean implements
	MailService {

	private static final Logger LOG = LoggerFactory.getLogger(
		MailServiceBean.class.getSimpleName()
	);

	@Inject
	private MailTemplateConfiguration mailTemplateConfig;

	@Inject
	private FallService fallService;

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	@Inject
	private GesuchService gesuchService;

	@Override
	public void sendInfoBetreuungenBestaetigt(@Nonnull Gesuch gesuch) {
		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(
			gesuch,
			gemeindeService
		);
		sendMail(
			gesuch,
			"InfoBetreuungBestaetigt",
			(gesuchsteller, adr) -> mailTemplateConfig
				.getInfoBetreuungenBestaetigt(
					gesuch,
					gesuchsteller,
					adr,
					sprache
				),
			AntragStatus.IN_BEARBEITUNG_GS,
			AntragStatus.IN_BEARBEITUNG_SOZIALDIENST
		);
	}

	@Override
	public void sendInfoBetreuungAbgelehnt(@Nonnull Betreuung betreuung) {
		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(
			betreuung.extractGesuch(),
			gemeindeService
		);
		sendMail(
			betreuung.extractGesuch(),
			"InfoBetreuungAbgelehnt",
			(gesuchsteller, adr) -> mailTemplateConfig
				.getInfoBetreuungAbgelehnt(
					betreuung,
					gesuchsteller,
					adr,
					sprache
				),
			AntragStatus.values()
		);
	}

	@Override
	public void sendInfoSchulamtAnmeldungTagesschuleUebernommen(
		@Nonnull AbstractAnmeldung abstractAnmeldung
	) {
		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(
			abstractAnmeldung.extractGesuch(),
			gemeindeService
		);
		sendMail(
			abstractAnmeldung.extractGesuch(),
			"InfoSchulamtAnmeldungTagesschuleUebernommen",
			(gesuchsteller, adr) -> mailTemplateConfig
				.getInfoSchulamtAnmeldungTagesschuleUebernommen(
					abstractAnmeldung,
					gesuchsteller,
					adr,
					sprache
				),
			AntragStatus.values()
		);
	}

	@Override
	public void sendInfoSchulamtAnmeldungAbgelehnt(
		@Nonnull AbstractAnmeldung abstractAnmeldung
	) {
		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(
			abstractAnmeldung.extractGesuch(),
			gemeindeService
		);
		sendMail(
			abstractAnmeldung.extractGesuch(),
			"InfoSchulamtAnmeldungAbgelehnt",
			(gesuchsteller, adr) -> mailTemplateConfig
				.getInfoSchulamtAnmeldungAbgelehnt(
					abstractAnmeldung,
					gesuchsteller,
					adr,
					sprache
				),
			AntragStatus.values()
		);
	}

	@Override
	public void sendInfoSchulamtAnmeldungFerieninselUebernommen(
		@Nonnull AbstractAnmeldung abstractAnmeldung
	) {
		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(
			abstractAnmeldung.extractGesuch(),
			gemeindeService
		);
		sendMail(
			abstractAnmeldung.extractGesuch(),
			"InfoSchulamtAnmeldungFerieninselUebernommen",
			(gesuchsteller, adr) -> mailTemplateConfig
				.getInfoSchulamtAnmeldungFerieninselUebernommen(
					abstractAnmeldung,
					gesuchsteller,
					adr,
					sprache
				),
			AntragStatus.values()
		);
	}

	@Override
	public void sendInfoMitteilungErhalten(@Nonnull Mitteilung mitteilung) {
		List<Sprache> sprachen =
			EbeguUtil.extractGemeindeSprachen(
				mitteilung.getDossier().getGemeinde(),
				gemeindeService
			);
		if (doSendMail(mitteilung.getFall())) {
			String mailaddress = fallService.getCurrentEmailAddress(
				mitteilung.getFall().getId()
			).orElse(null);
			if (StringUtils.isNotEmpty(mailaddress)) {
				String message = mailTemplateConfig.getInfoMitteilungErhalten(
					mitteilung,
					mailaddress,
					sprachen
				);
				Mandant mandant = mitteilung.getFall().getMandant();
				toOutboxMail(
					message,
					mailaddress,
					mandant.getMandantIdentifier()
				);
				LOG.debug(
					"Email fuer InfoMitteilungErhalten wird versendet an {}",
					mailaddress
				);
			} else {
				LOG.warn(
					"skipping sendInfoMitteilungErhalten because Mitteilungsempfaenger is null"
				);
			}
		}
	}

	@Override
	public void sendInfoVerfuegtGesuch(@Nonnull Gesuch gesuch) {
		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(
			gesuch,
			gemeindeService
		);
		sendMail(
			gesuch,
			"InfoVerfuegtGesuch",
			(gesuchsteller, adr) -> mailTemplateConfig
				.getInfoVerfuegtGesuch(
					gesuch,
					gesuchsteller,
					adr,
					sprache
				),
			AntragStatus.values()
		);
	}

	@Override
	public void sendInfoVerfuegtMutation(@Nonnull Gesuch gesuch) {
		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(
			gesuch,
			gemeindeService
		);
		sendMail(
			gesuch,
			"InfoVerfuegtMutation",
			(gesuchsteller, adr) -> mailTemplateConfig
				.getInfoVerfuegtMutation(
					gesuch,
					gesuchsteller,
					adr,
					sprache
				),
			AntragStatus.values()
		);
	}

	@Override
	public void sendInfoMahnung(@Nonnull Gesuch gesuch) {
		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(
			gesuch,
			gemeindeService
		);
		sendMail(
			gesuch,
			"InfoMahnung",
			(gesuchsteller, adr) -> mailTemplateConfig.getInfoMahnung(
				gesuch,
				gesuchsteller,
				adr,
				sprache
			),
			true,
			AntragStatus.values()
		);
	}

	@Override
	public void sendWarnungGesuchNichtFreigegeben(
		@Nonnull Gesuch gesuch,
		int anzahlTageBisLoeschung
	) {

		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(
			gesuch,
			gemeindeService
		);
		sendMail(
			gesuch,
			"WarnungGesuchNichtFreigegeben",
			(gesuchsteller, adr) -> mailTemplateConfig
				.getWarnungGesuchNichtFreigegeben(
					gesuch,
					gesuchsteller,
					adr,
					anzahlTageBisLoeschung,
					sprache
				),
			AntragStatus.values()
		);
	}

	@Override
	public void sendWarnungFreigabequittungFehlt(
		@Nonnull Gesuch gesuch,
		int anzahlTageBisLoeschung
	) {

		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(
			gesuch,
			gemeindeService
		);
		sendMail(
			gesuch,
			"WarnungFreigabequittungFehlt",
			(gesuchsteller, adr) -> mailTemplateConfig
				.getWarnungFreigabequittungFehlt(
					gesuch,
					gesuchsteller,
					adr,
					anzahlTageBisLoeschung,
					sprache
				),
			AntragStatus.values()
		);
	}

	@Override
	public void sendInfoGesuchGeloescht(@Nonnull Gesuch gesuch) {
		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(
			gesuch,
			gemeindeService
		);
		sendMail(
			gesuch,
			"InfoGesuchGeloescht",
			(gesuchsteller, adr) -> mailTemplateConfig
				.getInfoGesuchGeloescht(
					gesuch,
					gesuchsteller,
					adr,
					sprache
				),
			AntragStatus.values()
		);
	}

	@Override
	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Future<Integer> sendInfoFreischaltungGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull List<Gesuch> gesucheToSendMail
	) {
		int versendetZaehler = 0;
		for (Gesuch gesuch : gesucheToSendMail) {
			if (sendInfoFreischaltungGesuchsperiode(gesuchsperiode, gesuch)) {
				versendetZaehler++;
			}
		}
		return new AsyncResult<>(versendetZaehler);
	}

	@Override
	public boolean sendInfoFreischaltungGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Gesuch gesuch
	) {
		try {
			if (doSendMail(gesuch.getFall())) {
				Optional<String> emailAddress = findEMailAddress(gesuch);
				Optional<Gesuchsteller> gesuchsteller = gesuch
					.extractGesuchsteller1();
				if (gesuchsteller.isPresent() && emailAddress.isPresent()) {
					String adr = emailAddress.get();

					final Sprache sprache = EbeguUtil
						.extractKorrespondenzsprache(
							gesuch,
							gemeindeService
						);
					String message = mailTemplateConfig
						.getInfoFreischaltungGesuchsperiode(
							gesuchsperiode,
							gesuchsteller.get(),
							adr,
							gesuch,
							sprache
						);
					toOutboxMail(
						message,
						adr,
						gesuch.extractMandant().getMandantIdentifier()
					);

					LOG.debug(
						"Email fuer InfoFreischaltungGesuchsperiode wird versendet an {}",
						adr
					);
					return true;
				}

				LOG.info(
					"skipping InfoFreischaltungGesuchsperiode because Gesuchsteller 1 or email address are null: "
						+ "{} : {}",
					gesuchsteller,
					emailAddress
				);
				return false;
			}
		} catch (Exception e) {
			logExceptionAccordingToEnvironment(
				e,
				"Mail InfoFreischaltungGesuchsperiode konnte nicht verschickt werden fuer Gesuch {}",
				ebeguConfiguration.getIsDevmode(),
				gesuch.getId()
			);
		}
		return false;
	}

	@SuppressFBWarnings("REC_CATCH_EXCEPTION")
	@Override
	public void sendInfoBetreuungGeloescht(
		@Nonnull List<Betreuung> betreuungen
	) {

		for (Betreuung betreuung : betreuungen) {

			Institution institution = betreuung.getInstitutionStammdaten()
				.getInstitution();
			String mailaddress = betreuung.getInstitutionStammdaten().getMail();
			Gesuch gesuch = betreuung.extractGesuch();
			Fall fall = gesuch.getFall();
			Gesuchsteller gesuchsteller1 = gesuch.extractGesuchsteller1()
				.orElseThrow(
					() -> new EbeguEntityNotFoundException(
						"sendInfoBetreuungGeloescht",
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						"Gesuchsteller1"
					)
				);
			Kind kind = betreuung.getKind().getKindJA();
			Betreuungsstatus status = betreuung.getBetreuungsstatus();
			LocalDate datumErstellung = requireNonNull(
				betreuung.getTimestampErstellt()
			).toLocalDate();
			LocalDate birthdayKind = kind.getGeburtsdatum();

			final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(
				gesuch,
				gemeindeService
			);
			String message = mailTemplateConfig.getInfoBetreuungGeloescht(
				betreuung,
				fall,
				gesuchsteller1,
				kind,
				institution,
				mailaddress,
				datumErstellung,
				birthdayKind,
				sprache
			);

			Mandant mandant = gesuch.extractMandant();

			try {
				if (gesuch.getTyp().isMutation()) {
					// wenn Gesuch Mutation ist
					if (betreuung.getVorgaengerId() == null) { //this is a new Betreuung for this Antrag
						if (status.isSendToInstitution()) { //wenn status warten, abgewiesen oder bestaetigt ist
							toOutboxMail(
								message,
								mailaddress,
								mandant.getMandantIdentifier()
							);
							LOG.info(
								"Email fuer InfoBetreuungGeloescht wird versendet an {}",
								mailaddress
							);
						}
					} else {
						Betreuung vorgaengerBetreuung = betreuungService
							.findBetreuung(betreuung.getVorgaengerId())
							.orElseThrow(
								() -> new EbeguEntityNotFoundException(
									"sendInfoBetreuungGeloescht",
									ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
									betreuung.getVorgaengerId()
								)
							);

						// wenn Vorgaengerbetreuung vorhanden
						if ((status == Betreuungsstatus.BESTAETIGT
							&& !betreuung.isSame(vorgaengerBetreuung))
							|| (status == Betreuungsstatus.WARTEN
								|| status
									== Betreuungsstatus.ABGEWIESEN)) {
							// wenn status der aktuellen Betreuung bestaetigt ist UND wenn vorgaenger NICHT die gleiche
							// ist wie die aktuelle oder wenn status der aktuellen Betreuung warten oder abgewiesen ist
							toOutboxMail(
								message,
								mailaddress,
								mandant.getMandantIdentifier()
							);
							LOG.info(
								"Email fuer InfoBetreuungGeloescht wird versendet an {}",
								mailaddress
							);
						}
					}
				} else {
					//wenn es keine Mutation ist
					if (status.isSendToInstitution()) {
						//wenn status warten, abgewiesen oder bestaetigt ist
						toOutboxMail(
							message,
							mailaddress,
							mandant.getMandantIdentifier()
						);
						LOG.info(
							"Email fuer InfoBetreuungGeloescht wird versendet an {}",
							mailaddress
						);
					}

				}
			} catch (Exception e) {
				logExceptionAccordingToEnvironment(
					e,
					"Mail InfoBetreuungGeloescht konnte nicht verschickt werden fuer Betreuung {}",
					ebeguConfiguration.getIsDevmode(),
					betreuung.getId()
				);
			}
		}
	}

	@Override
	public void sendInfoBetreuungVerfuegt(@Nonnull Betreuung betreuung) {

		Institution institution = betreuung.getInstitutionStammdaten()
			.getInstitution();
		String mailaddress = betreuung.getInstitutionStammdaten().getMail();
		Gesuch gesuch = betreuung.extractGesuch();
		Fall fall = gesuch.getFall();
		Gesuchsteller gesuchsteller1 = gesuch.extractGesuchsteller1()
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"sendInfoBetreuungVerfuegt",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					"Gesuchsteller1"
				)
			);
		Kind kind = betreuung.getKind().getKindJA();
		LocalDate birthdayKind = kind.getGeburtsdatum();
		Mandant mandant = gesuch.extractMandant();

		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(
			gesuch,
			gemeindeService
		);

		String message = mailTemplateConfig.getInfoBetreuungVerfuegt(
			betreuung,
			fall,
			gesuchsteller1,
			kind,
			institution,
			mailaddress,
			birthdayKind,
			sprache
		);

		toOutboxMail(
			message,
			mailaddress,
			mandant.getMandantIdentifier()
		);
		LOG.info(
			"Email fuer InfoBetreuungVerfuegt wird versendet an {}",
			mailaddress
		);
	}

	@Override
	public void sendInfoStatistikGeneriert(
		@Nonnull String receiverEmail,
		@Nonnull String downloadurl,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	) {
		Sprache sprache = Sprache.DEUTSCH;
		if (Locale.FRENCH.getLanguage().equals(locale.getLanguage())) {
			sprache = Sprache.FRANZOESISCH;
		}
		String message = mailTemplateConfig.sendInfoStatistikGeneriert(
			receiverEmail,
			downloadurl,
			sprache,
			mandant
		);

		toOutboxMail(
			message,
			receiverEmail,
			mandant.getMandantIdentifier()
		);
		LOG.info(
			"Email fuer InfoStatistikGeneriert wird versendet an {}",
			removeNewLineChar(receiverEmail)
		);
	}

	private String removeNewLineChar(String str) {
		return NEW_LINE_CHAR_PATTERN.matcher(str).replaceAll("_");
	}

	@Override
	public void sendBenutzerEinladung(
		@Nonnull Benutzer einladender,
		@Nonnull Einladung einladung
	) {
		requireNonNull(einladender);
		requireNonNull(einladung);

		String message = mailTemplateConfig.getBenutzerEinladung(
			einladender,
			einladung
		);
		LOG.info(
			"Benutzereinladung wird gesendet an {}",
			einladung.getEingeladener().getEmail()
		);
		toOutboxMail(
			message,
			einladung.getEingeladener().getEmail(),
			einladender.getMandant().getMandantIdentifier()
		);
	}

	@Override
	public void sendSupportAnfrage(
		@Nonnull SupportAnfrageDTO supportAnfrageDTO
	) {
		Benutzer benutzer = benutzerService.getCurrentBenutzer()
			.orElseThrow(IllegalArgumentException::new);
		MandantIdentifier mandant = benutzer.getMandant()
			.getMandantIdentifier();

		String subject = "Supportanfrage KiBon von " + benutzer.getFullName();
		StringBuilder content = new StringBuilder();
		String userGemeinden = benutzer.extractGemeindenForUser()
			.stream()
			.map((Gemeinde::getName))
			.collect(Collectors.joining(", "));

		content.append("Anfrage: ")
			.append(Constants.LINE_BREAK)
			.append(supportAnfrageDTO.getBeschreibung())
			.append(Constants.LINE_BREAK);
		content.append(Constants.LINE_BREAK);

		if (supportAnfrageDTO.getBetroffeneFaelle() != null) {
			content.append("Betroffene Fälle: ")
				.append(supportAnfrageDTO.getBetroffeneFaelle())
				.append(Constants.LINE_BREAK);
		}
		if (supportAnfrageDTO.getBetroffenePeriode() != null) {
			content.append("Betroffene Periode: ")
				.append(supportAnfrageDTO.getBetroffenePeriode())
				.append(Constants.LINE_BREAK);
			content.append(Constants.LINE_BREAK);
		}

		content.append("Benutzer: ")
			.append(benutzer.getUsername())
			.append(" (")
			.append(benutzer.getFullName())
			.append(')')
			.append(Constants.LINE_BREAK);
		if (benutzer.getTraegerschaft() != null
			&& !benutzer.getTraegerschaft().getName().isBlank()) {
			content.append("Traegerschaft: ")
				.append(benutzer.getTraegerschaft().getName())
				.append(Constants.LINE_BREAK);
		}
		if (benutzer.getInstitution() != null
			&& !benutzer.getInstitution().getName().isBlank()) {
			content.append("Institution: ")
				.append(benutzer.getInstitution().getName())
				.append(Constants.LINE_BREAK);
		}
		if (!userGemeinden.isBlank()) {
			content.append("Gemeinde: ")
				.append(userGemeinden)
				.append(Constants.LINE_BREAK);
		}
		content.append("Email: ")
			.append(benutzer.getEmail())
			.append(Constants.LINE_BREAK);
		content.append("Rolle: ")
			.append(benutzer.getRole())
			.append(Constants.LINE_BREAK);
		content.append("Mandant: ")
			.append(mandant)
			.append(Constants.LINE_BREAK);
		content.append(Constants.LINE_BREAK);
		content.append("Erstellt am: ")
			.append(
				Constants.DATE_TIME_FORMATTER_SUPPORT_REQUEST.format(
					LocalDateTime.now()
				)
			)
			.append(Constants.LINE_BREAK);
		content.append("Id: ")
			.append(supportAnfrageDTO.getId())
			.append(Constants.LINE_BREAK);

		String supportMail = ebeguConfiguration.getSupportMail();
		toOutboxMail(subject, content.toString(), supportMail, mandant);
	}

	@Override
	public void sendInfoOffenePendenzenNeuMitteilungInstitution(
		@Nonnull InstitutionStammdaten institutionStammdaten,
		boolean offenePendenzen,
		boolean ungelesendeMitteilung
	) {
		String mailaddress = StringUtils.isNotBlank(
			institutionStammdaten.getErinnerungMail()
		) ?
			institutionStammdaten.getErinnerungMail() :
			institutionStammdaten.getMail();
		try {
			if (StringUtils.isNotBlank(mailaddress)) {
				String message = mailTemplateConfig
					.getInfoOffenePendenzenNeuMitteilungInstitution(
						institutionStammdaten,
						mailaddress,
						offenePendenzen,
						ungelesendeMitteilung
					);
				Mandant mandant = institutionStammdaten.getInstitution()
					.getMandant();
				toOutboxMail(
					message,
					mailaddress,
					mandant.getMandantIdentifier()
				);
				LOG.info(
					"Email fuer InfoOffenePendenzenInstitution wird versendet an {}",
					mailaddress
				);
			} else {
				LOG.warn(
					"Skipping InfoOffenePendenzenInstitution because E-Mail of Institution is null"
				);
			}
		} catch (Exception e) {
			logExceptionAccordingToEnvironment(
				e,
				"Mail InfoOffenePendenzenInstitution konnte nicht verschickt werden fuer Institution {}",
				ebeguConfiguration.getIsDevmode(),
				institutionStammdaten.getInstitution().getName()
			);
		}
	}

	private void sendMail(
		@Nonnull Gesuch gesuch,
		@Nonnull String mailTemplate,
		@Nonnull BiFunction<Gesuchsteller, String, String> messageProvider,
		@Nonnull AntragStatus... statusInWhichToSendMail
	) {
		sendMail(
			gesuch,
			mailTemplate,
			messageProvider,
			false,
			statusInWhichToSendMail
		);
	}

	private void sendMail(
		@Nonnull Gesuch gesuch,
		@Nonnull String mailTemplate,
		@Nonnull BiFunction<Gesuchsteller, String, String> messageProvider,
		boolean useErstgesuchAsFallback,
		@Nonnull AntragStatus... statusInWhichToSendMail
	) {

		if (!doSendMail(gesuch, useErstgesuchAsFallback)) {
			return;
		}
		// Gewisse Mails sollen nur in bestimmten Status gesendet werden.
		if (ArrayUtils.isNotEmpty(statusInWhichToSendMail)
			&& EnumUtil.isNoneOf(
				gesuch.getStatus(),
				statusInWhichToSendMail
			)) {
			return;
		}

		Optional<Gesuchsteller> gesuchsteller = gesuch.extractGesuchsteller1();
		Optional<String> emailAddress = findEMailAddress(gesuch);

		Mandant mandant = gesuch.extractMandant();

		if (gesuchsteller.isPresent() && emailAddress.isPresent()) {
			String message = messageProvider.apply(
				gesuchsteller.get(),
				emailAddress.get()
			);
			toOutboxMail(
				message,
				emailAddress.get(),
				mandant.getMandantIdentifier()
			);

			LOG.info("Sent Email {} to {}", mailTemplate, emailAddress.get());

			return;
		}

		if (gesuch.getEingangsart().isOnlineGesuch()) {
			LOG.info(
				"Not sending Email {} because Gesuchsteller or Email Address is NULL: {}, {}",
				mailTemplate,
				gesuchsteller,
				emailAddress
			);
		}
	}

	/**
	 * Hier wird an einer Stelle definiert, an welche Benutzergruppen ein Mail geschickt werden soll.
	 */
	private boolean doSendMail(@Nonnull Fall fall) {
		// Mail nur schicken, wenn es der Fall einen Besitzer hat
		return (fall.getBesitzer() != null
			&& fall.getBesitzer().getStatus() != BenutzerStatus.EINGELADEN)
			|| fall.getSozialdienstFall() != null;
	}

	/**
	 * Hier wird an einer Stelle definiert, an welche Benutzergruppen ein Mail geschickt werden soll.
	 */
	private boolean doSendMail(
		@Nonnull Gesuch gesuch,
		boolean useErstgesuchAsFallback
	) {
		// Mail nur schicken, wenn es der Fall einen Besitzer hat UND (das aktuelle Gesuch bzw. Mutation online
		// eingereicht wurde ODER die Papiermutation bereits verfügt wurde)

		// wenn das aktuelle Gesuch kein Onlinegesuch ist, können wir den Vorgänger betrachten, wenn erlaubt
		// (useVorgaengerGesuchAsFallback == true).
		boolean onlineGesuch = gesuch.getEingangsart().isOnlineGesuch()
			|| (useErstgesuchAsFallback
				&& gesuchService.isFirstGesuchOnline(gesuch));

		return doSendMail(gesuch.getFall())
			&& (onlineGesuch
				|| gesuch.getStatus()
					.isAnyStatusOfVerfuegt());
	}

	@Nonnull
	private Optional<String> findEMailAddress(@Nonnull Gesuch gesuch) {
		return fallService.getCurrentEmailAddress(gesuch.getFall().getId())
			.filter(StringUtils::isNotEmpty);
	}

	@Override
	public void sendInfoSchulamtAnmeldungTagesschuleAkzeptiert(
		@Nonnull AbstractAnmeldung abstractAnmeldung
	) {
		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(
			abstractAnmeldung.extractGesuch(),
			gemeindeService
		);
		sendMail(
			abstractAnmeldung.extractGesuch(),
			"InfoSchulamtAnmeldungTagesschuleAkzeptiert",
			(gesuchsteller, adr) -> mailTemplateConfig
				.getInfoSchulamtAnmeldungTagesschuleAkzeptiert(
					abstractAnmeldung,
					gesuchsteller,
					adr,
					sprache
				),
			AntragStatus.values()
		);
	}

	@Override
	public void sendInfoGemeindeAngebotAktiviert(
		@Nonnull Gemeinde gemeinde,
		@Nonnull GemeindeAngebotTyp angebot
	) {
		List<Sprache> sprachen =
			EbeguUtil.extractGemeindeSprachen(gemeinde, gemeindeService);

		GemeindeStammdaten stammdaten =
			gemeindeService.getGemeindeStammdatenByGemeindeId(
				gemeinde.getId()
			)
				.orElseThrow(
					() -> new EbeguEntityNotFoundException(
						"sendInfoGemeineAngebotAktiviert",
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						gemeinde.getId()
					)
				);
		Mandant mandant = gemeinde.getMandant();
		String mailaddress = stammdaten.getMail();
		if (StringUtils.isNotEmpty(mailaddress)) {
			String message = mailTemplateConfig.getInfoGemeindeAngebotAktiviert(
				gemeinde,
				mailaddress,
				angebot,
				sprachen
			);
			try {
				toOutboxMail(
					message,
					mailaddress,
					mandant.getMandantIdentifier()
				);
				LOG.debug(
					"Email fuer InfoGemeineAngebotAktiviert wird versendet an {}",
					mailaddress
				);
			} catch (Exception e) {
				logExceptionAccordingToEnvironment(
					e,
					"Mail InfoGemeineAngebotAktiviert konnte nicht verschickt werden fuer Gemeinde {}",
					ebeguConfiguration.getIsDevmode(),
					gemeinde.getName()
				);
			}
		} else {
			LOG.warn(
				"skipping setInfoGemeineAngebotAktiviert because Mitteilungsempfaenger is null"
			);
		}
	}

	@Override
	public void sendInfoGesuchVerfuegtVerantwortlicherTS(
		@Nonnull Gesuch gesuch,
		@Nonnull Benutzer verantwortlicherTS
	) {
		String mailaddressTS = verantwortlicherTS.getEmail();
		List<Sprache> sprachen =
			EbeguUtil.extractGemeindeSprachen(
				gesuch.extractGemeinde(),
				gemeindeService
			);
		if (verantwortlicherTS.getStatus() == BenutzerStatus.EINGELADEN) {
			LOG.info(
				"Benutzer {} ist gesperrt, Mail InfoGesuchVerfuegtVerantwortlicherTS wird nicht gesendet",
				verantwortlicherTS.getId()
			);
			return;
		}
		Mandant mandant = gesuch.extractMandant();
		if (StringUtils.isNotEmpty(mailaddressTS)) {
			String message = mailTemplateConfig
				.getInfoGesuchVerfuegtVerantwortlicherTS(
					gesuch,
					mailaddressTS,
					sprachen,
					verantwortlicherTS
				);
			toOutboxMail(
				message,
				mailaddressTS,
				mandant.getMandantIdentifier()
			);
			LOG.info(
				"Email fuer InfoGesuchVerfuegtVerantwortlicherSCH wird versendet an {}",
				mailaddressTS
			);
		} else {
			LOG.warn(
				"skipping InfoGesuchVerfuegtVerantwortlicherSCH because verantwortlicherSCH has no mailaddress"
			);
		}

	}

	@Override
	public void sendInfoLastenausgleichGemeinde(
		@Nonnull Gemeinde gemeinde,
		@Nonnull Lastenausgleich lastenausgleich
	) {
		try {
			LOG.info("Sende Mail für Gemeinde " + gemeinde.getName());
			List<Sprache> sprachen =
				EbeguUtil.extractGemeindeSprachen(
					gemeinde,
					gemeindeService
				);

			String mailaddress = findGemeindeMailAddress(gemeinde);
			Mandant mandant = gemeinde.getMandant();
			if (StringUtils.isNotEmpty(mailaddress)) {
				String message =
					mailTemplateConfig.getInfoGemeindeLastenausgleichDurch(
						lastenausgleich,
						sprachen,
						mailaddress
					);
				toOutboxMail(
					message,
					mailaddress,
					mandant.getMandantIdentifier()
				);
				LOG.debug(
					"Email fuer InfoGemeindeLastenausgleichDurch wird versendet an {}",
					mailaddress
				);
			} else {
				LOG.warn(
					"skipping InfoGemeindeLastenausgleichDurch because Gemeinde Email is null"
				);
			}
		} catch (EbeguEntityNotFoundException nf) {
			LOG.error("Gemeindestammdaten not Found: ", gemeinde.getId(), nf);
		} catch (Exception e) {
			logExceptionAccordingToEnvironment(
				e,
				"Mail InfoGemeindeLastenausgleichDurch konnte nicht verschickt werden fuer Gemeinde {}",
				ebeguConfiguration.getIsDevmode(),
				gemeinde.getName()
			);
		}
	}

	@Override
	public void sendInfoSchulamtAnmeldungStorniert(
		AbstractAnmeldung abstractAnmeldung
	) {
		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(
			abstractAnmeldung.extractGesuch(),
			gemeindeService
		);
		sendMail(
			abstractAnmeldung.extractGesuch(),
			"InfoSchulamtAnmeldungStorniert",
			(gesuchsteller, adr) -> mailTemplateConfig
				.getInfoSchulamtAnmeldungStorniert(
					abstractAnmeldung,
					gesuchsteller,
					adr,
					sprache
				),
			AntragStatus.values()
		);
	}

	@Override
	public void sendInfoLATSAntragZurueckAnGemeinde(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer wiederEroeffnet
	) {
		final List<Sprache> sprachen = EbeguUtil.extractGemeindeSprachen(
			wiederEroeffnet.getGemeinde(),
			gemeindeService
		);
		final Gemeinde gemeinde = wiederEroeffnet.getGemeinde();
		final Mandant mandant = gemeinde.getMandant();
		try {
			LOG.info("Sende Mail für Gemeinde {}", gemeinde.getName());

			String mailaddress = findGemeindeMailAddress(gemeinde);
			if (StringUtils.isNotEmpty(mailaddress)) {
				String message =
					mailTemplateConfig
						.getInfoGemeindeLastenausgleichTagesschuleZurueckAnGemeinde(
							wiederEroeffnet,
							sprachen,
							mailaddress
						);
				toOutboxMail(
					message,
					mailaddress,
					mandant.getMandantIdentifier()
				);
				LOG.debug(
					"Email fuer InfoGemeindeLastenausgleichDurch wird versendet an {}",
					mailaddress
				);
			} else {
				LOG.warn(
					"skipping InfoGemeindeLastenausgleichDurch because Gemeinde Email is null"
				);
			}
		} catch (EbeguEntityNotFoundException nf) {
			LOG.error(
				"Gemeindestammdaten not Found for {}",
				gemeinde.getId(),
				nf
			);
		} catch (Exception e) {
			logExceptionAccordingToEnvironment(
				e,
				"Mail InfoGemeindeLastenausgleichDurch konnte nicht verschickt werden fuer Gemeinde {}",
				ebeguConfiguration.getIsDevmode(),
				gemeinde.getName()
			);
		}

	}

	@Override
	public void sendInitGSZPVNr(
		@Nonnull String ssoInitURL,
		GesuchstellerContainer gesuchstellerContainer,
		@Nonnull String email,
		String korrespondenzSprache
	) {

		LOG.info(
			"Sende Init ZPV Nr. Mail für GS {}",
			gesuchstellerContainer.getGesuchstellerJA().getId()
		);
		MandantIdentifier mandantIdentifier = MandantIdentifier.BERN;
		String message = mailTemplateConfig.getInitGSZPVNr(
			ssoInitURL,
			Sprache.valueOf(korrespondenzSprache),
			email
		);
		toOutboxMail(message, email, mandantIdentifier);
		LOG.debug(
			"Email fuer sendInitGSZPVNr wird versendet an {}",
			removeNewLineChar(email)
		);
	}

	@Override
	public void sendInfoAuszahlungsdatenChanged(
		InstitutionStammdaten institutionStammdaten,
		@Nonnull String email
	) {
		LOG.info(
			"Sende Info Bankdaten von Instiution {} hat geändert.",
			institutionStammdaten.getInstitution().getId()
		);
		String message =
			mailTemplateConfig
				.getInfoGemeindeInstitutionAuszahlungsdatenChanged(
					institutionStammdaten,
					email
				);
		toOutboxMail(
			message,
			email,
			institutionStammdaten.getInstitution()
				.getMandant()
				.getMandantIdentifier()
		);
		LOG.debug(
			"Email fuer sendInfoAuszahlungsdatenChanged wird versendet an {}",
			email
		);
	}

	@Override
	public void sendInfoLastenausgleichProzessBeendet(
		@Nonnull String jahr,
		@Nonnull String receiverEmail,
		boolean isProcessSuccessfull,
		@Nonnull Mandant mandant
	) {
		String message = isProcessSuccessfull ?
			mailTemplateConfig
				.sendInfoLastenausgleichErfolgreichBeendet(
					receiverEmail,
					Sprache.DEUTSCH,
					jahr,
					mandant
				) :
			mailTemplateConfig
				.sendInfoLastenausgleichNichtErfolgreichBeendet(
					receiverEmail,
					Sprache.DEUTSCH,
					jahr,
					mandant
				);
		toOutboxMail(
			message,
			receiverEmail,
			mandant.getMandantIdentifier()
		);
	}

	private String findGemeindeMailAddress(Gemeinde gemeinde)
		throws EbeguEntityNotFoundException {
		GemeindeStammdaten stammdaten =
			gemeindeService.getGemeindeStammdatenByGemeindeId(
				gemeinde.getId()
			)
				.orElseThrow(
					() -> new EbeguEntityNotFoundException(
						"sendInfoLastenausgleichGemeinde",
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						gemeinde.getId()
					)
				);

		return stammdaten.getMail();
	}

}
