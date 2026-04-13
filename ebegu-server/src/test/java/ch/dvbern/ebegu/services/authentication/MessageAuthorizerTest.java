package ch.dvbern.ebegu.services.authentication;

import java.security.Principal;
import java.util.Set;

import jakarta.ejb.EJBAccessException;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.test.TestDataUtil;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.easymock.EasyMock.expect;
import static shadow.org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(EasyMockExtension.class)
class MessageAuthorizerTest extends EasyMockSupport {

	@TestSubject
	private final AuthorizerImpl authorizer = new AuthorizerImpl();

	@Mock
	private PrincipalBean principalBean;

	@ParameterizedTest
	@EnumSource(value = UserRole.class,
		names = { "SACHBEARBEITER_MANDANT", "ADMIN_MANDANT" })
	public void sachbearbeiterMandant_shouldNotThrow_whenCheckingBetreuungsmitteilungWithSchliessungFlagTrueAndMandantMatching(
		UserRole role
	) {
		Mandant mandant = TestDataUtil.getMandantKantonBern();
		Betreuungsmitteilung mitteilung = new Betreuungsmitteilung();
		setUpMandantDossier(mitteilung, mandant);
		mitteilung.setSchliessungMitteilung(true);

		expect(principalBean.discoverMostPrivilegedRole()).andReturn(role);
		expect(principalBean.isKibonServiceAccount()).andReturn(false);
		expect(principalBean.isKibonBenutzer()).andReturn(false);
		expect(principalBean.getMandant()).andReturn(mandant);
		replayAll();

		authorizer.checkWriteAuthorizationMitteilung(mitteilung);
	}

	@ParameterizedTest
	@EnumSource(value = UserRole.class,
		names = { "SACHBEARBEITER_MANDANT", "ADMIN_MANDANT" })
	public void sachbearbeiterMandant_shouldThrowAccessException_whenCheckingBetreuungsmitteilungWithSchliessungFlagFalseAndMandantMatching(
		UserRole role
	) {
		Mandant mandant = TestDataUtil.getMandantKantonBern();
		Betreuungsmitteilung mitteilung = new Betreuungsmitteilung();
		setUpMandantDossier(mitteilung, mandant);
		mitteilung.setSchliessungMitteilung(false);

		expect(principalBean.discoverMostPrivilegedRole()).andReturn(role);
		expect(principalBean.getPrincipal()).andReturn(mock(Principal.class));
		expect(principalBean.discoverRoles()).andReturn(Set.of(""));
		replayAll();

		assertThatThrownBy(
			() -> authorizer.checkWriteAuthorizationMitteilung(mitteilung)
		)
			.isInstanceOf(EJBAccessException.class)
			.hasMessageContaining("Access Violation");
	}

	@ParameterizedTest
	@EnumSource(value = UserRole.class,
		names = { "SACHBEARBEITER_MANDANT", "ADMIN_MANDANT" })
	public void sachbearbeiterMandant_shouldThrowAccessException_whenCheckingBetreuungsmitteilungWithSchliessungFlagFalseAndMandantNotMatching(
		UserRole role
	) {
		Mandant mandant = TestDataUtil.getMandantKantonBern();
		Betreuungsmitteilung mitteilung = new Betreuungsmitteilung();
		setUpMandantDossier(mitteilung, mandant);
		mitteilung.setSchliessungMitteilung(false);

		expect(principalBean.discoverMostPrivilegedRole()).andReturn(role);
		expect(principalBean.getPrincipal()).andReturn(mock(Principal.class));
		expect(principalBean.discoverRoles()).andReturn(Set.of(""));
		replayAll();

		assertThatThrownBy(
			() -> authorizer.checkWriteAuthorizationMitteilung(mitteilung)
		)
			.isInstanceOf(EJBAccessException.class)
			.hasMessageContaining("Access Violation");
	}

	@ParameterizedTest
	@EnumSource(value = UserRole.class,
		names = { "SACHBEARBEITER_MANDANT", "ADMIN_MANDANT" })
	public void sachbearbeiterMandant_shouldThrowMandantAccessException_whenCheckingBetreuungsmitteilungWithSchliessungFlagTrueAndMandantNotMatching(
		UserRole role
	) {
		Mandant mandant = TestDataUtil.getMandantKantonBern();
		Betreuungsmitteilung mitteilung = new Betreuungsmitteilung();
		setUpMandantDossier(mitteilung, mandant);
		mitteilung.setSchliessungMitteilung(true);

		expect(principalBean.discoverMostPrivilegedRole()).andReturn(role);
		expect(principalBean.getPrincipal()).andReturn(mock(Principal.class));
		expect(principalBean.discoverRoles()).andReturn(Set.of(""));
		expect(principalBean.isKibonServiceAccount()).andReturn(false);
		expect(principalBean.isKibonBenutzer()).andReturn(false);
		expect(principalBean.getMandant()).andReturn(
			TestDataUtil.getMandantLuzern()
		);
		replayAll();

		assertThatThrownBy(
			() -> authorizer.checkWriteAuthorizationMitteilung(mitteilung)
		)
			.isInstanceOf(EJBAccessException.class)
			.hasMessageContaining("Mandant Access Violation");
	}

	private Mitteilung setUpMandantDossier(
		Mitteilung mitteilung,
		Mandant mandant
	) {
		Dossier dossier = new Dossier();
		Fall fall = new Fall();
		fall.setMandant(mandant);
		dossier.setFall(fall);
		mitteilung.setDossier(dossier);
		return mitteilung;
	}

}
