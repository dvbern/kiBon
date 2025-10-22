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

package ch.dvbern.ebegu.services.gemeindeantrag;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.gemeindeantrag.gemeindekennzahlen.GemeindeKennzahlen;
import ch.dvbern.ebegu.entities.gemeindeantrag.gemeindekennzahlen.GemeindeKennzahlenStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.mail.MailTemplateConfiguration;
import ch.dvbern.ebegu.services.AbstractMailServiceBean;
import ch.dvbern.ebegu.services.BenutzerService;

/**
 * Service fuer Gemeindeantraege
 */
@Stateless
public class GemeindeKennzahlenMailService extends AbstractMailServiceBean {

	@Inject
	private MailTemplateConfiguration mailTemplateConfig;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private GemeindeKennzahlenService gemeindeKennzahlenService;

	public void sendFirstErinnerungsmailToAllAdminBGOfMandant(
		Mandant mandant,
		String mandantInfoMail
	) {
		Collection<Benutzer> activeBenutzerInRolesOfActiveGemeinden =
			benutzerService
				.getActiveBenutzerInRolesOfActiveGemeinden(
					mandant,
					UserRole.ADMIN_BG,
					UserRole.ADMIN_GEMEINDE
				);

		getBenutzerAndMandantInfoMails(
			mandantInfoMail,
			activeBenutzerInRolesOfActiveGemeinden
		).forEach(
			email -> sendFirstErinnerungsmailToEmpfaenger(mandant, email)
		);
	}

	public void sendFirstErinnerungsmailToEmpfaenger(
		Mandant mandant,
		String mail
	) {
		String messageBody = mailTemplateConfig
			.getGemeindeKennzahlenFirstErinnerung(mandant, mail);
		toOutboxMail(messageBody, mail, mandant.getMandantIdentifier());
	}

	public void sendSecondErinnerungsmailToAllAdminBGOfMandant(
		Mandant mandant,
		String mandantInfoMail
	) {
		List<Gemeinde> gemeindenWithOpenGemeindeKennzahlen =
			gemeindeKennzahlenService.getOffeneGemeindeKennzahlen(
				mandant,
				GemeindeKennzahlenStatus.IN_BEARBEITUNG_GEMEINDE
			)
				.stream()
				.map(GemeindeKennzahlen::getGemeinde)
				.collect(Collectors.toList());
		Collection<Benutzer> activeBgAdminsOfGemeinden = benutzerService
			.getActiveBenutzerInRolesOfGemeinden(
				mandant,
				gemeindenWithOpenGemeindeKennzahlen,
				UserRole.ADMIN_BG,
				UserRole.ADMIN_GEMEINDE
			);

		getBenutzerAndMandantInfoMails(
			mandantInfoMail,
			activeBgAdminsOfGemeinden
		).forEach(
			email -> sendSecondErinnerungsmailToEmpfaenger(mandant, email)
		);
	}

	public void sendSecondErinnerungsmailToEmpfaenger(
		Mandant mandant,
		String mail
	) {
		String messageBody = mailTemplateConfig
			.getGemeindeKennzahlenSecondErinnerung(mandant, mail);
		toOutboxMail(messageBody, mail, mandant.getMandantIdentifier());
	}

	private List<String> getBenutzerAndMandantInfoMails(
		String mandantInfoMail,
		Collection<Benutzer> baseBenutzer
	) {
		List<String> receipientList = baseBenutzer
			.stream()
			.map(Benutzer::getEmail)
			.collect(Collectors.toList());
		receipientList.add(mandantInfoMail);
		return receipientList;
	}
}
