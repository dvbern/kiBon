package ch.dvbern.ebegu.util;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Gemeinde;

public class MandantLocaleVisitorFactory {
	private MandantLocaleVisitorFactory() {
	}

	public static MandantLocaleVisitor getMandantLocaleVisitor(
		@Nonnull Locale locale,
		@Nullable Gemeinde gemeinde
	) {
		if (locale.getLanguage().equalsIgnoreCase("FR")) {
			return new MandantLocaleVisitor(Constants.FRENCH_LOCALE, gemeinde);
		} else {
			return new MandantLocaleVisitor(Constants.DEUTSCH_LOCALE, gemeinde);
		}
	}
}
