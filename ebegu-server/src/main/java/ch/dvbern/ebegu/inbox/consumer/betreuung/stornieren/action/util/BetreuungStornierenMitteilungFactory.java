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

package ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren.action.util;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.MitteilungStatus;
import ch.dvbern.ebegu.enums.MitteilungTeilnehmerTyp;
import ch.dvbern.ebegu.inbox.services.BetreuungEventHelper;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;

@Stateless
public class BetreuungStornierenMitteilungFactory {

	private static final String MESSAGE_KEY =
		"mutationsmeldung_stornieren_message";

	private static final String BETREFF_KEY =
		"mutationsmeldung_stornieren_betreff";

	/**
	 * Referenz auf den Service zum Ermitteln der Gemeinde, für die die Mitteilung erstellt
	 * werden soll.
	 */
	@Inject
	private GemeindeService gemeindeService;

	/**
	 * Referenz auf den Service, mit dem der Absender der Mutationsmitteilung ermittelt
	 * wird.
	 */
	@Inject
	private BetreuungEventHelper betreuungEventHelper;

	/**
	 * Ertellt eine Stornierenmitteilung für die gegebene Betreuung.
	 * 
	 * @param betreuung Die Betreuung für die die Stronierungsmitteilung erstellt werden soll.
	 * @return Eine neue Stornierungsmitteilung für die gegebene Betreuung.
	 */
	@Nonnull
	public Betreuungsmitteilung createBetreuungsStornierenMitteilung(
		@Nonnull Betreuung betreuung
	) {
		Gesuch gesuch = betreuung.extractGesuch();
		Locale locale = EbeguUtil.extractKorrespondenzsprache(
			gesuch,
			gemeindeService
		).getLocale();
		Mandant mandant = betreuung.extractGemeinde().getMandant();

		Betreuungsmitteilung betreuungsmitteilung = new Betreuungsmitteilung();
		betreuungsmitteilung.setDossier(gesuch.getDossier());
		betreuungsmitteilung.setSenderTyp(MitteilungTeilnehmerTyp.INSTITUTION);
		betreuungsmitteilung.setSender(
			betreuungEventHelper.getMutationsmeldungBenutzer(betreuung)
		);
		betreuungsmitteilung.setEmpfaengerTyp(
			MitteilungTeilnehmerTyp.JUGENDAMT
		);
		betreuungsmitteilung.setEmpfaenger(
			gesuch.getDossier().getFall().getBesitzer()
		);
		betreuungsmitteilung.setMitteilungStatus(MitteilungStatus.NEU);
		betreuungsmitteilung.setSubject(
			ServerMessageUtil.getMessage(
				BETREFF_KEY,
				locale,
				mandant,
				gesuch.extractGemeinde()
			)
		);
		betreuungsmitteilung.setBetreuung(betreuung);
		betreuungsmitteilung.setBetreuungStornieren(true);

		List<BetreuungsmitteilungPensum> betreuungsMitteilungPensen = betreuung
			.getBetreuungspensumContainers()
			.stream()
			.map(
				BetreuungStornierenMitteilungFactory::fromBetreuungspensumContainerToZero
			)
			.toList();
		betreuungsmitteilung.getBetreuungspensen()
			.addAll(betreuungsMitteilungPensen);
		betreuungsmitteilung.getBetreuungspensen()
			.forEach(p -> p.setBetreuungsmitteilung(betreuungsmitteilung));
		betreuungsmitteilung.setMessage(
			ServerMessageUtil.getMessage(
				MESSAGE_KEY,
				locale,
				mandant,
				betreuung.getReferenzNummer()
			)
		);

		return betreuungsmitteilung;
	}

	@Nonnull
	public static BetreuungsmitteilungPensum fromBetreuungspensumContainerToZero(
		@Nonnull BetreuungspensumContainer container
	) {
		BetreuungsmitteilungPensum pensum = new BetreuungsmitteilungPensum();

		container.getBetreuungspensumJA()
			.copyAbstractBetreuungspensumMahlzeitenEntity(
				pensum,
				AntragCopyType.MUTATION
			);
		pensum.setPensum(BigDecimal.ZERO);

		return pensum;
	}
}
