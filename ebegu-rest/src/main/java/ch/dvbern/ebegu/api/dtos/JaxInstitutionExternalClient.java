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

package ch.dvbern.ebegu.api.dtos;

import java.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import io.github.threetenjaxb.core.LocalDateXmlAdapter;

@XmlRootElement(name = "institutionExternalClient")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxInstitutionExternalClient {

	private static final long serialVersionUID = 4045567623627216321L;

	@Nonnull
	private @NotNull JaxExternalClient externalClient;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXmlAdapter.class)
	private LocalDate gueltigAb = null;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXmlAdapter.class)
	private LocalDate gueltigBis = null;

	@Nullable
	public LocalDate getGueltigAb() {
		return gueltigAb;
	}

	public void setGueltigAb(@Nullable LocalDate gueltigAb) {
		this.gueltigAb = gueltigAb;
	}

	@Nullable
	public LocalDate getGueltigBis() {
		return gueltigBis;
	}

	public void setGueltigBis(@Nullable LocalDate gueltigBis) {
		this.gueltigBis = gueltigBis;
	}

	@Nonnull
	public JaxExternalClient getExternalClient() {
		return externalClient;
	}

	public void setExternalClient(@Nonnull JaxExternalClient externalClient) {
		this.externalClient = externalClient;
	}
}
