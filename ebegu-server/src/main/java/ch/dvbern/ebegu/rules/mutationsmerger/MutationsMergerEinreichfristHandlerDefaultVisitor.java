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

public class MutationsMergerEinreichfristHandlerDefaultVisitor extends
	AbstractMandantDefaultVisitor<AbstractMutationsMergerEinreichfristHandler> {
	private final Locale locale;

	public MutationsMergerEinreichfristHandlerDefaultVisitor(Locale locale) {
		this.locale = locale;
	}

	public AbstractMutationsMergerEinreichfristHandler getEinreichfristHandler(
		@NotNull MandantIdentifier mandant
	) {
		return mandant.accept(this);
	}

	@Override
	protected AbstractMutationsMergerEinreichfristHandler visitDefault() {
		return new MutationsMergerEinreichfristHandlerDefault(locale);
	}

	@Override
	public AbstractMutationsMergerEinreichfristHandler visitSchwyz() {
		return new MutationsMergerEinreichfristHandlerSchwyz(locale);
	}
}
