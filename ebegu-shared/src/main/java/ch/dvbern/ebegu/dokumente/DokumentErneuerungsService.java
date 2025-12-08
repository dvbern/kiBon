package ch.dvbern.ebegu.dokumente;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.dokumente.anlageverzeichnis.DokumentenverzeichnisEvaluator;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.einstellung.EinstellungService;
import ch.dvbern.ebegu.entities.Dokument;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.file.FileSaverService;
import ch.dvbern.ebegu.i18n.LocaleThreadLocal;
import ch.dvbern.ebegu.services.DokumentGrundService;
import ch.dvbern.ebegu.services.DokumentService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;

import static ch.dvbern.ebegu.dokumente.DokumentErneuerungCalculator.adaptToNewPeriode;
import static ch.dvbern.ebegu.dokumente.DokumentErneuerungCalculator.calculateErneuerbareGrunde;

@Stateless
public class DokumentErneuerungsService {

	@Inject
	private DokumentenverzeichnisEvaluator dokumentenverzeichnisEvaluator;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private EinstellungService einstellungService;

	@Inject
	private DokumentGrundService dokumentGrundService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private FileSaverService fileSaverService;

	@Inject
	private DokumentService dokumentService;

	public List<DokumentErneuerung> getErneuerbareDokumente(Gesuch gesuch) {
		Set<DokumentGrund> uploadableDokumenteCurrentGesuch =
			getUploadableDokumente(
				gesuch
			);

		var vorjahrGesuch = getVorjahrGesuch(
			gesuch
		);

		if (vorjahrGesuch.isEmpty()) {
			return List.of();
		}

		Collection<DokumentGrund> dokumentGrundeVorjahr = dokumentGrundService
			.findAllDokumentGrundByGesuch(vorjahrGesuch.get());

		List<DokumentTyp> allowedDokumentTypsForErneuerung =
			getAllowedDokumentTypsForErneuerungFromEinstellung(gesuch);

		var erneuerbareGrunde = calculateErneuerbareGrunde(
			dokumentGrundeVorjahr,
			allowedDokumentTypsForErneuerung,
			uploadableDokumenteCurrentGesuch,
			gesuch,
			vorjahrGesuch.get()
		);

		return toDokumentErneuerungen(erneuerbareGrunde);
	}

	private Set<DokumentGrund> getUploadableDokumente(Gesuch gesuch) {
		Set<DokumentGrund> dokumentGrundsNeeded = dokumentenverzeichnisEvaluator
			.calculate(gesuch, LocaleThreadLocal.get());
		dokumentenverzeichnisEvaluator.addOptionalDokumentGruende(
			dokumentGrundsNeeded
		);
		return dokumentGrundsNeeded;
	}

	private Optional<Gesuch> getVorjahrGesuch(Gesuch gesuch) {
		var vorjahrPeriode = gesuchsperiodeService.getVorjahrGesuchsperiode(
			gesuch.getGesuchsperiode()
		);
		return vorjahrPeriode.flatMap(
			gesuchsperiode -> gesuchService
				.getNeustesVerfuegtesGesuchFuerGesuch(
					gesuchsperiode,
					gesuch.getDossier(),
					true
				)
		);
	}

	private List<DokumentTyp> getAllowedDokumentTypsForErneuerungFromEinstellung(
		Gesuch gesuch
	) {
		var erneuerbareDokumentTypsEinstellung = einstellungService
			.findEinstellung(
				EinstellungKey.ERNEUERBARE_DOKUMENT_TYPS,
				gesuch.extractGemeinde(),
				gesuch.getGesuchsperiode()
			);

		return Arrays.stream(
			erneuerbareDokumentTypsEinstellung.getValue().split(",")
		)
			.filter(stringValue -> !stringValue.isEmpty())
			.map(DokumentTyp::valueOf)
			.toList();
	}

	private static List<DokumentErneuerung> toDokumentErneuerungen(
		List<DokumentGrund> erneuerbareGrunde
	) {
		return erneuerbareGrunde.stream()
			.flatMap(
				grund -> grund.getDokumente()
					.stream()
					.map(
						dokument -> new DokumentErneuerung(
							grund,
							dokument
						)
					)
			)
			.toList();
	}

	public List<DokumentErneuerung> dokumenteErneuern(
		Gesuch gesuch,
		List<DokumentErneuerung> dokumenteToErneuern
	) {
		checkDokumenteToErneuern(gesuch, dokumenteToErneuern);
		List<DokumentErneuerung> erneuerteDokumente = new ArrayList<>();

		for (var erneuerung : dokumenteToErneuern) {
			var grund = findOrCreateGrund(erneuerung, gesuch);
			var dbDokument = dokumentService.findDokument(
				erneuerung.dokument().getId()
			);
			if (dbDokument.isEmpty()) {
				throw new EbeguEntityNotFoundException(
					"dokumenteErneuern",
					"Could not find Dokument "
						+ erneuerung.dokument().getId()
						+ " to be erneuert "
				);
			}
			var erneuerteEntity = dbDokument.get()
				.copyDokument(new Dokument(), AntragCopyType.MUTATION, grund);
			grund.getDokumente().add(erneuerteEntity);
			erneuerteDokumente.add(erneuerung);
			fileSaverService.copy(erneuerteEntity, gesuch.getId());

			grund.getDokumente().add(erneuerteEntity);
			erneuerteDokumente.add(erneuerung);
			dokumentGrundService.saveDokumentGrund(grund);
		}

		return erneuerteDokumente;
	}

	// XYYearMinus1 in previous periode is now XYYearMinus2 etc.
	private void adaptDokumentTypToNewPeriode(DokumentErneuerung erneuerung) {
		var typ = erneuerung.grund().getDokumentTyp();
		var adaptedTyp = adaptToNewPeriode(typ);
		erneuerung.grund().setDokumentTyp(adaptedTyp);
	}

	private DokumentGrund findOrCreateGrund(
		DokumentErneuerung erneuerung,
		Gesuch gesuch
	) {
		adaptDokumentTypToNewPeriode(erneuerung);
		DokumentGrund previousGround = erneuerung.grund();
		var existingGrunde = dokumentGrundService
			.findAllDokumentGrundByGesuchDokumentTypeDokumentGrundTypeAndTag(
				gesuch,
				previousGround.getDokumentGrundTyp(),
				previousGround.getDokumentTyp(),
				previousGround.getTag()
			)
			.stream()
			.findFirst();
		if (existingGrunde.isEmpty()) {
			DokumentGrund grund = new DokumentGrund(
				previousGround.getDokumentGrundTyp(),
				previousGround.getTag(),
				previousGround.getPersonType(),
				previousGround.getPersonNumber(),
				previousGround.getDokumentTyp()
			);
			grund.setGesuch(gesuch);
			return dokumentGrundService.saveDokumentGrund(grund);
		}
		return existingGrunde.get();
	}

	private void checkDokumenteToErneuern(
		Gesuch gesuch,
		List<DokumentErneuerung> dokumenteToErneuern
	) {
		var erneuerbareDokumentTypsFromDB = getErneuerbareDokumente(gesuch);
		for (var dokumentToErneuern : dokumenteToErneuern) {
			var match = erneuerbareDokumentTypsFromDB.stream()
				.filter(dbErneuerungCandidate -> {
					var dokumentTypMatches = dbErneuerungCandidate.grund()
						.getDokumentTyp()
						.equals(dokumentToErneuern.grund().getDokumentTyp());
					var dokumentGrundTypMatches = dbErneuerungCandidate.grund()
						.getDokumentGrundTyp()
						.equals(
							dokumentToErneuern.grund().getDokumentGrundTyp()
						);
					var dokumentMatches = dbErneuerungCandidate.dokument()
						.getFilename()
						.equals(dokumentToErneuern.dokument().getFilename());

					return dokumentTypMatches
						&& dokumentGrundTypMatches
						&& dokumentMatches;
				})
				.findAny();

			if (match.isEmpty()) {
				throw new EbeguRuntimeException(
					"checkDokumenteToErneuern",
					"Dokument {} is not allowed for erneuerung for gesuch {}",
					dokumentToErneuern.dokument().getId(),
					gesuch.getId()
				);
			}
		}
	}

}
