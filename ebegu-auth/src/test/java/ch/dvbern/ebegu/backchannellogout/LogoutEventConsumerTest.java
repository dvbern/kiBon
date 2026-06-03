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

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletContextEvent;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.objectmapping.json.ObjectMapperFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogoutEventConsumerTest {

	@Mock
	SessionRegistry sessionRegistry;

	@Mock
	StringKafkaConsumerFactory kafkaConsumerFactory;

	@Mock
	ObjectMapperFactory objectMapperFactory;

	@Mock
	EbeguConfiguration ebeguConfiguration;

	@InjectMocks
	LogoutEventConsumer eventConsumer;

	KafkaConsumer<String, String> kafkaConsumer;

	ServletContextEvent servletContextEvent;

	ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() {

		MockitoAnnotations.openMocks(this);

		kafkaConsumer = Mockito.mock(KafkaConsumer.class);
		Mockito.when(kafkaConsumerFactory.createKafkaConsumer())
			.thenReturn(kafkaConsumer);

		servletContextEvent = Mockito.mock(ServletContextEvent.class);

		Mockito.when(objectMapperFactory.create()).thenReturn(objectMapper);

		eventConsumer.initialize();
	}

	@Test
	void consumeLoop() throws JsonProcessingException {
		String subject = "abc";

		LogoutEvent event = LogoutEvent.builder()
			.subject(subject)
			.sessionId("123")
			.build();

		TopicPartition topicPartition = Mockito.mock(TopicPartition.class);
		ConsumerRecord<String, String> rec1 = Mockito.mock(
			ConsumerRecord.class
		);
		ConsumerRecord<String, String> rec2 = Mockito.mock(
			ConsumerRecord.class
		);
		String json = objectMapper.writeValueAsString(event);
		Mockito.when(rec1.value()).thenReturn(json);
		Mockito.when(rec2.value()).thenReturn(json);

		List<ConsumerRecord<String, String>> recordList = Arrays.asList(
			rec1,
			rec2
		);
		Map<TopicPartition, List<ConsumerRecord<String, String>>> recordMap =
			new HashMap<>();
		recordMap.put(topicPartition, recordList);

		ConsumerRecords<String, String> records = new ConsumerRecords<>(
			recordMap
		);

		Mockito.when(kafkaConsumer.poll(ArgumentMatchers.any(Duration.class)))
			.thenReturn(records);
		eventConsumer.consumeAndHandleLogoutEvent();

		Mockito.verify(sessionRegistry, Mockito.times(2))
			.logoutBySubject(subject);
	}

	@Test
	void cleanup() {
		eventConsumer.consumeAndHandleLogoutEvent();
		eventConsumer.cleanup();
		Mockito.verify(kafkaConsumer).close();
		Assertions.assertFalse(eventConsumer.isRunning());
	}
}
