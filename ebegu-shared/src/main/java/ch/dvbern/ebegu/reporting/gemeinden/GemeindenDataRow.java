/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.reporting.gemeinden;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

public class GemeindenDataRow {

	@Nullable
	private String nameGemeinde;

	@Nullable
	private Long bfsNummer;

	@Nullable
	private String gutscheinausgabestelle;

	@Nullable
	private String korrespondenzspracheGemeinde;

	@Nullable
	private Boolean angebotBG;

	@Nullable
	private Boolean angebotTS;

	@Nullable
	private LocalDate startdatumBG;

	@Nullable
	private LocalDate gemeindeGueltigBis;

	@Nullable
	private String eMailStammdaten;

	@Nullable
	private String phoneStammdaten;

	@Nullable
	private String eMailBG;

	@Nullable
	private String phoneBG;

	@Nullable
	private String eMailTSA;

	@Nullable
	private String phoneTSA;

	private Set<GemeindenDatenDataRow> gemeindenDaten = new HashSet<>();

	@Nullable
	public String getNameGemeinde() {
		return nameGemeinde;
	}

	public void setNameGemeinde(@Nullable String nameGemeinde) {
		this.nameGemeinde = nameGemeinde;
	}

	@Nullable
	public Long getBfsNummer() {
		return bfsNummer;
	}

	public void setBfsNummer(@Nullable Long bfsNummer) {
		this.bfsNummer = bfsNummer;
	}

	@Nullable
	public String getGutscheinausgabestelle() {
		return gutscheinausgabestelle;
	}

	public void setGutscheinausgabestelle(
		@Nullable String gutscheinausgabestelle
	) {
		this.gutscheinausgabestelle = gutscheinausgabestelle;
	}

	@Nullable
	public String getKorrespondenzspracheGemeinde() {
		return korrespondenzspracheGemeinde;
	}

	public void setKorrespondenzspracheGemeinde(
		@Nullable String korrespondenzspracheGemeinde
	) {
		this.korrespondenzspracheGemeinde = korrespondenzspracheGemeinde;
	}

	@Nullable
	public Boolean getAngebotBG() {
		return angebotBG;
	}

	public void setAngebotBG(@Nullable Boolean angebotBG) {
		this.angebotBG = angebotBG;
	}

	@Nullable
	public Boolean getAngebotTS() {
		return angebotTS;
	}

	public void setAngebotTS(@Nullable Boolean angebotTS) {
		this.angebotTS = angebotTS;
	}

	@Nullable
	public LocalDate getStartdatumBG() {
		return startdatumBG;
	}

	public void setStartdatumBG(@Nullable LocalDate startdatumBG) {
		this.startdatumBG = startdatumBG;
	}

	@Nullable
	public LocalDate getGemeindeGueltigBis() {
		return gemeindeGueltigBis;
	}

	public void setGemeindeGueltigBis(@Nullable LocalDate gemeindeGueltigBis) {
		this.gemeindeGueltigBis = gemeindeGueltigBis;
	}

	@Nullable
	public String getEMailStammdaten() {
		return eMailStammdaten;
	}

	public void setEMailStammdaten(@Nullable String eMailStammdaten) {
		this.eMailStammdaten = eMailStammdaten;
	}

	@Nullable
	public String getPhoneStammdaten() {
		return phoneStammdaten;
	}

	public void setPhoneStammdaten(@Nullable String phoneStammdaten) {
		this.phoneStammdaten = phoneStammdaten;
	}

	@Nullable
	public String getEMailBG() {
		return eMailBG;
	}

	public void setEMailBG(@Nullable String eMailBG) {
		this.eMailBG = eMailBG;
	}

	@Nullable
	public String getPhoneBG() {
		return phoneBG;
	}

	public void setPhoneBG(@Nullable String phoneBG) {
		this.phoneBG = phoneBG;
	}

	@Nullable
	public String getEMailTSA() {
		return eMailTSA;
	}

	public void setEMailTSA(@Nullable String eMailTSA) {
		this.eMailTSA = eMailTSA;
	}

	@Nullable
	public String getPhoneTSA() {
		return phoneTSA;
	}

	public void setPhoneTSA(@Nullable String phoneTSA) {
		this.phoneTSA = phoneTSA;
	}

	public Set<GemeindenDatenDataRow> getGemeindenDaten() {
		return gemeindenDaten;
	}
}
