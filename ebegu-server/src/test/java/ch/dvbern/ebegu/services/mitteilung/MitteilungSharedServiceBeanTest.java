package ch.dvbern.ebegu.services.mitteilung;

import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.enums.MitteilungTeilnehmerTyp;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.inbox.services.BetreuungEventHelper;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.test.TestDataUtil;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.test.TestDataUtil.createBenutzer;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static shadow.org.assertj.core.api.Assertions.assertThat;

@ExtendWith(EasyMockExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MitteilungSharedServiceBeanTest extends EasyMockSupport {

	Benutzer verantwortlicherBG;
	Benutzer kibonTechnicalUser;

	@TestSubject
	MitteilungSharedServiceBean service;

	@Mock
	Authorizer authorizer;

	@Mock
	PrincipalBean principal;

	@Mock
	BenutzerService benutzerService;

	@Mock
	MitteilungEmpfaengerResolver mitteilungEmpfaengerResolver;

	@Mock
	BetreuungEventHelper betreuungEventHelper;

	@BeforeEach
	void setUp() {
		verantwortlicherBG = createBenutzer(
			UserRole.SACHBEARBEITER_BG,
			TestDataUtil.getMandantKantonBern()
		);
		kibonTechnicalUser = createBenutzer(
			UserRole.SUPER_ADMIN,
			TestDataUtil.getMandantKantonBern()
		);
	}

	@Nested
	class SchliessungsmitteilungTest {
		@ParameterizedTest
		@EnumSource(value = UserRole.class,
			names = { ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
		public void setSenderAndEmpfaengerAndCheckAuthorization_setVerantwortlicherBGasEmpfaenger_whenMandantBenutzer(
			UserRole role
		) {
			Benutzer benutzer = createBenutzer(
				role,
				TestDataUtil.getMandantKantonBern()
			);
			var schliessungsMitteilung = createSchliessungsMitteilung();

			mockMethodCalls(benutzer, schliessungsMitteilung);

			service.setSenderAndEmpfaengerAndCheckAuthorization(
				schliessungsMitteilung
			);

			assertThat(schliessungsMitteilung.getEmpfaenger()).isEqualTo(
				verantwortlicherBG
			);
		}

		@ParameterizedTest
		@EnumSource(value = UserRole.class,
			names = { ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
		public void setSenderAndEmpfaengerAndCheckAuthorization_setKibonTechnicalUserAsSender_whenMandantBenutzer(
			UserRole role
		) {
			Benutzer benutzer = createBenutzer(
				role,
				TestDataUtil.getMandantKantonBern()
			);
			var schliessungsMitteilung = createSchliessungsMitteilung();

			mockMethodCalls(benutzer, schliessungsMitteilung);

			service.setSenderAndEmpfaengerAndCheckAuthorization(
				schliessungsMitteilung
			);

			assertThat(schliessungsMitteilung.getSender()).isEqualTo(
				kibonTechnicalUser
			);
		}

		@ParameterizedTest
		@EnumSource(value = UserRole.class,
			names = { ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
		public void setSenderAndEmpfaengerAndCheckAuthorization_setJUGENDAMTAsSenderTyp_whenMandantBenutzer(
			UserRole role
		) {
			Benutzer benutzer = createBenutzer(
				role,
				TestDataUtil.getMandantKantonBern()
			);
			var schliessungsMitteilung = createSchliessungsMitteilung();
			mockMethodCalls(benutzer, schliessungsMitteilung);

			service.setSenderAndEmpfaengerAndCheckAuthorization(
				schliessungsMitteilung
			);

			assertThat(schliessungsMitteilung.getSenderTyp()).isEqualTo(
				MitteilungTeilnehmerTyp.JUGENDAMT
			);
		}

		@ParameterizedTest
		@EnumSource(value = UserRole.class,
			names = { ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
		public void setSenderAndEmpfaengerAndCheckAuthorization_setJUGENDAMTAsEmpfaengerTyp_whenMandantBenutzer(
			UserRole role
		) {
			Benutzer benutzer = createBenutzer(
				role,
				TestDataUtil.getMandantKantonBern()
			);
			var schliessungsMitteilung = createSchliessungsMitteilung();
			mockMethodCalls(benutzer, schliessungsMitteilung);

			service.setSenderAndEmpfaengerAndCheckAuthorization(
				schliessungsMitteilung
			);

			assertThat(schliessungsMitteilung.getSenderTyp()).isEqualTo(
				MitteilungTeilnehmerTyp.JUGENDAMT
			);
		}

		private Betreuungsmitteilung createSchliessungsMitteilung() {
			var mitteilung = new Betreuungsmitteilung();
			mitteilung.setSchliessungMitteilung(true);
			Dossier dossier = new Dossier();
			mitteilung.setDossier(dossier);
			return mitteilung;
		}

		private void mockMethodCalls(
			@Nonnull Benutzer benutzer,
			@Nonnull Betreuungsmitteilung schliessungsMitteilung
		) {
			expect(benutzerService.getCurrentBenutzer()).andReturn(
				Optional.of(benutzer)
			);
			expect(principal.isCallerInRole(UserRole.SUPER_ADMIN)).andReturn(
				false
			).anyTimes();
			expect(
				mitteilungEmpfaengerResolver
					.getEmpfaengerBeiMitteilungAnGemeinde(
						schliessungsMitteilung
					)
			).andReturn(verantwortlicherBG);
			authorizer.checkWriteAuthorizationMitteilung(
				schliessungsMitteilung
			);
			expect(
				betreuungEventHelper.getSchliessungsmitteilungBenutzer(
					benutzer.getMandant()
				)
			).andReturn(
				kibonTechnicalUser
			);
			expectLastCall();
			replayAll();
		}
	}
}
