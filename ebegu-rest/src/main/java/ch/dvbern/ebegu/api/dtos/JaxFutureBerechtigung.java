/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ch.dvbern.ebegu.api.dtos;

import java.io.Serial;

import javax.annotation.Nonnull;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Wrapper DTO fuer eine zukünftige Berechtigung
 */
@XmlRootElement(name = "futureberechtigung")
public class JaxFutureBerechtigung extends JaxBerechtigung {

	@Serial
	private static final long serialVersionUID = -6492218481965129937L;

	@Nonnull
	private JaxBerechtigung predecessor;

	@Nonnull
	public JaxBerechtigung getPredecessor() {
		return predecessor;
	}

	public void setPredecessor(@Nonnull JaxBerechtigung predecessor) {
		this.predecessor = predecessor;
	}
}
