package ch.dvbern.ebegu.rules;

import ch.dvbern.ebegu.entities.EbeguParameter;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.EbeguParameterKey;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_MASSGEBENDES_EINKOMMEN_MAX;

/**
 * Configurator, welcher die Regeln und ihre Reihenfolge konfiguriert. Als Parameter erhält er den Mandanten sowie
 * die benötigten Ebegu-Parameter
 */
public class BetreuungsgutscheinConfigurator {

	private final DateRange defaultGueltigkeit = Constants.DEFAULT_GUELTIGKEIT;

	private List<Rule> rules = new LinkedList<>();

	public List<Rule> configureRulesForMandant(@Nullable Mandant mandant, @Nonnull Map<EbeguParameterKey, EbeguParameter> ebeguRuleParameter) {
		// TODO (team) Mandant abfragen, sobald es mehrere hat!
		useBernerRules(ebeguRuleParameter);
		return rules;
	}

	public Set<EbeguParameterKey> getRequiredParametersForMandant(@Nullable Mandant mandant){
		// TODO (team) Mandant abfragen, sobald es mehrere hat!
		return requiredBernerParameters();
	}

	public Set<EbeguParameterKey> requiredBernerParameters(){
		return EnumSet.of(PARAM_MASSGEBENDES_EINKOMMEN_MAX);
	}


	private void useBernerRules(Map<EbeguParameterKey, EbeguParameter> ebeguParameter) {

		// GRUNDREGELN_DATA: Abschnitte erstellen

		// - Erwerbspensum: Erstellt die grundlegenden Zeitschnitze (keine Korrekturen, nur einfügen)
		ErwerbspensumAbschnittRule erwerbspensumAbschnittRule = new ErwerbspensumAbschnittRule(defaultGueltigkeit);
		rules.add(erwerbspensumAbschnittRule);

		// - Betreuungspensum
		BetreuungspensumAbschnittRule betreuungspensumAbschnittRule = new BetreuungspensumAbschnittRule(defaultGueltigkeit);
		rules.add(betreuungspensumAbschnittRule);

		// - Fachstelle
		FachstelleAbschnittRule fachstelleAbschnittRule = new FachstelleAbschnittRule(defaultGueltigkeit);
		rules.add(fachstelleAbschnittRule);

		// - Einkommen / Einkommensverschlechterung / Maximales Einkommen
		EinkommenAbschnittRule einkommenAbschnittRule = new EinkommenAbschnittRule(defaultGueltigkeit);
		rules.add(einkommenAbschnittRule);

		// - Einreichungsfrist
		EinreichungsfristAbschnittRule einreichungsfristAbschnittRule = new EinreichungsfristAbschnittRule(defaultGueltigkeit);
		rules.add(einreichungsfristAbschnittRule);

		// Wohnsitz (Zuzug und Wegzug)
		WohnsitzAbschnittRule wohnsitzAbschnittRule = new WohnsitzAbschnittRule(defaultGueltigkeit);
		rules.add(wohnsitzAbschnittRule);

		// Mindestalter Kind
		MindestalterAbschnittRule mindestalterAbschnittRule = new MindestalterAbschnittRule(defaultGueltigkeit);
		rules.add(mindestalterAbschnittRule);

		// Abwesenheit
		AbwesenheitAbschnittRule abwesenheitAbschnittRule = new AbwesenheitAbschnittRule(defaultGueltigkeit);
		rules.add(abwesenheitAbschnittRule);


		// GRUNDREGELN_CALC: Berechnen / Ändern den Anspruch

		// - Erwerbspensum
		ErwerbspensumCalcRule erwerbspensumCalcRule = new ErwerbspensumCalcRule(defaultGueltigkeit);
		rules.add(erwerbspensumCalcRule);

		// - Betreuungspensum
		BetreuungspensumCalcRule betreuungspensumCalcRule = new BetreuungspensumCalcRule(defaultGueltigkeit);
		rules.add(betreuungspensumCalcRule);

		// - Fachstelle: Muss zwingend nach Erwerbspensum und Betreuungspensum durchgefuehrt werden
		FachstelleCalcRule fachstelleCalcRule = new FachstelleCalcRule(defaultGueltigkeit);
		rules.add(fachstelleCalcRule);

		// - Kind wohnt im gleichen Haushalt: Muss zwingend nach Fachstelle durchgefuehrt werden
		WohnhaftImGleichenHaushaltCalcRule wohnhaftImGleichenHaushaltRule = new WohnhaftImGleichenHaushaltCalcRule(defaultGueltigkeit);
		rules.add(wohnhaftImGleichenHaushaltRule);


		// REDUKTIONSREGELN: Setzen Anpsruch auf 0

		// - Einkommen / Einkommensverschlechterung / Maximales Einkommen
		EbeguParameter paramMassgebendesEinkommenMax = ebeguParameter.get(PARAM_MASSGEBENDES_EINKOMMEN_MAX);
		Objects.requireNonNull(paramMassgebendesEinkommenMax, "Parameter PARAM_MASSGEBENDES_EINKOMMEN_MAX muss gesetzt sein");
		EinkommenCalcRule maxEinkommenCalcRule = new EinkommenCalcRule(defaultGueltigkeit, paramMassgebendesEinkommenMax.getValueAsBigDecimal());
		rules.add(maxEinkommenCalcRule);

		// Betreuungsangebot Tagesschule nicht berechnen
		BetreuungsangebotTypCalcRule betreuungsangebotTypCalcRule = new BetreuungsangebotTypCalcRule(defaultGueltigkeit);
		rules.add(betreuungsangebotTypCalcRule);

		// Mindestalter Kind
		MindestalterCalcRule mindestalterRule = new MindestalterCalcRule(defaultGueltigkeit);
		rules.add(mindestalterRule);

		// Wohnsitz (Zuzug und Wegzug)
		WohnsitzCalcRule wohnsitzCalcRule = new WohnsitzCalcRule(defaultGueltigkeit);
		rules.add(wohnsitzCalcRule);

		// Einreichungsfrist
		EinreichungsfristCalcRule einreichungsfristRule = new EinreichungsfristCalcRule(defaultGueltigkeit);
		rules.add(einreichungsfristRule);

		// Mindestalter Kind
		MindestalterCalcRule mindestalterCalcRule = new MindestalterCalcRule(defaultGueltigkeit);
		rules.add(mindestalterCalcRule);

		// Abwesenheit
		AbwesenheitCalcRule abwesenheitCalcRule = new AbwesenheitCalcRule(defaultGueltigkeit);
		rules.add(abwesenheitCalcRule);

		//RESTANSPRUCH REDUKTION limitiert Anspruch auf  minimum(anspruchRest, anspruchPensum)
		RestanspruchLimitCalcRule restanspruchLimitCalcRule = new RestanspruchLimitCalcRule(defaultGueltigkeit);
		rules.add(restanspruchLimitCalcRule);

	}
}
