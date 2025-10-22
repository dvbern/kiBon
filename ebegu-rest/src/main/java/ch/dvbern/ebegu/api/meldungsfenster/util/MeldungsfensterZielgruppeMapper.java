package ch.dvbern.ebegu.api.meldungsfenster.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.meldungsfenster.Meldungsfenster;
import ch.dvbern.ebegu.entities.meldungsfenster.MeldungsfensterRole;
import ch.dvbern.ebegu.enums.UserRole;

@RequestScoped
public class MeldungsfensterZielgruppeMapper {

	@Inject
	private PrincipalBean principal;

	public boolean hasUserRoleInZielGruppe(Meldungsfenster meldungsfenster) {
		if (hasAnonymousRole(meldungsfenster.getZielgruppe())
			&& principal.isAnonymous()) {
			return true;
		}

		List<UserRole> allowedRoles = new ArrayList<>(
			findUserRoleForZielgruppe(meldungsfenster.getZielgruppe())
		);

		return principal.isCallerInAnyOfRole(allowedRoles);
	}

	private boolean hasAnonymousRole(
		List<MeldungsfensterRole> meldungsfensterRoles
	) {
		return meldungsfensterRoles.stream()
			.anyMatch(
				meldungsfensterRole -> meldungsfensterRole.equals(
					MeldungsfensterRole.ANONYMOUS
				)
			);
	}

	private List<UserRole> findUserRoleForZielgruppe(
		List<MeldungsfensterRole> meldungsfensterRoles
	) {
		return meldungsfensterRoles.stream()
			.flatMap(
				meldungsfensterRole -> meldungsfensterRole.getRoles().stream()
			)
			.collect(Collectors.toList());
	}
}
