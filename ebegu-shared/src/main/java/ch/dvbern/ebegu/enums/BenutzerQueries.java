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

package ch.dvbern.ebegu.enums;

import java.time.LocalDate;
import java.util.List;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Mandant;
import org.hibernate.annotations.processing.HQL;

public interface BenutzerQueries {
	@HQL("where email = :email and mandant = :mandant")
	Benutzer findByEmail(String email, Mandant mandant);

	@HQL("where email like '%.persona@mailbucket.dvbern.ch' and mandant = :mandant")
	List<Benutzer> findLocalLoginUsers(Mandant mandant);

	@HQL("from Benutzer where mandant = :mandant and status = :status and extract(date from timestampMutiert) < :expirationDate")
	List<Benutzer> getExpiredEingeladeneBenutzer(
		Mandant mandant,
		BenutzerStatus status, // for some reason enum literals (https://docs.jboss.org/hibernate/orm/6.4/querylanguage/html_single/Hibernate_Query_Language.html#enum-literals) do not work in this query with Hibernate 6.4
		LocalDate expirationDate
	);
}
