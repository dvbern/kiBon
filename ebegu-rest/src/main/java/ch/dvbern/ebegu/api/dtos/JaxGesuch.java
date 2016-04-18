package ch.dvbern.ebegu.api.dtos;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO fuer Faelle
 */
@XmlRootElement(name = "gesuch")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxGesuch extends JaxAbstractDTO {

	private static final long serialVersionUID = -1217019901364130097L;

	@NotNull
	private JaxFall fall;

	@Nullable
	private JaxPerson gesuchsteller1;

	@Nullable
	private JaxPerson gesuchsteller2;

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public JaxFall getFall() {
		return fall;
	}

	public void setFall(JaxFall fall) {
		this.fall = fall;
	}

	@Nullable
	public JaxPerson getGesuchsteller1() {
		return gesuchsteller1;
	}

	public void setGesuchsteller1(@Nullable JaxPerson gesuchsteller1) {
		this.gesuchsteller1 = gesuchsteller1;
	}

	@Nullable
	public JaxPerson getGesuchsteller2() {
		return gesuchsteller2;
	}

	public void setGesuchsteller2(@Nullable JaxPerson gesuchsteller2) {
		this.gesuchsteller2 = gesuchsteller2;
	}
}
