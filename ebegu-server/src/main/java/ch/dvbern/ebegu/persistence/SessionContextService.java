package ch.dvbern.ebegu.persistence;

import java.security.Principal;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import ch.dvbern.lib.cdipersistence.ISessionContextService;

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
