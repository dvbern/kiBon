/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services.wizardsteps.statusupdater;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.dokumente.anlageverzeichnis.DokumentenverzeichnisEvaluator;
import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.einstellung.EinstellungService;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.enums.WizardStepStatus;
import ch.dvbern.ebegu.finanziellesituation.FinanzielleSituationValidationService;
import ch.dvbern.ebegu.services.AntragStatusHistoryService;
import ch.dvbern.ebegu.services.DokumentGrundService;
import ch.dvbern.ebegu.services.ErwerbspensumService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GeneratedDokumentService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.KindService;
import ch.dvbern.ebegu.services.MailService;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.abwesenheit.WizardStepStatusUpdaterAbwesenheit;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.betreuung.WizardStepStatusUpdaterBetreuung;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.dokumente.WizardStepStatusUpdaterDokumente;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.einkommensverschlechterung.WizardStepStatusUpdaterEinkommensverschlechterung;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.erwerbspensum.WizardStepStatusUpdaterErwerbspensum;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.familiensituation.SharedWizardStepStatusUpdaterFamiliensituation;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.finanziellesituation.WizardStepStatusUpdaterFinSit;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.gesuchsteller.WizardStepStatusUpdaterGesuchsteller;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.kinder.WizardStepStatusUpdaterKinder;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.umzug.WizardStepStatusUpdaterUmzug;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.verfuegen.WizardStepStatusUpdaterVerfuegen;
import ch.dvbern.ebegu.test.TestDataUtil;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@ExtendWith(EasyMockExtension.class)
class WizardStepStatusUpdaterTest extends EasyMockSupport {

	@Mock
	KindService kindService;

	@Mock
	ErwerbspensumService erwerbspensumService;

	@Mock
	EinstellungService einstellungService;

	@Mock
	PrincipalBean principalBean;

	@Mock
	DokumentenverzeichnisEvaluator dokumentenverzeichnisEvaluator;

	@Mock
	DokumentGrundService dokumentGrundService;

	@Mock
	MailService mailService;

	@Mock
	GemeindeService gemeindeService;

	@Mock
	GeneratedDokumentService generatedDokumentService;

	@Mock
	EbeguConfiguration ebeguConfiguration;

	@Mock
	AntragStatusHistoryService antragStatusHistoryService;

	@Mock
	GesuchService gesuchService;

	@Mock
	WizardStepStatusUpdaterErwerbspensum updaterErwerbspensum;
	@Mock
	SharedWizardStepStatusUpdaterFamiliensituation updaterFamiliensituation;

	@Mock
	WizardStepStatusUpdaterBetreuung updaterBetreuung;

	@Mock
	WizardStepStatusUpdaterFinSit wizardStepStatusUpdaterFinSit;

	@Mock
	WizardStepStatusUpdaterKinder updaterKinder;

	@Mock
	WizardStepStatusUpdaterDokumente updaterDokumente;

	@Mock
	WizardStepStatusUpdaterVerfuegen updaterVerfuegen;

	@Mock
	WizardStepStatusUpdaterGesuchsteller wizardStepStatusUpdaterGesuchsteller;

	@Mock
	WizardStepStatusUpdaterUmzug wizardStepStatusUpdaterUmzug;
	@Mock
	WizardStepStatusUpdaterAbwesenheit wizardStepStatusUpdaterAbwesenheit;
	@Mock
	WizardStepStatusUpdaterEinkommensverschlechterung wizardStepStatusUpdaterEinkommensverschlechterung;

	@Mock
	WizardStepStatusUpdater updater;

	FinanzielleSituationValidationService finanzielleSituationValidationService =
		new FinanzielleSituationValidationService();

	@BeforeEach
	void setup() {
		updaterErwerbspensum =
			new WizardStepStatusUpdaterErwerbspensum(
				einstellungService,
				erwerbspensumService,
				gesuchService,
				null
			);
		updaterFamiliensituation =
			new SharedWizardStepStatusUpdaterFamiliensituation(
				kindService,
				erwerbspensumService,
				einstellungService,
				updaterErwerbspensum,
				finanzielleSituationValidationService,
				gesuchService,
				null,
				wizardStepStatusUpdaterGesuchsteller,
				wizardStepStatusUpdaterFinSit,
				wizardStepStatusUpdaterEinkommensverschlechterung
			);
		updaterBetreuung =
			new WizardStepStatusUpdaterBetreuung(
				principalBean,
				updaterErwerbspensum,
				finanzielleSituationValidationService,
				gesuchService,
				null
			);
		wizardStepStatusUpdaterFinSit = new WizardStepStatusUpdaterFinSit(
			finanzielleSituationValidationService,
			wizardStepStatusUpdaterEinkommensverschlechterung,
			gesuchService,
			null
		);
		updaterKinder =
			new WizardStepStatusUpdaterKinder(
				updaterBetreuung,
				updaterErwerbspensum,
				kindService,
				gesuchService,
				null
			);
		updaterDokumente =
			new WizardStepStatusUpdaterDokumente(
				dokumentenverzeichnisEvaluator,
				dokumentGrundService,
				gesuchService,
				null
			);
		updaterVerfuegen = new WizardStepStatusUpdaterVerfuegen(
			gesuchService,
			generatedDokumentService,
			gemeindeService,
			mailService,
			ebeguConfiguration,
			antragStatusHistoryService,
			null
		);
		updater = new WizardStepStatusUpdater(
			updaterFamiliensituation,
			updaterBetreuung,
			updaterErwerbspensum,
			updaterKinder,
			updaterDokumente,
			updaterVerfuegen,
			wizardStepStatusUpdaterFinSit,
			wizardStepStatusUpdaterGesuchsteller,
			wizardStepStatusUpdaterUmzug,
			wizardStepStatusUpdaterAbwesenheit,
			wizardStepStatusUpdaterEinkommensverschlechterung
		);
	}

	@Nested
	class FamilienSituationUpdateTest {

		@Test
		void beschaeftigungspensumShouldBeOKToNOKOnKardinalitaetAlleineToZuZweit() {
			Gesuch gesuch = setupGesuchWithOneGS();
			Familiensituation familiensituation = gesuch
				.extractFamiliensituation();
			assertThat(familiensituation, notNullValue());
			assertThat(gesuch.getFamiliensituationContainer(), notNullValue());
			familiensituation.setGeteilteObhut(true);
			familiensituation.setGesuchstellerKardinalitaet(
				EnumGesuchstellerKardinalitaet.ALLEINE
			);

			Familiensituation newFamiliensituation =
				familiensituation.copyFamiliensituation(
					new Familiensituation(),
					AntragCopyType.MUTATION
				);
			newFamiliensituation.setGesuchstellerKardinalitaet(
				EnumGesuchstellerKardinalitaet.ZU_ZWEIT
			);

			gesuch.getFamiliensituationContainer()
				.setFamiliensituationJA(newFamiliensituation);

			WizardStep beschaeftigungspensumStep =
				TestDataUtil.createWizardStepObject(
					gesuch,
					WizardStepName.ERWERBSPENSUM,
					WizardStepStatus.OK
				);
			final List<WizardStep> wizardSteps = List.of(
				beschaeftigungspensumStep
			);

			expect(
				einstellungService.findEinstellung(
					EinstellungKey.GESUCH_BEENDEN_BEI_TAUSCH_GS2,
					gesuch.extractGemeinde(),
					gesuch.getGesuchsperiode()
				)
			).andReturn(
				new Einstellung(
					EinstellungKey.GESUCH_BEENDEN_BEI_TAUSCH_GS2,
					"true",
					gesuch.getGesuchsperiode()
				)
			).times(2);
			expect(erwerbspensumService.isErwerbspensumRequired(gesuch))
				.andReturn(true);
			expect(
				einstellungService.findEinstellung(
					EinstellungKey.ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM,
					gesuch.extractGemeinde(),
					gesuch.getGesuchsperiode()
				)
			).andReturn(
				new Einstellung(
					EinstellungKey.ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM,
					"ABHAENGING",
					gesuch.getGesuchsperiode()
				)
			).times(2);
			replayAll();

			updater.updateAllStatus(
				wizardSteps,
				familiensituation,
				newFamiliensituation,
				WizardStepName.FAMILIENSITUATION,
				0
			);

			assertThat(
				beschaeftigungspensumStep.getWizardStepStatus(),
				is(WizardStepStatus.NOK)
			);
		}

		@Test
		void beschaeftigungspensumShouldBeNOKToNOKOnKardinalitaetAlleineToZuZweit() {
			Gesuch gesuch = setupGesuchWithOneGS();
			Familiensituation familiensituation = gesuch
				.extractFamiliensituation();
			assertThat(familiensituation, notNullValue());
			assertThat(gesuch.getFamiliensituationContainer(), notNullValue());
			assertThat(gesuch.getGesuchsteller1(), notNullValue());

			familiensituation.setGeteilteObhut(true);
			familiensituation.setGesuchstellerKardinalitaet(
				EnumGesuchstellerKardinalitaet.ALLEINE
			);
			gesuch.getGesuchsteller1().getErwerbspensenContainers().clear();

			Familiensituation newFamiliensituation =
				familiensituation.copyFamiliensituation(
					new Familiensituation(),
					AntragCopyType.MUTATION
				);
			newFamiliensituation.setGesuchstellerKardinalitaet(
				EnumGesuchstellerKardinalitaet.ZU_ZWEIT
			);

			gesuch.getFamiliensituationContainer()
				.setFamiliensituationJA(newFamiliensituation);

			WizardStep beschaeftigungspensumStep =
				TestDataUtil.createWizardStepObject(
					gesuch,
					WizardStepName.ERWERBSPENSUM,
					WizardStepStatus.NOK
				);
			final List<WizardStep> wizardSteps = List.of(
				beschaeftigungspensumStep
			);

			expect(erwerbspensumService.isErwerbspensumRequired(gesuch))
				.andReturn(true);
			replayAll();

			updater.updateAllStatus(
				wizardSteps,
				familiensituation,
				newFamiliensituation,
				WizardStepName.FAMILIENSITUATION,
				0
			);

			assertThat(
				beschaeftigungspensumStep.getWizardStepStatus(),
				is(WizardStepStatus.NOK)
			);
		}

		@Test
		void beschaeftigungspensumShouldBeOKToOKOnKardinalitaetZuZweitToAlleine() {
			Gesuch gesuch = setupGesuchWithOneGS();
			Familiensituation familiensituation = gesuch
				.extractFamiliensituation();
			assertThat(familiensituation, notNullValue());
			assertThat(gesuch.getFamiliensituationContainer(), notNullValue());
			familiensituation.setGeteilteObhut(true);
			familiensituation.setGesuchstellerKardinalitaet(
				EnumGesuchstellerKardinalitaet.ZU_ZWEIT
			);

			Familiensituation newFamiliensituation =
				familiensituation.copyFamiliensituation(
					new Familiensituation(),
					AntragCopyType.MUTATION
				);
			newFamiliensituation.setGesuchstellerKardinalitaet(
				EnumGesuchstellerKardinalitaet.ALLEINE
			);

			gesuch.getFamiliensituationContainer()
				.setFamiliensituationJA(newFamiliensituation);

			WizardStep beschaeftigungspensumStep =
				TestDataUtil.createWizardStepObject(
					gesuch,
					WizardStepName.ERWERBSPENSUM,
					WizardStepStatus.OK
				);
			final List<WizardStep> wizardSteps = List.of(
				beschaeftigungspensumStep
			);

			expect(erwerbspensumService.isErwerbspensumRequired(gesuch))
				.andReturn(true);
			expect(
				einstellungService.findEinstellung(
					EinstellungKey.GESUCH_BEENDEN_BEI_TAUSCH_GS2,
					gesuch.extractGemeinde(),
					gesuch.getGesuchsperiode()
				)
			).andReturn(
				new Einstellung(
					EinstellungKey.GESUCH_BEENDEN_BEI_TAUSCH_GS2,
					"true",
					gesuch.getGesuchsperiode()
				)
			).times(2);
			expect(
				einstellungService.findEinstellung(
					EinstellungKey.ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM,
					gesuch.extractGemeinde(),
					gesuch.getGesuchsperiode()
				)
			).andReturn(
				new Einstellung(
					EinstellungKey.ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM,
					"ABHAENGING",
					gesuch.getGesuchsperiode()
				)
			).times(2);
			replayAll();

			updater.updateAllStatus(
				wizardSteps,
				familiensituation,
				newFamiliensituation,
				WizardStepName.FAMILIENSITUATION,
				0
			);

			assertThat(
				beschaeftigungspensumStep.getWizardStepStatus(),
				is(WizardStepStatus.OK)
			);
		}

		@Test
		void beschaeftigungspensumShouldBeNOKToOKOnKardinalitaetZuZweitToAlleineIfBeschaeftigungspensumGS1Present() {
			Gesuch gesuch = setupGesuchWithOneGS();
			Familiensituation familiensituation = gesuch
				.extractFamiliensituation();
			assertThat(familiensituation, notNullValue());
			assertThat(gesuch.getFamiliensituationContainer(), notNullValue());
			familiensituation.setGeteilteObhut(true);
			familiensituation.setGesuchstellerKardinalitaet(
				EnumGesuchstellerKardinalitaet.ZU_ZWEIT
			);

			Familiensituation newFamiliensituation =
				familiensituation.copyFamiliensituation(
					new Familiensituation(),
					AntragCopyType.MUTATION
				);
			newFamiliensituation.setGesuchstellerKardinalitaet(
				EnumGesuchstellerKardinalitaet.ALLEINE
			);

			gesuch.getFamiliensituationContainer()
				.setFamiliensituationJA(newFamiliensituation);

			WizardStep beschaeftigungspensumStep =
				TestDataUtil.createWizardStepObject(
					gesuch,
					WizardStepName.ERWERBSPENSUM,
					WizardStepStatus.NOK
				);
			final List<WizardStep> wizardSteps = List.of(
				beschaeftigungspensumStep
			);

			expect(erwerbspensumService.isErwerbspensumRequired(gesuch))
				.andReturn(true);
			expect(
				einstellungService.findEinstellung(
					EinstellungKey.GESUCH_BEENDEN_BEI_TAUSCH_GS2,
					gesuch.extractGemeinde(),
					gesuch.getGesuchsperiode()
				)
			).andReturn(
				new Einstellung(
					EinstellungKey.GESUCH_BEENDEN_BEI_TAUSCH_GS2,
					"true",
					gesuch.getGesuchsperiode()
				)
			).times(2);

			expect(
				einstellungService.findEinstellung(
					EinstellungKey.ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM,
					gesuch.extractGemeinde(),
					gesuch.getGesuchsperiode()
				)
			).andReturn(
				new Einstellung(
					EinstellungKey.ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM,
					"ABHAENGING",
					gesuch.getGesuchsperiode()
				)
			).times(2);
			replayAll();

			updater.updateAllStatus(
				wizardSteps,
				familiensituation,
				newFamiliensituation,
				WizardStepName.FAMILIENSITUATION,
				0
			);

			assertThat(
				beschaeftigungspensumStep.getWizardStepStatus(),
				is(WizardStepStatus.OK)
			);
		}

		@Test
		void beschaeftigungspensumShouldBeNOKToNOKOnKardinalitaetZuZweitToAlleineIfBeschaeftigungspensumGS1NotPresent() {
			Gesuch gesuch = TestDataUtil.createDefaultGesuch();
			gesuch.setGesuchsteller1(
				TestDataUtil.createDefaultGesuchstellerContainer()
			);
			Familiensituation familiensituation = gesuch
				.extractFamiliensituation();
			assertThat(familiensituation, notNullValue());
			assertThat(gesuch.getFamiliensituationContainer(), notNullValue());
			assertThat(gesuch.getGesuchsteller1(), notNullValue());
			familiensituation.setGeteilteObhut(false);
			familiensituation.setGesuchstellerKardinalitaet(
				EnumGesuchstellerKardinalitaet.ZU_ZWEIT
			);
			gesuch.getGesuchsteller1().getErwerbspensenContainers().clear();

			Familiensituation newFamiliensituation =
				familiensituation.copyFamiliensituation(
					new Familiensituation(),
					AntragCopyType.MUTATION
				);
			newFamiliensituation.setGesuchstellerKardinalitaet(
				EnumGesuchstellerKardinalitaet.ALLEINE
			);

			gesuch.getFamiliensituationContainer()
				.setFamiliensituationJA(newFamiliensituation);

			WizardStep beschaeftigungspensumStep =
				TestDataUtil.createWizardStepObject(
					gesuch,
					WizardStepName.ERWERBSPENSUM,
					WizardStepStatus.OK
				);
			final List<WizardStep> wizardSteps = List.of(
				beschaeftigungspensumStep
			);

			expect(erwerbspensumService.isErwerbspensumRequired(gesuch))
				.andReturn(true);
			replayAll();

			updater.updateAllStatus(
				wizardSteps,
				familiensituation,
				newFamiliensituation,
				WizardStepName.FAMILIENSITUATION,
				0
			);

			assertThat(
				beschaeftigungspensumStep.getWizardStepStatus(),
				is(WizardStepStatus.NOK)
			);
		}

		@Nonnull
		private Gesuch setupGesuchWithOneGS() {
			Gesuch gesuch = TestDataUtil.createDefaultGesuch();
			gesuch.setGesuchsteller1(
				TestDataUtil.createDefaultGesuchstellerContainer()
			);
			assertThat(gesuch.getGesuchsteller1(), notNullValue());
			gesuch.getGesuchsteller1()
				.getErwerbspensenContainers()
				.add(TestDataUtil.createErwerbspensumContainer());
			Objects.requireNonNull(gesuch.extractFamiliensituation())
				.setFkjvFamSit(true);
			return gesuch;
		}
	}

}
