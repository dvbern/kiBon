/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.lib.invoicegenerator.pdf.PdfElementGenerator;
import ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities;
import com.google.common.collect.Lists;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Utilities;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.krysalis.barcode4j.impl.datamatrix.DataMatrixBean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.pdfgenerator.PdfUtil.DEFAULT_FONT_SIZE;

public abstract class AbstractFreigabequittungPdfGenerator extends
	DokumentAnGemeindeGenerator {

	private static final String FREIGABEQUITTUNG_TITLE =
		"PdfGeneration_Freigabequittung_Title";
	private static final String GESUCHSTELLER = "PdfGeneration_Gesuchsteller";
	private static final String BETREUUNGSANGEBOTE =
		"PdfGeneration_Betreuungsangebote";
	private static final String BETREUUNG_KIND = "PdfGeneration_Kind";
	private static final String BENOETIGTE_UNTERLAGEN =
		"PdfGeneration_BenoetigteUnterlagen";
	private static final String EINWILLIGUNG_STEUERDATEN_TITLE =
		"PdfGeneration_EinwilligungSteuerdaten_Title";
	private static final String EINWILLIGUNG_STEUERDATEN_CONTENT =
		"PdfGeneration_EinwilligungSteuerdaten_Content";
	private static final String VOLLSTAENDIGKEIT_TITLE =
		"PdfGeneration_Vollstaendigkeit_Title";
	private static final String VOLLSTAENDIGKEIT_CONTENT =
		"PdfGeneration_Vollstaendigkeit_Content";
	private static final String EINGEREICHT = "PdfGeneration_Eingereicht";
	private static final String UNTERSCHRIFTEN_ORT_DATUM =
		"PdfGeneration_UnterschriftenOrtDatum";

	private static final Logger LOG = LoggerFactory.getLogger(
		AbstractFreigabequittungPdfGenerator.class
	);

	private static final float SPACING_ONE_LINE = DEFAULT_FONT_SIZE
		* PdfUtilities.DEFAULT_MULTIPLIED_LEADING;

	@Nonnull
	private final List<DokumentGrund> benoetigteUnterlagen;

	private final float barcodeSpacingTop;
	private final float barcodeSpacingLeft;

	protected AbstractFreigabequittungPdfGenerator(
		@Nonnull Gesuch gesuch,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull List<DokumentGrund> benoetigteUnterlagen
	) {
		super(gesuch, stammdaten);
		this.benoetigteUnterlagen = benoetigteUnterlagen;

		this.barcodeSpacingTop = Utilities.millimetersToPoints(
			stammdaten.getGemeindeStammdatenKorrespondenz()
				.getBarcodeSpacingTop()
		);
		this.barcodeSpacingLeft = Utilities.millimetersToPoints(
			stammdaten.getGemeindeStammdatenKorrespondenz()
				.getBarcodeSpacingLeft()
		);
	}

	@Override
	@Nonnull
	protected String getDocumentTitle() {
		return translate(
			FREIGABEQUITTUNG_TITLE,
			getGesuch().getGesuchsperiode().getGesuchsperiodeString()
		);
	}

	@Override
	@Nonnull
	protected CustomGenerator getCustomGenerator() {
		return (generator, ctx) -> {
			Document document = generator.getDocument();
			createBarcode(document);
			createParagraphBitteAusdrucken(document);
			createGesuchstellerTable(document);
			createBetreuungsangeboteTable(document);
			createDocumentList(document);
			createZusatzTextIfAvailable(document);
			createSeite2(document);
		};
	}

	private void createDocumentList(Document document) {
		final List<String> dokumente = KibonPrintUtil
			.getBenoetigteDokumenteAsList(
				benoetigteUnterlagen,
				gesuch,
				sprache
			);

		if (dokumente.isEmpty()) {
			return;
		}

		document.add(PdfUtil.createSubTitle(translate(BENOETIGTE_UNTERLAGEN)));
		createParagraphBenoetigteUnterlagenInfo(document);

		Paragraph dokumenteParagraph = new Paragraph();
		dokumenteParagraph.setSpacingAfter(SPACING_ONE_LINE);
		dokumenteParagraph.add(PdfUtil.createListInParagraph(dokumente));

		document.add(dokumenteParagraph);
	}

	protected void createSeite2(@Nonnull Document document) {
		List<Element> seite2Paragraphs = Lists.newArrayList();
		seite2Paragraphs.add(
			PdfUtil.createSubTitle(
				translate(EINWILLIGUNG_STEUERDATEN_TITLE)
			)
		);
		seite2Paragraphs.add(
			PdfUtil.createParagraph(
				translate(
					EINWILLIGUNG_STEUERDATEN_CONTENT,
					gesuch.getDossier().getGemeinde().getName()
				)
			)
		);
		createVollstaendigkeitUndSignatur(seite2Paragraphs);
		document.add(PdfUtil.createKeepTogetherTable(seite2Paragraphs, 1, 0));
	}

	protected void createVollstaendigkeitUndSignatur(
		List<Element> seite2Paragraphs
	) {
		seite2Paragraphs.add(new Paragraph());
		seite2Paragraphs.add(
			PdfUtil.createSubTitle(translate(VOLLSTAENDIGKEIT_TITLE))
		);
		seite2Paragraphs.add(
			PdfUtil.createParagraph(translate(VOLLSTAENDIGKEIT_CONTENT))
		);
		createParagraphSofortEinrichten(seite2Paragraphs);
		seite2Paragraphs.add(createUnterschriftenTable());
		seite2Paragraphs.add(PdfUtil.createParagraph(translate(EINGEREICHT)));
	}

	protected void createZusatzTextIfAvailable(Document document) {
		if (getGemeindeStammdaten().getHasZusatzTextFreigabequittung()) {
			document.add(
				PdfUtil.createParagraph(
					Objects.requireNonNull(
						gemeindeStammdaten
							.getZusatzTextFreigabequittung()
					)
				)
			);
		}
	}

	public void createGesuchstellerTable(Document document) {
		PdfPTable table = new PdfPTable(3);
		// Init
		PdfUtil.setTableDefaultStyles(table);
		table.getDefaultCell().setPaddingBottom(SPACING_ONE_LINE);
		// Row: Referenznummer
		final Font defaultFont = getPageConfiguration().getFonts().getFont();
		table.addCell(new Phrase(translate(REFERENZ_NUMMER), defaultFont));
		table.addCell(
			new Phrase(
				getGesuch().getJahrFallAndGemeindenummer(),
				defaultFont
			)
		);
		table.addCell(new Phrase());
		// Row: Gesuchersteller-Adressen
		table.addCell(new Phrase(translate(GESUCHSTELLER), defaultFont));
		String gs1 = KibonPrintUtil.getGesuchstellerWithAddressAsString(
			getGesuch().getGesuchsteller1()
		);
		String gs2 = KibonPrintUtil.getGesuchstellerWithAddressAsString(
			getGesuch().getGesuchsteller2()
		);
		table.addCell(new Phrase(gs1, defaultFont));
		table.addCell(new Phrase(gs2, defaultFont));

		document.add(table);
	}

	private void createBarcode(Document document) {
		try {
			ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
			BitmapCanvasProvider canvas = new BitmapCanvasProvider(
				bytesOut,
				"image/x-png",
				175,
				BufferedImage.TYPE_BYTE_BINARY,
				false,
				0
			);
			new DataMatrixBean().generateBarcode(
				canvas,
				"§FREIGABE|OPEN|"
					+ getGesuch().getId()
					+ '|'
					+ getGesuch().getAnzahlGesuchZurueckgezogen()
					+ '§'
			);
			canvas.finish();
			setBarcodePositioning(
				Image.getInstance(bytesOut.toByteArray()),
				document
			);
		} catch (IOException | DocumentException e) {
			LOG.error("Failed to read the Barcode: {}", e.getMessage(), e);
		}
	}

	protected void setBarcodePositioning(Image image, Document document) {
		// absolute position is lower left corner of the image
		// zero point is lower left corner of the page
		// barcodeSpacingTop is the spacing between the upper left corner of the barcode and the upper left corner of the page
		// so we need to calculate the effectivePosition
		float effectiveBarcodePosition = document.getPageSize().getHeight()
			- image.getScaledHeight()
			- barcodeSpacingTop;
		image.setAbsolutePosition(barcodeSpacingLeft, effectiveBarcodePosition);
		document.add(image);
	}

	public void createBetreuungsangeboteTable(Document document)
		throws DocumentException {
		PdfPTable table = new PdfPTable(3);
		table.setWidthPercentage(PdfElementGenerator.FULL_WIDTH);
		table.setWidths(new int[] { 30, 50, 20 });
		table.setHeaderRows(1);
		table.setKeepTogether(true);
		table.addCell(PdfUtil.createTitleCell(translate(BETREUUNG_KIND)));
		table.addCell(
			PdfUtil.createTitleCell(translate(BETREUUNG_INSTITUTION))
		);
		table.addCell(PdfUtil.createTitleCell(translate(REFERENZ_NUMMER)));

		getGesuch().extractAllPlaetze().forEach(platz -> {
			final Font defaultFont = getPageConfiguration().getFonts()
				.getFont();
			table.addCell(
				new Phrase(
					platz.getKind().getKindJA().getFullName(),
					defaultFont
				)
			);
			table.addCell(
				new Phrase(
					platz.getInstitutionAndBetreuungsangebottyp(
						sprache
					),
					defaultFont
				)
			);
			table.addCell(new Phrase(platz.getReferenzNummer(), defaultFont));
		});
		table.setSpacingAfter(SPACING_ONE_LINE);

		document.add(PdfUtil.createSubTitle(translate(BETREUUNGSANGEBOTE)));
		document.add(table);
	}

	@Nonnull
	public PdfPTable createUnterschriftenTable() {
		PdfPTable table = new PdfPTable(2);
		PdfUtil.setTableDefaultStyles(table);
		table.getDefaultCell().setPaddingTop(3 * SPACING_ONE_LINE);
		GesuchstellerContainer gesuchsteller1 = getGesuch().getGesuchsteller1();
		GesuchstellerContainer gesuchsteller2 = getGesuch().getGesuchsteller2();
		if (gesuchsteller1 != null) {
			addGesuchstellerToUnterschriften(table, gesuchsteller1);
		}
		if (gesuchsteller2 != null) {
			addGesuchstellerToUnterschriften(table, gesuchsteller2);
		}
		table.setSpacingAfter(2 * SPACING_ONE_LINE);
		return table;
	}

	protected abstract void createParagraphBitteAusdrucken(Document document);

	protected abstract void createParagraphBenoetigteUnterlagenInfo(
		Document document
	);

	protected abstract void createParagraphSofortEinrichten(
		List<Element> paragraphlist
	);

	private void addGesuchstellerToUnterschriften(
		@Nonnull PdfPTable table,
		@Nonnull GesuchstellerContainer gesuchsteller
	) {
		final Font defaultFont = getPageConfiguration().getFonts().getFont();
		table.addCell(
			new Phrase(translate(UNTERSCHRIFTEN_ORT_DATUM), defaultFont)
		);
		table.addCell(new Phrase(gesuchsteller.extractFullName(), defaultFont));

		table.addCell(new Phrase());
		table.addCell(new Phrase());

		table.addCell(createCellWithBottomLine());
		table.addCell(createCellWithBottomLine());
	}

	private PdfPCell createCellWithBottomLine() {
		LineSeparator dottedline = new LineSeparator();
		dottedline.setOffset(-10);
		PdfPCell pdfPCell = new PdfPCell();
		pdfPCell.setBorderWidth(0);
		pdfPCell.addElement(dottedline);
		return pdfPCell;
	}
}
