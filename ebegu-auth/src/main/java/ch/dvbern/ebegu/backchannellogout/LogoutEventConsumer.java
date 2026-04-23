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
import java.util.Collections;
import java.util.concurrent.ExecutorService;

import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.security.RunAs;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.objectmapping.json.ObjectMapperFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.jboss.ejb3.annotation.RunAsPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines a listener that consumes the queue that is used by Keycloak for distributing user logout events. This queue
 * is named
 * Idendity Access Management (IAM) queue in general.
 */
//@WebListener
@Singleton
@RunAs(UserRoleName.SUPER_ADMIN)
@RunAsPrincipal(PrincipalBean.KIBON_SERVICE_ACCOUNT)
public class LogoutEventConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(
		LogoutEventConsumer.class
	);

	/**
	 * Provides configuration properties required to access the IAM queue.
	 */
	@Inject
	private EbeguConfiguration ebeguConfiguration;

	/**
	 * Used to create an appropriate {@link ExecutorService} which is used to execute the consume loop
	 * asychronously.
	 */
	@Inject
	private ExecutorServiceFactory executorServiceFactory;

	/**
	 * Used to execute the consume loop asynchronously.
	 */
	private ExecutorService executorService = null;

	/**
	 * Terminates this listener if set to false. For a clean termination of this serice, use
	 * {@link LogoutEventConsumer#cleanup()}.
	 */
	@Getter
	private volatile boolean running = true;

	/**
	 * Used to create an appropriate consumer for consuming logout events.
	 */
	@Inject
	private StringKafkaConsumerFactory kafkaConsumerFactory;

	/**
	 * Consumer for logout events.
	 */
	private KafkaConsumer<String, String> consumer = null;

	/**
	 * Used to create an appropriate object mapper for parsing logout events consumed from the IAM queue.
	 */
	@Inject
	private ObjectMapperFactory objectMapperFactory;

	/**
	 * Used to parse the JSON body of user logout events. Those bodies should conform to the {@link LogoutEvent} type.
	 */
	private ObjectMapper objectMapper = null;

	/**
	 * Provides access to all user sessions managed by this host, and offers methods to identify and terminate those
	 * sessions.
	 */
	@Inject
	private SessionRegistry sessionRegistry;

	@PostConstruct
	public void postConstruct() {
		initialize();
	}

	private void initialize() {

		assert objectMapperFactory != null;
		this.objectMapper = objectMapperFactory.create();
		assert executorServiceFactory != null;
		executorService = executorServiceFactory.createExecutorService();

		assert kafkaConsumerFactory != null;
		consumer = kafkaConsumerFactory.createKafkaConsumer();
		assert ebeguConfiguration != null;
		consumer.subscribe(
			Collections.singletonList(
				ebeguConfiguration.getKibonIameventqueueTopicLogout()
			)
		);
	}

	/**
	 * Periodically checks for and consumes logout events from the IAM event queue. Maps logout events to user sessions
	 * and
	 * terminates all matching sessions.
	 */
	@Schedule(info = "consume logout events",
		second = "*/3",
		minute = "*",
		hour = "*",
		persistent = false)
	public void consumeAndHandleLogoutEvent() {
		ConsumerRecords<String, String> records = consumer.poll(
			Duration.ofMillis(1000)
		);
		for (ConsumerRecord<String, String> rec : records) {
			handleLogoutEvent(rec.value());
		}
	}

	private void handleLogoutEvent(String jsonValue) {

		String subject = extractSubject(jsonValue);

		if (subject != null) {
			LOGGER.debug("Logout event received for user: {}", subject);
			sessionRegistry.logoutBySubject(subject);
		}
	}

	@Nullable
	private String extractSubject(String json) {

		try {
			LogoutEvent logoutEvent = objectMapper.readValue(
				json,
				LogoutEvent.class
			);
			return logoutEvent.getSubject();
		} catch (JsonProcessingException e) {
			LOGGER.error("Could not parse Logout-Event.", e);
		}
		return null;
	}

	/**
	 * Terminates this service and frees the resources taken.
	 */
	@PreDestroy
	public void cleanup() {
		running = false;
		if (consumer != null) {
			consumer.wakeup();
		}
		if (executorService != null) {
			executorService.shutdown();
		}
	}
}
