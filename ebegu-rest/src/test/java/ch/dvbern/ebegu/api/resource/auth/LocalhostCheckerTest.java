/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package ch.dvbern.ebegu.api.resource.auth;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test fuer LocalhostChecker
 */
public class LocalhostCheckerTest {

	private static LocalhostChecker checker;

	@BeforeClass
	public static void setUp() {
		checker = new LocalhostChecker();
		checker.init(); // since we do not use a container in this test we init manually
	}

	@Test
	public void isAddressLocalhost() {
		final boolean addressLocalhost = checker.isAddressLocalhost("127.0.0.1");
		Assert.assertTrue(addressLocalhost);
	}

	@Test
	public void findLocalIp() throws SocketException, UnknownHostException {
		try (final DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
			String ip = socket.getLocalAddress().getHostAddress();
			Assert.assertNotNull(ip);
		}
	}

	@Test
	public void testReplacementOfHost() throws URISyntaxException {
		final String s = LocalhostChecker.replaceHostInUrl("http://localhost:8080/connector/api/v1", "172.25.0.3");
		Assert.assertEquals("http://172.25.0.3:8080/connector/api/v1", s);

		String s2 = LocalhostChecker.replaceHostInUrl("http://localhost:8080/connector/api/v1", "172.25.0.3:38080");
		Assert.assertEquals("http://172.25.0.3:38080/connector/api/v1", s2);

		String s3 = LocalhostChecker.replaceHostInUrl("http://localhost/connector/api/v1", "172.25.0.3");
		Assert.assertEquals("http://172.25.0.3/connector/api/v1", s3);

		String s4 = LocalhostChecker.replaceHostInUrl("http://localhost:80/connector/api/v1", "172.25.0.3");
		Assert.assertEquals("http://172.25.0.3:80/connector/api/v1", s4);
	}
}
