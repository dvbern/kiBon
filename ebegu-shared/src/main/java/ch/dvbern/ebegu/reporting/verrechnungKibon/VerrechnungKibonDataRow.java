/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.reporting.verrechnungKibon;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * DTO für die Verrechnung von KiBon für eine Gemeinde in einer Gesuchsperiode
 */
public class VerrechnungKibonDataRow implements Comparable<VerrechnungKibonDataRow> {

	private BigDecimal betragProKind;
	private String gemeinde;
	private String gesuchsperiode;

	private Long kinderKantonTotal;
	private Long kinderKantonBereitsVerrechnet;
	private Long kinderBgTotal;
	private Long kinderBgBereitsVerrechnet;
	private Long kinderTsTotal;
	private Long kinderTsBereitsVerrechnet;
	private Long kinderKeinAngebotTotal;
	private Long kinderKeinAngebotBereitsVerrechnet;
	private Long kinderGemeindeTotal;
	private Long kinderGemeindeBereitsVerrechnet;
	private Long kinderFiTotal;
	private Long kinderFiBereitsVerrechnet;
	private Long kinderTagiTotal;
	private Long kinderTagiBereitsVerrechnet;

	public BigDecimal getBetragProKind() {
		return betragProKind;
	}

	public void setBetragProKind(BigDecimal betragProKind) {
		this.betragProKind = betragProKind;
	}

	public String getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(String gemeinde) {
		this.gemeinde = gemeinde;
	}

	public String getGesuchsperiode() {
		return gesuchsperiode;
	}

	public void setGesuchsperiode(String gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	public Long getKinderKantonTotal() {
		return kinderKantonTotal;
	}

	public void setKinderKantonTotal(Long kinderKantonTotal) {
		this.kinderKantonTotal = kinderKantonTotal;
	}

	public Long getKinderKantonBereitsVerrechnet() {
		return kinderKantonBereitsVerrechnet;
	}

	public void setKinderKantonBereitsVerrechnet(Long kinderKantonBereitsVerrechnet) {
		this.kinderKantonBereitsVerrechnet = kinderKantonBereitsVerrechnet;
	}

	public Long getKinderBgTotal() {
		return kinderBgTotal;
	}

	public void setKinderBgTotal(Long kinderBgTotal) {
		this.kinderBgTotal = kinderBgTotal;
	}

	public Long getKinderBgBereitsVerrechnet() {
		return kinderBgBereitsVerrechnet;
	}

	public void setKinderBgBereitsVerrechnet(Long kinderBgBereitsVerrechnet) {
		this.kinderBgBereitsVerrechnet = kinderBgBereitsVerrechnet;
	}

	public Long getKinderTsTotal() {
		return kinderTsTotal;
	}

	public void setKinderTsTotal(Long kinderTsTotal) {
		this.kinderTsTotal = kinderTsTotal;
	}

	public Long getKinderTsBereitsVerrechnet() {
		return kinderTsBereitsVerrechnet;
	}

	public void setKinderTsBereitsVerrechnet(Long kinderTsBereitsVerrechnet) {
		this.kinderTsBereitsVerrechnet = kinderTsBereitsVerrechnet;
	}

	public Long getKinderKeinAngebotTotal() {
		return kinderKeinAngebotTotal;
	}

	public void setKinderKeinAngebotTotal(Long kinderKeinAngebotTotal) {
		this.kinderKeinAngebotTotal = kinderKeinAngebotTotal;
	}

	public Long getKinderKeinAngebotBereitsVerrechnet() {
		return kinderKeinAngebotBereitsVerrechnet;
	}

	public void setKinderKeinAngebotBereitsVerrechnet(Long kinderKeinAngebotBereitsVerrechnet) {
		this.kinderKeinAngebotBereitsVerrechnet = kinderKeinAngebotBereitsVerrechnet;
	}

	public Long getKinderGemeindeTotal() {
		return kinderGemeindeTotal;
	}

	public void setKinderGemeindeTotal(Long kinderGemeindeTotal) {
		this.kinderGemeindeTotal = kinderGemeindeTotal;
	}

	public Long getKinderGemeindeBereitsVerrechnet() {
		return kinderGemeindeBereitsVerrechnet;
	}

	public void setKinderGemeindeBereitsVerrechnet(Long kinderGemeindeBereitsVerrechnet) {
		this.kinderGemeindeBereitsVerrechnet = kinderGemeindeBereitsVerrechnet;
	}

	public Long getKinderFiTotal() {
		return kinderFiTotal;
	}

	public void setKinderFiTotal(Long kinderFiTotal) {
		this.kinderFiTotal = kinderFiTotal;
	}

	public Long getKinderFiBereitsVerrechnet() {
		return kinderFiBereitsVerrechnet;
	}

	public void setKinderFiBereitsVerrechnet(Long kinderFiBereitsVerrechnet) {
		this.kinderFiBereitsVerrechnet = kinderFiBereitsVerrechnet;
	}

	public Long getKinderTagiTotal() {
		return kinderTagiTotal;
	}

	public void setKinderTagiTotal(Long kinderTagiTotal) {
		this.kinderTagiTotal = kinderTagiTotal;
	}

	public Long getKinderTagiBereitsVerrechnet() {
		return kinderTagiBereitsVerrechnet;
	}

	public void setKinderTagiBereitsVerrechnet(Long kinderTagiBereitsVerrechnet) {
		this.kinderTagiBereitsVerrechnet = kinderTagiBereitsVerrechnet;
	}

	@Override
	public int compareTo(VerrechnungKibonDataRow o) {
		int result = this.getGemeinde().compareTo(o.getGemeinde());
		if (result == 0) {
			result = this.getGesuchsperiode().compareTo(o.getGesuchsperiode());
		}
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof VerrechnungKibonDataRow)) {
			return false;
		}
		VerrechnungKibonDataRow that = (VerrechnungKibonDataRow) o;
		return Objects.equals(gemeinde, that.gemeinde) &&
			Objects.equals(gesuchsperiode, that.gesuchsperiode);
	}

	@Override
	public int hashCode() {
		return Objects.hash(gemeinde, gesuchsperiode);
	}
}
