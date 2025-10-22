/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.util.health;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

/**
 * Checks if there is a connection to Kafka
 *
 * @see
 * <a
 * href="https://github.com/quarkusio/quarkus/blob/master/extensions/kafka-client/runtime/src/main/java/io/quarkus/kafka/client/health/KafkaHealthCheck.java">Quarkus
 * KafkaHealthCheck</a>
 */
@ApplicationScoped
@Readiness
public class DVKafkaHealthCheck implements HealthCheck {

	private static final String NAME = "dv-kafka-connection-check";
	private static final String REASON_KEY = "reason";

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	private AdminClient client = null;

	@PostConstruct
	public void init() {
		if (ebeguConfiguration.getKafkaURL().isEmpty()) {
			return;
		}

		Map<String, Object> conf = new HashMap<>();
		conf.put(
			AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
			ebeguConfiguration.getKafkaURL().orElse("")
		);
		conf.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000");
		client = AdminClient.create(conf);
	}

	@PreDestroy
	public void stop() {
		if (client != null) {
			client.close();
		}
	}

	@Override
	@SuppressFBWarnings(value = "REC_CATCH_EXCEPTION",
		justification = "Health Check reports reason")
	public HealthCheckResponse call() {
		if (ebeguConfiguration.getKafkaURL().isEmpty()) {
			return HealthCheckResponse.named(NAME)
				.down()
				.withData(REASON_KEY, "Bootstrap URL not configured")
				.build();
		}

		try {
			String nodes = client.describeCluster()
				.nodes()
				.get()
				.stream()
				.map(node -> node.host() + ':' + node.port())
				.collect(Collectors.joining(","));
			return HealthCheckResponse.named(NAME)
				.up()
				.withData("nodes", nodes)
				.build();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return HealthCheckResponse.named(NAME)
				.down()
				.withData(REASON_KEY, e.getMessage())
				.build();
		} catch (Exception e) {
			return HealthCheckResponse.named(NAME)
				.down()
				.withData(REASON_KEY, e.getMessage())
				.build();
		}
	}
}
