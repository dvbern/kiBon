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

package ch.dvbern.ebegu.api.converter;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import jakarta.enterprise.context.Dependent;

import ch.dvbern.ebegu.api.dtos.JaxZahlung;
import ch.dvbern.ebegu.api.dtos.JaxZahlungsauftrag;
import ch.dvbern.ebegu.api.util.RestUtil;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.util.EnumUtil;
import ch.dvbern.ebegu.util.MathUtil;

import static ch.dvbern.ebegu.enums.UserRole.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRole.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRole.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRole.SACHBEARBEITER_TRAEGERSCHAFT;

@Dependent
public class JaxZahlungConverter extends AbstractBaseConverter {

	public JaxZahlungsauftrag zahlungsauftragToJAX(
		final Zahlungsauftrag persistedZahlungsauftrag,
		boolean convertZahlungen
	) {

		return getJaxZahlungsauftrag(
			persistedZahlungsauftrag,
			convertZahlungen
		);
	}

	private JaxZahlungsauftrag getJaxZahlungsauftrag(
		Zahlungsauftrag persistedZahlungsauftrag,
		boolean convertZahlungen
	) {

		final JaxZahlungsauftrag jaxZahlungsauftrag = new JaxZahlungsauftrag();
		convertAbstractDateRangedFieldsToJAX(
			persistedZahlungsauftrag,
			jaxZahlungsauftrag
		);
		jaxZahlungsauftrag.setZahlungslaufTyp(
			persistedZahlungsauftrag.getZahlungslaufTyp()
		);
		jaxZahlungsauftrag.setStatus(persistedZahlungsauftrag.getStatus());
		jaxZahlungsauftrag.setBeschrieb(
			persistedZahlungsauftrag.getBeschrieb()
		);
		jaxZahlungsauftrag.setBetragTotalAuftrag(
			persistedZahlungsauftrag.getBetragTotalAuftrag()
		);
		jaxZahlungsauftrag.setGemeinde(
			gemeindeToJAX(persistedZahlungsauftrag.getGemeinde())
		);
		jaxZahlungsauftrag.setDatumFaellig(
			persistedZahlungsauftrag.getDatumFaellig()
		);
		jaxZahlungsauftrag.setDatumGeneriert(
			persistedZahlungsauftrag.getDatumGeneriert()
		);
		jaxZahlungsauftrag.setDatumBeendet(
			persistedZahlungsauftrag.getDatumBeendet()
		);
		jaxZahlungsauftrag.setHasNegativeZahlungen(
			persistedZahlungsauftrag.getHasNegativeZahlungen()
		);

		if (convertZahlungen) {
			List<JaxZahlung> zahlungen = persistedZahlungsauftrag.getZahlungen()
				.stream()
				.map(this::zahlungToJAX)
				.collect(Collectors.toList());
			jaxZahlungsauftrag.getZahlungen().addAll(zahlungen);
		}
		return jaxZahlungsauftrag;
	}

	public JaxZahlungsauftrag zahlungsauftragToJAX(
		final Zahlungsauftrag persistedZahlungsauftrag,
		@Nullable UserRole userRole,
		Collection<Institution> allowedInst
	) {

		final JaxZahlungsauftrag jaxZahlungsauftrag = getJaxZahlungsauftrag(
			persistedZahlungsauftrag,
			true
		);

		// nur die Zahlungen welche inst sehen darf
		if (EnumUtil.isOneOf(
			userRole,
			ADMIN_TRAEGERSCHAFT,
			SACHBEARBEITER_TRAEGERSCHAFT,
			ADMIN_INSTITUTION,
			SACHBEARBEITER_INSTITUTION
		)
		) {
			// Institutionsbenutzer duerfen nur Zahlungslaeufe vom Typ GEMEINDE_INSTITUTION sehen
			if (persistedZahlungsauftrag.getZahlungslaufTyp()
				!= ZahlungslaufTyp.GEMEINDE_INSTITUTION) {
				throw new EbeguRuntimeException(
					"zahlungsauftragToJAX",
					"Institutionsbenutzer darf nur Institutions-Zahlungslaeufe sehen"
				);
			}
			// und davon nur diejenigen seiner Institution/Traegerschaft
			RestUtil.purgeZahlungenOfInstitutionen(
				jaxZahlungsauftrag,
				allowedInst
			);
			// es muss nochmal das Auftragstotal berechnet werden. Diesmal nur mit den erlaubten Zahlungen
			// Dies nur fuer Institutionen
			BigDecimal total = BigDecimal.ZERO;
			boolean hasAnyNegativeZahlung = false;
			for (JaxZahlung zahlung : jaxZahlungsauftrag.getZahlungen()) {
				total = MathUtil.DEFAULT.add(
					total,
					zahlung.getBetragTotalZahlung()
				);
				if (MathUtil.isNegative(zahlung.getBetragTotalZahlung())) {
					hasAnyNegativeZahlung = true;
				}
			}
			jaxZahlungsauftrag.setBetragTotalAuftrag(total);
			jaxZahlungsauftrag.setHasNegativeZahlungen(hasAnyNegativeZahlung);
		} else {
			jaxZahlungsauftrag.setBetragTotalAuftrag(
				persistedZahlungsauftrag.getBetragTotalAuftrag()
			);
		}
		return jaxZahlungsauftrag;
	}

	public JaxZahlung zahlungToJAX(final Zahlung persistedZahlung) {
		final JaxZahlung jaxZahlung = new JaxZahlung();
		convertAbstractVorgaengerFieldsToJAX(persistedZahlung, jaxZahlung);
		jaxZahlung.setStatus(persistedZahlung.getStatus());
		jaxZahlung.setBetragTotalZahlung(
			persistedZahlung.getBetragTotalZahlung()
		);
		jaxZahlung.setEmpfaengerName(persistedZahlung.getEmpfaengerName());
		jaxZahlung.setBetreuungsangebotTyp(
			persistedZahlung.getBetreuungsangebotTyp()
		);
		jaxZahlung.setEmpfaengerId(persistedZahlung.getEmpfaengerId());
		return jaxZahlung;
	}
}
