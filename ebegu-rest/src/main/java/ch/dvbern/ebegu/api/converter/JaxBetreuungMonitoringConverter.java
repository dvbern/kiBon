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

import javax.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;

import ch.dvbern.ebegu.api.dtos.JaxBetreuungMonitoring;
import ch.dvbern.ebegu.entities.BetreuungMonitoring;

@Dependent
public class JaxBetreuungMonitoringConverter extends AbstractBaseConverter {
	@Nonnull
	public JaxBetreuungMonitoring betreuungMonitoringToJax(
		@Nonnull BetreuungMonitoring betreuungMonitoring
	) {
		final JaxBetreuungMonitoring jaxBetreuungMonitoring =
			new JaxBetreuungMonitoring();
		convertAbstractFieldsToJAX(betreuungMonitoring, jaxBetreuungMonitoring);
		jaxBetreuungMonitoring.setBenutzer(betreuungMonitoring.getBenutzer());
		jaxBetreuungMonitoring.setInfoText(betreuungMonitoring.getInfoText());
		jaxBetreuungMonitoring.setRefNummer(betreuungMonitoring.getRefNummer());
		jaxBetreuungMonitoring.setTimestamp(betreuungMonitoring.getTimestamp());
		return jaxBetreuungMonitoring;
	}
}
