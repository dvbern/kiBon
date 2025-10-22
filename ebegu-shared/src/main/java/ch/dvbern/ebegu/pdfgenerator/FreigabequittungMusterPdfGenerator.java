/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.pdfgenerator;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FreigabequittungMusterPdfGenerator extends
	AbstractFreigabequittungPdfGenerator {

	private static final String BEGLEITSCHREIBEN_TITLE =
		"PdfGeneration_Freigabequittung_Muster_Title";
	private static final float PERCENT_MULTIPLICATOR = 100;
	private static final float MAX_BARCODE_WIDTH = 122;

	private static final Logger LOG = LoggerFactory.getLogger(
		FreigabequittungMusterPdfGenerator.class
	);

	public FreigabequittungMusterPdfGenerator(
		@Nonnull GemeindeStammdaten stammdaten
	) {
		// Fuer das MusterPDF wird kein Gesuch benoetigt. Da es in der Superklasse zwingend ist, geben wir
		// einfach ein leeres Gesuch mit
		super(new Gesuch(), stammdaten, Collections.emptyList());
	}

	@Nonnull
	@Override
	protected String getDocumentTitle() {
		return translate(BEGLEITSCHREIBEN_TITLE);
	}

	@Nonnull
	@Override
	protected CustomGenerator getCustomGenerator() {
		return (generator, ctx) -> {
			Document document = generator.getDocument();
			createBarcodePlaceholder(document);
		};
	}

	@Override
	protected void createParagraphBitteAusdrucken(Document document) {
		// no-op not used on Freigabequittung Muster
	}

	@Override
	protected void createParagraphBenoetigteUnterlagenInfo(Document document) {
		// no-op not used on Freigabequittung Muster
	}

	@Override
	protected void createParagraphSofortEinrichten(
		List<Element> paragraphlist
	) {
		// no-op not used on Freigabequittung Muster
	}

	public void createBarcodePlaceholder(Document document) {
		try {
			URL url = FreigabequittungMusterPdfGenerator.class
				.getResource(
					"/pdfgenerator/BarcodePlaceholder_"
						+ sprache.getLanguage()
						+ ".png"
				);
			Objects.requireNonNull(url);
			Image image = Image.getInstance(url);
			image.scalePercent(
				PERCENT_MULTIPLICATOR * MAX_BARCODE_WIDTH / image.getWidth()
			);
			setBarcodePositioning(image, document);
		} catch (IOException e) {
			LOG.error("Could not create Barcode-Placeholder");
		}
	}

	@Nonnull
	@Override
	protected List<String> getEmpfaengerAdresse() {
		String plzAndOrt = gemeindeStammdaten.getAdresse().getPlz()
			+ ' '
			+ gemeindeStammdaten.getAdresse().getOrt();
		List<String> empfaengerAdresse = new ArrayList<>();
		empfaengerAdresse.add("Thomas Muster");
		empfaengerAdresse.add("Testweg 10");
		empfaengerAdresse.add(plzAndOrt);
		return empfaengerAdresse;
	}
}
