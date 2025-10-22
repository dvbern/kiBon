package ch.dvbern.ebegu.util;

import java.util.Locale;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ServerMessageUtilTest {

	private Mandant mandant;

	private Gemeinde gemeinde;

	private String testBernDE;

	private static final String TEST_OHNE_PARAMETER_KEY = "test_without_param";
	private static final String TEST_MIT_PARAMETER_KEY = "test_with_param";

	private static final String TEST_PARAMETER = "gesetzte Parameter";

	private enum TestEnum {
		TEST_ENUM_VALUE
	}

	@BeforeEach
	public void setUp() {
		mandant = new Mandant();
		mandant.setMandantIdentifier(MandantIdentifier.BERN);
		gemeinde = new Gemeinde();
		gemeinde.setBfsNummer(1058L);
		testBernDE = ServerMessageUtil.getMessage(
			TEST_OHNE_PARAMETER_KEY,
			Locale.GERMAN,
			mandant
		);
	}

	@Test
	void getDefaultMessageTranslationTest() {
		String testBernFR = ServerMessageUtil.getMessage(
			TEST_OHNE_PARAMETER_KEY,
			Locale.FRENCH,
			mandant
		);
		assertThat(testBernDE.equals(testBernFR), is(false));
	}

	@Test
	void getOverridedMessageTest() {
		mandant.setMandantIdentifier(MandantIdentifier.LUZERN);
		String testLuzern = ServerMessageUtil.getMessage(
			TEST_OHNE_PARAMETER_KEY,
			Locale.GERMAN,
			mandant
		);
		assertThat(testBernDE.equals(testLuzern), is(false));
	}

	@Test
	void getDefaultMessageWhenFileNotExistTest() {
		String testBernFR = ServerMessageUtil.getMessage(
			TEST_OHNE_PARAMETER_KEY,
			Locale.FRENCH,
			mandant
		);
		String testLuzernFR = ServerMessageUtil.getMessage(
			TEST_OHNE_PARAMETER_KEY,
			Locale.FRENCH,
			mandant
		);
		assertThat(testBernFR.equals(testLuzernFR), is(true));
	}

	@Test
	void getDefaultMessageWhenKeyNotExistTest() {
		mandant.setMandantIdentifier(MandantIdentifier.SOLOTHURN);
		String testSolothurn = ServerMessageUtil.getMessage(
			TEST_OHNE_PARAMETER_KEY,
			Locale.GERMAN,
			mandant
		);
		assertThat(testBernDE.equals(testSolothurn), is(true));
	}

	@Test
	void getMessageMitGemeindeTest() {
		mandant.setMandantIdentifier(MandantIdentifier.LUZERN);
		String testLuzern = ServerMessageUtil.getMessage(
			TEST_OHNE_PARAMETER_KEY,
			Locale.GERMAN,
			mandant
		);
		String testLuzernGemeinde = ServerMessageUtil.getMessage(
			TEST_OHNE_PARAMETER_KEY,
			Locale.GERMAN,
			mandant,
			gemeinde
		);
		assertThat(testLuzern.equals(testLuzernGemeinde), is(false));
	}

	@Test
	void getKantonMessageWithGemeindeNotFoundTest() {
		mandant.setMandantIdentifier(MandantIdentifier.LUZERN);
		gemeinde.setBfsNummer(1060L);
		String testLuzern = ServerMessageUtil.getMessage(
			TEST_OHNE_PARAMETER_KEY,
			Locale.GERMAN,
			mandant
		);
		String testLuzernGemeinde = ServerMessageUtil.getMessage(
			TEST_OHNE_PARAMETER_KEY,
			Locale.GERMAN,
			mandant,
			gemeinde
		);
		assertThat(testLuzern.equals(testLuzernGemeinde), is(true));
	}

	@Test
	void getMessageMitArgsTest() {
		String testBernMitArgs = ServerMessageUtil.getMessage(
			TEST_MIT_PARAMETER_KEY,
			Locale.GERMAN,
			mandant,
			TEST_PARAMETER
		);
		assertThat(testBernMitArgs.contains(TEST_PARAMETER), is(true));
	}

	@Test
	void getMessageMitGemeindeUndArgsTest() {
		mandant.setMandantIdentifier(MandantIdentifier.LUZERN);
		String testLuzernMitParam = ServerMessageUtil.getMessage(
			TEST_MIT_PARAMETER_KEY,
			Locale.GERMAN,
			mandant,
			TEST_PARAMETER
		);
		String testLuzernGemeindeMitParam =
			ServerMessageUtil.getMessage(
				TEST_MIT_PARAMETER_KEY,
				Locale.GERMAN,
				mandant,
				gemeinde,
				TEST_PARAMETER
			);
		assertThat(
			testLuzernMitParam.equals(testLuzernGemeindeMitParam),
			is(false)
		);
	}

	@Test
	void getMessageMitGemeindeNotFoundUndArgsTest() {
		mandant.setMandantIdentifier(MandantIdentifier.LUZERN);
		gemeinde.setBfsNummer(1060L);
		String testLuzernMitParam = ServerMessageUtil.getMessage(
			TEST_MIT_PARAMETER_KEY,
			Locale.GERMAN,
			mandant,
			TEST_PARAMETER
		);
		String testLuzernGemeindeMitParam =
			ServerMessageUtil.getMessage(
				TEST_MIT_PARAMETER_KEY,
				Locale.GERMAN,
				mandant,
				gemeinde,
				TEST_PARAMETER
			);
		assertThat(
			testLuzernMitParam.equals(testLuzernGemeindeMitParam),
			is(true)
		);
	}

	@Test
	void translateEnumValueTest() {
		assertThat(
			ServerMessageUtil.translateEnumValue(
				null,
				Locale.GERMAN,
				mandant
			).equals(StringUtils.EMPTY),
			is(true)
		);
		String testEnumBEDE = ServerMessageUtil.translateEnumValue(
			TestEnum.TEST_ENUM_VALUE,
			Locale.GERMAN,
			mandant
		);
		String testEnumBEFR = ServerMessageUtil.translateEnumValue(
			TestEnum.TEST_ENUM_VALUE,
			Locale.FRENCH,
			mandant
		);
		assertThat(testEnumBEDE.equals(testEnumBEFR), is(false));
	}

	@Test
	void translateEnumValueMandantTest() {
		String testEnumBEDE = ServerMessageUtil.translateEnumValue(
			TestEnum.TEST_ENUM_VALUE,
			Locale.GERMAN,
			mandant
		);
		mandant.setMandantIdentifier(MandantIdentifier.LUZERN);
		String testEnumLUDE = ServerMessageUtil.translateEnumValue(
			TestEnum.TEST_ENUM_VALUE,
			Locale.GERMAN,
			mandant
		);
		assertThat(testEnumBEDE.equals(testEnumLUDE), is(false));
	}

	@Test
	void translateEnumValueMitGemeindeTest() {
		mandant.setMandantIdentifier(MandantIdentifier.LUZERN);
		assertThat(
			ServerMessageUtil.translateEnumValue(
				null,
				Locale.GERMAN,
				mandant,
				gemeinde
			).equals(StringUtils.EMPTY),
			is(true)
		);

		String testEnumLUDE = ServerMessageUtil.translateEnumValue(
			TestEnum.TEST_ENUM_VALUE,
			Locale.GERMAN,
			mandant
		);
		String testEnumGemeindeLUDE =
			ServerMessageUtil.translateEnumValue(
				TestEnum.TEST_ENUM_VALUE,
				Locale.GERMAN,
				mandant,
				gemeinde
			);
		assertThat(testEnumGemeindeLUDE.equals(testEnumLUDE), is(false));
	}

	@Test
	void translateEnumValueMitGemeindeNotFoundTest() {
		mandant.setMandantIdentifier(MandantIdentifier.LUZERN);
		String testEnumLUDE = ServerMessageUtil.translateEnumValue(
			TestEnum.TEST_ENUM_VALUE,
			Locale.GERMAN,
			mandant
		);
		gemeinde.setBfsNummer(100L);
		String testEnumGemeindeLUDE =
			ServerMessageUtil.translateEnumValue(
				TestEnum.TEST_ENUM_VALUE,
				Locale.GERMAN,
				mandant,
				gemeinde
			);
		assertThat(testEnumGemeindeLUDE.equals(testEnumLUDE), is(true));
	}
}
