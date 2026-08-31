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
 */

package ch.dvbern.ebegu.mail;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.annotations.SnapshotName;
import au.com.origin.snapshots.junit5.SnapshotExtension;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.einstellung.ApplicationPropertyKey;
import ch.dvbern.ebegu.einstellung.ApplicationPropertyService;
import ch.dvbern.ebegu.entities.AnmeldungFerieninsel;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.entities.gemeindeantrag.lastenausgleichtagesschulen.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.BenutzerStatus;
import ch.dvbern.ebegu.enums.GemeindeAngebotTyp;
import ch.dvbern.ebegu.enums.MitteilungTeilnehmerTyp;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.services.EinladungService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.test.mandant.MandantFactory;
import ch.dvbern.ebegu.test.util.TestDataInstitutionStammdatenBuilder;
import ch.dvbern.ebegu.testfaelle.Testfall01_WaeltiDagmar;
import ch.dvbern.ebegu.util.DateUtil;
import ch.dvbern.ebegu.util.MandantLocaleVisitor;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import lombok.Getter;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * <p>
 * Snapshot tests for every mail that {@link MailTemplateConfiguration} generates from the FreeMarker templates - one
 * per value of {@link MailTemplate}.
 * </p>
 * <p>
 * The complete generated message is verified, including the headers (From, To, Subject) and the HTML body. Most tests
 * run for every Mandant, since templates and translations can be overridden per Mandant (see
 * {@link MandantLocaleVisitor}). Some tests only run for a single mandant since they are not
 * currently meant to be mandantfähig.
 * </p>
 * <p>
 * Mails with a French template additionally run in French, but only for the Mandanten in
 * {@link #MANDANTEN_WITH_FRENCH}. Which language variants exist differs per template, so every test picks its
 * argument source accordingly: {@link GermanAndFrenchTest}, {@link GermanOnlyTest} or
 * {@link SingleMandantTest}.
 * </p>
 *
 * @see <a href="https://intra.dvbern.ch/spaces/KIB/pages/321978667/Snapshot-Testing">kiBon-Dokumentation</a>
 */
@ExtendWith({ MockitoExtension.class, SnapshotExtension.class })
// The dependencies are stubbed once for all tests; a single template only ever needs a subset of them.
@MockitoSettings(strictness = Strictness.LENIENT)
class MailTemplateConfigurationSnapshotTest {

	/** Fully qualified name of this class, needed to reference the argument sources from the nested test classes. */
	private static final String SELF =
		"ch.dvbern.ebegu.mail.MailTemplateConfigurationSnapshotTest";

	/** Only these Mandanten have French templates, so only they are tested in French. */
	private static final List<MandantIdentifier> MANDANTEN_WITH_FRENCH =
		List.of(MandantIdentifier.BERN, MandantIdentifier.DVB);

	private static final String EMPFAENGER_MAIL = "empfaenger@dvbern.ch";
	private static final String SENDER_ADDRESS = "kibon@dvbern.ch";
	private static final String DOWNLOAD_URL =
		"https://be.kibon.ch/download/statistik.xlsx";
	private static final String ZAHLUNGS_URL =
		"https://be.kibon.ch/zahlungsauftrag";
	private static final String SSO_INIT_URL =
		"https://be.kibon.ch/sso/init";
	private static final String LASTENAUSGLEICH_JAHR = "2025";
	private static final int ANZAHL_TAGE_BIS_LOESCHUNG = 30;

	private static final LocalDate EINLADUNG_EXPIRATION = LocalDate.of(
		2026,
		Month.MARCH,
		15
	);
	private static final LocalDate DATUM_ERSTELLUNG = LocalDate.of(
		2026,
		Month.JANUARY,
		5
	);
	private static final LocalDate EINGANGSDATUM = LocalDate.of(
		2017,
		Month.AUGUST,
		15
	);

	@Mock
	private EbeguConfiguration ebeguConfiguration;
	@Mock
	private GemeindeService gemeindeService;
	@Mock
	private EinladungService einladungService;
	@Mock
	private ApplicationPropertyService applicationPropertyService;

	@InjectMocks
	private MailTemplateConfiguration mailTemplateConfiguration =
		new MailTemplateConfiguration();

	@BeforeEach
	void setUp() {
		when(ebeguConfiguration.getIsDevmode()).thenReturn(false);
		when(ebeguConfiguration.getSenderAddress()).thenReturn(SENDER_ADDRESS);
		when(ebeguConfiguration.getFrontendBaseUrl(any())).thenAnswer(
			invocation -> getFrontendBaseUrl(invocation.getArgument(0))
		);

		when(einladungService.getExpirationDate()).thenReturn(
			EINLADUNG_EXPIRATION
		);
		when(einladungService.createInvitationLink(any(), any())).thenAnswer(
			invocation -> URI.create(
				getFrontendBaseUrl(
					invocation.getArgument(0, Benutzer.class)
						.getMandant()
						.getMandantIdentifier()
				) + "/einladung/annehmen"
			)
		);

		expectFrenchEnabledWhenHavingSprache(Sprache.DEUTSCH);
	}

	@Nested
	class GesuchMails {

		@SuppressWarnings({ "unused", "InstanceVariableMayNotBeInitialized" })
		private Expect expect;

		@SnapshotName("InfoBetreuungenBestaetigt")
		@GermanAndFrenchTest
		void infoBetreuungenBestaetigt_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);

			String mail = mailTemplateConfiguration
				.getInfoBetreuungenBestaetigt(
					fixture.getGesuch(),
					fixture.getGesuchsteller(),
					EMPFAENGER_MAIL,
					sprache
				);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}

		@SnapshotName("InfoVerfuegtGesuch")
		@GermanAndFrenchTest
		void infoVerfuegtGesuch_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);

			String mail = mailTemplateConfiguration.getInfoVerfuegtGesuch(
				fixture.getGesuch(),
				fixture.getGesuchsteller(),
				EMPFAENGER_MAIL,
				sprache
			);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}

		@SnapshotName("InfoVerfuegtMutation")
		@GermanAndFrenchTest
		void infoVerfuegtMutation_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);
			fixture.getGesuch().setTyp(AntragTyp.MUTATION);

			String mail = mailTemplateConfiguration.getInfoVerfuegtMutation(
				fixture.getGesuch(),
				fixture.getGesuchsteller(),
				EMPFAENGER_MAIL,
				sprache
			);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}

		@SnapshotName("InfoMahnung")
		@GermanAndFrenchTest
		void infoMahnung_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);

			String mail = mailTemplateConfiguration.getInfoMahnung(
				fixture.getGesuch(),
				fixture.getGesuchsteller(),
				EMPFAENGER_MAIL,
				sprache
			);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}

		@SnapshotName("WarnungGesuchNichtFreigegeben")
		@GermanAndFrenchTest
		void warnungGesuchNichtFreigegeben_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);

			String mail =
				mailTemplateConfiguration.getWarnungGesuchNichtFreigegeben(
					fixture.getGesuch(),
					fixture.getGesuchsteller(),
					EMPFAENGER_MAIL,
					ANZAHL_TAGE_BIS_LOESCHUNG,
					sprache
				);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}

		@SnapshotName("WarnungFreigabequittungFehlt")
		@GermanAndFrenchTest
		void warnungFreigabequittungFehlt_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);
			when(
				gemeindeService.getGemeindeStammdatenByGemeindeId(anyString())
			).thenReturn(Optional.of(fixture.createGemeindeStammdaten()));

			String mail =
				mailTemplateConfiguration.getWarnungFreigabequittungFehlt(
					fixture.getGesuch(),
					fixture.getGesuchsteller(),
					EMPFAENGER_MAIL,
					ANZAHL_TAGE_BIS_LOESCHUNG,
					sprache
				);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail,
				Map.of(
					DateUtil.toFormattedDate(
						LocalDate.now()
							.plusDays(ANZAHL_TAGE_BIS_LOESCHUNG)
							.minusDays(1)
					),
					"<DATUM_LOESCHUNG>"
				)
			);
		}

		@SnapshotName("InfoGesuchGeloescht")
		@GermanAndFrenchTest
		void infoGesuchGeloescht_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);

			String mail = mailTemplateConfiguration.getInfoGesuchGeloescht(
				fixture.getGesuch(),
				fixture.getGesuchsteller(),
				EMPFAENGER_MAIL,
				sprache
			);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}

		@SnapshotName("InfoFreischaltungGesuchsperiode")
		@GermanAndFrenchTest
		void infoFreischaltungGesuchsperiode_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);

			String mail =
				mailTemplateConfiguration.getInfoFreischaltungGesuchsperiode(
					fixture.getGesuchsperiode(),
					fixture.getGesuchsteller(),
					EMPFAENGER_MAIL,
					fixture.getGesuch(),
					sprache
				);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}

		@SnapshotName("InfoGesuchVerfuegtVerantwortlicherTS")
		@GermanAndFrenchTest
		void infoGesuchVerfuegtVerantwortlicherTS_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);

			String mail = mailTemplateConfiguration
				.getInfoGesuchVerfuegtVerantwortlicherTS(
					fixture.getGesuch(),
					EMPFAENGER_MAIL,
					List.of(sprache),
					fixture.createSachbearbeiterGemeinde()
				);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}
	}

	@Nested
	class BetreuungMails {

		@SuppressWarnings({ "unused", "InstanceVariableMayNotBeInitialized" })
		private Expect expect;

		@SnapshotName("InfoBetreuungAbgelehnt")
		@GermanAndFrenchTest
		void infoBetreuungAbgelehnt_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);

			String mail = mailTemplateConfiguration.getInfoBetreuungAbgelehnt(
				fixture.getBetreuung(),
				fixture.getGesuchsteller(),
				EMPFAENGER_MAIL,
				sprache
			);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}

		@SnapshotName("InfoBetreuungVerfuegt")
		@GermanAndFrenchTest
		void infoBetreuungVerfuegt_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);

			String mail = mailTemplateConfiguration.getInfoBetreuungVerfuegt(
				fixture.getBetreuung(),
				fixture.getFall(),
				fixture.getGesuchsteller(),
				fixture.getKind(),
				fixture.getInstitution(),
				EMPFAENGER_MAIL,
				fixture.getKind().getGeburtsdatum(),
				sprache
			);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}

		@SnapshotName("InfoBetreuungGeloescht")
		@GermanAndFrenchTest
		void infoBetreuungGeloescht_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);

			String mail = mailTemplateConfiguration.getInfoBetreuungGeloescht(
				fixture.getBetreuung(),
				fixture.getFall(),
				fixture.getGesuchsteller(),
				fixture.getKind(),
				fixture.getInstitution(),
				EMPFAENGER_MAIL,
				DATUM_ERSTELLUNG,
				fixture.getKind().getGeburtsdatum(),
				sprache
			);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}
	}

	@Nested
	class AnmeldungMails {

		@SuppressWarnings({ "unused", "InstanceVariableMayNotBeInitialized" })
		private Expect expect;

		@SnapshotName("InfoSchulamtAnmeldungTagesschuleUebernommen")
		@GermanAndFrenchTest
		void anmeldungTagesschuleUebernommen_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);

			String mail = mailTemplateConfiguration
				.getInfoSchulamtAnmeldungTagesschuleUebernommen(
					fixture.createAnmeldungTagesschule(),
					fixture.getGesuchsteller(),
					EMPFAENGER_MAIL,
					sprache
				);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}

		@SnapshotName("InfoSchulamtAnmeldungTagesschuleAkzeptiert")
		@GermanAndFrenchTest
		void anmeldungTagesschuleAkzeptiert_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);

			String mail = mailTemplateConfiguration
				.getInfoSchulamtAnmeldungTagesschuleAkzeptiert(
					fixture.createAnmeldungTagesschule(),
					fixture.getGesuchsteller(),
					EMPFAENGER_MAIL,
					sprache
				);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}

		@SnapshotName("InfoSchulamtAnmeldungAbgelehnt")
		@GermanAndFrenchTest
		void schulamtAnmeldungAbgelehnt_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);

			String mail = mailTemplateConfiguration
				.getInfoSchulamtAnmeldungAbgelehnt(
					fixture.createAnmeldungTagesschule(),
					fixture.getGesuchsteller(),
					EMPFAENGER_MAIL,
					sprache
				);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}

		@SnapshotName("InfoSchulamtAnmeldungStorniert")
		@GermanAndFrenchTest
		void schulamtAnmeldungStorniert_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);

			String mail = mailTemplateConfiguration
				.getInfoSchulamtAnmeldungStorniert(
					fixture.createAnmeldungTagesschule(),
					fixture.getGesuchsteller(),
					EMPFAENGER_MAIL,
					sprache
				);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}

		@SnapshotName("InfoSchulamtAnmeldungFerieninselUebernommen")
		@GermanAndFrenchTest
		void anmeldungFerieninselUebernommen_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);

			String mail = mailTemplateConfiguration
				.getInfoSchulamtAnmeldungFerieninselUebernommen(
					fixture.createAnmeldungFerieninsel(),
					fixture.getGesuchsteller(),
					EMPFAENGER_MAIL,
					sprache
				);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}
	}

	@Nested
	class MitteilungMails {

		@SuppressWarnings({ "unused", "InstanceVariableMayNotBeInitialized" })
		private Expect expect;

		@SnapshotName("InfoMitteilungErhalten")
		@GermanAndFrenchTest
		void infoMitteilungErhalten_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);

			String mail = mailTemplateConfiguration.getInfoMitteilungErhalten(
				fixture.createMitteilung(),
				EMPFAENGER_MAIL,
				List.of(sprache)
			);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}

		@SnapshotName("InfoOffenePendenzenNeueMitteilungInstitution")
		@GermanAndFrenchTest
		void infoOffenePendenzenInstitution_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);
			expectFrenchEnabledWhenHavingSprache(sprache);

			String mail = mailTemplateConfiguration
				.getInfoOffenePendenzenNeuMitteilungInstitution(
					fixture.createInstitutionStammdaten(),
					EMPFAENGER_MAIL,
					true,
					true
				);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}

		@SnapshotName("InfoOffenePendenzenNeueMitteilungGemeindeMitarbeitende")
		@GermanAndFrenchTest
		void infoOffenePendenzenGemeindeMitarbeitende_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);
			expectFrenchEnabledWhenHavingSprache(sprache);
			String gemeindeName = fixture.getGemeinde().getName();

			String mail = mailTemplateConfiguration
				.getInfoOffenePendenzenNeuMitteilungGemeindeMitarbeitende(
					fixture.createSachbearbeiterGemeinde(),
					true,
					true,
					gemeindeName,
					gemeindeName
				);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}
	}

	@Nested
	class GemeindeMails {

		@SuppressWarnings({ "unused", "InstanceVariableMayNotBeInitialized" })
		private Expect expect;

		@SnapshotName("InfoGemeindeAngebotAktiviert")
		@GermanAndFrenchTest
		void infoGemeindeAngebotAktiviert_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);

			String mail =
				mailTemplateConfiguration.getInfoGemeindeAngebotAktiviert(
					fixture.getGemeinde(),
					EMPFAENGER_MAIL,
					GemeindeAngebotTyp.TAGESSCHULE,
					List.of(sprache)
				);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}

		@SnapshotName("InfoGemeindeLastenausgleichDurch")
		@GermanAndFrenchTest
		void infoGemeindeLastenausgleichDurch_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);

			String mail =
				mailTemplateConfiguration.getInfoGemeindeLastenausgleichDurch(
					fixture.createLastenausgleich(),
					List.of(sprache),
					EMPFAENGER_MAIL
				);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}

		@SnapshotName("InfoGemeindeLastenausgleichZurueckAnGemeinde")
		@GermanAndFrenchTest
		void infoLATSZurueckAnGemeinde_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);

			String mail = mailTemplateConfiguration
				.getInfoGemeindeLastenausgleichTagesschuleZurueckAnGemeinde(
					fixture.createLatsContainer(),
					List.of(sprache),
					EMPFAENGER_MAIL
				);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}

		@SnapshotName("ReminderFirstGemeindeKennzahlen")
		@ParameterizedTest
		@EnumSource(Sprache.class)
		void reminderFirstGemeindeKennzahlen_shouldMatchSnapshot(
			Sprache sprache
		) {
			var identifier = MandantIdentifier.BERN;
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);
			expectFrenchEnabledWhenHavingSprache(Sprache.FRANZOESISCH);

			String mail =
				mailTemplateConfiguration.getGemeindeKennzahlenFirstErinnerung(
					fixture.getMandant(),
					EMPFAENGER_MAIL
				);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}

		@SnapshotName("ReminderSecondGemeindeKennzahlen")
		@ParameterizedTest
		@EnumSource(Sprache.class)
		void reminderSecondGemeindeKennzahlen_shouldMatchSnapshot(
			Sprache sprache
		) {
			var identifier = MandantIdentifier.BERN;
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);
			expectFrenchEnabledWhenHavingSprache(Sprache.FRANZOESISCH);

			String mail =
				mailTemplateConfiguration.getGemeindeKennzahlenSecondErinnerung(
					fixture.getMandant(),
					EMPFAENGER_MAIL
				);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}

		@SnapshotName("InfoGemeindeInstitutionAuszahlungsdatenChanged")
		@GermanOnlyTest
		void infoAuszahlungsdatenChanged_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);

			String mail = mailTemplateConfiguration
				.getInfoGemeindeInstitutionAuszahlungsdatenChanged(
					fixture.createInstitutionStammdaten(),
					EMPFAENGER_MAIL
				);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}
	}

	@Nested
	class BenutzerMails {

		@SuppressWarnings({ "unused", "InstanceVariableMayNotBeInitialized" })
		private Expect expect;

		@SnapshotName("BenutzerEinladung")
		@GermanAndFrenchTest
		void benutzerEinladung_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);
			expectFrenchEnabledWhenHavingSprache(sprache);

			String mail = mailTemplateConfiguration.getBenutzerEinladung(
				fixture.createSachbearbeiterGemeinde(),
				Einladung.forMitarbeiter(fixture.createSachbearbeiterGemeinde())
			);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}

		@SnapshotName("GesuchstellerInitZPV")
		@SingleMandantTest
		void gesuchstellerInitZpv_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			String mail = mailTemplateConfiguration.getInitGSZPVNr(
				SSO_INIT_URL,
				sprache,
				EMPFAENGER_MAIL
			);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}
	}

	@Nested
	class SystemMails {

		@SuppressWarnings({ "unused", "InstanceVariableMayNotBeInitialized" })
		private Expect expect;

		@SnapshotName("InfoStatistikGeneriert")
		@GermanAndFrenchTest
		void infoStatistikGeneriert_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);

			String mail = mailTemplateConfiguration.sendInfoStatistikGeneriert(
				EMPFAENGER_MAIL,
				DOWNLOAD_URL,
				sprache,
				fixture.getMandant()
			);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}

		@SnapshotName("InfoZahlungslaufGeneriert")
		@GermanAndFrenchTest
		void infoZahlungslaufGeneriert_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);

			String mail =
				mailTemplateConfiguration.sendInfoZahlungslaufGeneriert(
					EMPFAENGER_MAIL,
					ZAHLUNGS_URL,
					sprache,
					fixture.getMandant()
				);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}

		@SnapshotName("InfoZahlungslaufNichtErfolgreichGeneriert")
		@GermanAndFrenchTest
		void infoZahlungslaufNichtErfolgreichGeneriert_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);

			String mail = mailTemplateConfiguration
				.createInfoZahlungslaufNichtErfolgreichErstelltMail(
					EMPFAENGER_MAIL,
					sprache,
					fixture.getMandant()
				);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}

		/**
		 * The Lastenausgleich mails are currently only sent for BERN and always in German
		 */
		@SnapshotName("InfoLastenausgleichErfolgreich")
		@Test
		void infoLastenausgleichErfolgreich_shouldMatchSnapshot() {
			var identifier = MandantIdentifier.BERN;
			var sprache = Sprache.DEUTSCH;
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);

			String mail = mailTemplateConfiguration
				.sendInfoLastenausgleichErfolgreichBeendet(
					EMPFAENGER_MAIL,
					sprache,
					LASTENAUSGLEICH_JAHR,
					fixture.getMandant()
				);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}

		/** @see #infoLastenausgleichErfolgreich_shouldMatchSnapshot() */
		@SnapshotName("InfoLastenausgleichNichtErfolgreich")
		@GermanOnlyTest
		void infoLastenausgleichNichtErfolgreich_shouldMatchSnapshot(
			MandantIdentifier identifier,
			Sprache sprache
		) {
			MailTemplateFixture fixture = new MailTemplateFixture(identifier);

			String mail = mailTemplateConfiguration
				.sendInfoLastenausgleichNichtErfolgreichBeendet(
					EMPFAENGER_MAIL,
					sprache,
					LASTENAUSGLEICH_JAHR,
					fixture.getMandant()
				);

			MailTemplateSnapshotHelper.matchSnapshot(
				expect,
				identifier,
				sprache,
				mail
			);
		}
	}

	private void expectFrenchEnabledWhenHavingSprache(@Nonnull Sprache sprache) {
		when(
			applicationPropertyService.findApplicationPropertyAsBoolean(
				eq(ApplicationPropertyKey.FRENCH_ENABLED),
				any()
			)
		).thenReturn(sprache == Sprache.FRANZOESISCH);
	}

	@Nonnull
	private static String getFrontendBaseUrl(
		@Nonnull MandantIdentifier identifier
	) {
		return "https://" + identifier.getUrlCode() + ".kibon.example.com";
	}

	/**
	 * <p>
	 * Annotation representing a parameterized test case for scenarios involving
	 * both German and French languages. Test methods annotated with this will
	 * be executed for a combination of Mandant identifiers and languages,
	 * German and French.
	 * </p>
	 * <p>
	 * Usage Requirements:
	 * <ul>
	 * <li>Use it to annotate test methods that need to be executed for both German and French languages.</li>
	 * <li>The annotated method must accept parameters matching the structure </li>
	 * provided by the `germanAndFrench()` method.
	 * </ul>
	 * </p>
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@ParameterizedTest
	@MethodSource(SELF + "#germanAndFrench")
	@interface GermanAndFrenchTest {
	}

	/** Every Mandant in German, plus the Mandanten of {@link #MANDANTEN_WITH_FRENCH} in French. */
	static Stream<Arguments> germanAndFrench() {
		return Stream.concat(
			german(),
			MANDANTEN_WITH_FRENCH.stream()
				.map(mandant -> Arguments.of(mandant, Sprache.FRANZOESISCH))
		);
	}

	/**
	 * <p>
	 * Annotation representing a parameterized test case for scenarios involving
	 * only the German language. Test methods annotated with this will
	 * be executed for a combination of Mandant identifiers and German.
	 * </p>
	 * <p>
	 * Usage Requirements:
	 * <ul>
	 * <li>Use it to annotate test methods that need to be executed just in German.</li>
	 * <li>The annotated method must accept parameters matching the structure </li>
	 * provided by the `german()` method.
	 * </ul>
	 * </p>
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@ParameterizedTest
	@MethodSource(SELF + "#german")
	@interface GermanOnlyTest {
	}

	/** Every Mandant in German. */
	static Stream<Arguments> german() {
		return Arrays.stream(MandantIdentifier.values())
			.map(mandant -> Arguments.of(mandant, Sprache.DEUTSCH));
	}

	/**
	 * <p>
	 * Custom annotation used to mark parameterized test methods that are not specific
	 * to any Mandant in the context of mail template configuration testing.
	 * </p>
	 * <p>
	 * Usage Requirements:
	 * <ul>
	 * <li>Use it to annotate test methods that need to be executed for a single mandant
	 * in all languages.</li>
	 * <li>The annotated method must accept parameters matching the structure </li>
	 * provided by the `notMandantSpecific()` method.
	 * </ul>
	 * </p>
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@ParameterizedTest
	@MethodSource(SELF + "#notMandantSpecific")
	@interface SingleMandantTest {
	}

	/**
	 * Mails that are not mandantfähig take no Mandant at all, so every Mandant would produce the very same result.
	 * One Mandant per language is enough for those.
	 */
	static Stream<Arguments> notMandantSpecific() {
		MandantIdentifier mandant = MANDANTEN_WITH_FRENCH.get(0);

		return Stream.of(
			Arguments.of(mandant, Sprache.DEUTSCH),
			Arguments.of(mandant, Sprache.FRANZOESISCH)
		);
	}

	/**
	 * <p>
	 * Helper class for handling snapshots of mail templates during tests. It provides utilities to normalize
	 * generated mail content to compare it with stored snapshots, ensuring stability and readability in
	 * snapshot diffs.
	 * </p>
	 * <p>
	 * This class performs normalization operations such as:
	 * - Replacing dynamic data like UUIDs and dates with placeholders.
	 * - Decoding Base64-encoded headers in mail content for better readability.
	 * - Removing carriage return characters from the content.
	 * </p>
	 * <p>
	 * This helper is strictly intended for internal use during tests and serves to abstract the complexities of
	 * snapshot generation and comparison.
	 * </p>
	 */
	private static final class MailTemplateSnapshotHelper {

		private static final Pattern UUID_PATTERN = Pattern.compile(
			"\\p{XDigit}{8}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}"
		);
		private static final Pattern CARRIAGE_RETURN = Pattern.compile("\\r");
		private static final Pattern BASE64_HEADER = Pattern.compile(
			"=\\?UTF-8\\?B\\?([^?]*)\\?="
		);

		private MailTemplateSnapshotHelper() {
		}

		/**
		 * Overload of {@link #matchSnapshot(Expect, MandantIdentifier, Sprache, String, Map)} where
		 * the map of additional replacements is empty.
		 *
		 * @param expect the expectation handler used to define and verify snapshot scenarios
		 * @param identifier the identifier for the relevant Mandant (tenant) used to determine scenario context
		 * @param sprache the language in which the email is generated, represented as a Sprache enum
		 * @param generatedMail the generated email content to be validated and compared to its snapshot
		 */
		static void matchSnapshot(
			@Nonnull Expect expect,
			@Nonnull MandantIdentifier identifier,
			@Nonnull Sprache sprache,
			@Nonnull String generatedMail
		) {
			matchSnapshot(
				expect,
				identifier,
				sprache,
				generatedMail,
				Map.of()
			);
		}

		/**
		 * Compares a generated email against an expected snapshot, ensuring consistency by normalizing its content
		 * and validating the presence of required email headers before invoking the snapshot matcher. This method
		 * supports optional additional replacements to account for runtime variations in the email content.
		 *
		 * @param expect the expectation handler used to define and verify snapshot scenarios
		 * @param identifier the identifier for the relevant Mandant (tenant) used to determine the snapshot scenario
		 * context
		 * @param sprache the language in which the email is generated, represented as a Sprache enum
		 * @param generatedMail the generated email content to be normalized and validated against the snapshot
		 * @param additionalReplacements a map containing key-value pairs for additional replacements to apply during
		 * normalization
		 */
		static void matchSnapshot(
			@Nonnull Expect expect,
			@Nonnull MandantIdentifier identifier,
			@Nonnull Sprache sprache,
			@Nonnull String generatedMail,
			@Nonnull Map<String, String> additionalReplacements
		) {
			// sanity check to ensure that no incomplete mail can become a snapshot when newly generated
			assertThat(
				generatedMail,
				stringContainsInOrder(
					"From:",
					"To:",
					"Subject:",
					"Content-Type:"
				)
			);

			expect.scenario(identifier.name() + '_' + sprache.name())
				.toMatchSnapshot(
					normalize(
						generatedMail,
						new MandantLocaleVisitor(sprache.getLocale()).process(
							identifier
						),
						additionalReplacements
					)
				);
		}

		@Nonnull
		private static String normalize(
			@Nonnull String generatedMail,
			@Nonnull Locale locale,
			@Nonnull Map<String, String> additionalReplacements
		) {
			String normalized = CARRIAGE_RETURN.matcher(generatedMail)
				.replaceAll("");

			normalized = decodeBase64Headers(normalized);
			for (Map.Entry<String, String> replacement : additionalReplacements
				.entrySet()) {
				normalized = normalized.replace(
					replacement.getKey(),
					replacement.getValue()
				);
			}
			for (String today : todayInEveryFormat(locale)) {
				normalized = normalized.replace(today, "<TODAY>");
			}

			return replaceUuids(normalized);
		}

		/**
		 * Formats today's date in various styles according to the given locale and returns a list
		 * of distinct string representations, sorted by descending string length. This includes
		 * a custom LocalDate formatting and formats defined by the DateFormat class (FULL, LONG,
		 * MEDIUM, and SHORT).
		 *
		 * @param locale the locale to use for formatting the date
		 * @return a list of unique string representations of today's date in different formats,
		 * sorted by descending string length
		 */
		@Nonnull
		private static List<String> todayInEveryFormat(@Nonnull Locale locale) {
			Date today = new Date();

			return Stream.concat(
				Stream.of(DateUtil.toFormattedDate(LocalDate.now())),
				IntStream.of(
					DateFormat.FULL,
					DateFormat.LONG,
					DateFormat.MEDIUM,
					DateFormat.SHORT
				)
					.mapToObj(
						style -> DateFormat.getDateInstance(style, locale)
							.format(today)
					)
			)
				.distinct()
				.sorted(Comparator.comparingInt(String::length).reversed())
				.toList();
		}

		@Nonnull
		private static String decodeBase64Headers(@Nonnull String content) {
			return BASE64_HEADER.matcher(content)
				.replaceAll(
					result -> Matcher.quoteReplacement(
						new String(
							Base64.getDecoder().decode(result.group(1)),
							StandardCharsets.UTF_8
						)
					)
				);
		}

		/**
		 * Replaces UUIDs in the input content with unique placeholders in the format "<ID-X>",
		 * where X is a sequentially incremented number. The UUIDs are identified using a predefined pattern,
		 * ensuring that each unique UUID is replaced by a consistent placeholder.
		 *
		 * @param content the input string that may contain UUIDs to be replaced
		 * @return the resulting string where all UUIDs have been replaced with unique placeholders
		 */
		@Nonnull
		private static String replaceUuids(@Nonnull String content) {
			Map<String, String> knownIds = new LinkedHashMap<>();
			Matcher matcher = UUID_PATTERN.matcher(content);
			StringBuilder sb = new StringBuilder();

			while (matcher.find()) {
				String placeholder = knownIds.computeIfAbsent(
					matcher.group(),
					id -> "<ID-" + (knownIds.size() + 1) + '>'
				);
				matcher.appendReplacement(
					sb,
					Matcher.quoteReplacement(placeholder)
				);
			}
			matcher.appendTail(sb);

			return sb.toString();
		}
	}

	/**
	 * Fixture class to provide test data for email-template-related tests.
	 * This class is responsible for setting up various objects required to test
	 * email templates in association with the specified {@link MandantIdentifier}.
	 */
	@Getter
	private static final class MailTemplateFixture {

		@Nonnull
		private final Mandant mandant;
		@Nonnull
		private final Gesuchsperiode gesuchsperiode;
		@Nonnull
		private final Gemeinde gemeinde;
		@Nonnull
		private final Gesuch gesuch;

		MailTemplateFixture(@Nonnull MandantIdentifier identifier) {
			this.mandant = MandantFactory.fromIdentifier(identifier);
			this.gesuchsperiode = TestDataUtil.createGesuchsperiode1718(
				mandant
			);
			this.gemeinde = TestDataUtil.createGemeindeLondon(mandant);
			this.gesuch = new Testfall01_WaeltiDagmar(
				gesuchsperiode,
				true,
				gemeinde,
				new TestDataInstitutionStammdatenBuilder(gesuchsperiode)
			).setupGesuch();
			this.gesuch.getFall().setFallNummer(42);
			this.gesuch.setEingangsdatum(EINGANGSDATUM);
		}

		@Nonnull
		Fall getFall() {
			return gesuch.getFall();
		}

		@Nonnull
		Gesuchsteller getGesuchsteller() {
			return gesuch.extractGesuchsteller1().orElseThrow();
		}

		@Nonnull
		Betreuung getBetreuung() {
			return getKindContainer().getBetreuungen()
				.stream()
				.findFirst()
				.orElseThrow();
		}

		@Nonnull
		Kind getKind() {
			return getKindContainer().getKindJA();
		}

		@Nonnull
		Institution getInstitution() {
			return getBetreuung().getInstitutionStammdaten().getInstitution();
		}

		@Nonnull
		AnmeldungTagesschule createAnmeldungTagesschule() {
			return TestDataUtil.createAnmeldungTagesschuleWithModules(
				getKindContainer(),
				gesuchsperiode
			);
		}

		@Nonnull
		AnmeldungFerieninsel createAnmeldungFerieninsel() {
			KindContainer kindContainer = getKindContainer();
			AnmeldungFerieninsel anmeldung = TestDataUtil
				.createAnmeldungFerieninsel(kindContainer);
			kindContainer.getAnmeldungenFerieninsel().add(anmeldung);

			return anmeldung;
		}

		@Nonnull
		Mitteilung createMitteilung() {
			return TestDataUtil.createMitteilung(
				gesuch.getDossier(),
				createGesuchstellerBenutzer(),
				MitteilungTeilnehmerTyp.GESUCHSTELLER,
				createSachbearbeiterGemeinde(),
				MitteilungTeilnehmerTyp.JUGENDAMT
			);
		}

		@Nonnull
		InstitutionStammdaten createInstitutionStammdaten() {
			return TestDataUtil.createDefaultInstitutionStammdaten(mandant);
		}

		@Nonnull
		GemeindeStammdaten createGemeindeStammdaten() {
			return TestDataUtil.createGemeindeStammdaten(gemeinde);
		}

		@Nonnull
		Lastenausgleich createLastenausgleich() {
			Lastenausgleich lastenausgleich = new Lastenausgleich();
			lastenausgleich.setJahr(Integer.valueOf(LASTENAUSGLEICH_JAHR));
			lastenausgleich.setMandant(mandant);

			return lastenausgleich;
		}

		@Nonnull
		LastenausgleichTagesschuleAngabenGemeindeContainer createLatsContainer() {
			return TestDataUtil
				.createLastenausgleichTagesschuleAngabenGemeindeContainer(
					gesuchsperiode,
					gemeinde
				);
		}

		@Nonnull
		Benutzer createSachbearbeiterGemeinde() {
			Benutzer benutzer = createBenutzer(
				UserRole.SACHBEARBEITER_GEMEINDE,
				"sachbearbeiter",
				"Muster",
				"Sandra"
			);
			benutzer.getCurrentBerechtigung().getGemeindeList().add(gemeinde);

			return benutzer;
		}

		@Nonnull
		Benutzer createGesuchstellerBenutzer() {
			return createBenutzer(
				UserRole.GESUCHSTELLER,
				"gesuchsteller",
				"Wälti",
				"Dagmar"
			);
		}

		@Nonnull
		private Benutzer createBenutzer(
			@Nonnull UserRole role,
			@Nonnull String username,
			@Nonnull String nachname,
			@Nonnull String vorname
		) {
			Benutzer benutzer = TestDataUtil.createBenutzer(
				role,
				username,
				null,
				null,
				mandant,
				nachname,
				vorname
			);
			benutzer.setStatus(BenutzerStatus.AKTIV);
			benutzer.setEmail(EMPFAENGER_MAIL);
			setFullName(benutzer, vorname + ' ' + nachname);

			return benutzer;
		}

		/**
		 * {@link Benutzer#getFullName()} is a Hibernate {@code @Formula} and is therefore not populated outside a
		 * DB session. Since the templates use the name, we have to set it ourselves.
		 */
		private static void setFullName(
			@Nonnull Benutzer benutzer,
			@Nonnull String fullName
		) {
			try {
				FieldUtils.writeField(benutzer, "fullName", fullName, true);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(
					"Failed to set Benutzer.fullName",
					e
				);
			}
		}

		@Nonnull
		private KindContainer getKindContainer() {
			return gesuch.getKindContainers().iterator().next();
		}
	}
}
