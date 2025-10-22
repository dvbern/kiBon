package ch.dvbern.ebegu.services.lastenausgleich;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.entities.LastenausgleichDetail;
import ch.dvbern.ebegu.entities.LastenausgleichDetailZeitabschnitt;
import ch.dvbern.ebegu.entities.LastenausgleichGrundlagen;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.lastenausgleich.AbstractLastenausgleichRechner;
import ch.dvbern.ebegu.lastenausgleich.LastenausgleichCalculationService;
import ch.dvbern.ebegu.lastenausgleich.LastenausgleichRechnerNew;
import ch.dvbern.ebegu.lastenausgleich.LastenausgleichRechnerOld;
import ch.dvbern.ebegu.lastenausgleich.LastenausgleichZeitabschnitteDTO;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@Local(LastenausgleichCalculationService.class)
public class LastenausgleichCalculationServiceBean implements
	LastenausgleichCalculationService {

	private static final Logger LOG = LoggerFactory.getLogger(
		LastenausgleichServiceBean.class.getSimpleName()
	);

	@Inject
	private LastenausgleichServiceBean lastenausgleichService;

	@Inject
	private LastenausgleichGrundlageServiceBean lastenausgleichGrundlageService;

	@Override
	public Lastenausgleich calculateLastenausgleichForGemeinde(
		String lastenausgleichId,
		Gemeinde gemeinde
	) {
		Lastenausgleich lastenausgleich = lastenausgleichService
			.findLastenausgleich(lastenausgleichId);
		List<LastenausgleichGrundlagen> lastenausgleichGrundlagenList =
			lastenausgleichGrundlageService.getAll();
		LastenausgleichGrundlagen grundlagenErhebungsjahr =
			lastenausgleichGrundlagenList.stream()
				.filter(
					lastenausgleichGrundlagen -> lastenausgleichGrundlagen
						.getJahr()
						.equals(lastenausgleich.getJahr())
				)
				.findFirst()
				.orElseThrow(
					() -> new EbeguEntityNotFoundException(
						"calculateLastenausgleichForGemeinde",
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						lastenausgleich.getJahr()
					)
				);
		List<LastenausgleichZeitabschnitteDTO> abschnitteProGemeinde =
			lastenausgleichService.findZeitabschnitteByGemeinde(gemeinde);
		calculateRegulaereAbrechnung(
			lastenausgleich,
			grundlagenErhebungsjahr,
			gemeinde,
			abschnitteProGemeinde.stream()
				.filter(
					lastenausgleichZeitabschnitteDTO -> grundlagenErhebungsjahr
						.getJahr()
						.equals(
							lastenausgleichZeitabschnitteDTO
								.getVerfuegungZeitabschnittGueltigkeit()
								.getGueltigAb()
								.getYear()
						)
				)
				.collect(
					Collectors.toList()
				)
		);
		calculateKorrekturForGemeinde(
			lastenausgleich,
			gemeinde,
			lastenausgleichGrundlagenList,
			abschnitteProGemeinde
		);
		return lastenausgleich;
	}

	private void calculateKorrekturForGemeinde(
		Lastenausgleich lastenausgleich,
		Gemeinde gemeinde,
		List<LastenausgleichGrundlagen> lastenausgleichGrundlagenList,
		List<LastenausgleichZeitabschnitteDTO> abschnitteProGemeinde
	) {
		LOG.info(
			"Berechne Lastenausgleich Korrekturen für Gemeinde {} ",
			gemeinde.getName()
		);
		Collection<LastenausgleichDetail> detailsBereitsVerrechnetKorrektur =
			lastenausgleichService.findLastenausgleichDetailForKorrekturen(
				gemeinde
			);
		// Korrekturen frueherer Jahre: Wir gehen bis 10 Jahre retour
		for (int i = 1; i < 10; i++) {
			int korrekturJahr = lastenausgleich.getJahr() - i;
			Optional<LastenausgleichGrundlagen> grundlagenKorrekturjahr =
				lastenausgleichGrundlagenList.stream()
					.filter(
						lastenausgleichGrundlagen -> lastenausgleichGrundlagen
							.getJahr()
							.equals(korrekturJahr)
					)
					.findFirst();
			grundlagenKorrekturjahr.ifPresent(
				lastenausgleichGrundlagen -> handleKorrekturJahrFuerGemeinde(
					korrekturJahr,
					gemeinde,
					lastenausgleich,
					lastenausgleichGrundlagen,
					abschnitteProGemeinde.stream()
						.filter(
							lastenausgleichZeitabschnitteDTO -> korrekturJahr
								==
								lastenausgleichZeitabschnitteDTO
									.getVerfuegungZeitabschnittGueltigkeit()
									.getGueltigAb()
									.getYear()
						)
						.collect(
							Collectors.toList()
						),
					detailsBereitsVerrechnetKorrektur.stream()
						.filter(
							lastenausgleichDetail -> lastenausgleichDetail
								.getJahr()
								.equals(korrekturJahr)
						)
						.collect(
							Collectors.toList()
						)
				)
			);
		}
	}

	@Override
	public void calculateTotals(String lastenausgleichId) {
		Lastenausgleich lastenausgleich = lastenausgleichService
			.findLastenausgleich(lastenausgleichId);

		BigDecimal totalGesamterLastenausgleich = BigDecimal.ZERO;
		for (LastenausgleichDetail lastenausgleichDetail : lastenausgleich
			.getLastenausgleichDetails()) {
			totalGesamterLastenausgleich = MathUtil.DEFAULT.addNullSafe(
				totalGesamterLastenausgleich,
				lastenausgleichDetail.getBetragLastenausgleich()
			);
			totalGesamterLastenausgleich = MathUtil.DEFAULT.addNullSafe(
				totalGesamterLastenausgleich,
				lastenausgleichDetail
					.getTotalBetragGutscheineOhneSelbstbehalt()
			);
		}
		lastenausgleich.setTotalAlleGemeinden(
			totalGesamterLastenausgleich
		);
		lastenausgleichService.saveLastenausgleich(lastenausgleich);
	}

	private void calculateRegulaereAbrechnung(
		@Nonnull Lastenausgleich lastenausgleich,
		@Nonnull LastenausgleichGrundlagen grundlagenErhebungsjahr,
		@Nonnull Gemeinde gemeinde,
		List<LastenausgleichZeitabschnitteDTO> abschnitteProGemeindeUndJahr
	) {
		LOG.info(
			"Regulare Abrechnung für Gemeinde {} gestartet.",
			gemeinde.getName()
		);
		AbstractLastenausgleichRechner lastenausgleichRechner =
			getLastenausgleichRechnerForYear(lastenausgleich.getJahr());

		LastenausgleichDetail detailErhebung =
			lastenausgleichRechner.createLastenausgleichDetail(
				gemeinde,
				lastenausgleich,
				grundlagenErhebungsjahr,
				abschnitteProGemeindeUndJahr
			);
		if (detailErhebung != null) {
			lastenausgleich.addLastenausgleichDetail(
				detailErhebung
			);
		}
	}

	private void handleKorrekturJahrFuerGemeinde(
		int korrekturJahr,
		@Nonnull Gemeinde gemeinde,
		@Nonnull Lastenausgleich lastenausgleich,
		@Nonnull LastenausgleichGrundlagen grundlagenKorrekturjahr,
		List<LastenausgleichZeitabschnitteDTO> abschnitteProGemeindeUndJahr,
		Collection<LastenausgleichDetail> detailsBereitsVerrechnetKorrekturJahr
	) {
		LOG.info(
			"Korrektur für Gemeinde {} und Jahr {} gestartet",
			gemeinde.getName(),
			korrekturJahr
		);
		//Hier laden alles fuer die Gemeinde ohne Jahr
		//dann eingeben an der Rechner den Array mit dem Jahr
		//rechner wird es filtern
		AbstractLastenausgleichRechner lastenausgleichRechner =
			getLastenausgleichRechnerForYear(korrekturJahr);

		// Wir ermitteln für die Gemeinde und das Korrekurjahr den aktuell gültigen Wert
		LastenausgleichDetail detailAktuellesTotalKorrekturjahr =
			lastenausgleichRechner.createLastenausgleichDetail(
				gemeinde,
				lastenausgleich,
				grundlagenKorrekturjahr,
				abschnitteProGemeindeUndJahr
			);

		if (detailAktuellesTotalKorrekturjahr != null
			&& CollectionUtils.isNotEmpty(
				detailsBereitsVerrechnetKorrekturJahr
			)) {
			// Dieses Detail ist jetzt aber das aktuelle Total für das Jahr. Uns interessiert aber die eventuelle
			// Differenz zu bereits ausgeglichenen Beträgen
			LastenausgleichDetail detailBisherigeWerte =
				new LastenausgleichDetail();
			for (LastenausgleichDetail detailBereitsVerrechnet : detailsBereitsVerrechnetKorrekturJahr) {
				detailBisherigeWerte.add(detailBereitsVerrechnet);
			}

			addRelevantDetailZeitabschnitteForKorrektur(
				detailBisherigeWerte,
				detailsBereitsVerrechnetKorrekturJahr
			);

			// Gibt es eine Differenz?
			if (detailBisherigeWerte.hasChanged(
				detailAktuellesTotalKorrekturjahr
			)) {
				// Es gibt eine Differenz (wobei wir nur den Betrag des Lastenausgleiches anschauen)
				// Wir rechnen das bisher verrechnete minus
				LastenausgleichDetail detailKorrektur =
					lastenausgleichRechner
						.createLastenausgleichDetailKorrektur(
							detailBisherigeWerte
						);
				detailKorrektur.setLastenausgleich(lastenausgleich);
				lastenausgleich.addLastenausgleichDetail(detailKorrektur);
				// Und erstellen einen neuen Korrektur-Eintrag mit dem aktuell berechneten Wert
				lastenausgleich.addLastenausgleichDetail(
					detailAktuellesTotalKorrekturjahr
				);
			}
		}
	}

	private void addRelevantDetailZeitabschnitteForKorrektur(
		LastenausgleichDetail detailBisherigeWerte,
		Collection<LastenausgleichDetail> detailBereitsVerrechnet
	) {
		LastenausgleichDetail relevantBereitsVerrechnetesDetail =
			getRelevantDetailForKorrekturZeitbaschnitteLinking(
				detailBereitsVerrechnet
			);

		List<String> verfuegungZeitabschnittIds =
			lastenausgleichService
				.findVerfuegungZeitabschnittIdsFuerLastenausgleichDetail(
					relevantBereitsVerrechnetesDetail
				);
		verfuegungZeitabschnittIds.forEach(id -> {
			VerfuegungZeitabschnitt verfuegungZeitabschnitt =
				new VerfuegungZeitabschnitt();
			verfuegungZeitabschnitt.setId(id);
			var lastenausgleichDetailZeitabschnittForKorreketur =
				new LastenausgleichDetailZeitabschnitt(
					verfuegungZeitabschnitt,
					detailBisherigeWerte
				);
			detailBisherigeWerte
				.getLastenausgleichDetailZeitabschnitte()
				.add(
					lastenausgleichDetailZeitabschnittForKorreketur
				);
		});
	}

	private LastenausgleichDetail getRelevantDetailForKorrekturZeitbaschnitteLinking(
		Collection<LastenausgleichDetail> detailBereitsVerrechnet
	) {
		if (detailBereitsVerrechnet.size() == 1) {
			return detailBereitsVerrechnet.iterator().next();
		}

		// Wenn mer als ein Detail vorhanden ist, ist nur der der zu letzt verrechnete Lastenausgelich relevant
		int jahrLetzterLastenausgleich = detailBereitsVerrechnet
			.stream()
			.mapToInt(d -> d.getLastenausgleich().getJahr())
			.max()
			.orElseThrow();

		return detailBereitsVerrechnet.stream()
			.filter(
				d -> jahrLetzterLastenausgleich
					== d.getLastenausgleich().getJahr()
					&&
					!d.isNegatedKorrekturBetrag()
			)
			.findFirst()
			.orElseThrow();
	}

	@Nonnull
	private AbstractLastenausgleichRechner getLastenausgleichRechnerForYear(
		int jahr
	) {
		if (jahr < Constants.FIRST_YEAR_LASTENAUSGLEICH_WITHOUT_SELBSTBEHALT) {
			return new LastenausgleichRechnerOld();
		}
		return new LastenausgleichRechnerNew();
	}
}
