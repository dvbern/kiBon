package ch.dvbern.ebegu.api.dtos;

import ch.dvbern.ebegu.converters.LocalDateXMLConverter;
import ch.dvbern.ebegu.enums.MahnungTyp;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;

/**
 * DTO fuer Mahnungen
 */
@XmlRootElement(name = "mahnung")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxMahnung extends JaxAbstractDTO {

	private static final long serialVersionUID = -1217019901364130097L;

	@Nullable
	private JaxGesuch gesuch;

	@Nullable
	private MahnungTyp mahnungTyp;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate datumFristablauf = null;

	@Nullable
	private String bemerkungen;

	@NotNull
	private boolean active;


	@Nullable
	public JaxGesuch getGesuch() {
		return gesuch;
	}

	public void setGesuch(@Nullable JaxGesuch gesuch) {
		this.gesuch = gesuch;
	}

	@Nullable
	public MahnungTyp getMahnungTyp() {
		return mahnungTyp;
	}

	public void setMahnungTyp(@Nullable MahnungTyp mahnungTyp) {
		this.mahnungTyp = mahnungTyp;
	}

	@Nullable
	public LocalDate getDatumFristablauf() {
		return datumFristablauf;
	}

	public void setDatumFristablauf(@Nullable LocalDate datumFristablauf) {
		this.datumFristablauf = datumFristablauf;
	}

	@Nullable
	public String getBemerkungen() {
		return bemerkungen;
	}

	public void setBemerkungen(@Nullable String bemerkungen) {
		this.bemerkungen = bemerkungen;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}