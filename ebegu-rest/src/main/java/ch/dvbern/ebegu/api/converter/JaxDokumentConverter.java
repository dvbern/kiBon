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

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.api.dtos.JaxDokument;
import ch.dvbern.ebegu.api.dtos.JaxDokumentGrund;
import ch.dvbern.ebegu.api.dtos.JaxDokumente;
import ch.dvbern.ebegu.api.dtos.JaxDownloadFile;
import ch.dvbern.ebegu.entities.Dokument;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.DownloadFile;
import ch.dvbern.ebegu.services.BenutzerService;
import org.apache.commons.lang3.StringUtils;

import static java.util.Objects.requireNonNull;

@Dependent
public class JaxDokumentConverter extends AbstractBaseConverter {
	@Inject
	private BenutzerService benutzerService;
	@Inject
	private JaxBenutzerConverter benutzerConverter;

	public JaxDokumente dokumentGruendeToJAX(
		Set<DokumentGrund> dokumentGrunds
	) {
		JaxDokumente jaxDokumente = new JaxDokumente();

		for (DokumentGrund dokumentGrund : dokumentGrunds) {
			jaxDokumente.getDokumentGruende()
				.add(dokumentGrundToJax(dokumentGrund));
		}

		return jaxDokumente;

	}

	public JaxDokumentGrund dokumentGrundToJax(DokumentGrund dokumentGrund) {
		JaxDokumentGrund jaxDokumentGrund =
			convertAbstractVorgaengerFieldsToJAX(
				dokumentGrund,
				new JaxDokumentGrund()
			);

		jaxDokumentGrund.setDokumentGrundTyp(
			dokumentGrund.getDokumentGrundTyp()
		);
		jaxDokumentGrund.setTag(dokumentGrund.getTag());
		jaxDokumentGrund.setPersonType(dokumentGrund.getPersonType());
		jaxDokumentGrund.setPersonNumber(dokumentGrund.getPersonNumber());
		jaxDokumentGrund.setDokumentTyp(dokumentGrund.getDokumentTyp());
		jaxDokumentGrund.setNeeded(dokumentGrund.isNeeded());
		jaxDokumentGrund.setDokumente(new HashSet<>());
		dokumentGrund.getDokumente()
			.stream()
			.map(this::dokumentToJax)
			.forEach(d -> jaxDokumentGrund.getDokumente().add(d));

		return jaxDokumentGrund;
	}

	private JaxDokument dokumentToJax(Dokument dokument) {
		JaxDokument jaxDokument = convertAbstractVorgaengerFieldsToJAX(
			dokument,
			new JaxDokument()
		);
		convertFileToJax(dokument, jaxDokument);
		jaxDokument.setTimestampUpload(dokument.getTimestampUpload());
		if (StringUtils.isNotEmpty(dokument.getUserErstellt())) {
			int userMandantTrennungPosition = dokument.getUserErstellt()
				.indexOf(':');
			String username = userMandantTrennungPosition != -1 ?
				dokument.getUserErstellt()
					.substring(0, userMandantTrennungPosition) :
				dokument.getUserErstellt();
			benutzerService.findBenutzer(
				username,
				getPrincipalBean().getMandant()
			)
				.map(
					benutzer -> benutzerConverter.benutzerToJaxBenutzer(
						benutzer
					)
				)
				.ifPresent(jaxDokument::setUserUploaded);
		}
		return jaxDokument;
	}

	public DokumentGrund dokumentGrundToEntity(
		@Nonnull final JaxDokumentGrund dokumentGrundJAXP,
		@Nonnull final DokumentGrund dokumentGrund
	) {

		requireNonNull(dokumentGrund);
		requireNonNull(dokumentGrundJAXP);

		convertAbstractVorgaengerFieldsToEntity(
			dokumentGrundJAXP,
			dokumentGrund
		);

		dokumentGrund.setDokumentGrundTyp(
			dokumentGrundJAXP.getDokumentGrundTyp()
		);
		dokumentGrund.setTag(dokumentGrundJAXP.getTag());
		dokumentGrund.setPersonType(dokumentGrundJAXP.getPersonType());
		dokumentGrund.setPersonNumber(dokumentGrundJAXP.getPersonNumber());
		dokumentGrund.setDokumentTyp(dokumentGrundJAXP.getDokumentTyp());
		dokumentGrund.setNeeded(dokumentGrundJAXP.isNeeded());

		return dokumentGrund;
	}

	public JaxDownloadFile downloadFileToJAX(DownloadFile downloadFile) {
		JaxDownloadFile jaxDownloadFile = new JaxDownloadFile();
		convertFileToJax(downloadFile, jaxDownloadFile);
		jaxDownloadFile.setAccessToken(downloadFile.getAccessToken());
		return jaxDownloadFile;
	}

}
