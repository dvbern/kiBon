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

import java.util.UUID;

import javax.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.TokenLifespan;
import ch.dvbern.ebegu.util.UploadFileInfo;

/**
 * Entitaet zum Speichern von DownloadFile in der Datenbank.
 */
@Entity
@EntityListeners(DownloadFileListener.class)
public class DownloadFile extends FileMetadata {

	private static final long serialVersionUID = 5960979521430438226L;

	@Column(length = 36, nullable = false, updatable = false)
	private final String accessToken;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false)
	private TokenLifespan lifespan = TokenLifespan.SHORT;

	// Wert darf nicht leer sein, aber kein @NotNull, da Wert erst im @PrePersist gesetzt
	@ManyToOne(optional = false)
	@JoinColumn(name = "benutzer_id", nullable = false)
	private Benutzer benutzer;

	public DownloadFile() {
		this.accessToken = UUID.randomUUID().toString();
	}

	public DownloadFile(@Nonnull FileMetadata file) {
		super(file);
		this.accessToken = UUID.randomUUID().toString();
	}

	public DownloadFile(UploadFileInfo uploadFileInfo) {
		super(uploadFileInfo);
		this.accessToken = UUID.randomUUID().toString();
	}

	public String getAccessToken() {
		return accessToken;
	}

	public TokenLifespan getLifespan() {
		return lifespan;
	}

	public void setLifespan(TokenLifespan lifespan) {
		this.lifespan = lifespan;
	}

	public Benutzer getBenutzer() {
		return benutzer;
	}

	public void setBenutzer(Benutzer benutzer) {
		this.benutzer = benutzer;
	}
}
