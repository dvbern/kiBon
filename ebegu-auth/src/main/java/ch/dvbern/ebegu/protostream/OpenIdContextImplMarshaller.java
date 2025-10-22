/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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
 *
 */

package ch.dvbern.ebegu.protostream;

import java.io.StringReader;
import java.lang.reflect.Field;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

import org.glassfish.soteria.mechanisms.openid.domain.AccessTokenImpl;
import org.glassfish.soteria.mechanisms.openid.domain.IdentityTokenImpl;
import org.glassfish.soteria.mechanisms.openid.domain.RefreshTokenImpl;
import org.infinispan.commons.util.ReflectionUtil;
import org.infinispan.protostream.annotations.ProtoAdapter;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
import org.wildfly.security.soteria.original.OpenIdContextImpl;

@ProtoAdapter(OpenIdContextImpl.class)
public class OpenIdContextImplMarshaller {

	@ProtoFactory
	public static OpenIdContextImpl create(
		String tokenType,
		AccessTokenImpl accessToken,
		IdentityTokenImpl identityToken,
		RefreshTokenImpl refreshToken,
		Long expiresIn,
		String claims
	) {
		OpenIdContextImpl openIdContext = new OpenIdContextImpl();
		openIdContext.setTokenType(tokenType);
		openIdContext.setAccessToken(accessToken);
		openIdContext.setIdentityToken(identityToken);
		openIdContext.setRefreshToken(refreshToken);
		openIdContext.setExpiresIn(expiresIn);
		setClaims(openIdContext, claims);
		return openIdContext;
	}

	private static void setClaims(
		OpenIdContextImpl openIdContext,
		String claims
	) {
		Field field = ReflectionUtil.getField(
			"claims",
			OpenIdContextImpl.class
		);
		ReflectionUtil.setAccessibly(
			openIdContext,
			field,
			parseJsonString(claims)
		);
	}

	private static JsonObject parseJsonString(String claimsString) {
		try (JsonReader reader = Json.createReader(
			new StringReader(claimsString)
		)) {
			return reader.readObject();
		}
	}

	@ProtoField(1)
	public String getTokenType(OpenIdContextImpl token) {
		return token.getTokenType();
	}

	@ProtoField(2)
	public AccessTokenImpl getAccessToken(OpenIdContextImpl token) {
		return (AccessTokenImpl) token.getAccessToken();
	}

	@ProtoField(3)
	public IdentityTokenImpl getIdentityToken(OpenIdContextImpl token) {
		return (IdentityTokenImpl) token.getIdentityToken();
	}

	@ProtoField(4)
	public RefreshTokenImpl getRefreshToken(OpenIdContextImpl token) {
		return (RefreshTokenImpl) token.getRefreshToken().orElse(null);
	}

	@ProtoField(5)
	public Long getExpiresIn(OpenIdContextImpl token) {
		return token.getExpiresIn().orElse(0L);
	}

	@ProtoField(6)
	public String getClaims(OpenIdContextImpl token) {
		return token.getClaimsJson().toString();
	}
}
