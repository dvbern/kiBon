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
 */

package ch.dvbern.ebegu.rules.mutationsmerger;

import java.util.Locale;

import ch.dvbern.ebegu.util.mandant.AbstractMandantDefaultVisitor;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import com.sun.istack.NotNull;

public class MutationsMergerAnspruchHandlerDefaultVisitor extends
	AbstractMandantDefaultVisitor<AbstractMutationsMergerAnspruchHandler> {

	private final Locale locale;

	public MutationsMergerAnspruchHandlerDefaultVisitor(Locale locale) {
		this.locale = locale;
	}

	public AbstractMutationsMergerAnspruchHandler getAnspruchHandler(
		@NotNull MandantIdentifier mandant
	) {
		return mandant.accept(this);
	}

	@Override
	protected AbstractMutationsMergerAnspruchHandler visitDefault() {
		return new MutationsMergerAnspruchHandler(locale);
	}

	@Override
	public AbstractMutationsMergerAnspruchHandler visitLuzern() {
		return new MutationsMergerAnspruchHandlerLuzern(locale);
	}

	@Override
	public AbstractMutationsMergerAnspruchHandler visitSchwyz() {
		return new MutationsMergerAnspruchHandlerSchwyz(locale);
	}
}
