package ch.dvbern.ebegu.services;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Workjob;
import ch.dvbern.ebegu.enums.reporting.BatchJobStatus;

public interface WorkjobService {

	@Nonnull
	Workjob saveWorkjob(@Nonnull Workjob workJob);

	@Nullable
	Workjob findWorkjobByExecutionId(@Nonnull Long executionId);

	void removeOldWorkjobs();

	@Nonnull
	List<Workjob> findWorkjobs(
		@Nonnull String startingUserName,
		@Nonnull Set<BatchJobStatus> statesToSearch
	);

	/**
	 * gibt eine Liste aller Workjobs aus der DB zurueck
	 */
	@Nonnull
	List<Workjob> findAllWorkjobs();

	/**
	 * update query that changes state
	 */
	void changeStateOfWorkjob(long executionId, @Nonnull BatchJobStatus status);

	void addResultToWorkjob(
		@Nonnull String workjobID,
		@Nonnull String resultData
	);

	void removeWorkjob(Workjob workjob);

	/**
	 * Find a workjob with a given ID
	 *
	 * @param id of the workjob
	 * @return the Workjob related to the given id or null if not found
	 */
	@Nullable
	Workjob findById(@Nonnull String id);

	/**
	 * Find related workjobs with the given Params
	 *
	 * @param params the parameter(s) for the text search
	 * @return a list of workjobs matching the given params
	 */
	@Nonnull
	List<Workjob> getWorkjobsWithParams(@Nonnull String params);
}
