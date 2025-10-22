package ch.dvbern.ebegu.dto.statistik;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KinderStatistikParameterDto extends BaseDto {
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate auswertungVon;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate auswertungBis;
	private String gesuchsperiodeId;
	private String benutzerId;
}
