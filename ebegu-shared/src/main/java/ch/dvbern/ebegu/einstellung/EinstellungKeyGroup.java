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

package ch.dvbern.ebegu.einstellung;

import lombok.Getter;

public enum EinstellungKeyGroup implements ConfigurableKeyGroup {
	FREIGABE_VERFUEGUNG_GLOBALERE_EINSTELLUNGEN(
		0
	), ANTRAGSTELLENDE_NACHWEISE_FORMALITAETEN(1), FAMILIENSITUATION(
		2
	), ANSPRUCHSBERECHTIGUNG_PENSUM_REGELN(3), PLATZBESTAETIGUNG(
		4
	), BESONDERE_BEDUERFNISSE_AUSSERORDENTLICHER_BETREUUNGSAUFWAND(
		5
	), FACHSTELLEN_INTEGRATION(6), KITA_BETREUUNG(7), TFO_BETREUUNG(
		8
	), MITTAGSTISCH(9), TAGESSCHULE(10), FERIENBETREUUNG_FERIENINSELN(
		11
	), KINDERABZUG_PAUSCHALABZUG(
		12
	), FINANZIELLE_SITUATION_MASSGEBENDES_EINKOMMEN(13), EINKOMMENSAENDERUNG(
		14
	);

	@Getter
	private final int order;

	EinstellungKeyGroup(int order) {
		this.order = order;
	}
}
