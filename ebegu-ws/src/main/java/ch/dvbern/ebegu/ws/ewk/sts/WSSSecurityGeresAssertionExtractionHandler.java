/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.ws.ewk.sts;

import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.ws.sts.SAMLAuthenticationUtil;
import ch.dvbern.ebegu.ws.sts.STSAssertionExtractionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Da die Assertion 'EXAKT' so weitergegeben muss wie wir sie bekommen haben extrahiere ich sie direkt aus der SOAP
 * Mesage.
 *
 * Wenn wir sie zuerst in ein Assertion Object in Java Transformieren kann es beim marshallen naemlich sonst passieren,
 * dass
 * die Namespace-Prefixes nicht mehr gleich lauten. Da diese Prefixes wegen InclusiveNamespaces wohl in der Signatur
 * mitberuecksichtigt werden darf am namespace-prefix auf der Signatur sicher nichts aendern
 */
@Stateless
@LocalBean
public class WSSSecurityGeresAssertionExtractionHandler implements
	SOAPHandler<SOAPMessageContext> {
	private static final Logger LOGGER = LoggerFactory.getLogger(
		WSSSecurityGeresAssertionExtractionHandler.class.getSimpleName()
	);

	@Inject
	private STSGeresAssertionManagerBean stsAssertionManager;

	@Override
	public boolean handleMessage(SOAPMessageContext context) {
		Boolean outboundProperty =
			(Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		try {
			if (!outboundProperty) {// if not outbound assume inbound
				SOAPMessage response = context.getMessage();
				if (response.getSOAPBody().hasFault()) {
					throw new EbeguRuntimeException(
						"extractAssertionHandler",
						" SOAPMessage from STS did not contain the expected Assertion. Instead there"
							+ " was a fault. Error obtaining SAML Assertion for GERES: "
							+ response.getSOAPBody()
								.getFault()
								.getTextContent()
					);
				}
				//extract the assertion
				final STSAssertionExtractionResult stsWebServiceResult =
					SAMLAuthenticationUtil
						.extractSamlAssertionFromIssueResponse(
							response
						);
				LOGGER.trace("Assertion was successfully extracted");
				this.stsAssertionManager.handleUpdatedAssertion(
					stsWebServiceResult
				);
			}
		} catch (SOAPException ex) {
			throw new EbeguRuntimeException(
				"extractAssertionHandler",
				String.format(
					"Response does not seem to be a valid SOAP Mesage. Cannot extract "
						+ "Assertion. %s",
					ex.getMessage()
				),
				ex
			);

		}

		return true;
	}

	@Override
	public Set<QName> getHeaders() {
		return new TreeSet();
	}

	@Override
	public boolean handleFault(SOAPMessageContext context) {
		return false;
	}

	@Override
	public void close(MessageContext context) {
		//
	}
}
