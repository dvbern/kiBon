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

package ch.dvbern.ebegu.locallogin;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import com.ibm.icu.text.Transliterator;
import lombok.Builder;

public class UserTemplates {
	private UserTemplates() {
	}

	@Builder
	public record UserTemplate(
							   String vorname,
							   String name,
							   UserRole role,
							   MandantIdentifier mandantIdentifier,
							   Set<String> gemeindeIds,
							   String traegerschaftId,
							   String institutionId,
							   String sozialdienstId
	) {
		public String email(String emailTemplate) {
			return MessageFormat.format(
				emailTemplate,
				transliterate(vorname),
				transliterate(name),
				mandantIdentifier.getUrlCode()
			).toLowerCase(Constants.DEFAULT_LOCALE);
		}
	}

	private static final Transliterator germanTransliterator = Transliterator
		.getInstance("de-ASCII");
	private static final Transliterator latinTransliterator = Transliterator
		.getInstance("Latin-ASCII");

	// transliterates German umlauts first, then French accents etc
	private static String transliterate(String text) {
		return latinTransliterator.transliterate(
			germanTransliterator.transliterate(text)
		);
	}

	private static String getTraegerschaftId(
		MandantIdentifier mandantIdentifier
	) {
		return switch (mandantIdentifier) {
		case BERN -> "f9ddee82-81a1-4cda-b273-fb24e9299308";
		case LUZERN -> "31bf2433-30a3-11ec-a86f-b89a2ae4a038";
		case SOLOTHURN -> "5c537fd1-537b-11ec-98e8-f4390979fa3e";
		case APPENZELL_AUSSERRHODEN -> "c256ebf1-3999-11ed-a63d-b05cda43de9c";
		case SCHWYZ -> "ef7ef939-b3e7-11ee-829a-0242ac160002";
		case ZUG -> "99f9b18b-6434-11ef-8aab-005056bde697";
		case DVB -> "4b2b6105-f6a8-432f-9f42-e7d1c069d411";
		};
	}

	private static String getInstitutionId(
		MandantIdentifier mandantIdentifier
	) {
		return switch (mandantIdentifier) {
		case BERN -> "1b6f476f-e0f5-4380-9ef6-836d688853a3";
		case LUZERN -> "f5ceae4a-30a5-11ec-a86f-b89a2ae4a038";
		case SOLOTHURN -> "78051383-537e-11ec-98e8-f4390979fa3e";
		case APPENZELL_AUSSERRHODEN -> "caa83a6b-3999-11ed-a63d-b05cda43de9c";
		case SCHWYZ -> "1188c355-b3d6-11ee-829a-0242ac160002";
		case ZUG -> "9cc6d539-6434-11ef-8aab-005056bde697";
		case DVB -> "497a70cd-0c22-46e1-9787-1efffc8b73d8";
		};
	}

	@Nullable
	private static String getTagesschuleId(
		MandantIdentifier mandantIdentifier
	) {
		return switch (mandantIdentifier) {
		case BERN -> "f44a68f2-dda2-4bf2-936a-68e20264b610";
		case SOLOTHURN -> "bbf7f306-5392-11ec-98e8-f4390979fa3e";
		case APPENZELL_AUSSERRHODEN -> "5c136a35-39a9-11ed-a63d-b05cda43de9c";
		case SCHWYZ -> "e67aa195-b912-11ee-8d78-0242ac160002";
		default -> null;
		};
	}

	@Nullable
	private static String getSozialdienstId(
		MandantIdentifier mandantIdentifier
	) {
		return switch (mandantIdentifier) {
		case BERN -> "f44a68f2-dda2-4bf2-936a-68e20264b620";
		case LUZERN -> "7049ec48-30ab-11ec-a86f-b89a2ae4a038";
		case SOLOTHURN -> "1b1b4208-5394-11ec-98e8-f4390979fa3e";
		case APPENZELL_AUSSERRHODEN -> "1653a0c7-39ab-11ed-a63d-b05cda43de9c";
		case DVB -> "36b12691-c395-4e21-a412-8e3e69e9fd00";
		case SCHWYZ, ZUG -> null;
		};
	}

	private static String getDefaultGemeindeId(
		MandantIdentifier mandantIdentifier
	) {
		return switch (mandantIdentifier) {
		case BERN -> "ea02b313-e7c3-4b26-9ef7-e413f4046db2";
		case LUZERN -> "6fd6183c-30a2-11ec-a86f-b89a2ae4a038";
		case SOLOTHURN -> "47c4b3a8-5379-11ec-98e8-f4390979fa3e";
		case APPENZELL_AUSSERRHODEN -> "b3e44f85-3999-11ed-a63d-b05cda43de9c";
		case SCHWYZ -> "de7c81c0-b3d5-11ee-829a-0242ac160002";
		case ZUG -> "96e98f51-6434-11ef-8aab-005056bde697";
		case DVB -> "a4f472fe-a9cf-44c2-a34c-66cb78046a9f";
		};
	}

	@Nullable
	private static String getSecondGemeindeId(
		MandantIdentifier mandantIdentifier
	) {
		return switch (mandantIdentifier) {
		case BERN -> "80a8e496-b73c-4a4a-a163-a0b2caf76487";
		case SOLOTHURN -> "47c4b3a8-5371-11ec-98e8-f4390979fa3e";
		case ZUG -> "2a06f736-643b-11ef-8aab-005056bde697";
		case DVB -> "4ec3b3a5-85f8-4e50-a69d-ad0321af0883";
		default -> null;
		};
	}

	public static List<UserTemplate> getGesuchstellende(
		MandantIdentifier mandantIdentifier
	) {
		return List.of(
			UserTemplate.builder()
				.vorname(
					"Emma"
				)
				.name(
					"Gerber"
				)
				.role(
					UserRole.GESUCHSTELLER
				)
				.mandantIdentifier(
					mandantIdentifier
				)
				.build(),
			UserTemplate.builder()
				.vorname(
					"Heinrich"
				)
				.name(
					"Müller"
				)
				.role(
					UserRole.GESUCHSTELLER
				)
				.mandantIdentifier(
					mandantIdentifier
				)
				.build(),
			UserTemplate.builder()
				.vorname(
					"Michael"
				)
				.name(
					"Berger"
				)
				.role(
					UserRole.GESUCHSTELLER
				)
				.mandantIdentifier(
					mandantIdentifier
				)
				.build(),
			UserTemplate.builder()
				.vorname(
					"Jean"
				)
				.name(
					"Chambre"
				)
				.role(
					UserRole.GESUCHSTELLER
				)
				.mandantIdentifier(
					mandantIdentifier
				)
				.build(),
			UserTemplate.builder()
				.vorname(
					"Hans"
				)
				.name(
					"Zimmerman"
				)
				.role(
					UserRole.GESUCHSTELLER
				)
				.mandantIdentifier(
					mandantIdentifier
				)
				.build()
		);
	}

	public static List<UserTemplate> getSozialdienstUserTemplates(
		MandantIdentifier mandantIdentifier
	) {
		String sozialdienstId = getSozialdienstId(mandantIdentifier);
		if (sozialdienstId == null) {
			return List.of();
		}
		return List.of(
			UserTemplate.builder()
				.vorname(
					"Patrick"
				)
				.name(
					"Melcher"
				)
				.role(
					UserRole.ADMIN_SOZIALDIENST
				)
				.mandantIdentifier(
					mandantIdentifier
				)
				.sozialdienstId(sozialdienstId)
				.build(),
			UserTemplate.builder()
				.vorname(
					"Max"
				)
				.name(
					"Palmer"
				)
				.role(
					UserRole.SACHBEARBEITER_SOZIALDIENST
				)
				.mandantIdentifier(
					mandantIdentifier
				)
				.sozialdienstId(sozialdienstId)
				.build()
		);
	}

	public static List<UserTemplate> getTraegerschaftsUserTemplates(
		MandantIdentifier mandantIdentifier
	) {
		String traegerschaftId = getTraegerschaftId(mandantIdentifier);
		return List.of(
			UserTemplate.builder()
				.vorname(
					"Bernhard"
				)
				.name(
					"Bern"
				)
				.role(
					UserRole.ADMIN_TRAEGERSCHAFT
				)
				.mandantIdentifier(
					mandantIdentifier
				)
				.traegerschaftId(traegerschaftId)
				.build(),
			UserTemplate.builder()
				.vorname(
					"Agnes"
				)
				.name(
					"Krause"
				)
				.role(
					UserRole.SACHBEARBEITER_TRAEGERSCHAFT
				)
				.mandantIdentifier(
					mandantIdentifier
				)
				.traegerschaftId(traegerschaftId)
				.build()
		);
	}

	public static List<UserTemplate> getInstitutionsUserTemplates(
		MandantIdentifier mandantIdentifier
	) {
		String institutionId = getInstitutionId(mandantIdentifier);
		return List.of(
			UserTemplate.builder()
				.vorname(
					"Silvia"
				)
				.name(
					"Bergmann"
				)
				.role(
					UserRole.ADMIN_INSTITUTION
				)
				.mandantIdentifier(
					mandantIdentifier
				)
				.institutionId(institutionId)
				.build(),

			UserTemplate.builder()
				.vorname("Sophie")
				.name("Bergmann")
				.role(UserRole.SACHBEARBEITER_INSTITUTION)
				.mandantIdentifier(
					mandantIdentifier
				)
				.institutionId(institutionId)
				.build()
		);

	}

	public static List<UserTemplate> getTagesschuleUserTemplates(
		MandantIdentifier mandantIdentifier
	) {
		String tagesschuleId = getTagesschuleId(mandantIdentifier);
		if (tagesschuleId == null) {
			return List.of();
		}
		return List.of(
			UserTemplate.builder()
				.vorname(
					"Serge"
				)
				.name(
					"Gainsbourg"
				)
				.role(
					UserRole.ADMIN_INSTITUTION
				)
				.mandantIdentifier(
					mandantIdentifier
				)
				.institutionId(tagesschuleId)
				.build(),

			UserTemplate.builder()
				.vorname("Charlotte")
				.name("Gainsbourg")
				.role(UserRole.SACHBEARBEITER_INSTITUTION)
				.mandantIdentifier(
					mandantIdentifier
				)
				.institutionId(tagesschuleId)
				.build()
		);

	}

	public static List<UserTemplate> getMandantsUserTemplates(
		MandantIdentifier mandantIdentifier
	) {
		return List.of(
			UserTemplate.builder()
				.vorname("Super")
				.name("User")
				.role(UserRole.SUPER_ADMIN)
				.mandantIdentifier(mandantIdentifier)
				.build(),

			UserTemplate.builder()
				.vorname(
					"Bernhard"
				)
				.name(
					"Röthlisberger"
				)
				.role(UserRole.ADMIN_MANDANT)
				.mandantIdentifier(
					mandantIdentifier
				)
				.build(),

			UserTemplate.builder()
				.vorname(
					"Benno"
				)
				.name(
					"Röthlisberger"
				)
				.role(
					UserRole.SACHBEARBEITER_MANDANT
				)
				.mandantIdentifier(
					mandantIdentifier
				)
				.build()
		);
	}

	public static List<UserTemplate> getDefaultGemeindeUserTemplates(
		MandantIdentifier mandantIdentifier
	) {
		String defaultGemeindeId = getDefaultGemeindeId(mandantIdentifier);
		Set<String> gemeindeIds = Set.of(defaultGemeindeId);
		return List.of(
			UserTemplate.builder()
				.vorname("Kurt")
				.name("Blaser")
				.role(UserRole.ADMIN_BG)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),

			UserTemplate.builder()
				.vorname("Jörg")
				.name("Becker")
				.role(UserRole.SACHBEARBEITER_BG)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),
			UserTemplate.builder()
				.vorname("Adrian")
				.name("Schuler")
				.role(UserRole.ADMIN_TS)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),

			UserTemplate.builder()
				.vorname("Julien")
				.name("Schuler")
				.role(UserRole.SACHBEARBEITER_TS)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),

			UserTemplate.builder()
				.vorname("Gerlinde")
				.name("Hofstetter")
				.role(UserRole.ADMIN_GEMEINDE)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),

			UserTemplate.builder()
				.vorname("Stefan")
				.name("Wirth")
				.role(UserRole.SACHBEARBEITER_GEMEINDE)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),
			UserTemplate.builder()
				.vorname("Marlene")
				.name("Stöckli")
				.role(UserRole.SACHBEARBEITER_FERIENBETREUUNG)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),

			UserTemplate.builder()
				.vorname("Sarah")
				.name("Riesen")
				.role(UserRole.ADMIN_FERIENBETREUUNG)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),

			UserTemplate.builder()
				.vorname("Rodolfo")
				.name("Geldmacher")
				.role(UserRole.STEUERAMT)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),

			UserTemplate.builder()
				.vorname("Julia")
				.name("Jurist")
				.role(UserRole.JURIST)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),
			UserTemplate.builder()
				.vorname("Reto")
				.name("Staub")
				.role(UserRole.REVISOR)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build()
		);
	}

	public static List<UserTemplate> getSecondGemeindeUserTemplates(
		MandantIdentifier mandantIdentifier
	) {
		String defaultGemeindeId = getSecondGemeindeId(mandantIdentifier);
		if (defaultGemeindeId == null) {
			return List.of();
		}
		Set<String> gemeindeIds = Set.of(defaultGemeindeId);
		return List.of(
			UserTemplate.builder()
				.vorname("Kurt")
				.name("Schmid")
				.role(UserRole.ADMIN_BG)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),

			UserTemplate.builder()
				.vorname("Jörg")
				.name("Keller")
				.role(UserRole.SACHBEARBEITER_BG)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),
			UserTemplate.builder()
				.vorname("Adrian")
				.name("Huber")
				.role(UserRole.ADMIN_TS)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),

			UserTemplate.builder()
				.vorname("Julien")
				.name("Odermatt")
				.role(UserRole.SACHBEARBEITER_TS)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),

			UserTemplate.builder()
				.vorname("Gerlinde")
				.name("Bader")
				.role(UserRole.ADMIN_GEMEINDE)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),

			UserTemplate.builder()
				.vorname("Stefan")
				.name("Weibel")
				.role(UserRole.SACHBEARBEITER_GEMEINDE)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),

			UserTemplate.builder()
				.vorname("Rodolfo")
				.name("Iten")
				.role(UserRole.STEUERAMT)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),

			UserTemplate.builder()
				.vorname("Reto")
				.name("Werlen")
				.role(UserRole.REVISOR)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),
			UserTemplate.builder()
				.vorname("Julia")
				.name("Adler")
				.role(UserRole.JURIST)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),
			UserTemplate.builder()
				.vorname("Jordan")
				.name("Hefti")
				.role(UserRole.SACHBEARBEITER_FERIENBETREUUNG)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),

			UserTemplate.builder()
				.vorname("Jean-Pierre")
				.name("Kraeuchi")
				.role(UserRole.ADMIN_FERIENBETREUUNG)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build()

		);
	}

	public static List<UserTemplate> getCombinedGemeindeUserTemplates(
		MandantIdentifier mandantIdentifier
	) {
		String defaultGemeindeId = getDefaultGemeindeId(mandantIdentifier);
		String secondGemeindeId = getSecondGemeindeId(mandantIdentifier);
		if (secondGemeindeId == null) {
			return List.of();
		}
		Set<String> gemeindeIds = Set.of(defaultGemeindeId, secondGemeindeId);
		return List.of(
			UserTemplate.builder()
				.vorname("Kurt")
				.name("Schmid")
				.role(UserRole.ADMIN_BG)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),

			UserTemplate.builder()
				.vorname("Jörg")
				.name("Keller")
				.role(UserRole.SACHBEARBEITER_BG)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),
			UserTemplate.builder()
				.vorname("Adrian")
				.name("Huber")
				.role(UserRole.ADMIN_TS)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),

			UserTemplate.builder()
				.vorname("Julien")
				.name("Odermatt")
				.role(UserRole.SACHBEARBEITER_TS)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),

			UserTemplate.builder()
				.vorname("Gerlinde")
				.name("Bader")
				.role(UserRole.ADMIN_GEMEINDE)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),

			UserTemplate.builder()
				.vorname("Stefan")
				.name("Gerber")
				.role(UserRole.SACHBEARBEITER_GEMEINDE)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),

			UserTemplate.builder()
				.vorname("Rodolfo")
				.name("Iten")
				.role(UserRole.STEUERAMT)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),

			UserTemplate.builder()
				.vorname("Reto")
				.name("Werlen")
				.role(UserRole.REVISOR)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),
			UserTemplate.builder()
				.vorname("Julia")
				.name("Adler")
				.role(UserRole.JURIST)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),
			UserTemplate.builder()
				.vorname("Jordan")
				.name("Hefti")
				.role(UserRole.SACHBEARBEITER_FERIENBETREUUNG)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build(),

			UserTemplate.builder()
				.vorname("Jean-Pierre")
				.name("Kraeuchi")
				.role(UserRole.ADMIN_FERIENBETREUUNG)
				.mandantIdentifier(mandantIdentifier)
				.gemeindeIds(gemeindeIds)
				.build()

		);
	}
}
