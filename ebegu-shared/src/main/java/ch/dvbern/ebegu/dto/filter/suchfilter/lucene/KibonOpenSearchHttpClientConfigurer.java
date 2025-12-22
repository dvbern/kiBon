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

package ch.dvbern.ebegu.dto.filter.suchfilter.lucene;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import org.hibernate.search.backend.elasticsearch.client.ElasticsearchHttpClientConfigurationContext;
import org.hibernate.search.backend.elasticsearch.client.ElasticsearchHttpClientConfigurer;

@ApplicationScoped
@Named("kibonOpenSearchHttpClientConfigurer")
public class KibonOpenSearchHttpClientConfigurer
	implements
	ElasticsearchHttpClientConfigurer {
	@Override
	public void configure(ElasticsearchHttpClientConfigurationContext context) {
		try {
			context.clientBuilder()
				.setSSLContext(createSSLContext())
				.setSSLHostnameVerifier((hostname, session) -> true);
		} catch (CertificateException | KeyStoreException | IOException |
				 NoSuchAlgorithmException | KeyManagementException e) {
			throw new EbeguRuntimeException(
				"configure",
				"Unable to create SSL context for OpenSearch client",
				e
			);
		}
	}

	private SSLContext createSSLContext()
		throws CertificateException, KeyStoreException, IOException,
		NoSuchAlgorithmException, KeyManagementException {
		var certificate = CertificateFactory.getInstance("X.509")
			.generateCertificate(
				this.getClass()
					.getClassLoader()
					.getResourceAsStream("opensearch-demo-cert.pem")
			);

		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(null, null);
		keyStore.setCertificateEntry("server", certificate);

		TrustManagerFactory trustManagerFactory =
			TrustManagerFactory.getInstance(
				TrustManagerFactory.getDefaultAlgorithm()
			);
		trustManagerFactory.init(keyStore);

		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
		return sslContext;
	}
}
