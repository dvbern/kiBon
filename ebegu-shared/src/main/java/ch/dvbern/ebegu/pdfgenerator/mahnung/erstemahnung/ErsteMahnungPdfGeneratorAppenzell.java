package ch.dvbern.ebegu.pdfgenerator.mahnung.erstemahnung;

import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mahnung;

public class ErsteMahnungPdfGeneratorAppenzell extends
	AbstractErsteMahnungPdfGenerator {
	public ErsteMahnungPdfGeneratorAppenzell(
		@Nonnull Mahnung mahnung,
		@Nonnull GemeindeStammdaten stammdaten
	) {
		super(mahnung, stammdaten);
	}

	@Override
	protected String getBetreuungsString(
		KindContainer kindContainer,
		List<String> betreuungenList
	) {
		return kindContainer.getKindJA().getFullName();
	}
}
