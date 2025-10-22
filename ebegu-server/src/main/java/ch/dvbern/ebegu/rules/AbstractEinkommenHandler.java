package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import lombok.Getter;

@Getter
public abstract class AbstractEinkommenHandler {

	private final Locale locale;
	private final BigDecimal maxEinkommenEKV;
	private final BigDecimal maxEinkommen;

	protected AbstractEinkommenHandler(
		Locale locale,
		@Nullable BigDecimal maxEinkommenEKV,
		BigDecimal maxEinkommen
	) {
		this.locale = locale;
		this.maxEinkommenEKV = maxEinkommenEKV;
		this.maxEinkommen = maxEinkommen;
	}

	protected abstract void handleEinkommen(
		@Nonnull AbstractPlatz platz,
		@Nonnull BGCalculationInput inputData
	);

	protected void handleMaximalesEinkommenUeberschritten(
		@Nonnull BGCalculationInput inputData
	) {
		inputData.setKategorieMaxEinkommen(true);
		inputData.setKeinAnspruchAufgrundEinkommen(true);
	}
}
