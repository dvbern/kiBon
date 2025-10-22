package ch.dvbern.ebegu.testfaelle.testfealleschwyz;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.enums.Kinderabzug;
import lombok.Getter;
import lombok.Setter;

public class KindData {

	@Getter
	@Setter
	private String nachname;

	@Getter
	@Setter
	private String vorname;

	@Getter
	@Setter
	private Geschlecht geschlecht;

	@Getter
	@Setter
	private LocalDate geburtsdatum;

	@Getter
	@Setter
	private Kinderabzug kinderabzug;

	@Getter
	@Setter
	private Boolean hohereBeitraege = Boolean.FALSE;

	@Getter
	@Setter
	private Boolean unterhaltspflichtig;

	@Getter
	@Setter
	private Boolean lebtAlternierend;

	@Getter
	@Setter
	private Boolean gemeinsamesGesuch;

	@Getter
	@Setter
	private Boolean familienergaenzendBetreuug;

	@Getter
	@Setter
	private EinschulungTyp einschulungTyp;

	@Getter
	@Setter
	private List<BetreuungData> betreuungDataList = new ArrayList<>();
}
