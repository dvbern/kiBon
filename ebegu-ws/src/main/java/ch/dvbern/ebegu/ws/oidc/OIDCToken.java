package ch.dvbern.ebegu.ws.oidc;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

public class OIDCToken {

	private static final long LATENCY_AND_CLOCK_DELAY = 5;

	@JsonProperty("access_token")
	private String token;

	@JsonProperty("expires_in")
	private String expiresIn;

	@JsonProperty("token_type")
	private String tokenType;

	private LocalDateTime expiresAt;

	private LocalDateTime requestTime;

	public boolean isExpired() {
		if (expiresAt == null) {
			calculateExpiringTime();
			if (expiresAt == null) {
				return true;
			}
		}
		return LocalDateTime.now()
			.isAfter(expiresAt.minusSeconds(LATENCY_AND_CLOCK_DELAY));
	}

	private void calculateExpiringTime() {
		if (requestTime == null || StringUtils.isBlank(expiresIn)) {
			return;
		}
		expiresAt = requestTime.plusSeconds(Integer.parseInt(expiresIn));
	}

	public String getAuthToken() {
		return tokenType + ' ' + token;
	}

	public void setRequestTime(LocalDateTime requestTime) {
		this.requestTime = requestTime;
	}

	public String toString() {
		return new ToStringBuilder(this)
			.append("Token", token)
			.append("expiresIn", expiresIn)
			.append("expiresAt", expiresAt)
			.toString();
	}
}
