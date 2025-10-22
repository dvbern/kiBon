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

package ch.dvbern.ebegu.inbox.handler.pensum;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Validator;

import ch.dvbern.ebegu.einstellung.EinstellungService;
import ch.dvbern.ebegu.entities.AbstractMahlzeitenPensum;
import ch.dvbern.ebegu.enums.EingewoehnungTyp;
import ch.dvbern.ebegu.inbox.handler.ProcessingContext;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import static ch.dvbern.ebegu.einstellung.EinstellungKey.EINGEWOEHNUNG_TYP;

@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor
public class EingewoehnungMapperFactory {

	@Inject
	private EinstellungService einstellungService;

	@Inject
	private Validator validator;

	public PensumMapper<AbstractMahlzeitenPensum> createForEingewoehnung(
		ProcessingContext ctx
	) {
		EingewoehnungTyp eingewoehnungTyp =
			EingewoehnungTyp.valueOf(
				einstellungService.findEinstellung(
					EINGEWOEHNUNG_TYP,
					ctx.getBetreuung()
				).getValue()
			);

		return eingewoehnungTyp.isEingewoehnungTypPauschale() ?
			new EingewoehnungMapper(ctx, validator) :
			PensumMapper.nop();
	}
}
