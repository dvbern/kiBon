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

package ch.dvbern.ebegu.api.einstellung.convertor;

import java.util.Optional;

import javax.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.api.converter.AbstractBaseConverter;
import ch.dvbern.ebegu.api.dtos.JaxEnversRevision;
import ch.dvbern.ebegu.api.einstellung.dto.JaxApplicationProperties;
import ch.dvbern.ebegu.api.einstellung.dto.JaxApplicationPropertiesWithMetadata;
import ch.dvbern.ebegu.api.einstellung.dto.JaxConfigurationMetadata;
import ch.dvbern.ebegu.api.einstellung.dto.JaxEinstellung;
import ch.dvbern.ebegu.api.einstellung.dto.JaxEinstellungWithMetadata;
import ch.dvbern.ebegu.einstellung.ApplicationProperty;
import ch.dvbern.ebegu.einstellung.ApplicationPropertyKey;
import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.KeyGrouping;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.lib.date.DateConvertUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;

import static java.util.Objects.requireNonNull;

@Dependent
public class JaxConfigurationConverter extends AbstractBaseConverter {
	@Inject
	private GesuchsperiodeService gesuchsperiodeService;
	@Inject
	private GemeindeService gemeindeService;

	@Nonnull
	public JaxApplicationProperties applicationPropertyToJAX(
		@Nonnull final ApplicationProperty applicationProperty
	) {
		final JaxApplicationProperties jaxProperty =
			new JaxApplicationProperties();
		convertApplicationPropertyToJAX(applicationProperty, jaxProperty);
		return jaxProperty;
	}

	private void convertApplicationPropertyToJAX(
		@Nonnull final ApplicationProperty applicationProperty,
		@Nonnull final JaxApplicationProperties jaxProperty
	) {
		convertAbstractVorgaengerFieldsToJAX(applicationProperty, jaxProperty);
		jaxProperty.setName(applicationProperty.getName().toString());
		jaxProperty.setValue(applicationProperty.getValue());
		jaxProperty.setErklaerung(applicationProperty.getErklaerung());
	}

	@Nonnull
	public JaxApplicationPropertiesWithMetadata applicationPropertyToJAXWithMetadata(
		@Nonnull final ApplicationProperty applicationProperty
	) {
		final JaxApplicationPropertiesWithMetadata jaxProperty =
			new JaxApplicationPropertiesWithMetadata();
		convertApplicationPropertyToJAX(applicationProperty, jaxProperty);
		JaxConfigurationMetadata metadata = new JaxConfigurationMetadata();
		metadata.setKeyOrder(KeyGrouping.DEFAULT_ORDER);
		if (applicationProperty.getName().getKeyGrouping().isPresent()) {
			KeyGrouping keyGrouping = applicationProperty.getName()
				.getKeyGrouping()
				.get();
			metadata.setKeyGroup(keyGrouping.keyGroup());
			metadata.setKeyOrder(keyGrouping.keyOrder());
			metadata.setKeyGroupOrder(keyGrouping.keyGroup().getOrder());
			metadata.setSubKeyGroup(keyGrouping.subKeyGroup());
		}
		jaxProperty.setMetadata(metadata);
		jaxProperty.setUserMutiert(applicationProperty.getUserMutiert());

		return jaxProperty;
	}

	@Nonnull
	public ApplicationProperty applicationPropertieToEntity(
		final JaxApplicationProperties jaxAP,
		@Nonnull final ApplicationProperty applicationProperty
	) {

		requireNonNull(applicationProperty);
		requireNonNull(jaxAP);

		convertAbstractVorgaengerFieldsToEntity(jaxAP, applicationProperty);
		convertMandantFieldsToEntity(applicationProperty);
		applicationProperty.setName(
			Enum.valueOf(ApplicationPropertyKey.class, jaxAP.getName())
		);
		applicationProperty.setValue(jaxAP.getValue());
		applicationProperty.setErklaerung(jaxAP.getErklaerung());

		return applicationProperty;
	}

	@Nonnull
	public Einstellung einstellungToEntity(
		final JaxEinstellung jaxEinstellung,
		@Nonnull final Einstellung einstellung
	) {
		requireNonNull(einstellung);
		requireNonNull(jaxEinstellung);
		convertAbstractFieldsToEntity(jaxEinstellung, einstellung);
		convertMandantFieldsToEntity(einstellung);
		einstellung.setKey(jaxEinstellung.getKey());
		einstellung.setValue(jaxEinstellung.getValue());
		einstellung.setErklaerung(jaxEinstellung.getErklaerung());
		if (jaxEinstellung.getGemeindeId() != null) {
			einstellung.setGemeinde(
				gemeindeService.findGemeinde(jaxEinstellung.getGemeindeId())
					.orElse(null)
			);
		}
		final Optional<Gesuchsperiode> gesuchsperiode =
			gesuchsperiodeService.findGesuchsperiode(
				jaxEinstellung.getGesuchsperiodeId()
			);
		if (!gesuchsperiode.isPresent()) {
			throw new EbeguEntityNotFoundException(
				"einstellungToEntity",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				jaxEinstellung.getGesuchsperiodeId()
			);
		}
		einstellung.setGesuchsperiode(gesuchsperiode.get());
		// Mandant wird aktuell nicht gemappt
		return einstellung;
	}

	@Nonnull
	public JaxEinstellung einstellungToJAX(
		@Nonnull final Einstellung einstellung
	) {
		final JaxEinstellung jaxEinstellung = new JaxEinstellung();
		convertEinstellungToJAX(einstellung, jaxEinstellung);
		return jaxEinstellung;
	}

	private void convertEinstellungToJAX(
		@Nonnull final Einstellung einstellung,
		@Nonnull final JaxEinstellung jaxEinstellung
	) {
		convertAbstractFieldsToJAX(einstellung, jaxEinstellung);
		jaxEinstellung.setKey(einstellung.getKey());
		jaxEinstellung.setValue(einstellung.getValue());
		jaxEinstellung.setErklaerung(einstellung.getErklaerung());
		jaxEinstellung.setGemeindeId(
			null == einstellung.getGemeinde() ?
				null :
				einstellung.getGemeinde().getId()
		);
		jaxEinstellung.setGesuchsperiodeId(
			einstellung.getGesuchsperiode().getId()
		);
		// Mandant wird aktuell nicht gemappt
	}

	@Nonnull
	public JaxEinstellungWithMetadata einstellungToJAXWithMetadata(
		@Nonnull final Einstellung einstellung
	) {
		final JaxEinstellungWithMetadata jaxEinstellung =
			new JaxEinstellungWithMetadata();
		convertEinstellungToJAX(einstellung, jaxEinstellung);
		KeyGrouping keyGrouping = einstellung.getKey().getKeyGrouping();
		JaxConfigurationMetadata metadata = new JaxConfigurationMetadata();
		metadata.setKeyGroup(keyGrouping.keyGroup());
		metadata.setKeyOrder(keyGrouping.keyOrder());
		if (keyGrouping.keyGroup() != null) {
			metadata.setKeyGroupOrder(keyGrouping.keyGroup().getOrder());
		}
		jaxEinstellung.setMetadata(metadata);
		jaxEinstellung.setUserMutiert(einstellung.getUserMutiert());
		return jaxEinstellung;
	}

	@Nonnull
	public JaxEnversRevision enversRevisionToJAX(
		@Nonnull final DefaultRevisionEntity revisionEntity,
		@Nonnull final AbstractEntity abstractEntity,
		final RevisionType accessType
	) {

		final JaxEnversRevision jaxEnversRevision = new JaxEnversRevision();
		if (abstractEntity instanceof ApplicationProperty) {
			jaxEnversRevision.setEntity(
				applicationPropertyToJAX((ApplicationProperty) abstractEntity)
			);
		} else {
			throw new NotImplementedException(
				"Diese Funktion ist erst fuer ApplicationProperties umgesetzt!"
			);
		}
		jaxEnversRevision.setRev(revisionEntity.getId());
		jaxEnversRevision.setRevTimeStamp(
			requireNonNull(
				DateConvertUtils.asLocalDateTime(
					revisionEntity.getRevisionDate()
				)
			)
		);
		jaxEnversRevision.setAccessType(accessType);

		return jaxEnversRevision;
	}
}
