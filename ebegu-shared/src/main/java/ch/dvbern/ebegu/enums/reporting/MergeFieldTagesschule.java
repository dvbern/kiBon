/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ch.dvbern.ebegu.enums.reporting;

import javax.annotation.Nonnull;
import javax.print.DocFlavor.STRING;

import ch.dvbern.oss.lib.excelmerger.mergefields.MergeField;
import ch.dvbern.oss.lib.excelmerger.mergefields.MergeFieldProvider;
import ch.dvbern.oss.lib.excelmerger.mergefields.RepeatColMergeField;
import ch.dvbern.oss.lib.excelmerger.mergefields.RepeatRowMergeField;
import ch.dvbern.oss.lib.excelmerger.mergefields.RepeatValMergeField;
import ch.dvbern.oss.lib.excelmerger.mergefields.SimpleMergeField;

import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.DATE_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.STRING_CONVERTER;

public enum MergeFieldTagesschule implements MergeFieldProvider {

	tagesschuleOhneFinSitTitle(new SimpleMergeField<>("tagesschuleOhneFinSitTitle", STRING_CONVERTER)),
	periode(new SimpleMergeField<>("periode", STRING_CONVERTER)),
	nachnameKindTitle(new SimpleMergeField<>("nachnameKindTitle", STRING_CONVERTER)),
	vornameKindTitle(new SimpleMergeField<>("vornameKindTitle", STRING_CONVERTER)),
	geburtsdatumTitle(new SimpleMergeField<>("geburtsdatumTitle", STRING_CONVERTER)),
	referenznummerTitle(new SimpleMergeField<>("referenznummerTitle", STRING_CONVERTER)),
	abTitle(new SimpleMergeField<>("abTitle", STRING_CONVERTER)),
	statusTitle(new SimpleMergeField<>("statusTitle", STRING_CONVERTER)),

	repeatRow(new RepeatRowMergeField("repeatRow")),
	nachnameKind(new SimpleMergeField<>("nachnameKind", STRING_CONVERTER)),
	vornameKind(new SimpleMergeField<>("vornameKind", STRING_CONVERTER)),
	geburtsdatumKind(new SimpleMergeField<>("geburtsdatumKind", DATE_CONVERTER)),
	referenznummer(new SimpleMergeField<>("referenznummer", STRING_CONVERTER)),
	ab(new SimpleMergeField<>("ab", DATE_CONVERTER)),
	status(new SimpleMergeField<>("status", STRING_CONVERTER)),

	repeatCol(new RepeatColMergeField<>("repeatCol", STRING_CONVERTER)),
	modulName(new RepeatValMergeField<>("modulName", STRING_CONVERTER)),
	wochentag(new RepeatValMergeField<>("wochentag", STRING_CONVERTER)),
	angemeldet(new RepeatValMergeField<>("angemeldet", STRING_CONVERTER));

	@Nonnull
	private final MergeField<?> mergeField;

	<V> MergeFieldTagesschule(@Nonnull MergeField<V> mergeField) {
		this.mergeField = mergeField;
	}

	@Override
	@Nonnull
	public <V> MergeField<V> getMergeField() {
		//noinspection unchecked
		return (MergeField<V>) mergeField;
	}
}
