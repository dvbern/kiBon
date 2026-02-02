/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
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
 */

package ch.dvbern.ebegu.util;

import java.util.Arrays;

import ch.dvbern.ebegu.enums.AntragStatus;

/**
 * <p>
 * Unterstützt das erstellen von Tests, die auf Enums basieren. Zum Beispiel gibt es das Enum {@link AntragStatus} das
 * alle
 * Zustände eines Antrages, z.B. freigegeben, geprüft, verfügt etc. definiert. Angenommen man schreibt nun einen
 * Service, mit
 * dem ein Antrag verfügt werden kann. Ob das möglich ist, hängt aber von dem Zustand ab, den der Antrag aktuell
 * innehat. Wenn
 * man dafür nun einen Test schreib, ist es leicht, einen Zustand zu übersehen. Und wenn das Enum {@link AntragStatus}
 * mal
 * erweitert wird, ist schwer all Testfälle zu finden, die davon betroffen sind oder die erweitert werden müssten.
 * </p>
 * <p>
 * Mit dieser Hilfsklasse ist sichergestellt, dass immer alle Status eines Enums getestet werden.
 * </p>
 *
 * @param <R> Der Datentyp des Rückgabewertes der getesteten Methode.
 * @param <E> Der Datentyp des Enums dessem Status evaluliert werden sollen.
 */
public abstract class EnumTestSupport<R, E extends Enum<E>> extends
	EasyMockTestSupport {

	private final Class<E> enumType;

	protected EnumTestSupport(Class<E> enumType) {
		this.enumType = enumType;
	}

	/**
	 * Führt für jeden Status, den der, mit dieser Instanz verknüpfte Enum-Typ annehmen kann einen Test aus. Dabei
	 * werden die
	 * folgenden Methoden in der genannten Reihenfolge ausgeführt:
	 * 1. {@link EnumTestSupport#beforeTest(Enum)}
	 * 2. {@link EnumTestSupport#executeTestFor(Enum)}
	 * 3. {@link EnumTestSupport#assertFor}}
	 * 4. {@link EnumTestSupport#afterTest(Enum)}}
	 */
	public void test() {
		Arrays.stream(enumType.getEnumConstants())
			.forEach(
				status -> {
					beforeTest(status);
					R testResult = executeTestFor(status);
					assertFor(status, testResult);
					afterTest(status);
				}
			);
	}

	/**
	 * Führt alle Anweisungen vor dem Test für den gegebenen Enum-Status aus.
	 *
	 * @param state Der aktuelle Enum-Status mit dem der Test ausgeführt wird.
	 */
	public abstract void beforeTest(E state);

	/**
	 * Führt alle Test-Anweisungen für den gegebenen Status aus. Wird nach {@link EnumTestSupport#beforeTest(Enum)}
	 * ausgeführt.
	 *
	 * @param state Der aktuelle Enum-Status mit dem der Test ausgeführt wird.
	 * @return Der Rückgabewert des Tests - dieser wird an {@link EnumTestSupport#assertFor(Enum, Object)} übergeben und
	 * kann
	 * dann in den Asserts des Testframeworks verwendet werden.
	 */
	public abstract R executeTestFor(E state);

	/**
	 * Führt alle Assertions für den zuvor ausgeführten Test aus. Wird nach {@link EnumTestSupport#executeTestFor(Enum)}
	 * ausgef
	 * ührt.,
	 *
	 * @param state Der aktuelle Enum-Status des zuvor ausgeführten Tests.
	 * @param testResult Das Ergebnis des zuvor ausgeführten Tests.
	 */
	public abstract void assertFor(E state, R testResult);

	/**
	 * Führt alle finalen Anweisungen für den zuvor erfolgten Test aus. Wird nach
	 * {@link EnumTestSupport#assertFor(Enum, Object)} ausgeführt.
	 *
	 * @param state Der aktuelle Enum-State des zuvor ausgeführten Tests.
	 */
	public abstract void afterTest(E state);
}
