package ch.dvbern.ebegu.kind;

import ch.dvbern.ebegu.enums.EinschulungTyp;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class KindResetDecisionBasis {
	private final EinschulungTyp einschulungTyp;
	private final boolean hoehereBeitraegeWegenBeeintraechtigungBeantragen;
}
