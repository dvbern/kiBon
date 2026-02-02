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

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EasyMockExtension.class)
public class EasyMockTestSupport extends EasyMockSupport {

	/**
	 * Enthält alle, im Scope einer Testmethode definierten Mocks. Vereinfacht dann die Calls:
	 * {@link EasyMock#replay(Object...)}, {@link EasyMock#verify(Object...)}, {@link EasyMock#reset(Object...)}.
	 * Um Mocks zu erstellen und automatisch dieser Liste hinzuzufügen, kann die Methode:
	 * {@link SozialhilfeBezieherBeanTest#createNonInjectedMock(Class)} verwendet werden.
	 * Die Liste wird am Ende eines jeden Tests geleert.
	 */
	private final List<Object> nonInjectedMocks = new ArrayList<>();

	@AfterEach
	void afterEach() {
		this.resetAll();
		nonInjectedMocks.clear();
	}

	/**
	 * Setzt alle injizierten und via {@link EasyMockTestSupport#createdMock(Class)} erzeugten Mocks zurück.
	 */
	@Override
	public void resetAll() {
		super.resetAll();
		EasyMock.reset(nonInjectedMocks.toArray());
	}

	/**
	 * Führt alle injizierten und via {@link EasyMockTestSupport#createdMock(Class)} erzeugten Mocks aus.
	 */
	@Override
	public void replayAll() {
		super.replayAll();
		EasyMock.replay(nonInjectedMocks.toArray());
	}

	/**
	 * Verifiziert alle injizierten und via {@link EasyMockTestSupport#createdMock(Class)} erzeugten Mocks.
	 */
	@Override
	public void verifyAll() {
		super.verifyAll();
		EasyMock.verify(nonInjectedMocks.toArray());
	}

	/**
	 * Erzeugt ein neues Mock, dass von dieser Instanz verwaltet wird. So erzeugte Mocks können mit werden nach jedem
	 * Test
	 * ({@link AfterEach} verworfen und sie können mit {@link EasyMockTestSupport#verifyAll()} ()},
	 * {@link EasyMockTestSupport#replayAll()} und {@link EasyMockTestSupport#resetAll()} gesteuert werden.
	 *
	 * @param toMock Der Datentyp des zu erzeugenden Mocks.
	 * @param <T> Der Datentyp mit dem das erzeugte Mock identifiziert werden soll.
	 * @return Ein neues Mock des gegebenen Datentyps.
	 */
	protected <T> T createdMock(Class<?> toMock) {
		T mock = EasyMock.createMock(toMock);
		nonInjectedMocks.add(mock);
		return mock;
	}
}
