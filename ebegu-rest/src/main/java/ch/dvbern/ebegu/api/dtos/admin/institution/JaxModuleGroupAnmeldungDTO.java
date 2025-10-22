package ch.dvbern.ebegu.api.dtos.admin.institution;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class JaxModuleGroupAnmeldungDTO {
	String groupId;
	boolean hasAnmeldung;
}
