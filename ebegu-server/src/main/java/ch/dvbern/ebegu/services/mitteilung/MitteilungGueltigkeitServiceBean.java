/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services.mitteilung;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import ch.dvbern.ebegu.einstellung.ApplicationPropertyService;
import ch.dvbern.ebegu.entities.AbstractPlatz_;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuung_;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung_;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer_;
import ch.dvbern.ebegu.entities.Betreuungspensum_;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.entities.Mitteilung_;
import ch.dvbern.ebegu.enums.MitteilungStatus;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.MitteilungGueltigkeitChangeService;
import ch.dvbern.ebegu.services.MitteilungService;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EbeguUtil;

import static ch.dvbern.ebegu.enums.DemoFeatureTyp.INSTITUTIONSSCHLIESSUNG_MUTATIONSMELDUNG;

/**
 * Group the operation requried when an institution Gueltigkeit is adapted
 * Those change can possibly have an impact on the Mitteilung that already exists
 */
@ApplicationScoped
public class MitteilungGueltigkeitServiceBean implements
	MitteilungGueltigkeitChangeService {

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private Persistence persistence;

	@Inject
	private MitteilungService mitteilungService;

	@Inject
	private MitteilungSharedServiceBean mitteilungSharedServiceBean;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@Override
	public void adaptOffeneMutationsmitteilungenToInstiGueltigkeitChange(
		@Nonnull Institution institution,
		@Nonnull DateRange gueltigkeit
	) {
		Collection<Betreuungsmitteilung> offeneMutationsmitteilungenForInstitution =
			findAllBetreuungsMitteilungenForInstitution(institution);

		removeSchliessungMitteilungen(
			offeneMutationsmitteilungenForInstitution
		);

		boolean institutionSchliessungMutationsmeldungFeature =
			applicationPropertyService.getActivatedDemoFeatures(
				institution.getMandant()
			)
				.contains(
					INSTITUTIONSSCHLIESSUNG_MUTATIONSMELDUNG
				);
		if (institutionSchliessungMutationsmeldungFeature
			&& !Constants.END_OF_TIME.equals(
				gueltigkeit.getGueltigBis()
			)) {
			createNeueMutationsmitteilungenWennNoetig(
				institution,
				gueltigkeit,
				offeneMutationsmitteilungenForInstitution
			);
		}

		offeneMutationsmitteilungenForInstitution.forEach(mitteilung -> {

			if (!hasPensenInInstitutionGueltigkeit(
				mitteilung.getBetreuungspensen(),
				gueltigkeit
			)
				&& mitteilung.getBetreuung() != null
				&& mitteilung.getBetreuung()
					.extractGesuchsperiode()
					.getGueltigkeit()
					.intersects(gueltigkeit)) {
				mitteilung.setMarkedForDeletion(true);
				persistence.remove(mitteilung);
			} else {
				Set<BetreuungsmitteilungPensum> betreuungspensen =
					betreuungService
						.capBetreuungspensenToGueltigkeit(
							mitteilung.getBetreuungspensen(),
							gueltigkeit
						);
				mitteilung.setBetreuungspensen(betreuungspensen);
				Objects.requireNonNull(mitteilung.getBetreuung());
				final Locale locale =
					EbeguUtil.extractKorrespondenzsprache(
						mitteilung.getBetreuung().extractGesuch(),
						gemeindeService
					).getLocale();
				mitteilung.setMessage(
					mitteilungService.createNachrichtForMutationsmeldung(
						mitteilung,
						betreuungspensen,
						locale
					)
				);
				persistence.merge(mitteilung);
			}
		});
	}

	private boolean hasPensenInInstitutionGueltigkeit(
		Set<BetreuungsmitteilungPensum> betreuungspensen,
		DateRange gueltigkeit
	) {
		for (BetreuungsmitteilungPensum pensum : betreuungspensen) {
			if (pensum.getGueltigkeit().getOverlap(gueltigkeit).isPresent()) {
				return true;
			}
		}
		return false;
	}

	private void removeSchliessungMitteilungen(
		Collection<Betreuungsmitteilung> offeneMutationsmitteilungenForInstitution
	) {
		offeneMutationsmitteilungenForInstitution.removeIf(
			betreuungsmitteilung -> {
				if (betreuungsmitteilung.isSchliessungMitteilung()) {
					betreuungsmitteilung.setMarkedForDeletion(true);
					persistence.remove(betreuungsmitteilung);
					return true;
				}
				return false;
			}
		);
	}

	private void createNeueMutationsmitteilungenWennNoetig(
		Institution institution,
		DateRange gueltigkeit,
		Collection<Betreuungsmitteilung> offeneMutationsmitteilungenForInstitution
	) {
		//find alle Betreuung von die Institution und die im Gueltigkeit vorkommen
		Collection<Betreuung> betreuungen =
			findAlleBetreuungsForInstitutionDiePensenNachSchliessdatumBesitzen(
				institution,
				gueltigkeit
			);
		//dann schauen wir welche noch keine Mutationsmitteilung haben und erstellen wir eine
		betreuungen.forEach(betreuung -> {
			if (
				betreuung.extractGesuchsperiode()
					.getGueltigkeit()
					.intersects(gueltigkeit)
					&& !offeneMutationsmitteilungenForInstitution.stream()
						.anyMatch(
							betreuungsmitteilung -> betreuung.equals(
								betreuungsmitteilung.getBetreuung()
							)
						)) {

				Betreuungsmitteilung betreuungsmitteilung =
					copyBetreuungToBetreuungsSchliessungMitteilung(
						betreuung,
						gueltigkeit
					);
				persistence.persist(betreuungsmitteilung);

			}
		});
	}

	private Betreuungsmitteilung copyBetreuungToBetreuungsSchliessungMitteilung(
		Betreuung betreuung,
		DateRange gueltigkeit
	) {
		Betreuungsmitteilung betreuungsmitteilung = new Betreuungsmitteilung();
		betreuungsmitteilung.setSchliessungMitteilung(true);
		betreuungsmitteilung.setBetreuung(betreuung);
		betreuungsmitteilung.setMitteilungStatus(MitteilungStatus.NEU);
		betreuungsmitteilung.setSentDatum(LocalDateTime.now());
		betreuungsmitteilung.setDossier(betreuung.extractGesuch().getDossier());
		mitteilungSharedServiceBean.setSenderAndEmpfaengerAndCheckAuthorization(
			betreuungsmitteilung
		);
		betreuung.getBetreuungenJA().forEach(betreuungspensum -> {
			//CreateBetreuungsmitteilungPensum
			BetreuungsmitteilungPensum betreuungsmitteilungPensum =
				copyBetreuungspensumToNeueBetreuungsmitteilungPensum(
					betreuungspensum
				);
			//ADD Betreunngsmitteilungpensum
			betreuungsmitteilungPensum.setBetreuungsmitteilung(
				betreuungsmitteilung
			);
			betreuungsmitteilung.getBetreuungspensen()
				.add(betreuungsmitteilungPensum);
		});
		Set<BetreuungsmitteilungPensum> betreuungspensen =
			betreuungService
				.capBetreuungspensenToGueltigkeit(
					betreuungsmitteilung.getBetreuungspensen(),
					gueltigkeit
				);
		betreuungsmitteilung.setBetreuungspensen(betreuungspensen);
		final Locale locale =
			EbeguUtil.extractKorrespondenzsprache(
				betreuung.extractGesuch(),
				gemeindeService
			).getLocale();
		String msg = mitteilungSharedServiceBean
			.createNachrichtForMutationsmeldung(
				betreuungsmitteilung,
				betreuungsmitteilung.getBetreuungspensen(),
				locale
			);
		betreuungsmitteilung.setMessage(msg);
		mitteilungSharedServiceBean.setBetreuungsmitteilungSubject(
			betreuungsmitteilung,
			locale
		);
		return betreuungsmitteilung;
	}

	private BetreuungsmitteilungPensum copyBetreuungspensumToNeueBetreuungsmitteilungPensum(
		Betreuungspensum betreuungspensum
	) {
		BetreuungsmitteilungPensum betreuungsmitteilungPensum =
			new BetreuungsmitteilungPensum();
		betreuungsmitteilungPensum.setGueltigkeit(
			betreuungspensum.getGueltigkeit()
		);
		betreuungsmitteilungPensum.setPensum(betreuungspensum.getPensum());
		betreuungsmitteilungPensum.setBetreuteTage(
			betreuungspensum.getBetreuteTage()
		);
		betreuungsmitteilungPensum.setMonatlicheBetreuungskosten(
			betreuungspensum.getMonatlicheBetreuungskosten()
		);
		betreuungsmitteilungPensum.setBetreuungInFerienzeit(
			betreuungspensum.getBetreuungInFerienzeit()
		);
		betreuungsmitteilungPensum.setEingewoehnung(
			betreuungspensum.getEingewoehnung()
		);
		betreuungsmitteilungPensum.setMonatlicheHauptmahlzeiten(
			betreuungspensum.getMonatlicheHauptmahlzeiten()
		);
		betreuungsmitteilungPensum.setMonatlicheNebenmahlzeiten(
			betreuungspensum.getMonatlicheNebenmahlzeiten()
		);
		betreuungsmitteilungPensum.setUnitForDisplay(
			betreuungspensum.getUnitForDisplay()
		);
		betreuungsmitteilungPensum.setTarifProHauptmahlzeit(
			betreuungspensum.getTarifProHauptmahlzeit()
		);
		betreuungsmitteilungPensum.setTarifProNebenmahlzeit(
			betreuungspensum.getTarifProNebenmahlzeit()
		);
		betreuungsmitteilungPensum.setVollstaendig(
			betreuungspensum.isVollstaendig()
		);
		betreuungsmitteilungPensum.setStuendlicheVollkosten(
			betreuungspensum.getStuendlicheVollkosten()
		);
		return betreuungsmitteilungPensum;
	}

	private Collection<Betreuung> findAlleBetreuungsForInstitutionDiePensenNachSchliessdatumBesitzen(
		Institution institution,
		DateRange gueltigkeit
	) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Betreuung> query = cb.createQuery(
			Betreuung.class
		);
		Root<Betreuung> root = query.from(
			Betreuung.class
		);

		Predicate betreuungGueltig = cb.isTrue(
			root.get(AbstractPlatz_.gueltig)
		);

		Join<Betreuung, InstitutionStammdaten> stammdatenJoin =
			root.join(AbstractPlatz_.institutionStammdaten);

		Predicate predicateInstitution = cb.equal(
			stammdatenJoin.get(InstitutionStammdaten_.institution),
			institution
		);

		Join<Betreuung, BetreuungspensumContainer> betreuungspensumContainerJoin =
			root.join(Betreuung_.betreuungspensumContainers);
		Join<BetreuungspensumContainer, Betreuungspensum> pensumJoin =
			betreuungspensumContainerJoin.join(
				BetreuungspensumContainer_.betreuungspensumJA
			);

		Predicate pensumAfterStichtag =
			cb.greaterThan(
				pensumJoin.get(Betreuungspensum_.gueltigkeit)
					.get(DateRange_.gueltigBis),
				gueltigkeit.getGueltigBis()
			);

		query.where(
			betreuungGueltig,
			predicateInstitution,
			pensumAfterStichtag
		);
		return persistence.getCriteriaResults(query);
	}

	private Collection<Betreuungsmitteilung> findAllBetreuungsMitteilungenForInstitution(
		Institution institution
	) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Betreuungsmitteilung> query = cb.createQuery(
			Betreuungsmitteilung.class
		);
		Root<Betreuungsmitteilung> root = query.from(
			Betreuungsmitteilung.class
		);

		Join<Betreuungsmitteilung, Betreuung> betreuungJoin = root.join(
			Mitteilung_.betreuung
		);
		Join<Betreuung, InstitutionStammdaten> stammdatenJoin =
			betreuungJoin.join(AbstractPlatz_.institutionStammdaten);

		Predicate predicateInstitution = cb.equal(
			stammdatenJoin.get(InstitutionStammdaten_.institution),
			institution
		);
		Predicate notApplied = cb.notEqual(
			root.get(Betreuungsmitteilung_.APPLIED),
			true
		);

		query.where(predicateInstitution, notApplied);
		return persistence.getCriteriaResults(query);
	}

}
