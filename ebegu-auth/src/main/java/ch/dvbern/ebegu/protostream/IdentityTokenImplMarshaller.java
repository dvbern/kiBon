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

import ch.dvbern.ebegu.oidc.AuthConstants;
import org.glassfish.soteria.mechanisms.openid.domain.IdentityTokenImpl;
import org.infinispan.protostream.annotations.ProtoAdapter;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

@ProtoAdapter(IdentityTokenImpl.class)
public class IdentityTokenImplMarshaller {
	@ProtoFactory
	public static IdentityTokenImpl create(String token) {
		return new IdentityTokenImpl(token, AuthConstants.TOKEN_MIN_VALIDITY);
	}

	@ProtoField(1)
	public String getToken(IdentityTokenImpl token) {
		return token.getToken();
	}
}
