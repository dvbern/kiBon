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

package ch.dvbern.ebegu.dto.suchfilter.lucene;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurationContext;
import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurer;

/**
 * Analyzer that maps umlauts etc. to ASCII chars, e.g. é -> e, ä -> a.
 * Because of preserve_original both the transliteration and the original can be found.
 *
 * https://docs.jboss.org/hibernate/search/7.1/reference/en-US/html_single/#backend-elasticsearch-analysis-analyzers
 */
@ApplicationScoped
@Named("kibonElasticsearchAnalyzerConfigurer")
public class KibonElasticsearchAnalyzerConfigurer implements
	ElasticsearchAnalysisConfigurer {

	public static final String KIBON_GERMAN_ANALYZER = "kibon_german";

	@Override
	public void configure(
		ElasticsearchAnalysisConfigurationContext context
	) {
		context.tokenFilter("custom_asciifolding") // https://www.elastic.co/guide/en/elasticsearch/reference/current/analysis-asciifolding-tokenfilter.html
			.type("asciifolding")
			.param("preserve_original", true);

		context.analyzer(KIBON_GERMAN_ANALYZER)
			.custom()
			.tokenizer(
				"standard" // https://www.elastic.co/guide/en/elasticsearch/reference/current/analysis-standard-tokenizer.html
			)
			.tokenFilters(
				"lowercase", // https://www.elastic.co/guide/en/elasticsearch/reference/current/analysis-lowercase-tokenfilter.html
				"custom_asciifolding"
			);

	}
}
