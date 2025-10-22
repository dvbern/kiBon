package ch.dvbern.ebegu.api.dtos.gemeindeantrag.ferienbetreuung;

import java.time.LocalDateTime;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ch.dvbern.ebegu.api.dtos.JaxBenutzer;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungAngabenStatus;
import io.github.threetenjaxb.core.LocalDateTimeXmlAdapter;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
@Builder
@Getter
@Setter
public class FerienbetreuungAngabenContainerStatusHistoryDTO {
	private JaxBenutzer benutzer;
	private FerienbetreuungAngabenStatus status;
	@XmlJavaTypeAdapter(LocalDateTimeXmlAdapter.class)
	private LocalDateTime timestampVon;
	@XmlJavaTypeAdapter(LocalDateTimeXmlAdapter.class)
	private LocalDateTime timestampBis;
}
