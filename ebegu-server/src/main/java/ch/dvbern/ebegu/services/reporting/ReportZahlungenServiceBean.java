/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
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
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.dto.report.ReportZahlungspositionDTO;
import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.AbstractPersonEntity_;
import ch.dvbern.ebegu.entities.AbstractPlatz_;
import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.BGCalculationResult_;
import ch.dvbern.ebegu.entities.Dossier_;
import ch.dvbern.ebegu.entities.FamiliensituationContainer_;
import ch.dvbern.ebegu.entities.Familiensituation_;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.entities.KindContainer_;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt_;
import ch.dvbern.ebegu.entities.Verfuegung_;
import ch.dvbern.ebegu.entities.Zahlung_;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.entities.Zahlungsauftrag_;
import ch.dvbern.ebegu.entities.Zahlungsposition_;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.enums.ZahlungspositionStatus;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.reporting.ReportZahlungenService;
import ch.dvbern.ebegu.reporting.zahlungen.ReportZahlungenExcelConverter;
import ch.dvbern.ebegu.reporting.zahlungen.ZahlungenDataRow;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.FileSaverService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import ch.dvbern.oss.lib.excelmerger.RowFiller;
import ch.dvbern.oss.lib.excelmerger.mergefields.MergeFieldProvider;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.jboss.ejb3.annotation.TransactionTimeout;

import static ch.dvbern.ebegu.services.reporting.ReportUtil.createWorkbook;
import static ch.dvbern.ebegu.services.reporting.ReportUtil.getContentTypeForExport;

@Stateless
@Local(ReportZahlungenService.class)
public class ReportZahlungenServiceBean extends AbstractReportServiceBean
	implements
	ReportZahlungenService {

	private final ReportZahlungenExcelConverter zahlungenConverter =
		new ReportZahlungenExcelConverter();

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private InstitutionService institutionService;

	@Inject
	private Authorizer authorizer;

	@Inject
	private FileSaverService fileSaverService;

	@Inject
	private PrincipalBean principalBean;

	@PersistenceContext(unitName = "ebeguPersistenceUnit")
	private EntityManager entityManager;

	@Nonnull
	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES,
		unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public UploadFileInfo generateExcelReportZahlungen(
		@Nonnull ReportVorlage reportVorlage,
		@Nonnull Locale locale,
		@Nonnull String gesuchsperiodeId,
		@Nullable String gemeindeId,
		@Nullable String institutionId
	) throws ExcelMergeException, IOException {

		try (
			Workbook workbook = createWorkbook(reportVorlage)
		) {
			XSSFSheet sheet = (XSSFSheet) workbook.getSheet(
				reportVorlage.getDataSheetName()
			);

			var periode = gesuchsperiodeService.findGesuchsperiode(
				gesuchsperiodeId
			)
				.orElseThrow(
					() -> new EbeguEntityNotFoundException(
						"generateExcelReportZahlungen",
						gesuchsperiodeId
					)
				);

			Gemeinde gemeinde = null;
			if (gemeindeId != null) {
				gemeinde = gemeindeService.findGemeinde(gemeindeId)
					.orElseThrow(
						() -> new EbeguEntityNotFoundException(
							"generateExcelReportZahlungen",
							gemeindeId
						)
					);
			}
			Institution institution = null;
			if (institutionId != null) {
				institution = institutionService.findInstitution(
					institutionId,
					true
				)
					.orElseThrow(
						() -> new EbeguEntityNotFoundException(
							"generateExcelReportZahlungen",
							institutionId
						)
					);
			}

			sheet = zahlungenConverter.mergeHeaders(
				sheet,
				periode,
				gemeinde,
				institution
			);

			var zahlungsauftrage =
				findZahlungsauftrageWithAuszahlungsTypInstitution(
					periode,
					gemeinde,
					institution
				);
			var reportData = filterZahlungenAndConvertToDataRows(
				zahlungsauftrage,
				institutionId
			);
			final RowFiller rowFiller = fillAndMergeRows(
				reportVorlage,
				sheet,
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
				) + ".xlsx",
				Constants.TEMP_REPORT_FOLDERNAME,
				getContentTypeForExport()
			);
		}
	}

	private RowFiller fillAndMergeRows(
		ReportVorlage reportResource,
		XSSFSheet sheet,
		List<ZahlungenDataRow> reportData
	) {
		RowFiller rowFiller = RowFiller.initRowFiller(
			sheet,
			MergeFieldProvider.toMergeFields(
				reportResource.getMergeFields()
			),
			reportData.size()
		);

		zahlungenConverter.mergeRows(rowFiller, reportData);
		return rowFiller;
	}

	private List<ReportZahlungspositionDTO> findZahlungsauftrageWithAuszahlungsTypInstitution(
		@Nonnull Gesuchsperiode periode,
		@Nullable Gemeinde gemeinde,
		@Nullable Institution institution
	) {
		final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		final CriteriaQuery<ReportZahlungspositionDTO> query = cb.createQuery(
			ReportZahlungspositionDTO.class
		);
		List<Predicate> predicates = new ArrayList<>();

		Root<Zahlungsauftrag> root = query.from(Zahlungsauftrag.class);
		Predicate predicateInPeriode = cb.between(
			root.get(AbstractEntity_.timestampErstellt),
			periode.getGueltigkeit().getGueltigAb().atStartOfDay(),
			periode.getGueltigkeit().getGueltigBis().atStartOfDay()
		);
		predicates.add(predicateInPeriode);

		Predicate zahlungslaufTyp = cb.equal(
			root.get(Zahlungsauftrag_.zahlungslaufTyp),
			ZahlungslaufTyp.GEMEINDE_INSTITUTION
		);
		predicates.add(zahlungslaufTyp);

		if (gemeinde != null) {
			Predicate predicateGemeinde = cb.equal(
				root.get(Zahlungsauftrag_.gemeinde),
				gemeinde
			);
			predicates.add(predicateGemeinde);
		}

		var joinZahlungen = root.join(Zahlungsauftrag_.zahlungen);
		if (institution != null) {
			var predicateInstitution = cb.equal(
				joinZahlungen.get(Zahlung_.empfaengerId),
				institution.getId()
			);
			predicates.add(predicateInstitution);
		}

		var joinZahlungsposition = joinZahlungen.join(
			Zahlung_.zahlungspositionen
		);
		var joinZeitabschnitt = joinZahlungsposition.join(
			Zahlungsposition_.verfuegungZeitabschnitt
		);
		var joinClculationResult = joinZeitabschnitt
			.join(VerfuegungZeitabschnitt_.bgCalculationResultAsiv);
		var joinBetreuung = joinZeitabschnitt
			.join(VerfuegungZeitabschnitt_.verfuegung)
			.join(Verfuegung_.betreuung);
		var joinKindContainer = joinBetreuung
			.join(AbstractPlatz_.kind);
		var joinKind = joinKindContainer
			.join(KindContainer_.kindJA);
		var joinInstituion = joinBetreuung
			.join(AbstractPlatz_.institutionStammdaten);
		var joinGesuch = joinKindContainer
			.join(KindContainer_.gesuch);
		var joinFamSit = joinGesuch
			.join(Gesuch_.familiensituationContainer)
			.join(FamiliensituationContainer_.familiensituationJA);
		var joinDossier = joinGesuch
			.join(Gesuch_.dossier);

		query.select(
			cb.construct(
				ReportZahlungspositionDTO.class,
				joinDossier.get(Dossier_.gemeinde),
				joinInstituion.get(InstitutionStammdaten_.institution),
				joinDossier.get(AbstractEntity_.id),
				root.get(Zahlungsauftrag_.beschrieb),
				root.get(Zahlungsauftrag_.datumFaellig),
				root.get(AbstractEntity_.timestampErstellt),
				joinKind.get(AbstractPersonEntity_.vorname),
				joinKind.get(AbstractPersonEntity_.nachname),
				joinBetreuung.get(AbstractPlatz_.referenzNummer),
				joinZeitabschnitt.get(AbstractDateRangedEntity_.gueltigkeit),
				joinClculationResult.get(
					BGCalculationResult_.anspruchspensumProzent
				),
				joinClculationResult.get(
					BGCalculationResult_.betreuungspensumProzent
				),
				joinZahlungsposition.get(Zahlungsposition_.betrag),
				joinZahlungsposition.get(Zahlungsposition_.status),
				joinZahlungsposition.get(Zahlungsposition_.ignoriert),
				joinFamSit.get(
					Familiensituation_.auszahlungAusserhalbVonKibon
				)
			)
		);

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		return entityManager.createQuery(query).getResultList();
	}

	private List<ZahlungenDataRow> filterZahlungenAndConvertToDataRows(
		@Nonnull List<ReportZahlungspositionDTO> zahlungsauftrage,
		@Nullable String institutionId
	) {
		List<ReportZahlungspositionDTO> filteredReportData =
			filterZahlungenByInstitution(zahlungsauftrage, institutionId);

		return filteredReportData.stream()
			.map(reportZahlungspositionDTO -> {
				authorizer.checkReadAuthorizationZahlung(
					reportZahlungspositionDTO
				);
				setAuszahlungsdatenToDto(reportZahlungspositionDTO);
				return zahlungToDataRow(reportZahlungspositionDTO);
			})
			.collect(Collectors.toList());
	}

	private void setAuszahlungsdatenToDto(
		ReportZahlungspositionDTO zahlungspositionDTO
	) {
		if (!zahlungspositionDTO.isZahlungspositionIgnoriert()
			&& !zahlungspositionDTO.isAuszahlungAusserhalbVonKibon()) {
			return;
		}

		Auszahlungsdaten latesteAuszahlungsdaten =
			getNeusteAuszahlungsdatenForDossier(
				zahlungspositionDTO.getDossierId()
			);
		zahlungspositionDTO.setAuszahlungsdaten(latesteAuszahlungsdaten);
	}

	public static ZahlungenDataRow zahlungToDataRow(
		@Nonnull ReportZahlungspositionDTO zahlungspositionDTO
	) {
		var row = new ZahlungenDataRow()
			.setZahlungslaufTitle(
				zahlungspositionDTO.getZahlungsauftragBeschrieb()
			)
			.setZahlungsFaelligkeitsDatum(
				zahlungspositionDTO.getZahlungsauftragDatumFeallig()
			)
			.setGemeinde(zahlungspositionDTO.getGemeinde().getName())
			.setInstitution(zahlungspositionDTO.getInstitution().getName())
			.setTimestampZahlungslauf(
				zahlungspositionDTO.getZahlungsauftragTimestampErstellt()
			)
			.setKindVorname(zahlungspositionDTO.getKindVorname())
			.setKindNachname(zahlungspositionDTO.getKindNachname())
			.setReferenzNummer(zahlungspositionDTO.getReferenzNummer())
			.setZeitabschnittVon(
				zahlungspositionDTO.getZeitabschnittGueltigkeit().getGueltigAb()
			)
			.setZeitabschnittBis(
				zahlungspositionDTO.getZeitabschnittGueltigkeit()
					.getGueltigBis()
			)
			.setBetrag(zahlungspositionDTO.getBetrag())
			.setKorrektur(
				ZahlungspositionStatus.NORMAL
					!= zahlungspositionDTO.getZahlungspositionStatus()
			)
			.setIgnorieren(zahlungspositionDTO.isZahlungspositionIgnoriert());

		var pensum = MathUtil.EXACT.divide(
			zahlungspositionDTO.getBgPensumProzent(),
			BigDecimal.valueOf(100)
		);
		row.setBgPensum(pensum);

		if (zahlungspositionDTO.getAuszahlungsdaten() == null) {
			return row;
		}

		if (zahlungspositionDTO.getAuszahlungsdaten().getIban() != null) {
			row.setIbanEltern(
				zahlungspositionDTO.getAuszahlungsdaten().getIban().getIban()
			);
		}
		row.setKontoEltern(
			zahlungspositionDTO.getAuszahlungsdaten().getKontoinhaber()
		);

		return row;
	}

	private Auszahlungsdaten getNeusteAuszahlungsdatenForDossier(
		String dossierId
	) {
		final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		final CriteriaQuery<Auszahlungsdaten> query = cb.createQuery(
			Auszahlungsdaten.class
		);
		Root<Gesuch> root = query.from(Gesuch.class);

		var familiensituationJoin = root.join(
			Gesuch_.FAMILIENSITUATION_CONTAINER
		)
			.join(FamiliensituationContainer_.FAMILIENSITUATION_JA);

		var dossierJoin = root.join(Gesuch_.DOSSIER);

		Predicate predicateDossier = cb.equal(
			dossierJoin.get(AbstractEntity_.ID),
			dossierId
		);
		Predicate predicateStatus = root.get(Gesuch_.status)
			.in(AntragStatus.getAllVerfuegtStates());

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(predicateDossier);
		predicates.add(predicateStatus);

		query.select(
			familiensituationJoin.get(Familiensituation_.AUSZAHLUNGSDATEN)
		);
		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		query.orderBy(cb.desc(root.get(Gesuch_.LAUFNUMMER)));
		return entityManager.createQuery(query)
			.setMaxResults(1)
			.getSingleResult();
	}

	private List<ReportZahlungspositionDTO> filterZahlungenByInstitution(
		@Nonnull List<ReportZahlungspositionDTO> zahlungspositionDTOS,
		@Nullable String institutionId
	) {
		if (institutionId == null) {
			return zahlungspositionDTOS;
		}

		return zahlungspositionDTOS.stream()
			.filter(
				z -> z.getInstitution()
					.getId()
					.equals(institutionId)
			)
			.collect(Collectors.toList());
	}

}
