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

package ch.dvbern.ebegu.persistence;

import ch.dvbern.lib.cdipersistence.ISessionContextService;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import java.security.Principal;

/**
 * Implementation des SessionContext als Stateless Session Bean.
 */
@Stateless
@PermitAll
public class SessionContextService implements ISessionContextService {

	@Resource
	private SessionContext sessionContext;

	@Override
	public Principal getCallerPrincipal() {
		return sessionContext.getCallerPrincipal();
	}

	@Override
	public boolean isCallerInRole(final String s) {
		return sessionContext.isCallerInRole(s);
	}
}
