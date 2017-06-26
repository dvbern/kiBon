/*
 * Copyright (c) 2014 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschuetzt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulaessig. Dies gilt
 * insbesondere fuer Vervielfaeltigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht uebergeben ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

package ch.dvbern.ebegu.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.ApplicationProperty;
import ch.dvbern.ebegu.entities.ApplicationProperty_;
import ch.dvbern.ebegu.enums.ApplicationPropertyKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Service fuer ApplicationProperty
 */
@Stateless
@Local(ApplicationPropertyService.class)
@PermitAll
public class ApplicationPropertyServiceBean extends AbstractBaseService implements ApplicationPropertyService {

	private static final Logger LOG = LoggerFactory.getLogger(ApplicationPropertyServiceBean.class.getSimpleName());

	@Inject
	private Persistence<AbstractEntity> persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;
	private static final String NAME_MISSING_MSG = "name muss gesetzt sein";


	@Nonnull
	@Override
	@RolesAllowed({ADMIN, SUPER_ADMIN})
	public ApplicationProperty  saveOrUpdateApplicationProperty(@Nonnull final ApplicationPropertyKey key, @Nonnull final String value) {
		Validate.notNull(key);
		Validate.notNull(value);
		Optional<ApplicationProperty> property = readApplicationProperty(key);
		if (property.isPresent()) {
			property.get().setValue(value);
			return persistence.merge(property.get());
		} else {
			return persistence.persist(new ApplicationProperty(key, value));
		}
	}

	@Nonnull
	@Override
	@PermitAll
	public Optional<ApplicationProperty> readApplicationProperty(@Nonnull final ApplicationPropertyKey key) {
		return criteriaQueryHelper.getEntityByUniqueAttribute(ApplicationProperty.class, key, ApplicationProperty_.name);
	}

	@Override
	@PermitAll
	public Optional<ApplicationProperty> readApplicationProperty(String keyParam) {
		try {
			ApplicationPropertyKey keyToSearch = Enum.valueOf(ApplicationPropertyKey.class, keyParam);
			return readApplicationProperty(keyToSearch);
		} catch (IllegalArgumentException e) {
			LOG.warn("Property not found {}", keyParam, e);
			return Optional.empty();
		}
	}

	@Nonnull
	@Override
	@RolesAllowed({ADMIN, SUPER_ADMIN, REVISOR})
	public List<ApplicationProperty> getAllApplicationProperties() {
		return new ArrayList<>(criteriaQueryHelper.getAll(ApplicationProperty.class));
	}


	@Override
	@RolesAllowed({ADMIN, SUPER_ADMIN})
	public void removeApplicationProperty(@Nonnull ApplicationPropertyKey key) {
		Validate.notNull(key);
		Optional<ApplicationProperty> propertyToRemove = readApplicationProperty(key);
		propertyToRemove.orElseThrow(() -> new EbeguEntityNotFoundException("removeApplicationProperty", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, key));
		propertyToRemove.ifPresent(applicationProperty -> persistence.remove(applicationProperty));

	}

	@Override
	@Nullable
	@PermitAll
	public String findApplicationPropertyAsString(@Nonnull ApplicationPropertyKey name) {
		Objects.requireNonNull(name, NAME_MISSING_MSG);
		Optional<ApplicationProperty> property = criteriaQueryHelper.getEntityByUniqueAttribute(ApplicationProperty.class, name, ApplicationProperty_.name);
		return property.map(ApplicationProperty::getValue).orElse(null);
	}

	@Override
	@Nullable
	@PermitAll
	public BigDecimal findApplicationPropertyAsBigDecimal(@Nonnull ApplicationPropertyKey name) {
		Objects.requireNonNull(name, NAME_MISSING_MSG);
		String valueAsString = findApplicationPropertyAsString(name);
		if (valueAsString != null) {
			return new BigDecimal(valueAsString);
		}
		return null;
	}

	@Override
	@Nullable
	@PermitAll
	public Integer findApplicationPropertyAsInteger(@Nonnull ApplicationPropertyKey name) {
		Objects.requireNonNull(name, NAME_MISSING_MSG);
		String valueAsString = findApplicationPropertyAsString(name);
		if (valueAsString != null) {
			return Integer.valueOf(valueAsString);
		}
		return null;
	}

	@Override
	@Nullable
	@PermitAll
	public Boolean findApplicationPropertyAsBoolean(@Nonnull ApplicationPropertyKey name) {
		Objects.requireNonNull(name, NAME_MISSING_MSG);
		String valueAsString = findApplicationPropertyAsString(name);
		if (valueAsString != null) {
			return Boolean.valueOf(valueAsString);
		}
		return null;
	}

	@Override
	@Nonnull
	@PermitAll
	public Boolean findApplicationPropertyAsBoolean(@Nonnull ApplicationPropertyKey name, boolean defaultValue) {
		Boolean property = findApplicationPropertyAsBoolean(name);
		if (property == null) {
			return defaultValue;
		}
		return property;
	}
}
