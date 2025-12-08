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

package ch.dvbern.ebegu.services.wizardsteps.statusupdater.dokumente;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import ch.dvbern.ebegu.dokumente.anlageverzeichnis.DokumentenverzeichnisEvaluator;
import ch.dvbern.ebegu.entities.Dokument;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.enums.WizardStepStatus;
import ch.dvbern.ebegu.services.DokumentGrundService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.util.Constants;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(EasyMockExtension.class)
class WizardStepStatusUpdaterDokumenteTest extends EasyMockSupport {

	@TestSubject
	WizardStepStatusUpdaterDokumente updaterDokumente;

	@Mock
	DokumentenverzeichnisEvaluator dokumentenverzeichnisEvaluator;

	@Mock
	DokumentGrundService dokumentGrundService;

	@Mock
	GesuchService gesuchService;

	@BeforeEach
	void setup() {
		updaterDokumente =
			new WizardStepStatusUpdaterDokumente(
				dokumentenverzeichnisEvaluator,
				dokumentGrundService,
				gesuchService,
				null
			);
	}

	@Test
	void updateAllStatusStepNochNichtBesuchtReturnDirectly() {
		List<WizardStep> wizardSteps = getWizardStepsForTest(
			AntragTyp.ERSTGESUCH
		);
		wizardSteps.get(0).setWizardStepStatus(WizardStepStatus.UNBESUCHT);
		updaterDokumente.updateAllStatus(wizardSteps, null, null, null);
		assertThat(
			wizardSteps.get(0).getWizardStepStatus(),
			is(WizardStepStatus.UNBESUCHT)
		);
	}

	@ParameterizedTest
	@EnumSource(value = WizardStepName.class,
		names = "DOKUMENTE",
		mode = Mode.EXCLUDE)
	void updateAllStatusWhenStepNichtDokumenteReturnDirectly(
		WizardStepName wizardStepName
	) {
		List<WizardStep> wizardSteps = getWizardStepsForTest(
			AntragTyp.ERSTGESUCH
		);
		wizardSteps.get(0).setWizardStepName(wizardStepName);
		updaterDokumente.updateAllStatus(wizardSteps, null, null, null);
		assertThat(
			wizardSteps.get(0).getWizardStepStatus(),
			is(WizardStepStatus.IN_BEARBEITUNG)
		);
	}

	@Test
	void updateAllStatusAllDokumentNeededUploadedErstGesuch() {
		List<WizardStep> wizardSteps = getWizardStepsForTest(
			AntragTyp.ERSTGESUCH
		);
		Set<DokumentGrund> dokumentGrundSet = createDokumentGrundSetForTest(
			true
		);
		Collection<DokumentGrund> dokumentGrundCollection =
			createDokumentGrundSetForTest(true);
		expect(
			dokumentenverzeichnisEvaluator.calculate(
				wizardSteps.get(0).getGesuch(),
				Constants.DEFAULT_LOCALE
			)
		).andReturn(dokumentGrundSet);
		expect(dokumentGrundService.findAllDokumentGrundByGesuch(anyObject()))
			.andReturn(dokumentGrundCollection);
		replayAll();
		updaterDokumente.updateAllStatus(wizardSteps, null, null, null);
		assertThat(
			wizardSteps.get(0).getWizardStepStatus(),
			is(WizardStepStatus.OK)
		);
	}

	@Test
	void updateAllStatusAllDokumentNeededUploadedMutation() {
		List<WizardStep> wizardSteps = getWizardStepsForTest(
			AntragTyp.MUTATION
		);
		Set<DokumentGrund> dokumentGrundSet = createDokumentGrundSetForTest(
			true
		);
		Collection<DokumentGrund> dokumentGrundCollection =
			createDokumentGrundSetForTest(true);
		Collection<DokumentGrund> dokumentGrundCollection2 =
			createDokumentGrundSetForTest(true);
		globalExpectation(
			wizardSteps,
			dokumentGrundSet,
			dokumentGrundCollection
		);
		expect(
			dokumentGrundService.findAllDokumentGrundByGesuch(
				wizardSteps.get(0).getGesuch(),
				false
			)
		).andReturn(dokumentGrundCollection2);
		Gesuch erstAntrag = new Gesuch();
		expect(
			gesuchService.findGesuch(
				wizardSteps.get(0).getGesuch().getVorgaengerId(),
				false
			)
		).andReturn(Optional.of(erstAntrag));
		Set<DokumentGrund> dokumentGrundSetErstGesuch = new HashSet<>();
		expect(
			dokumentGrundService.findAllDokumentGrundByGesuch(erstAntrag, false)
		).andReturn(dokumentGrundSetErstGesuch);
		replayAll();
		updaterDokumente.updateAllStatus(wizardSteps, null, null, null);
		assertThat(
			wizardSteps.get(0).getWizardStepStatus(),
			is(WizardStepStatus.MUTIERT)
		);
	}

	@Test
	void updateAllStatusErstAntragDokumentNichtComplete() {
		List<WizardStep> wizardSteps = getWizardStepsForTest(
			AntragTyp.ERSTGESUCH
		);
		Set<DokumentGrund> dokumentGrundSet = createDokumentGrundSetForTest(
			true
		);
		Collection<DokumentGrund> dokumentGrundCollection =
			createDokumentGrundSetForTest(true);
		DokumentGrund dokumentGrund = new DokumentGrund();
		dokumentGrundSet.add(dokumentGrund);
		dokumentGrundCollection.add(dokumentGrund);
		globalExpectation(
			wizardSteps,
			dokumentGrundSet,
			dokumentGrundCollection
		);
		replayAll();
		updaterDokumente.updateAllStatus(wizardSteps, null, null, null);
		assertThat(
			wizardSteps.get(0).getWizardStepStatus(),
			is(WizardStepStatus.IN_BEARBEITUNG)
		);
	}

	@Test
	void updateAllStatusMutationDokumentHasNotChanged() {
		List<WizardStep> wizardSteps = getWizardStepsForTest(
			AntragTyp.MUTATION
		);
		Set<DokumentGrund> dokumentGrundSet = createDokumentGrundSetForTest(
			true
		);
		Collection<DokumentGrund> dokumentGrundCollection =
			createDokumentGrundSetForTest(true);
		DokumentGrund dokumentGrund = new DokumentGrund();
		dokumentGrund.setDokumentGrundTyp(DokumentGrundTyp.ERWERBSPENSUM);
		dokumentGrundSet.add(dokumentGrund);
		dokumentGrundCollection.add(dokumentGrund);
		globalExpectation(
			wizardSteps,
			dokumentGrundSet,
			dokumentGrundCollection
		);
		Gesuch erstAntrag = new Gesuch();
		erstAntrag.setDokumentGrunds(dokumentGrundSet);
		expect(
			gesuchService.findGesuch(
				wizardSteps.get(0).getGesuch().getVorgaengerId()
			)
		).andReturn(Optional.of(erstAntrag));
		replayAll();
		updaterDokumente.updateAllStatus(wizardSteps, null, null, null);
		assertThat(
			wizardSteps.get(0).getWizardStepStatus(),
			is(WizardStepStatus.OK)
		);
	}

	@Test
	void updateAllStatusMutationDokumentAdded() {
		List<WizardStep> wizardSteps = getWizardStepsForTest(
			AntragTyp.MUTATION
		);
		Set<DokumentGrund> dokumentGrundSet = createDokumentGrundSetForTest(
			true
		);
		Collection<DokumentGrund> dokumentGrundCollection =
			createDokumentGrundSetForTest(true);
		DokumentGrund dokumentGrund = new DokumentGrund();
		dokumentGrund.setDokumentGrundTyp(DokumentGrundTyp.ERWERBSPENSUM);
		dokumentGrundSet.add(dokumentGrund);
		dokumentGrundCollection.add(dokumentGrund);
		// Add a new Dokument:
		DokumentGrund dokumentGrundMitDokument = new DokumentGrund();
		dokumentGrundMitDokument.getDokumente().add(new Dokument());
		dokumentGrundCollection.add(dokumentGrundMitDokument);
		globalExpectation(
			wizardSteps,
			dokumentGrundSet,
			dokumentGrundCollection
		);
		replayAll();
		updaterDokumente.updateAllStatus(wizardSteps, null, null, null);
		assertThat(
			wizardSteps.get(0).getWizardStepStatus(),
			is(WizardStepStatus.MUTIERT)
		);
	}

	@Test
	void updateAllStatusMutationDokumentRemoved() {
		List<WizardStep> wizardSteps = getWizardStepsForTest(
			AntragTyp.MUTATION
		);
		Set<DokumentGrund> dokumentGrundSet = createDokumentGrundSetForTest(
			false
		);
		Collection<DokumentGrund> dokumentGrundCollection =
			createDokumentGrundSetForTest(false);
		DokumentGrund dokumentGrund = new DokumentGrund();
		dokumentGrund.setDokumentGrundTyp(DokumentGrundTyp.ERWERBSPENSUM);
		dokumentGrundSet.add(dokumentGrund);
		dokumentGrundCollection.add(dokumentGrund);
		globalExpectation(
			wizardSteps,
			dokumentGrundSet,
			dokumentGrundCollection
		);
		Gesuch erstAntrag = new Gesuch();
		erstAntrag.setDokumentGrunds(createDokumentGrundSetForTest(true));
		expect(
			gesuchService.findGesuch(
				wizardSteps.get(0).getGesuch().getVorgaengerId()
			)
		).andReturn(Optional.of(erstAntrag));
		replayAll();
		updaterDokumente.updateAllStatus(wizardSteps, null, null, null);
		assertThat(
			wizardSteps.get(0).getWizardStepStatus(),
			is(WizardStepStatus.MUTIERT)
		);
	}

	@Test
	void updateAllStatusMutationDokumentRemovedAndReAdded() {
		List<WizardStep> wizardSteps = getWizardStepsForTest(
			AntragTyp.MUTATION
		);
		Set<DokumentGrund> dokumentGrundSet = createDokumentGrundSetForTest(
			false
		);
		Dokument dokument = new Dokument();
		Collection<DokumentGrund> dokumentGrundCollection =
			createDokumentGrundSetForTest(false);
		DokumentGrund dokumentGrund = new DokumentGrund();
		dokumentGrund.setDokumentGrundTyp(DokumentGrundTyp.ERWERBSPENSUM);
		dokumentGrund.getDokumente().add(new Dokument());
		dokumentGrundSet.add(dokumentGrund);
		dokumentGrundCollection.add(dokumentGrund);
		globalExpectation(
			wizardSteps,
			dokumentGrundSet,
			dokumentGrundCollection
		);
		Gesuch erstAntrag = new Gesuch();
		Set<DokumentGrund> dokumentGrundSetErstAntrag =
			createDokumentGrundSetForTest(false);
		dokumentGrundSetErstAntrag.add(dokumentGrund);
		erstAntrag.setDokumentGrunds(dokumentGrundSetErstAntrag);
		expect(
			gesuchService.findGesuch(
				wizardSteps.get(0).getGesuch().getVorgaengerId()
			)
		).andReturn(Optional.of(erstAntrag));
		replayAll();
		updaterDokumente.updateAllStatus(wizardSteps, null, null, null);
		assertThat(
			wizardSteps.get(0).getWizardStepStatus(),
			is(WizardStepStatus.MUTIERT)
		);
	}

	@Test
	void updateAllStatusMutationNoVorgaengerDokumentGrund() {
		List<WizardStep> wizardSteps = getWizardStepsForTest(
			AntragTyp.MUTATION
		);
		Set<DokumentGrund> dokumentGrundSet = createDokumentGrundSetForTest(
			false
		);
		Collection<DokumentGrund> dokumentGrundCollection =
			createDokumentGrundSetForTest(false);
		DokumentGrund dokumentGrund = new DokumentGrund();
		dokumentGrund.setDokumentGrundTyp(DokumentGrundTyp.ERWERBSPENSUM);
		dokumentGrundSet.add(dokumentGrund);
		dokumentGrundCollection.add(dokumentGrund);
		globalExpectation(
			wizardSteps,
			dokumentGrundSet,
			dokumentGrundCollection
		);
		Gesuch erstAntrag = new Gesuch();
		expect(
			gesuchService.findGesuch(
				wizardSteps.get(0).getGesuch().getVorgaengerId()
			)
		).andReturn(Optional.of(erstAntrag));
		replayAll();
		updaterDokumente.updateAllStatus(wizardSteps, null, null, null);
		assertThat(
			wizardSteps.get(0).getWizardStepStatus(),
			is(WizardStepStatus.OK)
		);
	}

	private Set<DokumentGrund> createDokumentGrundSetForTest(
		boolean withDokument
	) {
		Set<DokumentGrund> dokumentGrundSet = new HashSet<>();
		DokumentGrund dokumentGrund = new DokumentGrund();
		dokumentGrund.setDokumentGrundTyp(
			DokumentGrundTyp.FINANZIELLESITUATION
		);
		dokumentGrund.setNeeded(true);
		if (withDokument) {
			Dokument dokument = new Dokument();
			dokument.setVorgaengerId("VORGAENGER_ID");
			dokumentGrund.getDokumente().add(dokument);
		}
		dokumentGrundSet.add(dokumentGrund);
		return dokumentGrundSet;
	}

	private List<WizardStep> getWizardStepsForTest(AntragTyp antragTyp) {
		List<WizardStep> wizardStepList = new ArrayList<>();
		WizardStep wizardStep = new WizardStep();
		wizardStep.setWizardStepStatus(WizardStepStatus.IN_BEARBEITUNG);
		wizardStep.setWizardStepName(WizardStepName.DOKUMENTE);
		wizardStep.setGesuch(new Gesuch());
		if (AntragTyp.MUTATION == antragTyp) {
			wizardStep.getGesuch().setVorgaengerId("VORGEANGER_ID");
		}
		wizardStep.getGesuch().setTyp(antragTyp);
		wizardStepList.add(wizardStep);
		return wizardStepList;
	}

	private void globalExpectation(
		List<WizardStep> wizardSteps,
		Set<DokumentGrund> dokumentGrundSet,
		Collection<DokumentGrund> dokumentGrundCollection
	) {
		expect(
			dokumentenverzeichnisEvaluator.calculate(
				wizardSteps.get(0).getGesuch(),
				Constants.DEFAULT_LOCALE
			)
		).andReturn(dokumentGrundSet);
		expect(
			dokumentGrundService.findAllDokumentGrundByGesuch(
				wizardSteps.get(0).getGesuch()
			)
		).andReturn(dokumentGrundCollection);
	}
}
