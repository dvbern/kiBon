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
 */

package ch.dvbern.ebegu.api.search.service;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.SetJoin;

import ch.dvbern.ebegu.entities.AbstractPlatz_;
import ch.dvbern.ebegu.entities.AnmeldungFerieninsel;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuung_;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Dossier_;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Fall_;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.KindContainer_;
import ch.dvbern.ebegu.entities.sozialdienst.SozialdienstFall;

public class SearchServiceContext {
	private Join<Gesuch, Dossier> joinDossier;
	private Join<Dossier, Fall> joinFall;
	private Join<Fall, SozialdienstFall> joinSozialdienstFall;
	private Join<Dossier, Benutzer> joinVerantwortlicherBG;
	private Join<Dossier, Benutzer> joinVerantwortlicherTS;
	private Join<Dossier, Gemeinde> joinGemeinde;
	private Join<Gesuch, Gesuchsperiode> joinGesuchsperiode;
	private SetJoin<Gesuch, KindContainer> joinKindContainers;
	private SetJoin<KindContainer, Betreuung> joinBetreuungen;
	private SetJoin<KindContainer, AnmeldungTagesschule> joinAnmeldungTagesschule;
	private SetJoin<KindContainer, AnmeldungFerieninsel> joinAnmeldungFerieninsel;
	private Join<KindContainer, Kind> joinKinder;
	private Join<Betreuung, InstitutionStammdaten> joinInstitutionstammdatenBetreuungen;
	private Join<AnmeldungTagesschule, InstitutionStammdaten> joinInstitutionstammdatenTagesschule;
	private Join<AnmeldungFerieninsel, InstitutionStammdaten> joinInstitutionstammdatenFerieninsel;
	private Join<InstitutionStammdaten, Institution> joinInstitutionBetreuungen;
	private Join<InstitutionStammdaten, Institution> joinInstitutionTagesschule;
	private Join<InstitutionStammdaten, Institution> joinInstitutionFerieninsel;
	private Benutzer user;
	private Root<Gesuch> root;

	public SearchServiceContext(Benutzer user, Root<Gesuch> root) {
		this.user = user;
		this.root = root;
	}

	public Join<Gesuch, Dossier> getJoinDossier() {
		if (joinDossier == null) {
			joinDossier = root.join(
				Gesuch_.dossier,
				JoinType.INNER
			);
		}
		return joinDossier;
	}

	public Join<Dossier, Fall> getJoinFall() {
		if (joinFall == null) {
			joinFall = getJoinDossier().join(
				Dossier_.fall,
				JoinType.INNER
			);
		}
		return joinFall;
	}

	public Join<Fall, SozialdienstFall> getJoinSozialdienstFall() {
		if (joinSozialdienstFall == null) {
			joinSozialdienstFall = getJoinFall().join(
				Fall_.sozialdienstFall,
				JoinType.LEFT
			);
		}
		return joinSozialdienstFall;
	}

	public Join<Dossier, Benutzer> getJoinVerantwortlicherBG() {
		if (joinVerantwortlicherBG == null) {
			joinVerantwortlicherBG = getJoinDossier().join(
				Dossier_.verantwortlicherBG,
				JoinType.LEFT
			);
		}
		return joinVerantwortlicherBG;
	}

	public Join<Dossier, Benutzer> getJoinVerantwortlicherTS() {
		if (joinVerantwortlicherTS == null) {
			joinVerantwortlicherTS = getJoinDossier().join(
				Dossier_.verantwortlicherTS,
				JoinType.LEFT
			);
		}
		return joinVerantwortlicherTS;
	}

	public Join<Dossier, Gemeinde> getJoinGemeinde() {
		if (joinGemeinde == null) {
			joinGemeinde = getJoinDossier().join(
				Dossier_.gemeinde,
				JoinType.LEFT
			);
		}
		return joinGemeinde;
	}

	public Join<Gesuch, Gesuchsperiode> getJoinGesuchsperiode() {
		if (joinGesuchsperiode == null) {
			joinGesuchsperiode = root.join(
				Gesuch_.gesuchsperiode,
				JoinType.INNER
			);
		}
		return joinGesuchsperiode;
	}

	public SetJoin<Gesuch, KindContainer> getJoinKindContainers() {
		if (joinKindContainers == null) {
			joinKindContainers = root.join(
				Gesuch_.kindContainers,
				JoinType.LEFT
			);
		}
		return joinKindContainers;
	}

	public SetJoin<KindContainer, Betreuung> getJoinBetreuungen() {
		if (joinBetreuungen == null) {
			joinBetreuungen = getJoinKindContainers().join(
				KindContainer_.betreuungen,
				JoinType.LEFT
			);
		}
		return joinBetreuungen;
	}

	public SetJoin<KindContainer, AnmeldungTagesschule> getJoinAnmeldungTagesschule() {
		if (joinAnmeldungTagesschule == null) {
			joinAnmeldungTagesschule = getJoinKindContainers().join(
				KindContainer_.anmeldungenTagesschule,
				JoinType.LEFT
			);
		}

		return joinAnmeldungTagesschule;
	}

	public SetJoin<KindContainer, AnmeldungFerieninsel> getJoinAnmeldungFerieninsel() {
		if (joinAnmeldungFerieninsel == null) {
			joinAnmeldungFerieninsel = getJoinKindContainers().join(
				KindContainer_.anmeldungenFerieninsel,
				JoinType.LEFT
			);
		}
		return joinAnmeldungFerieninsel;
	}

	public Join<KindContainer, Kind> getJoinKinder() {
		if (joinKinder == null) {
			joinKinder = getJoinKindContainers().join(
				KindContainer_.kindJA,
				JoinType.LEFT
			);
		}
		return joinKinder;
	}

	public Join<Betreuung, InstitutionStammdaten> getJoinInstitutionstammdatenBetreuungen() {
		if (joinInstitutionstammdatenBetreuungen == null) {
			joinInstitutionstammdatenBetreuungen = getJoinBetreuungen().join(
				Betreuung_.institutionStammdaten,
				JoinType.LEFT
			);
		}
		return joinInstitutionstammdatenBetreuungen;
	}

	public Join<AnmeldungTagesschule, InstitutionStammdaten> getJoinInstitutionstammdatenTagesschule() {
		if (joinInstitutionstammdatenTagesschule == null) {
			joinInstitutionstammdatenTagesschule = getJoinAnmeldungTagesschule()
				.join(
					AbstractPlatz_.institutionStammdaten,
					JoinType.LEFT
				);
		}
		return joinInstitutionstammdatenTagesschule;
	}

	public Join<AnmeldungFerieninsel, InstitutionStammdaten> getJoinInstitutionstammdatenFerieninsel() {
		if (joinInstitutionstammdatenFerieninsel == null) {
			joinInstitutionstammdatenFerieninsel = getJoinAnmeldungFerieninsel()
				.join(
					AbstractPlatz_.institutionStammdaten,
					JoinType.LEFT
				);
		}
		return joinInstitutionstammdatenFerieninsel;
	}

	public Join<InstitutionStammdaten, Institution> getJoinInstitutionBetreuungen() {
		if (joinInstitutionBetreuungen == null) {
			joinInstitutionBetreuungen =
				getJoinInstitutionstammdatenBetreuungen().join(
					InstitutionStammdaten_.institution,
					JoinType.LEFT
				);
		}
		return joinInstitutionBetreuungen;
	}

	public Join<InstitutionStammdaten, Institution> getJoinInstitutionTagesschule() {
		if (joinInstitutionTagesschule == null) {
			joinInstitutionTagesschule =
				getJoinInstitutionstammdatenTagesschule().join(
					InstitutionStammdaten_.institution,
					JoinType.LEFT
				);
		}
		return joinInstitutionTagesschule;
	}

	public Join<InstitutionStammdaten, Institution> getJoinInstitutionFerieninsel() {
		if (joinInstitutionFerieninsel == null) {
			joinInstitutionFerieninsel =
				getJoinInstitutionstammdatenFerieninsel().join(
					InstitutionStammdaten_.institution,
					JoinType.LEFT
				);
		}
		return joinInstitutionFerieninsel;
	}

	public Benutzer getUser() {
		return user;
	}
}
