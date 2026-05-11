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
 */

package ch.dvbern.ebegu.outbox.statistik;

import java.util.Properties;

import jakarta.annotation.PreDestroy;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.dto.statistik.KinderStatistikParameterDto;
import ch.dvbern.ebegu.dto.statistik.LastenausgleichBGStatistikParameterDto;
import ch.dvbern.ebegu.dto.statistik.MitarbeitendeStatistikParameterDto;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class KafkaStatistikProducer {

	private static final Logger LOG = LoggerFactory.getLogger(
		KafkaStatistikProducer.class
	);

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	private Producer<String, String> producer;
	private final ObjectMapper mapper =
		new ObjectMapper().registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	private void initIfNeeded() {
		if (ebeguConfiguration.getKafkaStatistikURL().isEmpty()
			|| (ebeguConfiguration.getKafkaStatistikURL().isPresent()
				&& StringUtils.isEmpty(
					ebeguConfiguration.getKafkaStatistikURL().get()
				))) {
			LOG.error(
				"Kafka Statistik URL not set, Statistik cannot be generated."
			);
			return;
		}
		if (producer == null) {
			Properties props = new Properties();
			props.put(
				ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
				ebeguConfiguration.getKafkaStatistikURL().get()
			);
			props.put(
				ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
				StringSerializer.class.getName()
			);
			props.put(
				ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
				StringSerializer.class.getName()
			);
			props.put(ProducerConfig.ACKS_CONFIG, "all");
			producer = new KafkaProducer<>(props);
		}
	}

	public void sendKinderStatistik(KinderStatistikParameterDto dto) {
		initIfNeeded();
		try {
			String json = mapper.writeValueAsString(dto);
			sendStatistikToKafka("kinderstatistik", json);

		} catch (JsonProcessingException e) {
			throw new EbeguRuntimeException(
				"sendKinderStatistik",
				"Could not serialize DTO",
				e
			);
		}
	}

	public void sendMitarbeitendeStatistik(
		MitarbeitendeStatistikParameterDto dto
	) {
		initIfNeeded();
		try {
			String json = mapper.writeValueAsString(dto);
			sendStatistikToKafka("mitarbeitendestatistik", json);

		} catch (JsonProcessingException e) {
			throw new EbeguRuntimeException(
				"sendMitarbeitendeStatistik",
				"Could not serialize DTO",
				e
			);
		}
	}

	public void sendLastenausgleichBGStatistik(
		LastenausgleichBGStatistikParameterDto dto
	) {
		initIfNeeded();
		try {
			String json = mapper.writeValueAsString(dto);
			sendStatistikToKafka("lastenausgleichbgstatistik", json);

		} catch (JsonProcessingException e) {
			throw new EbeguRuntimeException(
				"sendLastenausgleichBGStatistik",
				"Could not serialize DTO",
				e
			);
		}
	}

	private void sendStatistikToKafka(String topic, String json) {
		ProducerRecord<String, String> record =
			new ProducerRecord<>(topic, json);

		producer.send(
			record,
			(metadata, exception) -> {
				if (exception != null) {
					LOG.error("Failed to send: {}", exception.getMessage());
					throw new EbeguRuntimeException(
						topic,
						"Could not send message to Kafka topic",
						exception
					);
				} else {
					LOG.info("Sent to topic {}", metadata.topic());
				}
			}
		);
	}

	@PreDestroy
	void destroy() {
		producer.close();
	}
}
