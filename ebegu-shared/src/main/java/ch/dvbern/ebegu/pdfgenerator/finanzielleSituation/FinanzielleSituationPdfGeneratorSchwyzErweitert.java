/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.pdfgenerator.finanzielleSituation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.finanziellesituation.AbstractFinanzielleSituationContainer;
import com.lowagie.text.pdf.PdfPTable;

public class FinanzielleSituationPdfGeneratorSchwyzErweitert extends
	FinanzielleSituationPdfGeneratorSchwyz {

	protected static final String FOOTER_LIEGENSCHAFTSABZUG =
		"PdfGeneration_FinSit_FooterLiegenschaftsabzug";

	public FinanzielleSituationPdfGeneratorSchwyzErweitert(
		@Nonnull Gesuch gesuch,
		@Nonnull Verfuegung verfuegungFuerMassgEinkommen,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull LocalDate erstesEinreichungsdatum
	) {
		super(
			gesuch,
			verfuegungFuerMassgEinkommen,
			stammdaten,
			erstesEinreichungsdatum
		);
	}

	@Override
	protected <T extends AbstractFinanzielleSituation> List<PdfPTable> createTablesDeklarationByVeranlagung(
		@Nonnull AbstractFinanzielleSituationContainer<T> finSit,
		@Nonnull String gesuchstellerName,
		@Nonnull BigDecimal massgebendesEinkommen,
		@Nonnull BigDecimal liegenschaftsaufwand
	) {
		var einkommenTable = createFinSitTableSingleGS(
			createRow(translate(EIKOMMEN_TITLE), gesuchstellerName),
			createRow(
				translate(STEUERBARES_EINKOMMEN),
				T::getSteuerbaresEinkommen,
				finSit
			),
			createRow(
				translate(EINKAEUFE_VORSORGE),
				T::getEinkaeufeVorsorge,
				finSit
			),
			createRow(
				translate(ABZUEGE_LIEGENSCHAFTEN),
				printCHF(liegenschaftsaufwand)
			).withFooter(FOOTER_LIEGENSCHAFTSABZUG, footers)
		);
		var vermoegenTable = createFinSitTableSingleGS(
			createRow(translate(VERMOEGEN_TITLE)),
			createRow(
				translate(STEUERBARES_VERMOEGEN),
				T::getSteuerbaresVermoegen,
				finSit
			)
				.withFooter(FOOTER_STEUERBARES_VERMOEGEN, footers),
			createRow(
				translate(MASSG_EINK),
				printCHF(massgebendesEinkommen)
			)
				.bold()
		);

		return List.of(einkommenTable, vermoegenTable);
	}
}
