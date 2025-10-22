package ch.dvbern.ebegu.pdfgenerator.mahnung.zweitemahnung;

import jakarta.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Mahnung;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.util.mandant.AbstractMandantDefaultVisitor;

public class ZweiteMahnungPdfGeneratorDefaultVisitor extends
	AbstractMandantDefaultVisitor<AbstractZweiteMahnungPdfGenerator> {

	private final GemeindeStammdaten stammdaten;
	private final Mahnung mahnung;
	private final Mahnung ersteMahnung;

	public ZweiteMahnungPdfGeneratorDefaultVisitor(
		Mahnung mahnung,
		Mahnung ersteMahnung,
		GemeindeStammdaten stammdaten
	) {
		this.stammdaten = stammdaten;
		this.mahnung = mahnung;
		this.ersteMahnung = ersteMahnung;
	}

	public AbstractZweiteMahnungPdfGenerator getZweiteMahnungPdfGeneratorForMandant(
		@NotNull Mandant mandant
	) {
		return mandant.getMandantIdentifier().accept(this);
	}

	@Override
	protected AbstractZweiteMahnungPdfGenerator visitDefault() {
		return new ZweiteMahnungPdfGenerator(mahnung, ersteMahnung, stammdaten);
	}

	@Override
	public AbstractZweiteMahnungPdfGenerator visitSchwyz() {
		return new ZweiteMahnungPdfGeneratorSchwyz(
			mahnung,
			ersteMahnung,
			stammdaten
		);
	}
}
