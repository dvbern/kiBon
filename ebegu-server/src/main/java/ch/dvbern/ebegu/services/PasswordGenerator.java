package ch.dvbern.ebegu.services;

import java.security.SecureRandom;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PasswordGenerator {

	private final SecureRandom secureRandom = new SecureRandom();

	public String createRandomPassword() {
		// leading K to fulfill password policy
		return "K" + Long.toHexString(secureRandom.nextLong());
	}
}
