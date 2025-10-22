/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.util;

import java.util.Locale;

import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;

/**
 * Die Locale von kiBon haben drei moegliche Stuffen
 * 1. server-messages[_SPRACHE].properties => Default Uebersetungen, alles muss dort vorhanden sein
 * 2. server-messages_SPRACHE_KANTON.properties => Uebersetzungen die man pro Kanton ueberschreiben kann
 * 3. server-messages_SPRACHE_KANTON_BFSNUMMER => Uebersetzungen die man pro Gemeiende ueberschreiben kann
 *
 * Der Fallback Strategy laeuft in der oben gegebene Ordnung von unter nach oben
 * - Erst wird wenn die Gemeinde ist gegeben die Uebersetung in der Gemeinde server-messages gesucht
 * - Wenn die Datei oder die MSG_KEY nicht gefunden ist dann ist die Uebersetzung in der server-messages des Kanton
 * gesucht
 * - Wenn die Datei oder die MSG_KEY nicht gefunden ist dann ist die Uebersetzung in der basis server-messages gesucht
 */
public class MandantLocaleVisitor implements MandantVisitor<Locale> {

	private static final String VARIANT_BE = "be";
	private static final String VARIANT_LU = "lu";
	private static final String VARIANT_SO = "so";
	private static final String VARIANT_APPENZELL_AUSSERRHODEN = "ar";
	private static final String VARIANT_SCHWYZ = "sz";
	private static final String VARIANT_ZUG = "zg";
	private static final String VARIANT_DVB = "dv";

	private final Locale locale;
	@Nullable
	private Gemeinde gemeinde;

	public MandantLocaleVisitor(Locale locale) {
		this.locale = locale;
	}

	public MandantLocaleVisitor(Locale locale, @Nullable Gemeinde gemeinde) {
		this.locale = locale;
		this.gemeinde = gemeinde;
	}

	public Locale process(Mandant mandant) {
		return process(mandant.getMandantIdentifier());
	}

	public Locale process(MandantIdentifier mandantIdentifier) {
		return mandantIdentifier.accept(this);
	}

	@Override
	public Locale visitBern() {
		return createMandantGemeindeLocale(VARIANT_BE, gemeinde);
	}

	@Override
	public Locale visitLuzern() {
		return createMandantGemeindeLocale(VARIANT_LU, gemeinde);
	}

	@Override
	public Locale visitSolothurn() {
		return createMandantGemeindeLocale(VARIANT_SO, gemeinde);
	}

	@Override
	public Locale visitAppenzellAusserrhoden() {
		return createMandantGemeindeLocale(
			VARIANT_APPENZELL_AUSSERRHODEN,
			gemeinde
		);
	}

	private Locale createMandantGemeindeLocale(
		String kantonVariant,
		@Nullable Gemeinde gemeinde
	) {
		if (gemeinde != null) {
			return new Locale(
				locale.getLanguage(),
				kantonVariant,
				gemeinde.getBfsNummer().toString()
			);
		}
		return new Locale(locale.getLanguage(), kantonVariant);
	}

	@Override
	public Locale visitSchwyz() {
		return createMandantGemeindeLocale(VARIANT_SCHWYZ, gemeinde);
	}

	@Override
	public Locale visitZug() {
		return createMandantGemeindeLocale(VARIANT_ZUG, gemeinde);
	}

	@Override
	public Locale visitDvb() {
		return createMandantGemeindeLocale(VARIANT_DVB, gemeinde);
	}
}
