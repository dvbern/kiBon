package ch.dvbern.ebegu.testfaelle.testfealleschwyz;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class BetreuungData {

	@Getter
	@Setter
	private boolean auszahlungAnEltern;

	@Getter
	@Setter
	private String begruendung;

	@Getter
	@Setter
	private String instiutionId;

	@Getter
	@Setter
	private boolean bestaetigt;

	@Getter
	List<PensumData> betreuungspensum = new ArrayList<>();
}
