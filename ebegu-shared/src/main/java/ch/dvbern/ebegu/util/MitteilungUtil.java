/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.util;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.containers.PensumUtil;
import ch.dvbern.ebegu.enums.MitteilungStatus;
import ch.dvbern.ebegu.enums.MitteilungTeilnehmerTyp;

public final class MitteilungUtil {

	private MitteilungUtil() {
		// Should not be initialized
	}

	/**
	 * Takes a {@link Mitteilung} and checks if it is a {@link Betreuungsmitteilung} with the flag for
	 * {@code schliessungMitteilung} set
	 *
	 * @param mitteilung the {@link Mitteilung} to check
	 * @return whether the {@link Mitteilung} is an Institution-Schliessung-Mitteilung
	 */
	public static boolean isSchliessungsmitteilung(
		@Nonnull Mitteilung mitteilung
	) {
		return mitteilung instanceof Betreuungsmitteilung
			&& ((Betreuungsmitteilung) mitteilung).isSchliessungMitteilung();
	}

	public static void initializeBetreuungsmitteilung(
		@Nonnull Betreuungsmitteilung betreuungsmitteilung,
		@Nonnull Betreuung betreuung,
		@Nonnull Benutzer currentBenutzer,
		@Nonnull BigDecimal oeffnungstageMittagstisch,
		Locale locale
	) {
		PensumUtil.transformBetreuungsPensumContainers(
			betreuungsmitteilung,
			oeffnungstageMittagstisch
		);
		betreuungsmitteilung.setDossier(betreuung.extractGesuch().getDossier());
		betreuungsmitteilung.setSenderTyp(MitteilungTeilnehmerTyp.INSTITUTION);
		betreuungsmitteilung.setEmpfaengerTyp(
			MitteilungTeilnehmerTyp.JUGENDAMT
		);
		betreuungsmitteilung.setSender(currentBenutzer);
		betreuungsmitteilung.setEmpfaenger(
			betreuung.extractGesuch().getDossier().getFall().getBesitzer()
		);
		betreuungsmitteilung.setMitteilungStatus(MitteilungStatus.NEU);
		betreuungsmitteilung.setSubject(
			ServerMessageUtil.getMessage(
				"mutationsmeldung_betreff",
				locale,
				currentBenutzer.getMandant(),
				betreuung.extractGemeinde()
			)
		);
	}

	public static boolean areZeitabschnittIgnorierend(Gesuch gesuch) {
		return gesuch.getKindContainers()
			.stream()
			.flatMap(kindContainer -> kindContainer.getBetreuungen().stream())
			.map(Betreuung::getVerfuegung)
			.filter(Objects::nonNull)
			.flatMap(verfuegung -> verfuegung.getZeitabschnitte().stream())
			.anyMatch(MitteilungUtil::hasIgnorierenderZahlungsstatus);
	}

	private static boolean hasIgnorierenderZahlungsstatus(
		VerfuegungZeitabschnitt zeitabschnitt
	) {
		return zeitabschnitt.getZahlungsstatusInstitution().isIgnorierend()
			|| zeitabschnitt.getZahlungsstatusAntragsteller().isIgnorierend();
	}
}
