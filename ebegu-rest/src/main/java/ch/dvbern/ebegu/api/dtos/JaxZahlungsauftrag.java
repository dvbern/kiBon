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

package ch.dvbern.ebegu.api.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ch.dvbern.ebegu.enums.ZahlungauftragStatus;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import io.github.threetenjaxb.core.LocalDateTimeXmlAdapter;
import io.github.threetenjaxb.core.LocalDateXmlAdapter;

/**
 * DTO fuer Zahlungsauftrag
 */
@XmlRootElement(name = "zahlungsauftrag")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxZahlungsauftrag extends JaxAbstractDateRangedDTO {

	private static final long serialVersionUID = 5908117979039694339L;

	@NotNull
	@Nonnull
	private ZahlungslaufTyp zahlungslaufTyp;

	@NotNull
	@XmlJavaTypeAdapter(LocalDateXmlAdapter.class)
	private LocalDate datumFaellig;

	@NotNull
	@XmlJavaTypeAdapter(LocalDateTimeXmlAdapter.class)
	private LocalDateTime datumGeneriert;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateTimeXmlAdapter.class)
	private LocalDateTime datumBeendet;

	@NotNull
	private ZahlungauftragStatus status;

	@NotNull
	private String beschrieb;

	@NotNull
	private BigDecimal betragTotalAuftrag;

	private boolean hasNegativeZahlungen;

	@NotNull
	private JaxGemeinde gemeinde;

	@Nonnull
	private List<JaxZahlung> zahlungen = new ArrayList<>();

	@Nonnull
	public ZahlungslaufTyp getZahlungslaufTyp() {
		return zahlungslaufTyp;
	}

	public void setZahlungslaufTyp(@Nonnull ZahlungslaufTyp zahlungslaufTyp) {
		this.zahlungslaufTyp = zahlungslaufTyp;
	}

	public LocalDate getDatumFaellig() {
		return datumFaellig;
	}

	public void setDatumFaellig(LocalDate datumFaellig) {
		this.datumFaellig = datumFaellig;
	}

	public LocalDateTime getDatumGeneriert() {
		return datumGeneriert;
	}

	public void setDatumGeneriert(LocalDateTime datumGeneriert) {
		this.datumGeneriert = datumGeneriert;
	}

	public ZahlungauftragStatus getStatus() {
		return status;
	}

	public void setStatus(ZahlungauftragStatus status) {
		this.status = status;
	}

	public String getBeschrieb() {
		return beschrieb;
	}

	public void setBeschrieb(String beschrieb) {
		this.beschrieb = beschrieb;
	}

	@Nonnull
	public List<JaxZahlung> getZahlungen() {
		return zahlungen;
	}

	public void setZahlungen(@Nonnull List<JaxZahlung> zahlungen) {
		this.zahlungen = zahlungen;
	}

	public BigDecimal getBetragTotalAuftrag() {
		return betragTotalAuftrag;
	}

	public void setBetragTotalAuftrag(BigDecimal betragTotalAuftrag) {
		this.betragTotalAuftrag = betragTotalAuftrag;
	}

	public boolean getHasNegativeZahlungen() {
		return this.hasNegativeZahlungen;
	}

	public void setHasNegativeZahlungen(boolean hasNegativeZahlungen) {
		this.hasNegativeZahlungen = hasNegativeZahlungen;
	}

	@Nonnull
	public JaxGemeinde getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(@Nonnull JaxGemeinde gemeinde) {
		this.gemeinde = gemeinde;
	}

	@Nullable
	public LocalDateTime getDatumBeendet() {
		return datumBeendet;
	}

	public void setDatumBeendet(@Nullable LocalDateTime datumBeendet) {
		this.datumBeendet = datumBeendet;
	}
}
