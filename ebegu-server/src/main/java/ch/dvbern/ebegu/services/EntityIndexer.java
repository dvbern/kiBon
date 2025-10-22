/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services;

import java.util.Properties;

import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.RunAs;
import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.persistence.Persistence;
import org.hibernate.search.jakarta.batch.core.massindexing.MassIndexingJob;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.work.SearchIndexingPlan;
import org.jboss.ejb3.annotation.RunAsPrincipal;

@Stateless
@RunAs(UserRoleName.SUPER_ADMIN)
@RunAsPrincipal(PrincipalBean.KIBON_SERVICE_ACCOUNT)
public class EntityIndexer {

	@Inject
	private Persistence persistence;

	@RolesAllowed("**")
	@SuppressWarnings("PMD.CloseResource")
	public void updateSingleEntity(
		Class<? extends AbstractEntity> clazz,
		String id
	) {
		// Den Opensearch-Index manuell nachführen, da es bei unidirektionalen Relationen nicht automatisch geschieht!
		EntityManager entityManager = persistence.getEntityManager();
		var searchSession = Search.session(entityManager);

		SearchIndexingPlan indexingPlan = searchSession.indexingPlan();
		Object entity = entityManager.getReference(clazz, id);
		indexingPlan.addOrUpdate(entity);
	}

	@RolesAllowed(UserRoleName.SUPER_ADMIN)
	public void rebuildSearchIndex() {
		Properties jobProps = MassIndexingJob.parameters()
			.forEntities(
				Gesuch.class,
				GesuchstellerContainer.class,
				Dossier.class,
				KindContainer.class,
				Betreuung.class
			)
			.build();

		JobOperator jobOperator = BatchRuntime.getJobOperator();
		jobOperator.start(MassIndexingJob.NAME, jobProps);
	}
}
