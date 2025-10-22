package ch.dvbern.ebegu.pdfgenerator.mahnung.erstemahnung;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Mahnung;

public class ErsteMahnungPdfGenerator extends AbstractErsteMahnungPdfGenerator {
	public ErsteMahnungPdfGenerator(
		@Nonnull Mahnung mahnung,
		@Nonnull GemeindeStammdaten stammdaten
	) {
		super(mahnung, stammdaten);
	}
}
