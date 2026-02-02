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

package ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren;

import java.util.function.Predicate;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.inbox.consumer.betreuung.event.BetreuungEvent;
import ch.dvbern.ebegu.inbox.consumer.betreuung.pattern.decisiontree.BetreuungActionNode;
import ch.dvbern.ebegu.inbox.consumer.betreuung.pattern.decisiontree.BetreuungDecisionNode;
import ch.dvbern.ebegu.inbox.consumer.betreuung.pattern.decisiontree.DecisionNode;
import ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren.action.BetreuungAbweisenAction;
import ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren.action.BetreuungStornierenAction;
import ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren.action.DoNothingAction;
import ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren.action.SendStornierungsInfoEmailAction;
import ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren.action.StornierungsMitteilungErstellenAction;
import ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren.condition.BetreuungNeuErfasstCondition;
import ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren.condition.BetreuungStatusAbgeschlossenCondition;
import ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren.condition.BetreuungStatusWartenCondition;
import ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren.condition.ErstantragCondition;
import ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren.condition.GesuchInVerfuegungCondition;
import ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren.condition.OffeneMutationCondition;
import ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren.condition.OnlineGesuchCondition;

/**
 * Beschreibt den Entscheidungsbaum für das automatisierte Stornieren von Betreuungen via Exchange-Service.
 * Das hier verwendete Pattern ist
 * <a href="https://intra.dvbern.ch/spaces/KIB/pages/259426671/Entscheidungsbaum">in Confluence</a> beschrieben.
 *
 * @see
 * <a href="https://intra.dvbern.ch/spaces/KIB/pages/259425236/Betreuungen+stornieren+via+Exchange-Service">Confluence:
 * Betreuung stornieren via Exchange-Service</a>
 */
@Stateless
public class BetreuungStornierenDecisionTree {

	@Inject
	private BetreuungAbweisenAction betreuungAbweisenAction;

	@Inject
	private BetreuungStornierenAction betreuungStornierenAction;

	@Inject
	private DoNothingAction doNothingAction;

	@Inject
	private SendStornierungsInfoEmailAction sendStornierungsInfoEmailAction;

	@Inject
	private StornierungsMitteilungErstellenAction stornierungsMitteilungErstellenAction;

	/**
	 * Erzeugt die Objektstruktur für den Entscheidungsbaum für das Stornieren von Betreuungen.
	 * Da der Baum dabei von "unten nach oben" definiert werden muss, lässt sich das leider nur schwer nachvollziehen,
	 * insbesondere dann, wenn der Baum komplexer wird.
	 * Für das Verstehen des Codes, kann die
	 * <a href="https://intra.dvbern.ch/spaces/KIB/pages/259425236/Betreuungen+stornieren+via+Exchange-Service">hier
	 * gezeigte Abbildung</a> verwendet werden. Am besten ist es dabei der Abbildung von den Blättern aus, Richtung
	 * Wurzel zu folgen.
	 */
	private DecisionNode<BetreuungEvent> buildDecisionTree() {

		// Alle benötigten Bedingungen erstellen

		Predicate<Betreuung> erstAntragCondition = new ErstantragCondition();
		Predicate<Betreuung> betreuungStatusWartenCondition =
			new BetreuungStatusWartenCondition();
		Predicate<Betreuung> betreuungStatusAbgeschlossenCondition =
			new BetreuungStatusAbgeschlossenCondition();
		Predicate<Betreuung> betreuungNeuErfasstCondition =
			new BetreuungNeuErfasstCondition();
		Predicate<Betreuung> gesuchInVerfuegungCondition =
			new GesuchInVerfuegungCondition();
		Predicate<Betreuung> offeneMutationCondition =
			new OffeneMutationCondition();
		Predicate<Betreuung> onlineAntragCondition =
			new OnlineGesuchCondition();

		// Den Entscheidungsbaum erstellen. Wir beginnen mit den Blättern und schliessen mit der Wurzel ab.

		// Blätter:
		// Jedes Blatt ist eine Aktion.

		// Eine Betreuung wird nicht in jedem Fall storniert oder abgewiesen. Es gibt Pfade, an deren Ende wir das Stornierungs-
		// Event einfach ignorieren und nichts unternehmen.
		BetreuungActionNode doNothingActionNode =
			new BetreuungActionNode(doNothingAction);

		// Wenn die stornierte Betreuung zu einem Online-Gesuch gehört, müssen GS per E-Mail infomiert werden.
		BetreuungActionNode sendStornierungsInfoMailActionNode =
			new BetreuungActionNode(sendStornierungsInfoEmailAction);

		// Bevor die E-Mail versendet wird, muss gepfüft werden, ob es sich überhaupt um einen Online-Antrag handelt.
		DecisionNode<BetreuungEvent> onlineAntragDecisionNode =
			new BetreuungDecisionNode(
				onlineAntragCondition,
				sendStornierungsInfoMailActionNode,
				doNothingActionNode
			);

		// Die Betreuung wird storniert.
		BetreuungActionNode betreuungStornierenActionNode =
			new BetreuungActionNode(
				betreuungStornierenAction,
				onlineAntragDecisionNode
			);

		// Eine noch nicht von einer Institution bestätigte Betreuung kann nicht storniert werden (ohne Betätigung, keine
		// Buchung). Um hier aber auch solche Fälle behandeln zu können, werden unbestätigte Betreuungen abgewiesen.
		BetreuungActionNode betreuungAbweisenActionNode =
			new BetreuungActionNode(
				betreuungAbweisenAction
			);

		// Die Entscheidung, ob eine Betreuung stroniert wird, muss bei Gesuchen in Verfügung von einem Gemeindemitarbeiter
		// getroffen werden. Statt der autonatischen Stornierung, erstellen wir also eine Stornierungsmitteilung.
		BetreuungActionNode stornierungsMitteilungErstellenActionNode =
			new BetreuungActionNode(
				stornierungsMitteilungErstellenAction
			);

		// Entscheidungsknoten:
		// Wir verwenden hier nur binäre Entscheidungen (ja/nein).
		// Jede Entscheidung basiert auf einer Bedingung. Is diese erfüllt, wird mit "ja" entschieden, sonst mit "nein".
		// Entscheidungen sind also Verzweigungen.
		// Eine Verzweigung resultiert entweder in einem weiteren Entscheidungsknoten oder in einer Aktion.
		// Eine Aktion bildet immer das Ende eines Pfades.

		DecisionNode<BetreuungEvent> gesuchInVerfuegungDecisionNode =
			new BetreuungDecisionNode(
				gesuchInVerfuegungCondition,
				stornierungsMitteilungErstellenActionNode, // ja
				betreuungAbweisenActionNode // nein
			);

		DecisionNode<BetreuungEvent> betreuungStatusAbgeschlossenDecisionNode =
			new BetreuungDecisionNode(
				betreuungStatusAbgeschlossenCondition,
				gesuchInVerfuegungDecisionNode, // ja
				doNothingActionNode // nein
			);

		DecisionNode<BetreuungEvent> betreuungStatusWartenDecisionNode =
			new BetreuungDecisionNode(
				betreuungStatusWartenCondition,
				betreuungAbweisenActionNode, // ja
				betreuungStatusAbgeschlossenDecisionNode // nein
			);

		// Bei einer neu erfassten Betreuung in abgeschlossenem Zustand muss eine Stornierungsmitteilung versendet werden
		DecisionNode<BetreuungEvent> betreuungNeuErfasstAndStatusAbgeschlossenDecisionNode =
			new BetreuungDecisionNode(
				betreuungStatusAbgeschlossenCondition,
				stornierungsMitteilungErstellenActionNode, // ja
				doNothingActionNode // nein
			);

		// Bei einer neu erfassten Betreuung im Status "WARTEN" kann die Betreuung direkt storniert werden.
		DecisionNode<BetreuungEvent> betreuungNeuErfasstAndStatusWartenDecisionNode =
			new BetreuungDecisionNode(
				betreuungStatusWartenCondition,
				betreuungStornierenActionNode, // ja
				betreuungNeuErfasstAndStatusAbgeschlossenDecisionNode // nein
			);

		DecisionNode<BetreuungEvent> betreuungNeuErfasstDecisionNode =
			new BetreuungDecisionNode(
				betreuungNeuErfasstCondition,
				betreuungStatusWartenDecisionNode, // ja
				betreuungNeuErfasstAndStatusWartenDecisionNode // nein
			);

		DecisionNode<BetreuungEvent> offeneMutationDecisionNode =
			new BetreuungDecisionNode(
				offeneMutationCondition,
				betreuungNeuErfasstDecisionNode, // ja
				betreuungStatusAbgeschlossenDecisionNode // nein
			);

		// Wurzel (handelt es sich um einen Erstantrag?)
		return new BetreuungDecisionNode(
			erstAntragCondition,
			betreuungStatusWartenDecisionNode, // ja
			offeneMutationDecisionNode // nein
		);
	}

	public void evaluate(BetreuungEvent betreuungEvent) {
		DecisionNode<BetreuungEvent> root = buildDecisionTree();
		root.evaluate(betreuungEvent);
	}
}
