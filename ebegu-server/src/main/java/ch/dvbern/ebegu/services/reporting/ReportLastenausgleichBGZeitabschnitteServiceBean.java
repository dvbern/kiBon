/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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
 */

package ch.dvbern.ebegu.services.reporting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.BGCalculationResult_;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuung_;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.entities.Institution_;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.KindContainer_;
import ch.dvbern.ebegu.entities.Kind_;
import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.entities.LastenausgleichDetail;
import ch.dvbern.ebegu.entities.LastenausgleichDetailZeitabschnitt;
import ch.dvbern.ebegu.entities.LastenausgleichDetailZeitabschnitt_;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt_;
import ch.dvbern.ebegu.enums.ReportFileName;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.reporting.ReportLastenausgleichBGZeitabschnitteService;
import ch.dvbern.ebegu.reporting.lastenausgleich.LastenausgleichBGZeitabschnittDataRow;
import ch.dvbern.ebegu.reporting.lastenausgleich.LastenausgleichBGZeitabschnitteCSVConverter;
import ch.dvbern.ebegu.reporting.lastenausgleich.LastenausgleichBGZeitabschnitteExcelConverter;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.FileSaverService;
import ch.dvbern.ebegu.services.lastenausgleich.LastenausgleichServiceBean;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.DateUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import ch.dvbern.oss.lib.excelmerger.RowFiller;
import ch.dvbern.oss.lib.excelmerger.mergefields.MergeFieldProvider;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.jboss.ejb3.annotation.TransactionTimeout;

import static ch.dvbern.ebegu.services.reporting.ReportUtil.createWorkbook;
import static ch.dvbern.ebegu.services.reporting.ReportUtil.getContentTypeForCSVExport;
import static ch.dvbern.ebegu.services.reporting.ReportUtil.getContentTypeForExport;
import static java.util.Objects.requireNonNull;

@Stateless
@Local(ReportLastenausgleichBGZeitabschnitteService.class)
public class ReportLastenausgleichBGZeitabschnitteServiceBean extends
	AbstractReportServiceBean
	implements
	ReportLastenausgleichBGZeitabschnitteService {

	private LastenausgleichBGZeitabschnitteExcelConverter lastenausgleichBGZeitabschnitteExcelConverter =
		new LastenausgleichBGZeitabschnitteExcelConverter();

	private LastenausgleichBGZeitabschnitteCSVConverter lastenausgleichBGZeitabschnitteCSVConverter =
		new LastenausgleichBGZeitabschnitteCSVConverter();

	@Inject
	private LastenausgleichServiceBean lastenausgleichService;

	@Inject
	private FileSaverService fileSaverService;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private Authorizer authorizer;

	@PersistenceContext(unitName = "ebeguPersistenceUnit")
	private EntityManager entityManager;

	@Nonnull
	@Override
	@TransactionTimeout(
		value = Constants.STATISTIK_TIMEOUT_MINUTES,
		unit = TimeUnit.MINUTES
	)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public UploadFileInfo generateReportLastenausgleichBGZeitabschnitte(
		@Nonnull Locale locale,
		@Nullable String von,
		@Nullable String bis,
		@Nullable String gemeindeId,
		@Nonnull Integer lastenausgleichJahr
	) throws ExcelMergeException, IOException {
		Lastenausgleich lastenausgleich = lastenausgleichService
			.findLastenausgleich(lastenausgleichJahr)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"generateExcelReportLastenausgleichBGZeitabschnitte",
					lastenausgleichJahr
				)
			);
		if (gemeindeId != null) {
			return generateExcelReportLastenausgleichBGZeitabschnitte(
				locale,
				von,
				bis,
				gemeindeId,
				lastenausgleichJahr,
				lastenausgleich
			);
		}

		return generateCSVReportLastenausgleichBGZeitabschnitte(
			locale,
			lastenausgleich,
			von,
			bis
		);
	}

	private UploadFileInfo generateExcelReportLastenausgleichBGZeitabschnitte(
		@Nonnull Locale locale,
		@Nullable String von,
		@Nullable String bis,
		@Nullable String gemeindeId,
		@Nonnull Integer lastenausgleichJahr,
		@Nonnull Lastenausgleich lastenausgleich
	) throws ExcelMergeException, IOException {

		List<LastenausgleichDetail> lastenausgleichDetails = new ArrayList<>(
			lastenausgleich.getLastenausgleichDetails()
				.stream()
				.filter(
					detail -> filterLastenausgleichDetail(detail, gemeindeId)
				)
				.collect(Collectors.toList())
		);

		List<LastenausgleichBGZeitabschnittDataRow> reportData =
			getReportLastenausgleichZeitabschnitte(
				lastenausgleichDetails,
				locale,
				von,
				bis
			);

		return generateExcelReportLastenausgleichBGZeitabschnitte(
			locale,
			lastenausgleichJahr,
			reportData
		);
	}

	private UploadFileInfo generateCSVReportLastenausgleichBGZeitabschnitte(
		@Nonnull Locale locale,
		@Nonnull Lastenausgleich lastenausgleich,
		@Nullable String von,
		@Nullable String bis
	) throws IOException {
		final UploadFileInfo uploadFileInfo = new UploadFileInfo(
			ServerMessageUtil.translateEnumValue(
				ReportFileName.LASTENAUSGLEICH_BG_ZEITABSCHNITTE,
				locale,
				principalBean.getMandant()
			) + ".csv",
			getContentTypeForCSVExport()
		);
		fileSaverService.generateFileName(
			uploadFileInfo,
			Constants.TEMP_REPORT_FOLDERNAME
		);

		File file = new File(uploadFileInfo.getPathAsString());
		if (!Files.exists(uploadFileInfo.getPath().getParent())) {
			Files.createDirectories(uploadFileInfo.getPath().getParent());
		}
		long size = 0;
		Iterator<LastenausgleichDetail> lastenausgleichDetailIterable =
			lastenausgleich.getLastenausgleichDetails().listIterator();
		try (
			BufferedWriter writer = new BufferedWriter(
				new FileWriter(file, StandardCharsets.UTF_8)
			)
		) {
			// Write CSV header
			String header = lastenausgleichBGZeitabschnitteCSVConverter
				.createLastenausgleichBGZeitabschnitteCSVHeader();
			writer.write(
				header
			);
			size = header.getBytes(StandardCharsets.UTF_8).length;
			while (lastenausgleichDetailIterable.hasNext()) {
				LastenausgleichDetail detail = lastenausgleichDetailIterable
					.next();
				List<LastenausgleichBGZeitabschnittDataRow> reportData =
					getReportLastenausgleichZeitabschnitte(
						Collections.singletonList(detail),
						locale,
						von,
						bis
					);

				String lastenausgleichCSV =
					lastenausgleichBGZeitabschnitteCSVConverter
						.createLastenausgleichBGZeitabschnitteCSV(reportData);
				writer.write(lastenausgleichCSV);
				size += lastenausgleichCSV.getBytes(
					StandardCharsets.UTF_8
				).length;
				reportData.clear();  // Clear the report data
			}
		}

		uploadFileInfo.setSize(size);
		return uploadFileInfo;
	}

	private UploadFileInfo generateExcelReportLastenausgleichBGZeitabschnitte(
		@Nonnull Locale locale,
		@Nonnull Integer lastenausgleichJahr,
		@Nonnull List<LastenausgleichBGZeitabschnittDataRow> reportData
	) throws ExcelMergeException, IOException {
		final ReportVorlage reportVorlage =
			ReportVorlage.VORLAGE_REPORT_LASTENAUSGLEICH_BG_ZEITABSCHNITTE;

		try (
			Workbook workbook = createWorkbook(reportVorlage);
		) {
			Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

			final XSSFSheet xsslSheet =
				(XSSFSheet) lastenausgleichBGZeitabschnitteExcelConverter
					.mergeHeaders(
						sheet,
						lastenausgleichJahr,
						locale,
						requireNonNull(principalBean.getMandant())
					);

			final RowFiller rowFiller = fillAndMergeRows(
				reportVorlage,
				xsslSheet,
				reportData
			);

			byte[] bytes = createWorkbook(rowFiller.getSheet().getWorkbook());
			rowFiller.getSheet().getWorkbook().dispose();

			return fileSaverService.save(
				bytes,
				ServerMessageUtil.translateEnumValue(
					reportVorlage.getDefaultExportFilename(),
					locale,
					principalBean.getMandant()
				)
					+ ".xlsx",
				Constants.TEMP_REPORT_FOLDERNAME,
				getContentTypeForExport()
			);
		}
	}

	private boolean filterLastenausgleichDetail(
		@NotNull LastenausgleichDetail lastenausgleichDetail,
		String gemeindeId
	) {
		boolean isGemeinde = lastenausgleichDetail.getGemeinde()
			.getId()
			.equals(gemeindeId);
		if (!isGemeinde) {
			return false;
		}
		authorizer.checkReadAuthorization(lastenausgleichDetail.getGemeinde());
		return true;
	}

	private boolean filterLastenausgleichDetailVonBis(
		@NotNull LastenausgleichBGZeitabschnittDataRow reportData,
		String von,
		String bis
	) {
		LocalDate formattedVon = DateUtil.parseStringToDate(von)
			.with(TemporalAdjusters.firstDayOfMonth());
		LocalDate formattedBis = DateUtil.parseStringToDate(bis)
			.with(TemporalAdjusters.lastDayOfMonth());
		return (reportData.getVon().equals(formattedVon)
			|| formattedVon.isBefore(reportData.getVon()))
			&& (reportData.getBis()
				.equals(formattedBis)
				|| formattedBis.isAfter(reportData.getBis()));
	}

	/**
	 * fuegt die Daten der Excelsheet hinzu und gibt den Rowfiller zurueck
	 */
	@Nonnull
	private RowFiller fillAndMergeRows(
		ReportVorlage reportResource,
		XSSFSheet sheet,
		List<LastenausgleichBGZeitabschnittDataRow> reportData
	) {

		RowFiller rowFiller =
			RowFiller.initRowFiller(
				sheet,
				MergeFieldProvider.toMergeFields(
					reportResource.getMergeFields()
				),
				reportData.size()
			);

		lastenausgleichBGZeitabschnitteExcelConverter.mergeRows(
			rowFiller,
			reportData
		);
		lastenausgleichBGZeitabschnitteExcelConverter.applyAutoSize(sheet);

		return rowFiller;
	}

	private List<LastenausgleichBGZeitabschnittDataRow> getReportLastenausgleichZeitabschnitte(
		@Nonnull Collection<LastenausgleichDetail> lastenausgleichDetails,
		@Nonnull Locale locale,
		@Nullable String von,
		@Nullable String bis
	) {
		List<LastenausgleichBGZeitabschnittDataRow> rows = new ArrayList<>();
		for (LastenausgleichDetail lastenausgleichDetail : lastenausgleichDetails) {
			// create a query that directly return the field mapped into the Rows:
			CriteriaBuilder criteriaBuilder = entityManager
				.getCriteriaBuilder();
			CriteriaQuery<LastenausgleichBGZeitabschnittDataRow> cq =
				criteriaBuilder.createQuery(
					LastenausgleichBGZeitabschnittDataRow.class
				);
			Root<LastenausgleichDetailZeitabschnitt> lastenausgleichDetailZeitabschnitt =
				cq.from(LastenausgleichDetailZeitabschnitt.class);
			Join<LastenausgleichDetailZeitabschnitt, VerfuegungZeitabschnitt> verfuegungZeitabschnittJoin =
				lastenausgleichDetailZeitabschnitt.join(
					LastenausgleichDetailZeitabschnitt_.zeitabschnitt
				);
			Join<VerfuegungZeitabschnitt, Verfuegung> verfuegungJoin =
				verfuegungZeitabschnittJoin.join(
					VerfuegungZeitabschnitt_.verfuegung
				);
			Join<Verfuegung, Betreuung> betreuungJoin = verfuegungJoin.join(
				"betreuung"
			);
			Join<Betreuung, KindContainer> kindContainerBetreuungJoin =
				betreuungJoin.join(Betreuung_.kind);
			Join<KindContainer, Kind> kindJoin = kindContainerBetreuungJoin
				.join(KindContainer_.kindJA);
			Join<Betreuung, InstitutionStammdaten> institutionStammdatenBetreuungJoin =
				betreuungJoin.join(Betreuung_.institutionStammdaten);
			Join<InstitutionStammdaten, Institution> institutionJoin =
				institutionStammdatenBetreuungJoin.join(
					InstitutionStammdaten_.institution
				);
			Join<VerfuegungZeitabschnitt, BGCalculationResult> bgCalculationResultJoin =
				verfuegungZeitabschnittJoin.join(
					VerfuegungZeitabschnitt_.bgCalculationResultAsiv
				);

			cq.select(
				criteriaBuilder.construct(
					LastenausgleichBGZeitabschnittDataRow.class,
					betreuungJoin.get(Betreuung_.referenzNummer),
					kindJoin.get(Kind_.nachname),
					kindJoin.get(Kind_.vorname),
					kindJoin.get(Kind_.geburtsdatum),
					verfuegungZeitabschnittJoin.get(
						VerfuegungZeitabschnitt_.gueltigkeit
					),
					institutionJoin.get(Institution_.name),
					institutionStammdatenBetreuungJoin.get(
						InstitutionStammdaten_.betreuungsangebotTyp
					),
					bgCalculationResultJoin.get(
						BGCalculationResult_.betreuungspensumProzent
					),
					bgCalculationResultJoin.get(
						BGCalculationResult_.anspruchspensumProzent
					),
					kindContainerBetreuungJoin.get(
						KindContainer_.keinSelbstbehaltDurchGemeinde
					),
					bgCalculationResultJoin.get(
						BGCalculationResult_.verguenstigung
					)
				)
			);

			cq.where(
				criteriaBuilder.equal(
					lastenausgleichDetailZeitabschnitt.get(
						LastenausgleichDetailZeitabschnitt_.lastenausgleichDetail
					),
					lastenausgleichDetail
				)
			);

			TypedQuery<LastenausgleichBGZeitabschnittDataRow> query =
				entityManager.createQuery(cq);

			/* Ein Zeitabschnitt kann entweder ein regulärer Zeitabschnitt des aktuellen Lastenausgleichjahres
						sein oder eine Korrektur eines Vorjahres. Bei einer Korrektur gibt es jeweils zwei Zeitabschnitte,
						einmal der negierte Betrag, der vor der Mutation gegolten hat und einmal der Betrag nach der Mutation.
						Bei einem negierten Zeitabschnitt müssen wir dies auch im Excel Report entsprechend ausweisen.
						*/
			BigDecimal multiplyer;
			if (lastenausgleichDetail.isNegatedKorrekturBetrag()) {
				multiplyer = BigDecimal.valueOf(-1);
			} else {
				multiplyer = BigDecimal.ONE;
			}
			Gemeinde gemeinde = lastenausgleichDetail.getGemeinde();

			// wir haben erst ab dem Jahr 2022 die zum Lastenausgleich dazugehörenden Zeitabschnitte in der Datenbank
			// persistiert
			// deshalb können wir in der Statistik erst ab 2022 die Korrekturwerte der Zeitabschnitte ausgeben.
			rows.addAll(
				query.getResultList()
					.stream()
					.filter(
						dataRow -> dataRow.getVon().getYear()
							>= Constants.FIRST_YEAR_LASTENAUSGLEICH_WITHOUT_SELBSTBEHALT
					)
					.filter(
						dataRow -> dataRow.getBgPensum()
							.compareTo(BigDecimal.ZERO)
							!= 0
					)
					.peek(
						row -> {
							row.setNameGemeinde(gemeinde.getName());
							row.setBfsNummer(gemeinde.getBfsNummer());
							row.setGutschein(
								row.getGutschein().multiply(multiplyer)
							);
							row.setBgPensum(
								row.getBgPensum().multiply(multiplyer)
							);
							row.setBetreuungsangebotTypTranslated(
								ServerMessageUtil.translateEnumValue(
									row.getBetreuungsangebotTyp(),
									locale,
									gemeinde.getMandant()
								)
							);
							row.setIsKorrektur(
								lastenausgleichDetail.isKorrektur()
							);
						}
					)
					.collect(Collectors.toList())
			);
		}
		rows.sort(
			Comparator.comparing(
				LastenausgleichBGZeitabschnittDataRow::getIsKorrektur
			)
				.thenComparing(
					LastenausgleichBGZeitabschnittDataRow::getReferenzNummer
				)
				.thenComparing(LastenausgleichBGZeitabschnittDataRow::getVon)
		);

		if (von != null && bis != null) {
			rows = rows.stream()
				.filter(
					detail -> filterLastenausgleichDetailVonBis(
						detail,
						von,
						bis
					)
				)
				.collect(Collectors.toList());
		}

		return rows;
	}
}
