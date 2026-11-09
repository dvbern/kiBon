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

package ch.dvbern.ebegu.util.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.util.crypto.PBKDF2PasswordHash.createHash;
import static ch.dvbern.ebegu.util.crypto.PBKDF2PasswordHash.validatePassword;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class PBKDF2PasswordHashTest {
	private static final Logger LOG = LoggerFactory.getLogger(
		PBKDF2PasswordHashTest.class
	);

	@Test
	public void testCrypto() throws Exception {

		// Print out 10 hashes
		for (int i = 0; i < 10; i++) {
			String hash = createHash("p\r\nassw0Rd!");
			assertNotNull(hash);
			//			LOG.info("generated hash: "+ hash);
		}

		// Test password validation
		for (int i = 0; i < 100; i++) {
			String password = "" + i;
			String hash = createHash(password);
			String secondHash = createHash(password);
			if (hash.equals(secondHash)) {
				fail("FAILURE: TWO HASHES ARE EQUAL!");
			}
			String wrongPassword = "" + (i + 1);
			if (validatePassword(wrongPassword, hash)) {
				fail("FAILURE: WRONG PASSWORD ACCEPTED!");
			}
			if (!validatePassword(password, hash)) {
				fail("FAILURE: GOOD PASSWORD NOT ACCEPTED!");
			}
		}
	}

	@Test
	public void testCreate() throws InvalidKeySpecException,
		NoSuchAlgorithmException {
		String hash = createHash("password10");
		assertNotNull(hash);
		//		System.out.println(hash);
	}
}
