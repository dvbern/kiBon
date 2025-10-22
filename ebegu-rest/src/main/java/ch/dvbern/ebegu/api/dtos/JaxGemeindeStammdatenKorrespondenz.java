package ch.dvbern.ebegu.api.dtos;

import java.util.Arrays;

import javax.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import ch.dvbern.ebegu.entities.GemeindeStammdatenKorrespondenz;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@XmlRootElement(name = "gemeindeStammdatenKorrespondenz")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JaxGemeindeStammdatenKorrespondenz extends JaxAbstractDTO {

	private static final long serialVersionUID = 1413073353878066142L;

	@NotNull
	private Integer senderAddressSpacingLeft;

	@NotNull
	private Integer senderAddressSpacingTop;

	@NotNull
	private Integer receiverAddressSpacingLeft;

	@NotNull
	private Integer receiverAddressSpacingTop;

	@NotNull
	private Integer logoWidth;

	@NotNull
	private Integer logoSpacingLeft;

	@NotNull
	private Integer logoSpacingTop;

	@Nullable
	private String standardSignatur;

	@NotNull
	private boolean hasAlternativeLogoTagesschule;

	@NotNull
	private Integer barcodeSpacingLeft;

	@NotNull
	private Integer barcodeSpacingTop;

	@NotNull
	public static JaxGemeindeStammdatenKorrespondenz from(
		@NotNull GemeindeStammdatenKorrespondenz stammdaten
	) {
		return new JaxGemeindeStammdatenKorrespondenz(
			stammdaten.getSenderAddressSpacingLeft(),
			stammdaten.getSenderAddressSpacingTop(),
			stammdaten.getReceiverAddressSpacingLeft(),
			stammdaten.getReceiverAddressSpacingTop(),
			stammdaten.getLogoWidth(),
			stammdaten.getLogoSpacingLeft(),
			stammdaten.getLogoSpacingTop(),
			stammdaten.getStandardSignatur(),
			!Arrays.equals(
				stammdaten.getAlternativesLogoTagesschuleContent(),
				GemeindeStammdatenKorrespondenz.EMPTY_BYTE_ARRAY
			)
				&& stammdaten.getAlternativesLogoTagesschuleName()
					!= null
				&& stammdaten.getAlternativesLogoTagesschuleType()
					!= null,
			stammdaten.getBarcodeSpacingLeft(),
			stammdaten.getBarcodeSpacingTop()
		);
	}

	public void apply(@NotNull GemeindeStammdatenKorrespondenz entity) {
		entity.setSenderAddressSpacingLeft(senderAddressSpacingLeft);
		entity.setSenderAddressSpacingTop(senderAddressSpacingTop);
		entity.setReceiverAddressSpacingLeft(receiverAddressSpacingLeft);
		entity.setReceiverAddressSpacingTop(receiverAddressSpacingTop);
		entity.setLogoWidth(logoWidth);
		entity.setLogoSpacingLeft(logoSpacingLeft);
		entity.setLogoSpacingTop(logoSpacingTop);
		entity.setStandardSignatur(standardSignatur);
		entity.setBarcodeSpacingLeft(barcodeSpacingLeft);
		entity.setBarcodeSpacingTop(barcodeSpacingTop);
	}
}
