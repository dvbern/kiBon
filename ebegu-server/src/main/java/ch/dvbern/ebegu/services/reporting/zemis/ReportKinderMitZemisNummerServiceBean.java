/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
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
 *
 */

package ch.dvbern.ebegu.services.reporting.zemis;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.einstellung.ApplicationPropertyService;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.file.FileSaverService;
import ch.dvbern.ebegu.outbox.ExportedEvent;
import ch.dvbern.ebegu.outbox.verfuegungselbstbehaltgemeinde.GemeindeSelbstbehaltEventConverter;
import ch.dvbern.ebegu.reporting.ReportKinderMitZemisNummerService;
import ch.dvbern.ebegu.reporting.lastenausgleich.KindMitZemisNummerDataRow;
import ch.dvbern.ebegu.reporting.lastenausgleich.KinderMitZemisNummerExcelConverter;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.services.KindService;
import ch.dvbern.ebegu.services.MailService;
import ch.dvbern.ebegu.services.reporting.AbstractReportServiceBean;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jboss.ejb3.annotation.TransactionTimeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.services.reporting.ReportUtil.createWorkbook;
import static ch.dvbern.ebegu.services.reporting.ReportUtil.getContentTypeForExport;
import static org.apache.commons.collections4.ListUtils.partition;

@Stateless
@Local(ReportKinderMitZemisNummerService.class)
public class ReportKinderMitZemisNummerServiceBean extends
	AbstractReportServiceBean implements
	ReportKinderMitZemisNummerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(
		ReportKinderMitZemisNummerServiceBean.class
	);
	private static final int BATCH_SIZE = 500;

	private final KinderMitZemisNummerExcelConverter kinderMitZemisNummerExcelConverter =
		new KinderMitZemisNummerExcelConverter();

	@Inject
	private FileSaverService fileSaverService;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private KindService kindService;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private MailService mailService;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private GemeindeSelbstbehaltEventConverter gemeindeSelbstbehaltEventConverter;

	@Inject
	private Event<ExportedEvent> event;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Nonnull
	@Override
	@TransactionTimeout(
		value = Constants.STATISTIK_TIMEOUT_MINUTES,
		unit = TimeUnit.MINUTES
	)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public UploadFileInfo generateZemisReport(
		@Nonnull Integer lastenausgleichJahr,
		@Nonnull Locale locale
	) throws ExcelMergeException, IOException {

		final ReportVorlage reportVorlage = ReportVorlage.VORLAGE_REPORT_ZEMIS;

		try (
			Workbook workbook = createWorkbook(reportVorlage);
		) {
			Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

			List<KindMitZemisNummerDataRow> reportData =
				getReportKinderMitZemisNummer(lastenausgleichJahr);

			ExcelMergerDTO excelMergerDTO =
				kinderMitZemisNummerExcelConverter.toExcelMergerDTO(
					reportData,
					lastenausgleichJahr
				);

			mergeData(sheet, excelMergerDTO, reportVorlage.getMergeFields());
			kinderMitZemisNummerExcelConverter.applyAutoSize(sheet);

			byte[] bytes = createWorkbook(workbook);

			return fileSaverService.save(
				bytes,
				ServerMessageUtil.translateEnumValue(
					reportVorlage.getDefaultExportFilename(),
					locale,
					Objects.requireNonNull(principalBean.getMandant())
				) + ".xlsx",
				Constants.TEMP_REPORT_FOLDERNAME,
				getContentTypeForExport()
			);
		}
	}

	@Override
	public void setFlagAndSaveZemisExcel(
		@Nonnull byte[] fileContent,
		@Nonnull Mandant mandant
	)
		throws IOException {
		try (
			InputStream is = new ByteArrayInputStream(fileContent);
			Workbook workbook = WorkbookFactory.create(is);
		) {
			Sheet sheet = workbook.getSheetAt(0);

			final int firstRelevantRow = 6;

			try {
				List<ZemisImportRow> rows = ZemisExcelParserUtils.readExcel(
					sheet,
					firstRelevantRow
				);
				// Group all the Zemis by Gesuchsperiode
				Map<Integer, List<ZemisImportRow>> rowsByStartYear =
					rows.stream()
						.collect(
							Collectors.groupingBy(
								ZemisImportRow::gesuchsperiodeStartJahr
							)
						);

				boolean publishSchnittstelleEventAktiviert =
					applicationPropertyService
						.isPublishSchnittstelleEventsAktiviert(
							mandant
						);

				for (List<ZemisImportRow> periodeRows : rowsByStartYear
					.values()) {

					int startYear = periodeRows.get(0)
						.gesuchsperiodeStartJahr();
					// load each periods only once
					Optional<Gesuchsperiode> gesuchsperiodeOpt =
						gesuchsperiodeService.getGesuchsperiodeAm(
							LocalDate.of(startYear, Month.AUGUST, 1),
							mandant
						);

					Set<Integer> fallNummern = periodeRows.stream()
						.map(ZemisImportRow::fallNummer)
						.collect(Collectors.toSet());

					for (List<Integer> fallBatch : partition(
						new ArrayList<>(fallNummern),
						BATCH_SIZE
					)) {
						processBatch(
							gesuchsperiodeOpt.orElseThrow(
								EntityNotFoundException::new
							),
							fallBatch,
							periodeRows,
							publishSchnittstelleEventAktiviert
						);
					}
				}

				sendMail(
					"ZEMIS Excel verarbeitet",
					"Die Verarbeitung des ZEMIS Excels wurde "
						+ "erfolgreich abgeschlossen"
				);

			} catch (EbeguRuntimeException e) {
				sendErrorMail(
					"Fehler bei der Verarbeitung des ZEMIS Excels",
					ServerMessageUtil.translateEnumValue(
						e.getErrorCodeEnum(),
						Constants.DEFAULT_LOCALE,
						mandant,
						e.getArgs().toArray()
					)
				);
				throw e;
			}
		}
	}

	private void processBatch(
		Gesuchsperiode gesuchsperiode,
		List<Integer> fallBatch,
		List<ZemisImportRow> periodeRows,
		boolean publishSchnittstelleEventAktiviert
	) {
		// Load all the kind from the given fallBatch for the givenPeriode at once
		Collection<KindContainer> kindContainersToUpdate =
			kindService.getKindContainerToUpdate(fallBatch, gesuchsperiode);

		// Put all the excel row in a map, grouped by a given key
		Map<ZemisImportRow.KindKey, ZemisImportRow> rowsByKey =
			periodeRows.stream()
				.collect(
					Collectors.toMap(
						ZemisImportRow::key,
						Function.identity(),
						(first, second) -> first
					)
				);

		for (KindContainer kindContainerToUpdate : kindContainersToUpdate) {
			long fallNummer = kindContainerToUpdate.getGesuch()
				.getFall()
				.getFallNummer();
			try {
				ZemisImportRow.KindKey key = new ZemisImportRow.KindKey(
					(int) fallNummer,
					kindContainerToUpdate.getKindNummer(),
					kindContainerToUpdate.getGesuch()
						.getDossier()
						.getGemeinde()
						.getName()
				);

				ZemisImportRow row = rowsByKey.get(key);

				if (row != null) {
					boolean keinSelbstbehaltFuerGemeinde =
						row.keinSelbstbehalt();

					if (needsUpdateKeinSelbsbehalt(
						kindContainerToUpdate,
						keinSelbstbehaltFuerGemeinde
					)) {
						updateKeinSelbstbehalt(
							kindContainerToUpdate,
							keinSelbstbehaltFuerGemeinde,
							publishSchnittstelleEventAktiviert
						);
					}
				}
			} catch (RuntimeException e) {
				throw new EbeguRuntimeException(
					"processBatch",
					ErrorCodeEnum.ERROR_ZEMIS_UPDATE_FALL,
					e,
					fallNummer
				);
			}
		}
	}

	private void fireGemeindeSelbstbehaltChangedEvent(
		KindContainer kindContainerToUpdate,
		boolean keinSelbstbehaltFuerGemeinde
	) {
		kindContainerToUpdate.getAllPlaetze()
			.forEach(
				platz -> event.fire(
					gemeindeSelbstbehaltEventConverter.of(
						platz,
						keinSelbstbehaltFuerGemeinde
					)
				)
			);
	}

	private boolean needsUpdateKeinSelbsbehalt(
		KindContainer kindContainer,
		boolean keinSelbstbehaltFuerGemeinde
	) {
		//Update nur machen wenn, das Flag wirklich geändert hat
		if (kindContainer.getKeinSelbstbehaltDurchGemeinde() != null
			&&
			keinSelbstbehaltFuerGemeinde
				== kindContainer.getKeinSelbstbehaltDurchGemeinde()) {
			return false;
		}

		if (overrideAllowed(kindContainer)) {
			logOverrideAllowed(kindContainer, keinSelbstbehaltFuerGemeinde);
			return true;
		}

		logOverrideNotAllowed(kindContainer);
		return false;
	}

	private void logOverrideNotAllowed(KindContainer kindContainer) {
		LOGGER.info(
			"Updating KindContainer with id {}, Fallnummer {}, and Kindnummer {}.has kindAusAsylwesen == false or has wrong Gesuchstatus. Not setting keinSelbstbehaltFuerGemeinde",
			kindContainer.getId(),
			kindContainer.getGesuch().getFall().getFallNummer(),
			kindContainer.getKindNummer()
		);
	}

	private void logOverrideAllowed(
		KindContainer kindContainer,
		boolean keinSelbstbehaltFuerGemeinde
	) {
		LOGGER.info(
			"Updating KindContainer with id {}, Fallnummer {}, and Kindnummer {}. Set keine keinSelbstbehaltFuerGemeinde = {}",
			kindContainer.getId(),
			kindContainer.getGesuch().getFall().getFallNummer(),
			kindContainer.getKindNummer(),
			keinSelbstbehaltFuerGemeinde
		);
	}

	private void updateKeinSelbstbehalt(
		KindContainer kindContainer,
		boolean keinSelbstbehaltFuerGemeinde,
		boolean publishSchnittstelleEventsAktiviert
	) {
		kindService.updateKeinSelbstbehaltFuerGemeinde(
			kindContainer,
			keinSelbstbehaltFuerGemeinde
		);
		if (!publishSchnittstelleEventsAktiviert
		) {
			return;
		}
		fireGemeindeSelbstbehaltChangedEvent(
			kindContainer,
			keinSelbstbehaltFuerGemeinde
		);
	}

	/**
	 * Nur KindContainers überschreiben die zu verfügten Gesuchen gehören und die immer noch kindAusAsylwesen == true
	 * haben.
	 *
	 * @param kindContainer
	 * @return
	 */
	private boolean overrideAllowed(@Nonnull KindContainer kindContainer) {
		boolean kindAusAyslwesen = false;
		if (kindContainer.getKindJA() != null
			&& kindContainer.getKindJA().getAusAsylwesen() != null) {
			kindAusAyslwesen = kindContainer.getKindJA().getAusAsylwesen();
		}
		boolean gesuchVerfuegt = kindContainer.getGesuch()
			.getStatus()
			.isAnyStatusOfVerfuegt();
		return kindAusAyslwesen && gesuchVerfuegt;
	}

	private void sendMail(@Nonnull String subject, @Nonnull String message) {
		mailService.toOutboxMail(
			subject,
			message,
			getCurrentBenutzerEmail(),
			getCurrentMandantIdentifier()
		);
	}

	private void sendErrorMail(
		@Nonnull String subject,
		@Nonnull String message
	) {
		mailService.toOutboxErrorMailInNewTransaction(
			subject,
			message,
			getCurrentBenutzerEmail(),
			getCurrentMandantIdentifier()
		);
	}

	@Nonnull
	private String getCurrentBenutzerEmail() {
		Benutzer benutzer = benutzerService.getCurrentBenutzer()
			.orElseThrow(
				() -> new EbeguRuntimeException(
					"sendMail",
					"No User is logged in"
				)
			);
		return benutzer.getEmail();
	}

	@Nonnull
	private MandantIdentifier getCurrentMandantIdentifier() {
		return principalBean.getMandant().getMandantIdentifier();
	}

	@Nonnull
	private List<KindMitZemisNummerDataRow> getReportKinderMitZemisNummer(
		@Nonnull Integer lastenausgleichJahr
	) {
		List<Gesuch> gesuchList = gesuchService.findGesucheForZemisList(
			lastenausgleichJahr
		);
		List<KindMitZemisNummerDataRow> dataRows = new ArrayList<>();
		gesuchList.forEach(gesuch -> {
			List<KindMitZemisNummerDataRow> kinder = gesuch.getKindContainers()
				.stream()
				// Abfrage gibt alle Gesuche mit mindesten einem Kind mit Zemis Nummer und BG-Betreuung zurück.
				// Darum müssen die Kinder nochmals gefiltert werden
				.filter(
					kindContainer -> kindContainer.getKindJA()
						.getZemisNummer()
						!= null
						&& !kindContainer.getBetreuungen().isEmpty()
				)
				.map(kindContainer -> {
					KindMitZemisNummerDataRow dataRow =
						new KindMitZemisNummerDataRow();
					dataRow.setFall(gesuch.getFall().getFallNummer());
					dataRow.setPeriode(
						gesuch.getGesuchsperiode()
							.getGesuchsperiodeString()
					);
					dataRow.setGemeinde(
						gesuch.getDossier().getGemeinde().getName()
					);
					dataRow.setName(
						kindContainer.getKindJA().getNachname()
					);
					dataRow.setVorname(
						kindContainer.getKindJA().getVorname()
					);
					dataRow.setKindNummer(kindContainer.getKindNummer());
					dataRow.setGeburtsdatum(
						kindContainer.getKindJA().getGeburtsdatum()
					);
					dataRow.setZemisNummer(
						kindContainer.getKindJA().getZemisNummer()
					);
					dataRow.setKeinSelbstbehaltFuerGemeinde(
						kindContainer.getKeinSelbstbehaltDurchGemeinde()
					);
					return dataRow;
				})
				.collect(Collectors.toList());
			dataRows.addAll(kinder);
		});
		return dataRows;
	}
}
