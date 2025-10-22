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

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.util.EbeguUtil;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;

public class BegleitschreibenPdfGenerator extends DokumentAnFamilieGenerator {

	private static final String BEGLEITSCHREIBEN_TITLE =
		"PdfGeneration_Begleitschreiben_Title";
	private static final String BEGLEITSCHREIBEN_CONTENT =
		"PdfGeneration_Begleitschreiben_Content";
	private static final String BEILAGEN = "PdfGeneration_Beilagen";
	private static final String BEILAGE_VERFUEGUNG =
		"PdfGeneration_BeilageVerfuegung";
	private static final String BEILAGE_FINANZIELLESITUATION =
		"PdfGeneration_BeilageFinanzielleSituation";
	private static final String BEILAGE_ERLAEUTERUNG =
		"PdfGeneration_BeilageErlaeuterung";
	private static final String BEILAGE_NR_IN_KLAMMERN =
		"PdfGeneration_BeilageNr";

	private final GesuchsperiodeService gesuchsperiodeService;

	public BegleitschreibenPdfGenerator(
		@Nonnull Gesuch gesuch,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull GesuchsperiodeService gesuchsperiodeService
	) {
		super(gesuch, stammdaten);
		this.gesuchsperiodeService = gesuchsperiodeService;
	}

	@Nonnull
	@Override
	protected String getDocumentTitle() {
		return translate(
			BEGLEITSCHREIBEN_TITLE,
			getGesuch().getJahrFallAndGemeindenummer(),
			getGesuch().getGesuchsperiode().getGesuchsperiodeString()
		);
	}

	@Nonnull
	@Override
	protected CustomGenerator getCustomGenerator() {
		return (generator, ctx) -> {
			Document document = generator.getDocument();
			document.add(createAnrede());
			document.add(getCustomBegleitschreibenParagraph());
			document.add(createParagraphGruss());
			document.add(
				PdfUtil.createParagraph(
					translate(
						DokumentAnFamilieGenerator.SACHBEARBEITUNG
					),
					2
				)
			);
			document.add(PdfUtil.createParagraph(translate(BEILAGEN), 0));
			document.add(PdfUtil.createListInParagraph(getBeilagen()));
		};
	}

	protected Paragraph getCustomBegleitschreibenParagraph() {
		return PdfUtil.createParagraph(translate(BEGLEITSCHREIBEN_CONTENT, 2));
	}

	@Nonnull
	private List<String> getBeilagen() {
		// Verfügungen
		List<String> beilagen = getGesuch().extractAllBetreuungen()
			.stream()
			.filter(this::isOrCanBeVerfuegt)
			.map(this::getBeilagenText)
			.collect(Collectors.toList());
		// Finanzielle Situation
		boolean erlaeuterungZurVerfuegungAvailable =
			isErlaeuterungZurVerfuegungAvailable();
		if (EbeguUtil.isFinanzielleSituationRequired(gesuch)
			&& getGesuch().getFinSitStatus() == FinSitStatus.AKZEPTIERT) {
			String title = translate(BEILAGE_FINANZIELLESITUATION);
			if (erlaeuterungZurVerfuegungAvailable) {
				title += ' ' + translate(BEILAGE_NR_IN_KLAMMERN, 1);
			}
			beilagen.add(title);
		}
		// Erläuterung zur Verfügung: Nur anhängen, wenn mindestens eine regulare Verfügung vorhanden
		if (erlaeuterungZurVerfuegungAvailable
			&& EbeguUtil.isErlaeuterungenZurVerfuegungRequired(getGesuch())) {
			String title = translate(BEILAGE_ERLAEUTERUNG)
				+ ' '
				+ translate(BEILAGE_NR_IN_KLAMMERN, 2);
			beilagen.add(title);
		}
		return beilagen;
	}

	private boolean isErlaeuterungZurVerfuegungAvailable() {

		String gesuchsperiodeId = getGesuch().getGesuchsperiode().getId();
		Sprache docLang = Sprache.fromLocale(super.sprache);

		return gesuchsperiodeService.existDokument(
			gesuchsperiodeId,
			docLang,
			DokumentTyp.ERLAUTERUNG_ZUR_VERFUEGUNG
		);
	}

	/**
	 * Will return true when the given Betreuung is verfügbar, that means when it is kleinkind and hasn't been
	 * marked yet as GESCHLOSSEN_OHNE_VERFUEGUNG
	 */
	private boolean isOrCanBeVerfuegt(@Nonnull Betreuung betreuung) {
		return betreuung.getBetreuungsangebotTyp()
			.isBetreuungsgutscheinAngebot()
			&& betreuung.getBetreuungsstatus()
				!= Betreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG;
	}

	@Nonnull
	private String getBeilagenText(@Nonnull Betreuung betreuung) {
		return translate(
			BEILAGE_VERFUEGUNG,
			betreuung.getKind().getKindJA().getNachname()
				+ ' '
				+ betreuung.getKind().getKindJA().getVorname(),
			betreuung.getReferenzNummer()
		);
	}
}
