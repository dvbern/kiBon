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

package ch.dvbern.ebegu.api.dtos.gemeindeantrag;

import java.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.ebegu.api.dtos.JaxAbstractDTO;
import ch.dvbern.ebegu.api.dtos.JaxBenutzerNoDetails;
import ch.dvbern.ebegu.api.dtos.JaxGemeinde;
import ch.dvbern.ebegu.api.dtos.JaxGesuchsperiode;
import ch.dvbern.ebegu.enums.gemeindeantrag.GemeindeAntragTyp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JaxGemeindeAntrag extends JaxAbstractDTO {

	private static final long serialVersionUID = 4099969051581833190L;

	@NotNull
	@Nonnull
	private GemeindeAntragTyp gemeindeAntragTyp;

	@NotNull
	@Nonnull
	private JaxGemeinde gemeinde;

	@NotNull
	@Nonnull
	private JaxGesuchsperiode gesuchsperiode;

	@NotNull
	@Nonnull
	private String statusString;

	private boolean antragAbgeschlossen;

	private JaxBenutzerNoDetails verantwortlicher;

	@Nullable
	private LocalDate einreichedatum;

}
