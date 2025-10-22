package ch.dvbern.ebegu.lastenausgleich;

import javax.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public interface WorkjobLastenausgleichService {

	void startLastenausgleichWorkjob(
		@NotNull String jahr,
		@Nullable String selbstbehaltPro100ProzentPlatz
	);
}
