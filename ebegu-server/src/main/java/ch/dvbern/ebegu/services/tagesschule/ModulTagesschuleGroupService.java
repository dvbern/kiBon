package ch.dvbern.ebegu.services.tagesschule;

import java.util.Collection;
import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.BelegungTagesschuleModul;
import ch.dvbern.ebegu.entities.BelegungTagesschuleModul_;
import ch.dvbern.ebegu.entities.ModulTagesschule;
import ch.dvbern.ebegu.entities.ModulTagesschuleGroup;
import ch.dvbern.ebegu.entities.ModulTagesschule_;
import ch.dvbern.ebegu.persistence.Persistence;
import lombok.NoArgsConstructor;

@Stateless
@NoArgsConstructor
public class ModulTagesschuleGroupService {

	private Persistence persistence;

	@Inject
	public ModulTagesschuleGroupService(
		Persistence persistence
	) {
		this.persistence = persistence;
	}

	public Collection<ModulTagesschuleGroup> getModulTagesschuleGroupWithAnmeldung(
		List<String> modulIdsToSearchAnmeldungenFor
	) {
		if (modulIdsToSearchAnmeldungenFor.isEmpty()) {
			return List.of();
		}
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<ModulTagesschuleGroup> query = cb.createQuery(
			ModulTagesschuleGroup.class
		);
		final Root<BelegungTagesschuleModul> belegungRoot = query.from(
			BelegungTagesschuleModul.class
		);

		Join<BelegungTagesschuleModul, ModulTagesschule> modulJoin =
			belegungRoot.join(
				BelegungTagesschuleModul_.modulTagesschule,
				JoinType.INNER
			);
		Join<ModulTagesschule, ModulTagesschuleGroup> groupJoin = modulJoin
			.join(ModulTagesschule_.modulTagesschuleGroup, JoinType.INNER);

		query.select(groupJoin)
			.distinct(true)
			.where(
				groupJoin.get(AbstractEntity_.id)
					.in(modulIdsToSearchAnmeldungenFor)
			);

		return persistence.getEntityManager()
			.createQuery(query)
			.getResultList();
	}
}
