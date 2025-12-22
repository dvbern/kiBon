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

package ch.dvbern.ebegu.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.ejb.EJBAccessException;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Workjob;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.WorkJobConstants;
import ch.dvbern.ebegu.enums.reporting.BatchJobStatus;
import ch.dvbern.ebegu.enums.reporting.DatumTyp;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.util.Constants;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.WorkJobConstants.BETRAG_PRO_KIND;
import static ch.dvbern.ebegu.enums.WorkJobConstants.DATE_FROM_PARAM;
import static ch.dvbern.ebegu.enums.WorkJobConstants.DATE_TO_PARAM;
import static ch.dvbern.ebegu.enums.WorkJobConstants.DATUM_TYP;
import static ch.dvbern.ebegu.enums.WorkJobConstants.DO_SAVE;
import static ch.dvbern.ebegu.enums.WorkJobConstants.INKL_BG_GESUCHE;
import static ch.dvbern.ebegu.enums.WorkJobConstants.INKL_MISCH_GESUCHE;
import static ch.dvbern.ebegu.enums.WorkJobConstants.INKL_TS_GESUCHE;
import static ch.dvbern.ebegu.enums.WorkJobConstants.KANTON_SELBSTBEHALT;
import static ch.dvbern.ebegu.enums.WorkJobConstants.LANGUAGE;
import static ch.dvbern.ebegu.enums.WorkJobConstants.OHNE_ERNEUERUNGSGESUCHE;
import static ch.dvbern.ebegu.enums.WorkJobConstants.REPORT_VORLAGE_TYPE_PARAM;
import static ch.dvbern.ebegu.enums.WorkJobConstants.TEXT;

/**
 * Data Acess Object Bean zum zugriff auf Workjoben in der DB
 */
@Stateless
@Local(WorkjobReportService.class)
public class WorkjobReportServiceBean extends AbstractBaseService implements
	WorkjobReportService {

	private static final Logger LOG = LoggerFactory.getLogger(
		WorkjobReportServiceBean.class.getSimpleName()
	);

	@Inject
	private Persistence persistence;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private WorkjobService workjobService;

	public WorkjobReportServiceBean() {

	}

	@Nonnull
	@Override
	public Workjob createNewReporting(
		@Nonnull Workjob workJob,
		@Nonnull ReportVorlage vorlage,
		@Nullable LocalDate datumVon,
		@Nullable LocalDate datumBis,
		@Nullable String gesuchPeriodIdParam,
		boolean inklBgGesuche,
		boolean inklMischGesuche,
		boolean inklTsGesuche,
		boolean ohneErneuerungsgesuch,
		@Nullable Gemeinde gemeinde,
		@Nullable Institution institution,
		@Nullable Integer jahr,
		@Nullable String text,
		@Nonnull Locale locale
	) {
		return createNewReporting(
			workJob,
			vorlage,
			datumVon,
			datumBis,
			null,
			gesuchPeriodIdParam,
			null,
			inklBgGesuche,
			inklMischGesuche,
			inklTsGesuche,
			ohneErneuerungsgesuch,
			jahr,
			gemeinde,
			institution,
			text,
			false,
			BigDecimal.ZERO,
			null,
			locale
		);
	}

	@Nonnull
	private Workjob createNewReporting(
		@Nonnull Workjob workJob,
		@Nonnull ReportVorlage vorlage,
		@Nullable LocalDate datumVon,
		@Nullable LocalDate datumBis,
		@Nullable DatumTyp datumTyp,
		@Nullable String gesuchPeriodIdParam,
		@Nullable String stammdatenIdParam,
		boolean inklBgGesuche,
		boolean inklMischGesuche,
		boolean inklTsGesuche,
		boolean ohneErneuerungsgesuch,
		@Nullable Integer jahr,
		@Nullable Gemeinde gemeinde,
		@Nullable Institution institution,
		@Nullable String text,
		boolean doSave,
		@Nonnull BigDecimal betragProKind,
		@Nullable BigDecimal kantonSelbstbehalt,
		@Nonnull Locale locale
	) {
		checkIfJobCreationAllowed(workJob, vorlage);

		JobOperator jobOperator = BatchRuntime.getJobOperator();
		final Properties jobParameters = new Properties();
		final String datumVonString;
		if (datumVon != null) {
			datumVonString = Constants.SQL_DATE_FORMAT.format(datumVon);
			jobParameters.setProperty(DATE_FROM_PARAM, datumVonString);
		}
		final String datumBisString;
		if (datumBis != null) {
			datumBisString = Constants.SQL_DATE_FORMAT.format(datumBis);
			jobParameters.setProperty(DATE_TO_PARAM, datumBisString);
		}

		if (datumTyp != null) {
			jobParameters.setProperty(DATUM_TYP, String.valueOf(datumTyp));
		}
		jobParameters.setProperty(
			INKL_BG_GESUCHE,
			String.valueOf(inklBgGesuche)
		);
		jobParameters.setProperty(
			INKL_MISCH_GESUCHE,
			String.valueOf(inklMischGesuche)
		);
		jobParameters.setProperty(
			INKL_TS_GESUCHE,
			String.valueOf(inklTsGesuche)
		);
		jobParameters.setProperty(
			OHNE_ERNEUERUNGSGESUCHE,
			String.valueOf(ohneErneuerungsgesuch)
		);
		if (StringUtils.isNotEmpty(text)) {
			jobParameters.setProperty(TEXT, text);
		}
		jobParameters.setProperty(DO_SAVE, String.valueOf(doSave));
		jobParameters.setProperty(
			BETRAG_PRO_KIND,
			String.valueOf(betragProKind)
		);
		if (kantonSelbstbehalt != null) {
			jobParameters.setProperty(
				KANTON_SELBSTBEHALT,
				String.valueOf(kantonSelbstbehalt)
			);
		}
		jobParameters.setProperty(REPORT_VORLAGE_TYPE_PARAM, vorlage.name());
		jobParameters.setProperty(LANGUAGE, locale.getLanguage());

		setPropertyIfPresent(
			jobParameters,
			WorkJobConstants.GESUCH_PERIODE_ID_PARAM,
			gesuchPeriodIdParam
		);
		setPropertyIfPresent(
			jobParameters,
			WorkJobConstants.STAMMDATEN_ID_PARAM,
			stammdatenIdParam
		);
		if (gemeinde != null) {
			jobParameters.setProperty(
				WorkJobConstants.GEMEINDE_ID_PARAM,
				gemeinde.getId()
			);
		}
		if (institution != null) {
			jobParameters.setProperty(
				WorkJobConstants.INSTITUTION_ID_PARAM,
				institution.getId()
			);
		}
		if (jahr != null) {
			jobParameters.setProperty(
				WorkJobConstants.JAHR_PARAM,
				jahr.toString()
			);
		}
		jobParameters.setProperty(
			WorkJobConstants.EMAIL_OF_USER,
			principalBean.getBenutzer().getEmail()
		);
		jobParameters.setProperty(
			WorkJobConstants.REPORT_MANDANT_ID,
			Objects.requireNonNull(principalBean.getMandant())
				.getId()
		);
		jobParameters.setProperty(
			WorkJobConstants.REPORT_MANDANT_IDENTIFIER,
			principalBean.getMandant().getMandantIdentifier().name()
		);
		jobOperator.getJobNames();
		workJob.setStatus(BatchJobStatus.REQUESTED);
		workJob = workjobService.saveWorkjob(workJob);
		persistence.getEntityManager().flush(); //so we can actually set state to running using an update script in the job-listener
		long executionId = jobOperator.start("reportbatch", jobParameters);

		this.persistence.getEntityManager().refresh(workJob); //evtl hat job schon gestartet
		workJob.setExecutionId(executionId);
		workJob = workjobService.saveWorkjob(workJob);

		LOG.debug(
			"Startet workjob für {} with executionId {}",
			vorlage.getDefaultExportFilename(),
			executionId
		);

		return workJob;
	}

	@Nonnull
	@Override
	public Workjob createNewReporting(
		@Nonnull Workjob workJob,
		@Nonnull ReportVorlage vorlage,
		@Nullable LocalDate datumVon,
		@Nullable LocalDate datumBis,
		@Nullable String gesuchPeriodIdParam,
		@Nonnull Locale locale
	) {
		return createNewReporting(
			workJob,
			vorlage,
			datumVon,
			datumBis,
			gesuchPeriodIdParam,
			false,
			false,
			false,
			false,
			null,
			null,
			null,
			null,
			locale
		);
	}

	@Nonnull
	@Override
	public Workjob createNewReporting(
		@Nonnull Workjob workJob,
		@Nonnull ReportVorlage vorlage,
		@Nullable LocalDate datumVon,
		@Nullable LocalDate datumBis,
		@Nullable BigDecimal kantonSelbstbehalt,
		@Nullable String gesuchPeriodIdParam,
		@Nonnull Locale locale
	) {
		return createNewReporting(
			workJob,
			vorlage,
			datumVon,
			datumBis,
			gesuchPeriodIdParam,
			kantonSelbstbehalt,
			locale
		);
	}

	@Nonnull
	private Workjob createNewReporting(
		@Nonnull Workjob workJob,
		@Nonnull ReportVorlage vorlage,
		@Nullable LocalDate datumVon,
		@Nullable LocalDate datumBis,
		@Nullable String gesuchPeriodIdParam,
		@Nullable BigDecimal kantonSelbstbehalt,
		@Nonnull Locale locale
	) {
		return createNewReporting(
			workJob,
			vorlage,
			datumVon,
			datumBis,
			null,
			gesuchPeriodIdParam,
			null,
			false,
			false,
			false,
			false,
			null,
			null,
			null,
			null,
			false,
			BigDecimal.ZERO,
			kantonSelbstbehalt,
			locale
		);
	}

	@Nonnull
	@Override
	public Workjob createNewReporting(
		@Nonnull Workjob workJob,
		@Nonnull ReportVorlage vorlage,
		boolean doSave,
		@Nonnull BigDecimal betragProKind,
		@Nonnull Locale locale
	) {
		return createNewReporting(
			workJob,
			vorlage,
			null,
			null,
			null,
			null,
			null,
			false,
			false,
			false,
			false,
			null,
			null,
			null,
			null,
			doSave,
			betragProKind,
			null,
			locale
		);
	}

	@Nonnull
	@Override
	public Workjob createNewReporting(
		@Nonnull Workjob workJob,
		@Nonnull ReportVorlage vorlage,
		@Nonnull String stammdatenIds,
		@Nonnull String gesuchsperiodeId,
		@Nonnull Locale locale
	) {
		return createNewReporting(
			workJob,
			vorlage,
			null,
			null,
			null,
			gesuchsperiodeId,
			stammdatenIds,
			false,
			false,
			false,
			false,
			null,
			null,
			null,
			null,
			false,
			BigDecimal.ZERO,
			null,
			locale
		);
	}

	@Nonnull
	@Override
	public Workjob createNewReporting(
		@Nonnull final Workjob workJob,
		@Nonnull final ReportVorlage vorlage,
		@Nullable final LocalDate datumVon,
		@Nullable final LocalDate datumBis,
		@Nonnull final DatumTyp datumTyp,
		@Nullable final String gesuchPeriodIdParam,
		@Nonnull final Locale locale
	) {
		return createNewReporting(
			workJob,
			vorlage,
			datumVon,
			datumBis,
			datumTyp,
			gesuchPeriodIdParam,
			null,
			false,
			false,
			false,
			false,
			null,
			null,
			null,
			null,
			false,
			BigDecimal.ZERO,
			null,
			locale
		);
	}

	@Nonnull
	@Override
	public Workjob persistWorkjobForReport(@Nonnull Workjob workJob) {
		workJob.setStatus(BatchJobStatus.REQUESTED);
		persistence.persist(workJob);
		return workJob;
	}

	private void setPropertyIfPresent(
		@Nonnull Properties jobParameters,
		@Nonnull String paramName,
		@Nullable String paramValue
	) {
		if (paramValue != null) {
			jobParameters.setProperty(paramName, paramValue);
		}
	}

	private void checkIfJobCreationAllowed(
		@Nonnull Workjob workJob,
		ReportVorlage vorlage
	) {
		UserRole userRole = principalBean.discoverMostPrivilegedRole();
		if (!ReportVorlage.checkAllowed(userRole, vorlage)) {
			throw new EJBAccessException(
				"Access Violation"
					+ " for Report: "
					+ vorlage
					+ " for current user: "
					+ principalBean.getPrincipal()
					+ " in role(s): "
					+ userRole
			);
		}
		Set<BatchJobStatus> statesToSearch = Sets.newHashSet(
			BatchJobStatus.REQUESTED,
			BatchJobStatus.RUNNING
		);

		final List<Workjob> openWorkjobs = workjobService.findWorkjobs(
			principalBean.getPrincipal().getName(),
			statesToSearch
		);
		final boolean alreadyQueued = openWorkjobs.stream()
			.anyMatch(workJob::isSame);
		if (alreadyQueued) {
			String messsage = String.format(
				"An identical Workjob was already queued by this user; %s ",
				workJob
			);
			throw new EbeguRuntimeException(
				KibonLogLevel.INFO,
				"checkIfJobCreationAllowed",
				messsage,
				ErrorCodeEnum.ERROR_JOB_ALREADY_EXISTS
			);
		}
	}
}
