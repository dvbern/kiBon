package ch.dvbern.ebegu.einstellung;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface EnumEinstellung {

	Class<? extends Enum> value();
}
