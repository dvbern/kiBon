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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.mailing.OutboxMail;
import ch.dvbern.ebegu.mailing.OutboxMailService;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allgemeine Mailing-Funktionalität
 */
public abstract class AbstractMailServiceBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(
		AbstractMailServiceBean.class
	);

	private static final Pattern PATTERN = Pattern.compile("(\r\n|\n)");
	private static final Pattern REGEX = Pattern.compile("\\r?\\n");

	@Inject
	private OutboxMailService outboxMailService;

	public void toOutboxMail(
		@Nonnull String subject,
		@Nonnull String messageBody,
		@Nonnull String mailadress,
		@Nonnull MandantIdentifier mandantIdentifier
	) {

		String processedMessageBody = messageBody;

		if (!messageBody.trim()
			.toLowerCase(new Locale("de", "CH"))
			.contains("<html")) {
			processedMessageBody = PATTERN.matcher(messageBody)
				.replaceAll("<br>");
		}

		outboxMailService.saveOutboxMail(
			new OutboxMail(
				subject,
				processedMessageBody,
				mailadress,
				mandantIdentifier
			)
		);

	}

	protected void toOutboxMail(
		@Nonnull final String messageBody,
		@Nonnull final String mailadress,
		@Nonnull final MandantIdentifier mandantIdentifier
	) {
		outboxMailService.saveOutboxMail(
			new OutboxMail(
				extractSubjectFromMessageBody(messageBody),
				extractContentFromMessageBody(messageBody),
				mailadress,
				mandantIdentifier
			)
		);
	}

	private String extractSubjectFromMessageBody(String messageBody) {
		String decodedSubject = messageBody.substring(
			messageBody.indexOf("Subject: ") + 9,
			messageBody.indexOf("Content-Type")
		);
		return decodeMixedBase64String(decodedSubject);
	}

	private String extractContentFromMessageBody(String messageBody) {
		try {
			String[] lines = REGEX.split(messageBody);
			StringBuilder contentBuilder = new StringBuilder();

			boolean insideHeaders = true;
			for (String line : lines) {
				if (insideHeaders && line.trim().isEmpty()) {
					insideHeaders = false;
					continue;
				}

				// Skip specific headers if we're still in header section
				if (insideHeaders
					&& (line.startsWith("From:")
						||
						line.startsWith("To:")
						||
						line.startsWith("Subject:")
						||
						line.startsWith("Content-Type:"))) {
					continue;
				}

				contentBuilder.append(line).append('\n');
			}

			return contentBuilder.toString().trim();

		} catch (Exception e) {
			LOGGER.error("Failed to parse message body", e);
			return messageBody; // fallback: return original if something goes wrong
		}
	}

	private static String decodeMixedBase64String(String mixedString) {
		StringBuilder decodedBuilder = new StringBuilder();
		int start = 0; // Start index for the non-encoded part

		while (start < mixedString.length()) {
			int startIndex = mixedString.indexOf("=?", start);
			if (startIndex == -1) {
				// No more encoded parts, append the rest of the string and break
				decodedBuilder.append(mixedString.substring(start));
				return decodedBuilder.toString();
			}
			// Append non-encoded part before the encoded section
			if (startIndex != start) {
				decodedBuilder.append(mixedString.substring(start, startIndex));
			}
			int endIndex = mixedString.indexOf("?=", startIndex) + 2;
			if (endIndex == 1) { // No closing tag found, break to avoid an infinite loop
				return decodedBuilder.toString();
			}
			// Extract the encoded part without the MIME and encoding prefix and suffix
			String encodedPart = mixedString.substring(
				startIndex + 10,
				endIndex - 2
			);
			// Decode and append the encoded part
			byte[] decodedBytes = Base64.getDecoder().decode(encodedPart);
			decodedBuilder.append(
				new String(decodedBytes, StandardCharsets.UTF_8)
			);

			// Move start index forward
			start = endIndex;
		}

		return decodedBuilder.toString();
	}

}
