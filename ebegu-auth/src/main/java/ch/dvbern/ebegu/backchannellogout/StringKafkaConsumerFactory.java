/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.backchannellogout;

import java.util.Properties;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

@ApplicationScoped
public class StringKafkaConsumerFactory {

	@Inject
	private EbeguConfiguration configurationProvider;

	/**
	 * @return A appropriate instance of {@link KafkaConsumer} for consuming string messages from a Kafka event queue.
	 */
	public KafkaConsumer<String, String> createKafkaConsumer() {
		if (configurationProvider.getKibonIameventqueueHost() == null
			|| configurationProvider.getKibonIameventqueueHost().isEmpty()) {
			return null;
		}
		return new KafkaConsumer<>(getProperties());
	}

	private Properties getProperties() {

		Properties props = new Properties();
		props.setProperty(
			ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
			configurationProvider.getKibonIameventqueueHost()
		);
		props.setProperty(
			ConsumerConfig.GROUP_ID_CONFIG,
			configurationProvider.getKibonIameventqueueConsumerGroup()
		);
		props.setProperty(
			ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
			StringDeserializer.class.getName()
		);
		props.setProperty(
			ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
			StringDeserializer.class.getName()
		);
		props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

		return props;
	}
}
