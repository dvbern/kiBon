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

package ch.dvbern.ebegu.api.converter.gesuch;

import java.util.Objects;

import javax.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.api.converter.AbstractBaseConverter;
import ch.dvbern.ebegu.api.dtos.JaxInternePendenz;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.InternePendenz;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.GesuchService;

@Dependent
public class JaxInternePendenzConverter extends AbstractBaseConverter {
	@Inject
	private GesuchService gesuchService;
	@Inject
	private JaxAntragConverter antragConverter;

	public InternePendenz internePendenzToEntity(
		@Nonnull JaxInternePendenz jaxInternePendenz,
		@Nonnull InternePendenz internePendenz
	) {
		Objects.requireNonNull(jaxInternePendenz.getGesuch().getId());
		convertAbstractFieldsToEntity(jaxInternePendenz, internePendenz);
		Gesuch gesuch = gesuchService.findGesuch(
			jaxInternePendenz.getGesuch().getId()
		)
			.orElseThrow(
				() -> new EbeguRuntimeException(
					"internePendenzToEntity",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					internePendenz.getGesuch().getId()
				)
			);
		internePendenz.setGesuch(gesuch);
		internePendenz.setTermin(jaxInternePendenz.getTermin());
		internePendenz.setText(jaxInternePendenz.getText());
		internePendenz.setErledigt(jaxInternePendenz.getErledigt());
		return internePendenz;
	}

	public JaxInternePendenz internePendenzToJax(
		@Nonnull final InternePendenz internePendenz
	) {
		final JaxInternePendenz jaxInternePendenz = new JaxInternePendenz();
		convertAbstractFieldsToJAX(internePendenz, jaxInternePendenz);
		jaxInternePendenz.setGesuch(
			antragConverter.gesuchToJAX(internePendenz.getGesuch())
		);
		jaxInternePendenz.setTermin(internePendenz.getTermin());
		jaxInternePendenz.setText(internePendenz.getText());
		jaxInternePendenz.setErledigt(internePendenz.getErledigt());
		return jaxInternePendenz;
	}
}
