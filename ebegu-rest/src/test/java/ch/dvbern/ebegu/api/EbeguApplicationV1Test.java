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

package ch.dvbern.ebegu.api;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.security.DeclareRoles;

import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.UserRoleName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EbeguApplicationV1Test {
	@Test
	void declareRolesMustMatchUserRoles() {
		Set<String> declaredRoles =
			Arrays.stream(
				EbeguApplicationV1.class.getDeclaredAnnotation(
					DeclareRoles.class
				)
					.value()
			).collect(Collectors.toSet());
		Set<String> userRoleNames = Arrays.stream(
			UserRoleName.class.getDeclaredFields()
		)
			.map(Field::getName)
			.collect(Collectors.toSet());
		Set<String> roleNames =
			Arrays.stream(UserRole.values())
				.map(Enum::name)
				.collect(Collectors.toSet());

		assertEquals(
			declaredRoles,
			roleNames
		);
		assertEquals(
			declaredRoles,
			userRoleNames
		);
	}
}