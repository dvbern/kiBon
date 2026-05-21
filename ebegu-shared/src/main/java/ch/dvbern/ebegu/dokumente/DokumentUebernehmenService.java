package ch.dvbern.ebegu.dokumente;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
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
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.DokumentGrundService;
import ch.dvbern.ebegu.services.DokumentService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;

import static ch.dvbern.ebegu.dokumente.DokumentUebernehmenCalculator.adaptToNewPeriode;
import static ch.dvbern.ebegu.dokumente.DokumentUebernehmenCalculator.calculateGrundeZuUbernehmen;

@Stateless
public class DokumentUebernehmenService {

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

	@Inject
	private Authorizer authorizer;

	public List<DokumentZuUebernehmen> getDokumenteZuUebernehmen(
		Gesuch gesuch
	) {
		Set<DokumentGrund> uploadableDokumenteCurrentGesuch =
			getUploadableDokumente(
				gesuch
			);

		var optionalGesuchMitDokumenten =
			findLatestVerfuegtesGesuchOfLatestEingereichtErstgesuchOfVorperiode(
				gesuch
			);
		if (optionalGesuchMitDokumenten.isEmpty()
			|| !authorizer.isReadAuthorized(
				optionalGesuchMitDokumenten.get()
			)) {
			return List.of();
		}

		Collection<DokumentGrund> dokumentGrundeVorjahr = dokumentGrundService
			.findAllDokumentGrundByGesuch(optionalGesuchMitDokumenten.get());

		List<DokumentTyp> allowedDokumentTypsZuUebernehmen =
			getAllowedDokumentTypsZuUebernehmenFromEinstellung(gesuch);

		var grundeZuUbernehmen = calculateGrundeZuUbernehmen(
			dokumentGrundeVorjahr,
			allowedDokumentTypsZuUebernehmen,
			uploadableDokumenteCurrentGesuch,
			gesuch,
			optionalGesuchMitDokumenten.get()
		);

		return toDokumentZuUebernehmen(grundeZuUbernehmen);
	}

	/**
	 * Find the latest verfuegt gesuch/mutations of the latest eingereicht erstgesuch of the vorperiode.
	 * If multiple dossiers have verfuegt erstgesuch, the newest verfuegt mutation/gesuch of the latest eingereicht
	 * erstgesuch is returned.
	 *
	 * @param gesuch the gesuch to find the latest verfuegt gesuch/mutations of the latest eingereicht erstgesuch of the
	 * vorperiode for
	 * @return the latest verfuegt gesuch/mutations of the latest eingereicht erstgesuch of the vorperiode, if found,
	 * otherwise empty
	 */
	public Optional<Gesuch> findLatestVerfuegtesGesuchOfLatestEingereichtErstgesuchOfVorperiode(
		Gesuch gesuch
	) {
		List<Gesuch> allGesucheOfFall = gesuchService.getAllGesuchsForFall(
			gesuch.getFall().getId()
		);
		var periodeVorjahr = gesuchsperiodeService.getVorjahrGesuchsperiode(
			gesuch.getGesuchsperiode()
		).orElseThrow();

		var latestEingereichterErstantrag = allGesucheOfFall.stream()
			.filter(g -> g.getStatus().isAnyStatusOfVerfuegt())
			.filter(g -> !g.isMutation())
			.filter(
				g -> g.getGesuchsperiode().isSame(periodeVorjahr)
			)
			.max(
				Comparator
					.comparing(
						Gesuch::getEingangsdatum
					)
			);

		return latestEingereichterErstantrag.flatMap(
			value -> allGesucheOfFall.stream()
				.filter(g -> g.getDossier().isSame(value.getDossier()))
				.filter(g -> g.getGesuchsperiode().isSame(periodeVorjahr))
				.filter(g -> g.getStatus().isAnyStatusOfVerfuegt())
				.max(Comparator.comparing(Gesuch::getLaufnummer))
		);
	}

	private Set<DokumentGrund> getUploadableDokumente(Gesuch gesuch) {
		Set<DokumentGrund> dokumentGrundsNeeded = dokumentenverzeichnisEvaluator
			.calculate(gesuch, LocaleThreadLocal.get());
		dokumentenverzeichnisEvaluator.addOptionalDokumentGruende(
			dokumentGrundsNeeded
		);
		return dokumentGrundsNeeded;
	}

	private List<DokumentTyp> getAllowedDokumentTypsZuUebernehmenFromEinstellung(
		Gesuch gesuch
	) {
		var dokumentTypsZuUebernehmenEinstellung = einstellungService
			.findEinstellung(
				EinstellungKey.DOKUMENT_ZU_UEBERNEHMEN_TYPS,
				gesuch.extractGemeinde(),
				gesuch.getGesuchsperiode()
			);

		return Arrays.stream(
			dokumentTypsZuUebernehmenEinstellung.getValue().split(",")
		)
			.filter(stringValue -> !stringValue.isEmpty())
			.map(DokumentTyp::valueOf)
			.toList();
	}

	private static List<DokumentZuUebernehmen> toDokumentZuUebernehmen(
		List<DokumentGrund> grundeZuUebernehmen
	) {
		return grundeZuUebernehmen.stream()
			.flatMap(
				grund -> grund.getDokumente()
					.stream()
					.map(
						dokument -> new DokumentZuUebernehmen(
							grund,
							dokument
						)
					)
			)
			.toList();
	}

	/**
	 * Takes over the documents from the given list to the {@link Gesuch} provided.
	 * <p>
	 * First, the documents are checked to be valid candidates to be taken over.
	 * Then, for each document, a new {@link DokumentGrund} is created or retrieved if it already exists.
	 * The document is then copied and associated with the new {@link DokumentGrund}. This also makes
	 * a copy of the file in the file system.
	 * </p>
	 *
	 * @param gesuch the {@link Gesuch} to which the documents should be taken over
	 * @param dokumenteToErneuern the list of {@link DokumentZuUebernehmen} to be taken over
	 * @return the list of {@link DokumentZuUebernehmen} that were successfully taken over
	 */
	public List<DokumentZuUebernehmen> dokumenteUebernehmen(
		Gesuch gesuch,
		List<DokumentZuUebernehmen> dokumenteToErneuern
	) {
		checkDokumenteToErneuern(gesuch, dokumenteToErneuern);
		List<DokumentZuUebernehmen> erneuerteDokumente = new ArrayList<>();

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
	private void adaptDokumentTypToNewPeriode(
		DokumentZuUebernehmen erneuerung
	) {
		var typ = erneuerung.grund().getDokumentTyp();
		var adaptedTyp = adaptToNewPeriode(typ);
		erneuerung.grund().setDokumentTyp(adaptedTyp);
	}

	private DokumentGrund findOrCreateGrund(
		DokumentZuUebernehmen erneuerung,
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
		List<DokumentZuUebernehmen> dokumenteToUebernehmen
	) {
		var dokumentTypsZuUebernehmenFromDB = getDokumenteZuUebernehmen(gesuch);
		for (var dokumentToUebernehmen : dokumenteToUebernehmen) {
			var match = dokumentTypsZuUebernehmenFromDB.stream()
				.filter(dbErneuerungCandidate -> {
					var dokumentTypMatches = dbErneuerungCandidate.grund()
						.getDokumentTyp()
						.equals(dokumentToUebernehmen.grund().getDokumentTyp());
					var dokumentGrundTypMatches = dbErneuerungCandidate.grund()
						.getDokumentGrundTyp()
						.equals(
							dokumentToUebernehmen.grund().getDokumentGrundTyp()
						);
					var dokumentMatches = dbErneuerungCandidate.dokument()
						.getFilename()
						.equals(dokumentToUebernehmen.dokument().getFilename());

					return dokumentTypMatches
						&& dokumentGrundTypMatches
						&& dokumentMatches;
				})
				.findAny();

			if (match.isEmpty()) {
				throw new EbeguRuntimeException(
					"checkDokumenteToErneuern",
					"Dokument {} is not allowed for erneuerung for gesuch {}",
					dokumentToUebernehmen.dokument().getId(),
					gesuch.getId()
				);
			}
		}
	}

}
