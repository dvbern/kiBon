/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
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

package ch.dvbern.ebegu.entities;

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

import static ch.dvbern.ebegu.util.Constants.TEN_MB;

/**
 * Entitaet zum Speichern von Dokumente in der Datenbank.
 */
@Entity
public class Vorlage extends AbstractEntity implements HasMandant {

	private static final long serialVersionUID = -895840426585785097L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(
		foreignKey = @ForeignKey(name = "FK_vorlage_mandant_id"),
		updatable = false
	)
	private Mandant mandant;

	@Nullable
	@Column(nullable = true, length = TEN_MB) // 10 megabytes
	@Lob
	@Basic(fetch = FetchType.LAZY)
	private byte[] vorlageDokument;

	public Vorlage(Mandant mandant) {
		this.mandant = mandant;
	}

	public Vorlage() {
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		return getId().equals(other.getId());
	}

	@NotNull
	@Override
	public Mandant getMandant() {
		return mandant;
	}

	@Override
	public void setMandant(Mandant mandant) {
		this.mandant = mandant;
	}

	@Nonnull
	public byte[] getVorlageDokument() {
		if (vorlageDokument == null) {
			return new byte[0];
		}
		return Arrays.copyOf(vorlageDokument, vorlageDokument.length);
	}

	public void setVorlageDokument(@Nullable byte[] vorlageDokument) {
		if (vorlageDokument == null) {
			this.vorlageDokument = null;
		} else {
			this.vorlageDokument = Arrays.copyOf(
				vorlageDokument,
				vorlageDokument.length
			);
		}
	}
}
