/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import ch.dvbern.ebegu.einstellung.ApplicationProperty;
import ch.dvbern.ebegu.einstellung.ApplicationPropertyKey;
import ch.dvbern.ebegu.einstellung.ApplicationPropertyService;
import ch.dvbern.ebegu.einstellung.ApplicationProperty_;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.DemoFeatureTyp;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.DateUtil;
import com.google.common.base.Enums;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service fuer ApplicationProperty
 */
@Stateless
@Local(ApplicationPropertyService.class)
public class ApplicationPropertyServiceBean extends AbstractBaseService
	implements
	ApplicationPropertyService {

	private static final Logger LOG = LoggerFactory.getLogger(
		ApplicationPropertyServiceBean.class.getSimpleName()
	);

	@Inject
	private Persistence persistence;

	@Inject
	private Authorizer authorizer;

	private static final String NAME_MISSING_MSG = "name muss gesetzt sein";

	@Nonnull
	@Override
	public ApplicationProperty saveOrUpdateApplicationProperty(
		@Nonnull final ApplicationPropertyKey key,
		@Nonnull final String value,
		@Nonnull final Mandant mandant
	) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(value);
		Optional<ApplicationProperty> property = readApplicationProperty(
			key,
			mandant
		);
		if (property.isPresent()) {
			authorizer.checkWriteAuthorization(property.get());
			property.get().setValue(value);
			final ApplicationProperty mergedProperty = persistence.merge(
				property.get()
			);
			return mergedProperty;
		}
		final ApplicationProperty newProperty = new ApplicationProperty(
			key,
			value,
			mandant
		);
		authorizer.checkWriteAuthorization(newProperty);
		return persistence.persist(newProperty);
	}

	@Nonnull
	@Override
	public Optional<ApplicationProperty> readApplicationProperty(
		@Nonnull final ApplicationPropertyKey key,
		@Nonnull final Mandant mandant
	) {
		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<ApplicationProperty> query = builder.createQuery(
			ApplicationProperty.class
		);
		final Root<ApplicationProperty> root = query.from(
			ApplicationProperty.class
		);
		Predicate keyPredicate = builder.equal(
			root.get(ApplicationProperty_.name),
			key
		);
		Predicate mandantPredicate = builder.equal(
			root.get(ApplicationProperty_.MANDANT),
			mandant
		);
		query.where(keyPredicate, mandantPredicate);
		try {
			return Optional.ofNullable(
				persistence.getCriteriaSingleResult(query)
			);
		} catch (NoResultException exception) {
			return Optional.empty();
		}
	}

	@Nonnull
	@Override
	public Collection<String> readMimeTypeWhitelist(
		@Nonnull final Mandant mandant
	) {
		//note this is a candidate for caching
		Set<String> allowedTypes = Collections.emptySet();
		final Optional<ApplicationProperty> whitelistVal =
			this.readApplicationProperty(
				ApplicationPropertyKey.UPLOAD_FILETYPES_WHITELIST,
				mandant
			);
		if (whitelistVal.isPresent()
			&& StringUtils.isNotEmpty(whitelistVal.get().getValue())) {
			final String[] values = whitelistVal.get().getValue().split(",");
			allowedTypes = Arrays.stream(values)
				.map(StringUtils::trimToNull)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());

		}
		return allowedTypes;
	}

	@Override
	public Optional<ApplicationProperty> readApplicationProperty(
		String keyParam,
		@Nonnull Mandant mandant
	) {
		try {
			ApplicationPropertyKey keyToSearch = Enum.valueOf(
				ApplicationPropertyKey.class,
				keyParam
			);
			return readApplicationProperty(keyToSearch, mandant);
		} catch (IllegalArgumentException e) {
			LOG.warn("Property not found {}", keyParam, e);
			return Optional.empty();
		}
	}

	@Nonnull
	@Override
	public List<ApplicationProperty> getAllApplicationProperties(
		@Nonnull Mandant mandant
	) {
		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<ApplicationProperty> query = builder.createQuery(
			ApplicationProperty.class
		);
		final Root<ApplicationProperty> root = query.from(
			ApplicationProperty.class
		);
		Predicate mandantPredicate = builder.equal(
			root.get(ApplicationProperty_.MANDANT),
			mandant
		);
		query.where(mandantPredicate);
		return persistence.getCriteriaResults(query);
	}

	@Override
	public void removeApplicationProperty(
		@Nonnull ApplicationPropertyKey key,
		@Nonnull Mandant mandant
	) {
		Objects.requireNonNull(key);
		ApplicationProperty toRemove =
			readApplicationProperty(key, mandant).orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"removeApplicationProperty",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					key
				)
			);
		persistence.remove(toRemove);
	}

	@Override
	@Nullable
	public String findApplicationPropertyAsString(
		@Nonnull ApplicationPropertyKey name,
		@Nonnull Mandant mandant
	) {
		Objects.requireNonNull(name, NAME_MISSING_MSG);
		Optional<ApplicationProperty> property = readApplicationProperty(
			name,
			mandant
		);
		return property.map(ApplicationProperty::getValue).orElse(null);
	}

	@Override
	@Nullable
	public BigDecimal findApplicationPropertyAsBigDecimal(
		@Nonnull ApplicationPropertyKey name,
		@Nonnull Mandant mandant
	) {
		Objects.requireNonNull(name, NAME_MISSING_MSG);
		String valueAsString = findApplicationPropertyAsString(name, mandant);
		if (valueAsString != null) {
			return new BigDecimal(valueAsString);
		}
		return null;
	}

	@Override
	@Nullable
	public Integer findApplicationPropertyAsInteger(
		@Nonnull ApplicationPropertyKey name,
		@Nonnull Mandant mandant
	) {
		Objects.requireNonNull(name, NAME_MISSING_MSG);
		String valueAsString = findApplicationPropertyAsString(name, mandant);
		if (valueAsString != null) {
			return Integer.valueOf(valueAsString);
		}
		return null;
	}

	@Override
	@Nullable
	public Boolean findApplicationPropertyAsBoolean(
		@Nonnull ApplicationPropertyKey name,
		@Nonnull Mandant mandant
	) {
		Objects.requireNonNull(name, NAME_MISSING_MSG);
		String valueAsString = findApplicationPropertyAsString(name, mandant);
		if (valueAsString != null) {
			return Boolean.valueOf(valueAsString);
		}
		return null;
	}

	@Override
	@Nonnull
	public Boolean findApplicationPropertyAsBoolean(
		@Nonnull ApplicationPropertyKey name,
		@Nonnull Mandant mandant,
		boolean defaultValue
	) {
		Boolean property = findApplicationPropertyAsBoolean(name, mandant);
		if (property == null) {
			return defaultValue;
		}
		return property;
	}

	@Override
	@Nonnull
	public LocalDate getStadtBernAsivStartDatum(@Nonnull Mandant mandant) {
		String valueAsString =
			findApplicationPropertyAsString(
				ApplicationPropertyKey.STADT_BERN_ASIV_START_DATUM,
				mandant
			);
		if (valueAsString != null) {
			return DateUtil.parseStringToDate(valueAsString);
		}
		// Default ist 1.1.2021
		return LocalDate.of(2021, Month.JANUARY, 1);
	}

	@Override
	@Nonnull
	public Boolean isStadtBernAsivConfigured(@Nonnull Mandant mandant) {
		return findApplicationPropertyAsBoolean(
			ApplicationPropertyKey.STADT_BERN_ASIV_CONFIGURED,
			mandant,
			false
		);
	}

	@Override
	@Nonnull
	public Boolean isPublishSchnittstelleEventsAktiviert(
		@Nonnull Mandant mandant
	) {
		return findApplicationPropertyAsBoolean(
			ApplicationPropertyKey.SCHNITTSTELLE_EVENTS_AKTIVIERT,
			mandant,
			true
		);
	}

	@Override
	public List<DemoFeatureTyp> getActivatedDemoFeatures(
		@Nonnull Mandant mandant
	) {
		String activatedDemoFeatures =
			findApplicationPropertyAsString(
				ApplicationPropertyKey.ACTIVATED_DEMO_FEATURES,
				mandant
			);

		List<DemoFeatureTyp> activatdDemoFeatures = new ArrayList<>();

		if (activatedDemoFeatures == null) {
			return activatdDemoFeatures;
		}

		return Arrays.stream(activatedDemoFeatures.split(","))
			.map(
				demoFeatureString -> Enums.getIfPresent(
					DemoFeatureTyp.class,
					demoFeatureString.stripLeading().stripTrailing()
				).orNull()
			)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	@Override
	@Nullable
	public LocalDate getSchnittstelleSprachfoerderungAktivAb(
		@Nonnull Mandant mandant
	) {
		String valueAsString =
			findApplicationPropertyAsString(
				ApplicationPropertyKey.SCHNITTSTELLE_SPRACHFOERDERUNG_AKTIV_AB,
				mandant
			);
		if (valueAsString != null) {
			return LocalDate.parse(valueAsString, Constants.SQL_DATE_FORMAT);
		}
		return null;
	}

	@Nullable
	@Override
	public Boolean isAuszahlungAnElternAktiviert(@Nonnull Mandant mandant) {
		return findApplicationPropertyAsBoolean(
			ApplicationPropertyKey.AUSZAHLUNGEN_AN_ELTERN,
			mandant
		);
	}

	@Nonnull
	@Override
	public Boolean isGemeindeKennzahlenAktiviert(@Nonnull Mandant mandant) {
		return findApplicationPropertyAsBoolean(
			ApplicationPropertyKey.GEMEINDE_KENNZAHLEN_AKTIV,
			mandant,
			false
		);
	}

	@Nonnull
	@Override
	public Boolean isReminderGemeindeKennzahlenAktiviert(
		@Nonnull Mandant mandant
	) {
		return findApplicationPropertyAsBoolean(
			ApplicationPropertyKey.GEMEINDE_KENNZAHLEN_REMINDER_ACTIVATED,
			mandant,
			false
		);
	}
}
