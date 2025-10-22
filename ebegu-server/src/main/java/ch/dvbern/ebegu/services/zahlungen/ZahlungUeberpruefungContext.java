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

package ch.dvbern.ebegu.services.zahlungen;

import java.util.ArrayList;
import java.util.List;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.util.zahlungslauf.ZahlungslaufHelper;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZahlungUeberpruefungContext {
	private String beschrieb;
	private Gemeinde gemeinde;
	private String zahlungsauftragId;
	private List<String> potentielleFehlerList = new ArrayList<>();
	private List<String> potenzielleFehlerListZusammenfassung =
		new ArrayList<>();
	private int anzahlMonateInZukunft;
	private List<String> whiteListOfReferenzNummmern = new ArrayList<>();
	private ZahlungslaufHelper zahlungslaufHelper;

	public ZahlungUeberpruefungContext(
		Gemeinde gemeinde,
		String beschrieb,
		String zahlungsauftragId,
		ZahlungslaufHelper zahlungslaufHelper
	) {
		this.beschrieb = beschrieb;
		this.gemeinde = gemeinde;
		this.zahlungsauftragId = zahlungsauftragId;
		this.zahlungslaufHelper = zahlungslaufHelper;
	}

	public void resetAllPotentielleFehlerData() {
		this.potentielleFehlerList = new ArrayList<>();
		this.potenzielleFehlerListZusammenfassung = new ArrayList<>();
	}
}
