/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.util.mandant;

import java.net.URI;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import com.google.common.net.InternetDomainName;

public enum MandantIdentifier {

	BERN {
		@Override
		public <T> T accept(MandantVisitor<T> visitor) {
			return visitor.visitBern();
		}

		@Override
		public String getUrlCode() {
			return "be";
		}

		@Override
		public boolean hasIdentityProvider() {
			return true;
		}
	},
	LUZERN {
		@Override
		public <T> T accept(MandantVisitor<T> visitor) {
			return visitor.visitLuzern();
		}

		@Override
		public String getUrlCode() {
			return "lu";
		}

		@Override
		public boolean hasIdentityProvider() {
			return false;
		}

		@Override
		public String getRealmName() {
			return "luzern";
		}
	},
	SOLOTHURN {
		@Override
		public <T> T accept(MandantVisitor<T> visitor) {
			return visitor.visitSolothurn();
		}

		@Override
		public String getUrlCode() {
			return "so";
		}

		@Override
		public boolean hasIdentityProvider() {
			return false;
		}
	},
	APPENZELL_AUSSERRHODEN {
		@Override
		public <T> T accept(MandantVisitor<T> visitor) {
			return visitor.visitAppenzellAusserrhoden();
		}

		@Override
		public String getUrlCode() {
			return "ar";
		}

		@Override
		public boolean hasIdentityProvider() {
			return false;
		}
	},
	SCHWYZ {
		@Override
		public <T> T accept(MandantVisitor<T> visitor) {
			return visitor.visitSchwyz();
		}

		@Override
		public String getUrlCode() {
			return "sz";
		}

		@Override
		public boolean hasIdentityProvider() {
			return false;
		}
	},
	ZUG {
		@Override
		public <T> T accept(MandantVisitor<T> visitor) {
			return visitor.visitZug();
		}

		@Override
		public String getUrlCode() {
			return "zg";
		}

		@Override
		public boolean hasIdentityProvider() {
			return false;
		}
	},
	DVB {
		@Override
		public <T> T accept(MandantVisitor<T> visitor) {
			return visitor.visitDvb();
		}

		@Override
		public String getUrlCode() {
			return "dv";
		}

		@Override
		public boolean hasIdentityProvider() {
			return false;
		}
	};

	public abstract <T> T accept(MandantVisitor<T> visitor);

	public abstract String getUrlCode();

	public abstract boolean hasIdentityProvider();

	public String getRealmName() {
		return this.name().toLowerCase(Locale.ROOT);
	}

	public static List<MandantIdentifier> getAll() {
		return List.of(MandantIdentifier.values());
	}

	public static Optional<MandantIdentifier> findByUrlCode(String urlCode) {
		for (MandantIdentifier mandantIdentifier : getAll()) {
			if (mandantIdentifier.getUrlCode().equals(urlCode)) {
				return Optional.of(mandantIdentifier);
			}
		}
		return Optional.empty();
	}

	public static Optional<MandantIdentifier> findByHostname(URI uri) {
		InternetDomainName domainName = InternetDomainName.from(uri.getHost());
		if (domainName.parts().size() != 3) {
			return Optional.empty();
		}
		var subdomain = domainName.parts().get(0);
		var urlCode = subdomain.contains("-") ?
			subdomain.split("-", 0)[1] :
			subdomain;
		return MandantIdentifier.findByUrlCode(urlCode);
	}

	public static MandantIdentifier getByHostname(URI uri) {
		return MandantIdentifier.findByHostname(uri)
			.orElseThrow(
				() -> new EbeguRuntimeException(
					"getMandantFromHostname",
					MessageFormat.format(
						"could not extract mandant from {0}",
						uri
					)
				)
			);
	}
}
