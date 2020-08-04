/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.pdfgenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.lib.invoicegenerator.dto.PageConfiguration;
import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;
import ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities;
import com.lowagie.text.Chunk;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import org.apache.commons.io.IOUtils;

import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_MULTIPLIED_LEADING;
import static com.lowagie.text.Utilities.millimetersToPoints;

public abstract class MandantPdfGenerator {

	protected static final String ABSENDER_TELEFON = "PdfGeneration_Telefon";
	protected static final String EINSCHREIBEN = "PdfGeneration_VerfuegungEingeschrieben"; //wird bei der Definitiv
	// verwendet werden

	@Nonnull
	private PdfGenerator pdfGenerator;

	public MandantPdfGenerator() {
		byte[] mandantLogo = new byte[0];
		try {
			mandantLogo = IOUtils.toByteArray(MandantPdfGenerator.class.getResourceAsStream(
				"KantonBernLogo"
					+ ".png"));
		}
		catch (IOException e) {
			//TODO Throw Exception Logo not Found
		}
		initGenerator(mandantLogo);
	}

	@Nonnull
	public PageConfiguration getPageConfiguration() {
		return pdfGenerator.getConfiguration();
	}

	@Nonnull
	protected PdfGenerator getPdfGenerator() {
		return pdfGenerator;
	}

	@Nonnull
	protected abstract String getDocumentTitle();

	@Nonnull
	protected abstract List<String> getEmpfaengerAdresse();

	@Nonnull
	protected abstract CustomGenerator getCustomGenerator();

	public void generate(@Nonnull final OutputStream outputStream) throws InvoiceGeneratorException {
		getPdfGenerator().generate(outputStream, getDocumentTitle(), getEmpfaengerAdresse(), getCustomGenerator());
	}

	private void initGenerator(@Nonnull final byte[] mandantLogo) {
		this.pdfGenerator = PdfGenerator.create(mandantLogo, getAbsenderAdresse(), true);
	}

	@Nonnull
	protected final List<String> getAbsenderAdresse() {
		List<String> absender = new ArrayList<>();
		absender.add(getMandantAddressAsString());
		absender.addAll(getMandantKontaktdaten());
		return absender;
	}

	@Nonnull
	public String getMandantAddressAsString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Gesundheits-, Sozial- und Integrationsdirektion");
		sb.append(Constants.LINE_BREAK);
		sb.append("Amt für Integration und Soziales");
		sb.append(Constants.LINE_BREAK);
		sb.append("Abteilung Familie");
		sb.append(Constants.LINE_BREAK);
		sb.append(Constants.LINE_BREAK);
		sb.append("Rathausgasse 1");
		sb.append(Constants.LINE_BREAK);
		sb.append("Postfach");
		sb.append(Constants.LINE_BREAK);
		sb.append("3000 Bern 8");
		sb.append(Constants.LINE_BREAK);
		return sb.toString();
	}

	@Nonnull
	private List<String> getMandantKontaktdaten() {
		String email = "info.fam.ais.gsi@be.ch";
		String telefon = "+41 31 633 78 83";
		String webseite = "www.be.ch/gsi";
		return Arrays.asList(
			translate(ABSENDER_TELEFON, telefon),
			PdfUtil.printString(email),
			PdfUtil.printString(webseite),
			"",
			""
		);
	}

	@Nonnull
	protected String translate(String key, Object... args) {
		return ServerMessageUtil.getMessage(key, Sprache.DEUTSCH.getLocale(), args);
	}

	protected void createFusszeile(@Nonnull PdfContentByte dirPdfContentByte, List<String> content) throws DocumentException {
		ColumnText fz = new ColumnText(dirPdfContentByte);
		final float height = millimetersToPoints(30);
		final float width = millimetersToPoints(170);
		final float loverLeftX = millimetersToPoints(PageConfiguration.LEFT_PAGE_DEFAULT_MARGIN_MM);
		final float loverLeftY = millimetersToPoints(PdfLayoutConfiguration.LOGO_TOP_IN_MM / 4.0f);
		fz.setSimpleColumn(loverLeftX, loverLeftY, loverLeftX + width, loverLeftY + height);
		fz.setLeading(0, DEFAULT_MULTIPLIED_LEADING);
		Font fontWithSize = PdfUtilities.createFontWithSize(getPageConfiguration().getFont(), 6.5f);
		for (int i = 0; i < content.size(); i++) {
			Chunk chunk = new Chunk((i + 1) + " ", PdfUtilities.createFontWithSize(getPageConfiguration().getFont(),
				5));
			chunk.setTextRise(2);
			fz.addText(chunk);
			fz.addText(new Phrase(content.get(i) + '\n', fontWithSize));
		}
		fz.go();
	}

	/**
	 * Wenn man etwas ganz genau platzieren muss...
	 * @param dirPdfContentByte
	 * @param content
	 * @param y
	 * @param x
	 * @param font
	 * @param size
	 * @throws DocumentException
	 */
	protected void createContentWhereIWant(@Nonnull PdfContentByte dirPdfContentByte, String content, float y,
		float x, Font font, float size) throws DocumentException {
		ColumnText fz = new ColumnText(dirPdfContentByte);
		final float height = millimetersToPoints(20);
		final float width = millimetersToPoints(75);
		final float loverLeftX = millimetersToPoints(x);
		final float loverLeftY = y;
		fz.setSimpleColumn(loverLeftX, loverLeftY, loverLeftX + width, loverLeftY + height);
		fz.setLeading(0, DEFAULT_MULTIPLIED_LEADING);
		Font fontWithSize = PdfUtilities.createFontWithSize(font, size);
		fz.addText(new Phrase(content + '\n', fontWithSize));
		fz.go();
	}
}
