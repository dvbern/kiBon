package ch.dvbern.ebegu.services.lastenausgleich;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.entities.LastenausgleichGrundlagen;
import ch.dvbern.ebegu.entities.LastenausgleichGrundlagen_;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class LastenausgleichGrundlageServiceBean {

	private static final Logger LOG = LoggerFactory.getLogger(
		LastenausgleichServiceBean.class.getSimpleName()
	);

	private static final BigDecimal SELBSTBEHALT = MathUtil.EXACT.fromNullSafe(
		0.20
	);

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Nonnull
	public Optional<LastenausgleichGrundlagen> findLastenausgleichGrundlagen(
		int jahr
	) {
		return criteriaQueryHelper.getEntityByUniqueAttribute(
			LastenausgleichGrundlagen.class,
			jahr,
			LastenausgleichGrundlagen_.jahr
		);
	}

	public void createLastenausgleichGrundlage(
		Lastenausgleich lastenausgleich,
		@Nullable String sSelbstbehaltPro100ProzentPlatz
	) {
		if (lastenausgleich.getJahr()
			< Constants.FIRST_YEAR_LASTENAUSGLEICH_WITHOUT_SELBSTBEHALT) {
			Objects.requireNonNull(sSelbstbehaltPro100ProzentPlatz);
			BigDecimal selbstbehaltPro100ProzentPlatz = MathUtil.DEFAULT.from(
				sSelbstbehaltPro100ProzentPlatz
			);
			createLastenausgleichGrundlageOld(
				lastenausgleich,
				selbstbehaltPro100ProzentPlatz
			);
		} else {
			createLastenausgleichGrundlageNew(lastenausgleich);
		}
	}

	/**
	 * Alte Lastenausgleich Berechung.
	 * Berechnet einen Lastenausgleich fuer das uebergebene Jahr. Die Kosten pro 100% Platz werden als
	 * LastenausgleichGrundlagen gespeichert.
	 * Der Lastenausgleich kann pro Jahr nur einmal erstellt werden, auch die Grundlagen duerfen nicht mehr geaendert
	 * werden.
	 * Es werden auch rueckwirkende Korrekturen vorgenommen und zwar fuer die letzten 10 Jahre
	 */
	private void createLastenausgleichGrundlageOld(
		Lastenausgleich lastenausgleich,
		@Nonnull BigDecimal selbstbehaltPro100ProzentPlatz
	) {
		BigDecimal kostenPro100ProzentPlatz =
			MathUtil.DEFAULT.divideNullSafe(
				selbstbehaltPro100ProzentPlatz,
				SELBSTBEHALT
			);
		LOG.info(
			"Erstelle Lastenausgleich für Jahr {}  bei einem Selbstbehalt pro 100% Platz von {}. Kosten pro 100% Platz: {}",
			lastenausgleich.getJahr(),
			selbstbehaltPro100ProzentPlatz,
			kostenPro100ProzentPlatz
		);
		LastenausgleichGrundlagen grundlagenErhebungsjahr =
			new LastenausgleichGrundlagen();
		grundlagenErhebungsjahr.setJahr(lastenausgleich.getJahr());
		grundlagenErhebungsjahr.setSelbstbehaltPro100ProzentPlatz(
			selbstbehaltPro100ProzentPlatz
		);
		grundlagenErhebungsjahr.setKostenPro100ProzentPlatz(
			kostenPro100ProzentPlatz
		);
		persistence.persist(grundlagenErhebungsjahr);
	}

	/**
	 * Neue Lastenausgleich Berechnung.
	 * Berechnet einen Lastenausgleich fuer das uebergebene Jahr. Ab dem Jahr 2022 wird der Lastenausgleich nicht mehr
	 * mit dem Selbstbehalt pro 100% Platz berechnet.
	 * Der Lastenausgleich kann pro Jahr nur einmal erstellt werden, auch die Grundlagen duerfen nicht mehr geaendert
	 * werden.
	 * Es werden auch rueckwirkende Korrekturen vorgenommen und zwar fuer die letzten 10 Jahre
	 */
	private void createLastenausgleichGrundlageNew(
		Lastenausgleich lastenausgleich
	) {
		LOG.info(
			"Erstelle Lastenausgleich für Jahr {} ohne Selbstbehalt pro 100% Platz.",
			lastenausgleich.getJahr()
		);
		LastenausgleichGrundlagen grundlagenErhebungsjahr =
			new LastenausgleichGrundlagen();
		grundlagenErhebungsjahr.setJahr(lastenausgleich.getJahr());
		persistence.persist(grundlagenErhebungsjahr);
	}

	@SuppressWarnings("PMD.CloseResource")
	public List<LastenausgleichGrundlagen> getAll() {
		EntityManager entityManager = persistence.getEntityManager();
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<LastenausgleichGrundlagen> cq = cb.createQuery(
			LastenausgleichGrundlagen.class
		);
		Root<LastenausgleichGrundlagen> rootEntry = cq.from(
			LastenausgleichGrundlagen.class
		);
		cq.select(rootEntry);
		TypedQuery<LastenausgleichGrundlagen> query = entityManager.createQuery(
			cq
		);
		return query.getResultList();
	}
}
