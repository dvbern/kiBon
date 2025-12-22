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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.dto.filter.suchfilter.lucene.IndexedEBEGUFieldName;
import ch.dvbern.ebegu.dto.filter.suchfilter.lucene.QuickSearchResultDTO;
import ch.dvbern.ebegu.dto.filter.suchfilter.lucene.SearchEntityType;
import ch.dvbern.ebegu.dto.filter.suchfilter.lucene.SearchFilter;
import ch.dvbern.ebegu.dto.filter.suchfilter.lucene.SearchResultEntryDTO;
import ch.dvbern.ebegu.dto.filter.suchfilter.lucene.Searchable;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.mapper.orm.Search;

import static ch.dvbern.ebegu.dto.filter.suchfilter.lucene.IndexedEBEGUFieldName.DOSSIER_FALL_MANDANT;
import static ch.dvbern.ebegu.dto.filter.suchfilter.lucene.IndexedEBEGUFieldName.GESUCH_FALL_MANDANT;
import static ch.dvbern.ebegu.dto.filter.suchfilter.lucene.IndexedEBEGUFieldName.KIND_FALL_MANDANT;

@Slf4j
@Stateless
public class SearchIndexServiceBean implements SearchIndexService {

	@Inject
	private PrincipalBean principalBean;

	@Nonnull
	private static final List<SearchFilter> SEARCH_FILTER_FOR_ALL_ENTITIES =
		Arrays.stream(SearchEntityType.values())
			.filter(SearchEntityType::isGlobalSearch)
			.map(SearchFilter::new)
			.toList();

	@Nonnull
	private static final List<SearchFilter> SEARCH_FILTER_FOR_ALL_ENTITIES_WITH_LIMIT =
		Arrays.stream(SearchEntityType.values())
			.filter(SearchEntityType::isGlobalSearch)
			.map(
				searchEntityType -> new SearchFilter(
					searchEntityType,
					Constants.MAX_LUCENE_QUICKSEARCH_RESULTS
				)
			)
			.toList();

	private static final String WILDCARD = "*";

	@Inject
	private Persistence persistence;

	@Nonnull
	@Override
	public QuickSearchResultDTO search(
		@Nonnull String searchText,
		@Nonnull List<SearchFilter> filters
	) {
		Objects.requireNonNull(searchText, "searchText must be set");
		Objects.requireNonNull(filters, "filters must be set");
		QuickSearchResultDTO result = new QuickSearchResultDTO();
		Objects.requireNonNull(filters);
		for (SearchFilter filter : filters) {
			QuickSearchResultDTO subResult = searchInSingleIndex(
				split(searchText),
				filter
			);
			result.addSubResult(subResult);
		}
		return result;
	}

	private static List<String> split(String searchText) {
		return List.of(searchText.split("\\s"));
	}

	/**
	 * sucht im durch den SearchFilter spezifizierten Index nach dem
	 * searchText.
	 * Es wird nicht laaenger als 500ms
	 * gesucht.
	 */
	@Nonnull
	private QuickSearchResultDTO searchInSingleIndex(
		@Nonnull List<String> searchText,
		@Nonnull SearchFilter filter
	) {
		QuickSearchResultDTO result = new QuickSearchResultDTO();
		SearchResult<Searchable> query = buildLuceneQuery(searchText, filter);

		List<SearchResultEntryDTO> searchResultEntryDTOS =
			SearchResultEntryDTO.convertSearchResult(filter, query.hits());
		result.getResultEntities().addAll(searchResultEntryDTOS);
		result.setNumberOfResults(
			Math.toIntExact(
				query.total()
					.hitCount()
			)
		);
		return result;
	}

	@Override
	public QuickSearchResultDTO quicksearch(
		String searchStringParam,
		boolean limitResult
	) {

		List<SearchFilter> filterToUse =
			limitResult ?
				SEARCH_FILTER_FOR_ALL_ENTITIES_WITH_LIMIT :
				SEARCH_FILTER_FOR_ALL_ENTITIES;
		return this.search(searchStringParam, filterToUse);
	}

	@SuppressWarnings("PMD.CloseResource")
	@Nonnull
	private SearchResult<Searchable> buildLuceneQuery(
		@Nonnull List<String> searchTermList,
		@Nonnull SearchFilter filter
	) {
		Class<Searchable> entityClass =
			filter.getSearchEntityType().getEntityClass();
		Objects.requireNonNull(filter.getSearchEntityType());
		EntityManager em = persistence.getEntityManager();

		var query = Search
			.session(em)
			.search(entityClass)
			.where((searchPredicateFactory, root) -> {

				String mandantField =
					getMandantField(filter.getSearchEntityType());
				if (mandantField != null) {
					root.add(
						searchPredicateFactory.match()
							.field(mandantField)
							.matching(
								principalBean.getMandant()
									.getMandantIdentifier()
							)
					);

				}

				searchTermList.stream()
					.map(
						searchTerm -> createTermQuery(
							searchTerm,
							filter,
							searchPredicateFactory
						)
					)
					.forEach(root::add);

			})
			.toQuery();

		return query.fetch(filter.getMaxResults());
	}

	private static String getMandantField(SearchEntityType searchEntityType) {
		String indexedFieldName = null;

		if (searchEntityType == SearchEntityType.GESUCH
			|| searchEntityType == SearchEntityType.DOSSIER) {
			indexedFieldName = (searchEntityType == SearchEntityType.GESUCH) ?
				GESUCH_FALL_MANDANT.getIndexedFieldName() :
				DOSSIER_FALL_MANDANT.getIndexedFieldName();
		} else if (searchEntityType == SearchEntityType.KIND_CONTAINER) {
			indexedFieldName = KIND_FALL_MANDANT.getIndexedFieldName();
		}
		return indexedFieldName;
	}

	/**
	 * creats a 'subquery' for the given search term and returns it.
	 */
	private SearchPredicate createTermQuery(
		String currSearchTerm,
		SearchFilter filter,
		SearchPredicateFactory searchPredicateFactory
	) {
		String[] array = Arrays.stream(filter.getFieldsToSearch())
			.map(IndexedEBEGUFieldName::getIndexedFieldName)
			.toArray(String[]::new);

		return searchPredicateFactory.simpleQueryString()
			.fields(array)
			.matching(
				currSearchTerm.endsWith(WILDCARD) ?
					currSearchTerm :
					currSearchTerm.concat(WILDCARD)
			)
			.toPredicate();
	}

}
