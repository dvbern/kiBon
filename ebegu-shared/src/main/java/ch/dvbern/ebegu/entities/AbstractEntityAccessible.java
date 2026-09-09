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

package ch.dvbern.ebegu.entities;

import java.time.LocalDateTime;

public interface AbstractEntityAccessible {

	String getId();

	long getVersion();

	LocalDateTime getTimestampErstellt();

	LocalDateTime getTimestampMutiert();

	String getUserErstellt();

	String getUserMutiert();

	void setId(String id);

	void setVersion(long version);

	void setTimestampErstellt(LocalDateTime timestamp);

	void setTimestampMutiert(LocalDateTime timestamp);

	void setUserErstellt(String user);

	void setUserMutiert(String user);
}
