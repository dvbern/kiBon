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

package ch.dvbern.ebegu.persistence;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;

/*
 * copied from ch.dvbern.lib.cdipersistence.Persistence and adapted to jakarta namespace.
 */
public interface Persistence {

	/**
	 * Make an instance managed and persistent.
	 *
	 * @param entity entity instance
	 * @return the persisted entity
	 * @throws jakarta.persistence.EntityExistsException if the entity already exists.
	 * (If the entity already exists, the {@code EntityExistsException} may
	 * be thrown when the persist operation is invoked, or the
	 * {@code EntityExistsException} or another {@code PersistenceException} may be
	 * thrown at flush or commit time.)
	 * @throws IllegalArgumentException if the instance is not an
	 * entity
	 */
	<T> T persist(T entity);

	/**
	 * Merge the state of the given entity into the
	 * current persistence context.
	 *
	 * @param entity entity instance
	 * @return the managed instance that the state was merged to
	 * @throws IllegalArgumentException if instance is not an
	 * entity or is a removed entity
	 * @throws jakarta.persistence.TransactionRequiredException if invoked on a
	 * container-managed entity manager of type
	 * {@code PersistenceContextType.TRANSACTION} and there is
	 * no transaction
	 */
	<T> T merge(T entity);

	/**
	 * Remove the entity instance.
	 *
	 * @param entity entity instance
	 * @throws IllegalArgumentException if the instance is not an
	 * entity or is a detached entity
	 * @throws jakarta.persistence.TransactionRequiredException if invoked on a
	 * container-managed entity manager of type
	 * {@code PersistenceContextType.TRANSACTION} and there is
	 * no transaction
	 */
	<T> void remove(T entity);

	/**
	 * Find by primary key.
	 * Search for an entity of the specified class and primary key.
	 * If the entity instance is contained in the persistence context,
	 * it is returned from there.
	 *
	 * @param entityClass entity class
	 * @param primaryKey primary key
	 * @return the found entity instance or null if the entity does
	 * not exist
	 * @throws IllegalArgumentException if the first argument does
	 * not denote an entity type or the second argument is
	 * is not a valid type for that entitys primary key or
	 * is null
	 */
	<T> T find(Class<T> entityClass, String primaryKey);

	/**
	 * Get an instance, whose state may be lazily fetched.
	 * If the requested instance does not exist in the database,
	 * the {@code EntityNotFoundException} is thrown when the instance
	 * state is first accessed. (The persistence provider runtime is
	 * permitted to throw the {@code EntityNotFoundException} when
	 * {@code getReference} is called.)
	 * The application should not expect that the instance state will
	 * be available upon detachment, unless it was accessed by the
	 * application while the entity manager was open.
	 *
	 * @param entityClass entity class
	 * @param primaryKey primary key
	 * @return the found entity instance
	 * @throws IllegalArgumentException if the first argument does
	 * not denote an entity type or the second argument is
	 * not a valid type for that entitys primary key or
	 * is null
	 * @throws jakarta.persistence.EntityNotFoundException if the entity state
	 * cannot be accessed
	 */
	<T> T getReference(Class<T> entityClass, String primaryKey);

	/**
	 * Remove by primary Key.
	 */
	<T> void remove(Class<T> entityClass, String primaryKey);

	/**
	 * Return an instance of {@code CriteriaBuilder} for the creation of
	 * {@code CriteriaQuery} objects.
	 *
	 * @return CriteriaBuilder instance
	 * @throws IllegalStateException if the entity manager has
	 * been closed
	 * @since Java Persistence 2.0
	 */
	CriteriaBuilder getCriteriaBuilder();

	/**
	 * @param query the query to execute
	 * @param <T> Type of Entity to return
	 * @return resultlist
	 */
	<T> List<T> getCriteriaResults(CriteriaQuery<T> query);

	/**
	 * @param query the query to execute
	 * @param <T> Type of Entity to return
	 * @return resultlist with maxResults entries
	 */
	<T> List<T> getCriteriaResults(
		final CriteriaQuery<T> query,
		int maxResults
	);

	/**
	 * @param query the query to execute
	 * @param <T> Type of Entity to return
	 * @return single result, <tt>null</tt> if there is no result, or
	 * throws an exception if more than one result is found.
	 * @throws jakarta.persistence.NonUniqueResultException if more than one result
	 * @throws IllegalStateException if called for a Java Persistence query language UPDATE or DELETE statement
	 */
	<T> T getCriteriaSingleResult(CriteriaQuery<T> query);

	/**
	 * Returns the EntityManager
	 */
	EntityManager getEntityManager();
}
