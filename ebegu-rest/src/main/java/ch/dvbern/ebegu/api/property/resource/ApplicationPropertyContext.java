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

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.api.dtos.JaxPublicAppConfig;
import ch.dvbern.ebegu.einstellung.ApplicationProperty;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import lombok.Setter;

@Setter
public class ApplicationPropertyContext {

	boolean devmode;
	String whitelist;
	boolean dummyMode;
	String sentryEnvName;
	String background;
	boolean zahlungentestmode;
	boolean personenSucheDisabled;
	String kitaxHost;
	String kitaxendpoint;
	boolean multimandantEnabled;
	boolean isEbeguKibonAnfrageTestGuiEnabled;
	boolean testfaelleEnabled;

	ApplicationProperty ferienbetreuungAktiv;
	ApplicationProperty lastenausgleichAktiv;
	ApplicationProperty lastenausgleichTagesschulenAktiv;
	ApplicationProperty gemeindeKennzahlenAktiv;
	ApplicationProperty lastenausgleichTagesschulenAnteilZweitpruefungDe;
	ApplicationProperty lastenausgleichTagesschulenAnteilZweitpruefungFr;
	ApplicationProperty lastenausgleichTagesschulenAutoZweitpruefungDe;
	ApplicationProperty lastenausgleichTagesschulenAutoZweitpruefungFr;
	ApplicationProperty ferienbetreuungAnteilZweitpruefungDe;
	ApplicationProperty ferienbetreuungAnteilZweitpruefungFr;
	ApplicationProperty ferienbetreuungAutoZweitpruefungDe;
	ApplicationProperty ferienbetreuungAutoZweitpruefungFr;
	ApplicationProperty primaryColor;
	ApplicationProperty primaryColorDark;
	ApplicationProperty primaryColorLight;
	ApplicationProperty infomaZahlungen;
	ApplicationProperty auszahlungAnEltern;
	ApplicationProperty frenchEnabled;
	ApplicationProperty geresEnabledForMandant;
	ApplicationProperty steuerschnittstelleAktivAb;
	ApplicationProperty zusatzinformationenInstitution;
	ApplicationProperty activatedDemoFeatures;
	ApplicationProperty checkboxAuszahlungInZukunft;
	ApplicationProperty institutionenDurchGemeindenEinladen;
	ApplicationProperty erlaubenInstitutionenZuWaehlen;
	ApplicationProperty angebotTSEnabled;
	ApplicationProperty angebotFIEnabled;
	ApplicationProperty angebotMittagstischEnabled;
	ApplicationProperty angebotTFOEnabled;
	ApplicationProperty gemeindeVereinfachteKonfigAktiv;
	ApplicationProperty abgeloesteViewBeschaeftigungSingleEnabled;
	ApplicationProperty gemeindeKennzahlenReminderActivated;
	String nodeName = "";
	BigDecimal lastenausgleichTagesschulenAnteilZweitpruefungDeConverted;
	BigDecimal lastenausgleichTagesschulenAnteilZweitpruefungFrConverted;
	BigDecimal lastenausgleichTagesschulenAutoZweitpruefungDeConverted;
	BigDecimal lastenausgleichTagesschulenAutoZweitpruefungFrConverted;
	BigDecimal ferienbetreuungAnteilZweitpruefungDeConverted;
	BigDecimal ferienbetreuungAnteilZweitpruefungFrConverted;
	BigDecimal ferienbetreuungAutoZweitpruefungDeConverted;
	BigDecimal ferienbetreuungAutoZweitpruefungFrConverted;

	public JaxPublicAppConfig buildJaxPublicAppConfig()
		throws EbeguRuntimeException {
		transformSonderApplicationProperty();
		return JaxPublicAppConfig.builder()
			.currentNode(nodeName)
			.devmode(devmode)
			.whitelist(whitelist)
			.dummyMode(dummyMode)
			.sentryEnvName(sentryEnvName)
			.backgroundColor(background)
			.zahlungentestmode(zahlungentestmode)
			.personenSucheDisabled(personenSucheDisabled)
			.kitaxHost(kitaxHost)
			.kitaxEndpoint(kitaxendpoint)
			.lastenausgleichAktiv(
				stringToBool(lastenausgleichAktiv.getValue())
			)
			.ferienbetreuungAktiv(
				stringToBool(ferienbetreuungAktiv.getValue())
			)
			.lastenausgleichTagesschulenAktiv(
				stringToBool(
					lastenausgleichTagesschulenAktiv.getValue()
				)
			)
			.gemeindeKennzahlenAktiv(
				stringToBool(gemeindeKennzahlenAktiv.getValue())
			)
			.lastenausgleichTagesschulenAnteilZweitpruefungDe(
				lastenausgleichTagesschulenAnteilZweitpruefungDeConverted
			)
			.lastenausgleichTagesschulenAnteilZweitpruefungFr(
				lastenausgleichTagesschulenAnteilZweitpruefungFrConverted
			)
			.lastenausgleichTagesschulenAutoZweitpruefungDe(
				lastenausgleichTagesschulenAutoZweitpruefungDeConverted
			)
			.lastenausgleichTagesschulenAutoZweitpruefungFr(
				lastenausgleichTagesschulenAutoZweitpruefungFrConverted
			)
			.ferienbetreuungAnteilZweitpruefungDe(
				ferienbetreuungAnteilZweitpruefungDeConverted
			)
			.ferienbetreuungAutoZweitpruefungDe(
				ferienbetreuungAutoZweitpruefungDeConverted
			)
			.ferienbetreuungAnteilZweitpruefungFr(
				ferienbetreuungAnteilZweitpruefungFrConverted
			)
			.ferienbetreuungAutoZweitpruefungFr(
				ferienbetreuungAutoZweitpruefungFrConverted
			)
			.primaryColor(primaryColor.getValue())
			.primaryColorDark(primaryColorDark.getValue())
			.primaryColorLight(primaryColorLight.getValue())
			.multimandantAktiv(multimandantEnabled)
			.infomaZahlungen(stringToBool(infomaZahlungen.getValue()))
			.frenchEnabled(stringToBool(frenchEnabled.getValue()))
			.geresEnabledForMandant(
				stringToBool(geresEnabledForMandant.getValue())
			)
			.ebeguKibonAnfrageTestGuiEnabled(
				isEbeguKibonAnfrageTestGuiEnabled
			)
			.steuerschnittstelleAktivAb(
				steuerschnittstelleAktivAb.getValue()
			)
			.zusatzinformationenInstitution(
				stringToBool(zusatzinformationenInstitution.getValue())
			)
			.activatedDemoFeatures(activatedDemoFeatures.getValue())
			.checkboxAuszahlungInZukunft(
				stringToBool(checkboxAuszahlungInZukunft.getValue())
			)
			.institutionenDurchGemeindenEinladen(
				stringToBool(
					institutionenDurchGemeindenEinladen.getValue()
				)
			)
			.erlaubenInstitutionenZuWaehlen(
				stringToBool(erlaubenInstitutionenZuWaehlen.getValue())
			)
			.angebotTSActivated(stringToBool(angebotTSEnabled.getValue()))
			.angebotFIActivated(stringToBool(angebotFIEnabled.getValue()))
			.angebotMittagstischActivated(
				stringToBool(angebotMittagstischEnabled.getValue())
			)
			.angebotTFOActivated(stringToBool(angebotTFOEnabled.getValue()))
			.auszahlungAnEltern(stringToBool(auszahlungAnEltern.getValue()))
			.gemeindeVereinfachteKonfigAktiv(
				stringToBool(gemeindeVereinfachteKonfigAktiv.getValue())
			)
			.testfaelleEnabled(testfaelleEnabled)
			.abgeloesteViewBeschaeftigungSingleEnabled(
				stringToBool(
					abgeloesteViewBeschaeftigungSingleEnabled.getValue()
				)
			)
			.gemeindeKennzahlenReminderActivated(
				stringToBool(gemeindeKennzahlenReminderActivated.getValue())
			)
			.build();
	}

	private void transformSonderApplicationProperty()
		throws EbeguRuntimeException {
		try {
			nodeName = InetAddress.getLocalHost().getHostName();
			lastenausgleichTagesschulenAnteilZweitpruefungDeConverted =
				new BigDecimal(
					lastenausgleichTagesschulenAnteilZweitpruefungDe
						.getValue()
				);
			lastenausgleichTagesschulenAnteilZweitpruefungFrConverted =
				new BigDecimal(
					lastenausgleichTagesschulenAnteilZweitpruefungFr
						.getValue()
				);
			lastenausgleichTagesschulenAutoZweitpruefungDeConverted =
				new BigDecimal(
					lastenausgleichTagesschulenAutoZweitpruefungDe
						.getValue()
				);
			lastenausgleichTagesschulenAutoZweitpruefungFrConverted =
				new BigDecimal(
					lastenausgleichTagesschulenAutoZweitpruefungFr
						.getValue()
				);
			ferienbetreuungAnteilZweitpruefungDeConverted =
				new BigDecimal(
					ferienbetreuungAnteilZweitpruefungDe
						.getValue()
				);
			ferienbetreuungAnteilZweitpruefungFrConverted =
				new BigDecimal(
					ferienbetreuungAnteilZweitpruefungFr
						.getValue()
				);
			ferienbetreuungAutoZweitpruefungDeConverted =
				new BigDecimal(
					ferienbetreuungAutoZweitpruefungDe
						.getValue()
				);
			ferienbetreuungAutoZweitpruefungFrConverted =
				new BigDecimal(
					ferienbetreuungAutoZweitpruefungFr
						.getValue()
				);
		} catch (
			UnknownHostException e) {
			throw new EbeguRuntimeException(
				"getHostName",
				"Hostname konnte nicht ermittelt werden",
				e
			);
		} catch (
			NumberFormatException e) {
			throw new EbeguRuntimeException(
				"new BigDecimal()",
				"Fehler beim Parsen einer Einstellung",
				e
			);
		}
	}

	private boolean stringToBool(@Nonnull String str) {
		return str.equals("true");
	}
}
