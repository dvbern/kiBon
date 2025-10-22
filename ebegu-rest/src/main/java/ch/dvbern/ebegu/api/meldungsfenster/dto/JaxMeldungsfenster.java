/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.meldungsfenster.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ch.dvbern.ebegu.api.dtos.JaxAbstractDTO;
import ch.dvbern.ebegu.entities.meldungsfenster.MeldungsfensterRole;
import ch.dvbern.ebegu.entities.meldungsfenster.MeldungsfensterStatus;
import io.github.threetenjaxb.core.LocalDateTimeXmlAdapter;

@XmlRootElement(name = "meldungsfenster")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxMeldungsfenster extends JaxAbstractDTO {

	private static final long serialVersionUID = 8244737446639847784L;

	@NotNull
	private List<MeldungsfensterRole> zielgruppe;

	@NotNull
	@XmlJavaTypeAdapter(LocalDateTimeXmlAdapter.class)
	private LocalDateTime gueltigAb = null;

	@NotNull
	@XmlJavaTypeAdapter(LocalDateTimeXmlAdapter.class)
	private LocalDateTime gueltigBis = null;

	@NotNull
	private MeldungsfensterStatus status;

	@NotNull
	private String titleDe;

	@Nullable
	private String titleFr;

	@NotNull
	private String inhaltDe;

	@Nullable
	private String inhaltFr;

	public List<MeldungsfensterRole> getZielgruppe() {
		return zielgruppe;
	}

	public void setZielgruppe(List<MeldungsfensterRole> zielgruppe) {
		this.zielgruppe = zielgruppe;
	}

	public @NotNull LocalDateTime getGueltigAb() {
		return gueltigAb;
	}

	public void setGueltigAb(@NotNull LocalDateTime gueltigAb) {
		this.gueltigAb = gueltigAb;
	}

	public @NotNull LocalDateTime getGueltigBis() {
		return gueltigBis;
	}

	public void setGueltigBis(@NotNull LocalDateTime gueltigBis) {
		this.gueltigBis = gueltigBis;
	}

	public @NotNull MeldungsfensterStatus getStatus() {
		return status;
	}

	public void setStatus(@NotNull MeldungsfensterStatus status) {
		this.status = status;
	}

	public @NotNull String getTitleDe() {
		return titleDe;
	}

	public void setTitleDe(@NotNull String titleDe) {
		this.titleDe = titleDe;
	}

	@Nullable
	public String getTitleFr() {
		return titleFr;
	}

	public void setTitleFr(@Nullable String titleFr) {
		this.titleFr = titleFr;
	}

	public @NotNull String getInhaltDe() {
		return inhaltDe;
	}

	public void setInhaltDe(@NotNull String inhaltDe) {
		this.inhaltDe = inhaltDe;
	}

	@Nullable
	public String getInhaltFr() {
		return inhaltFr;
	}

	public void setInhaltFr(@Nullable String inhaltFr) {
		this.inhaltFr = inhaltFr;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof JaxMeldungsfenster)) {
			return false;
		}
		JaxMeldungsfenster other = (JaxMeldungsfenster) o;
		//noinspection NonFinalFieldReferenceInEquals
		return Objects.equals(this.getZielgruppe(), other.getZielgruppe())
			&& Objects.equals(this.getStatus(), other.getStatus())
			&& Objects.equals(this.getGueltigAb(), other.getGueltigAb())
			&& Objects.equals(this.getGueltigBis(), other.getGueltigBis())
			&& Objects.equals(this.getTitleDe(), other.getTitleDe())
			&& Objects.equals(this.getTitleFr(), other.getTitleFr())
			&& Objects.equals(this.getInhaltDe(), other.getInhaltDe())
			&& Objects.equals(this.getInhaltFr(), other.getInhaltFr());
	}

	@Override
	public int hashCode() {
		return super.hashCode()
			+ Objects.hashCode(this.getZielgruppe())
			+ Objects.hashCode(this.getStatus())
			+ Objects.hashCode(this.getGueltigAb())
			+ Objects.hashCode(this.getGueltigBis())
			+ Objects.hashCode(this.getTitleDe())
			+ Objects.hashCode(this.getTitleFr())
			+ Objects.hashCode(this.getInhaltDe())
			+ Objects.hashCode(this.getInhaltFr());
	}
}
