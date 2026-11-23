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

package ch.dvbern.ebegu.api.dtos;

import java.io.Serial;
import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ch.dvbern.ebegu.enums.BatchjobStatus;
import io.github.threetenjaxb.core.LocalDateTimeXmlAdapter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JaxBatchjobHistory extends JaxAbstractDTO {

	@Serial
	private static final long serialVersionUID = 4124612715931396554L;

	@Nonnull
	private String name;

	@Nonnull
	private BatchjobStatus status;

	@Nonnull
	@XmlJavaTypeAdapter(LocalDateTimeXmlAdapter.class)
	private LocalDateTime timestampStarted;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateTimeXmlAdapter.class)
	private LocalDateTime timestampFinished;

	@Nullable
	private String result;
}
