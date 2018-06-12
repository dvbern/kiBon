/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.entities;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.dto.suchfilter.lucene.EBEGUGermanAnalyzer;
import ch.dvbern.ebegu.dto.suchfilter.lucene.Searchable;
import ch.dvbern.ebegu.validationgroups.ChangeVerantwortlicherBGValidationGroup;
import ch.dvbern.ebegu.validationgroups.ChangeVerantwortlicherGMDEValidationGroup;
import ch.dvbern.ebegu.validationgroups.ChangeVerantwortlicherTSValidationGroup;
import ch.dvbern.ebegu.validators.CheckVerantwortlicherBG;
import ch.dvbern.ebegu.validators.CheckVerantwortlicherGMDE;
import ch.dvbern.ebegu.validators.CheckVerantwortlicherTS;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.bridge.builtin.LongBridge;

@Audited
@Entity
@Indexed
@Analyzer(impl = EBEGUGermanAnalyzer.class)
@Table(
	uniqueConstraints = @UniqueConstraint(columnNames = { "fall_id", "gemeinde_id" }, name = "UK_dossier_fall_gemeinde"),
	indexes = {
		@Index(name = "IX_dossier_verantwortlicher_bg", columnList = "verantwortlicherBG_id"),
		@Index(name = "IX_dossier_verantwortlicher_ts", columnList = "verantwortlicherTS_id"),
		@Index(name = "IX_dossier_verantwortlicher_gmde", columnList = "verantwortlicherGMDE_id"),
	}
)
@CheckVerantwortlicherBG(groups = ChangeVerantwortlicherBGValidationGroup.class)
@CheckVerantwortlicherTS(groups = ChangeVerantwortlicherTSValidationGroup.class)
@CheckVerantwortlicherGMDE(groups = ChangeVerantwortlicherGMDEValidationGroup.class)
public class Dossier extends AbstractEntity implements Searchable {

	private static final long serialVersionUID = -2511152887055775241L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_dossier_fall_id"))
	@IndexedEmbedded
	private Fall fall;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_dossier_gemeinde_id"))
	private Gemeinde gemeinde;

	@NotNull
	@Column(nullable = false)
	@Field(bridge = @FieldBridge(impl = LongBridge.class))
	private long dossierNummer = 0;

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_dossier_verantwortlicher_bg_id"))
	private Benutzer verantwortlicherBG = null; // Mitarbeiter des JA

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_dossier_verantwortlicher_ts_id"))
	private Benutzer verantwortlicherTS = null; // Mitarbeiter des SCH

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_dossier_verantwortlicher_gmde_id"))
	private Benutzer verantwortlicherGMDE = null; // Mitarbeiter der Gemeinde

	@Nonnull
	public Fall getFall() {
		return fall;
	}

	public void setFall(@Nonnull Fall fall) {
		this.fall = fall;
	}

	@Nonnull
	public Gemeinde getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(@Nonnull Gemeinde gemeinde) {
		this.gemeinde = gemeinde;
	}

	public long getDossierNummer() {
		return dossierNummer;
	}

	public void setDossierNummer(long dossierNummer) {
		this.dossierNummer = dossierNummer;
	}

	@Nullable
	public Benutzer getVerantwortlicherBG() {
		return verantwortlicherBG;
	}

	public void setVerantwortlicherBG(@Nullable Benutzer verantwortlicherBG) {
		this.verantwortlicherBG = verantwortlicherBG;
	}

	@Nullable
	public Benutzer getVerantwortlicherTS() {
		return verantwortlicherTS;
	}

	public void setVerantwortlicherTS(@Nullable Benutzer verantwortlicherTS) {
		this.verantwortlicherTS = verantwortlicherTS;
	}

	@Nullable
	public Benutzer getVerantwortlicherGMDE() {
		return verantwortlicherGMDE;
	}

	public void setVerantwortlicherGMDE(@Nullable Benutzer verantwortlicherGMDE) {
		this.verantwortlicherGMDE = verantwortlicherGMDE;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (!(other instanceof Dossier)) {
			return false;
		}
		if (!super.equals(other)) {
			return false;
		}
		Dossier dossier = (Dossier) other;
		return dossierNummer == dossier.dossierNummer &&
			Objects.equals(fall, dossier.fall) &&
			Objects.equals(gemeinde, dossier.gemeinde);
	}

	@Nonnull
	@Override
	public String getSearchResultId() {
		return getId();
	}

	@Nonnull
	@Override
	public String getSearchResultSummary() {
		return getFall().getPaddedFallnummer();
	}

	@Nullable
	@Override
	public String getSearchResultAdditionalInformation() {
		return getFall().toString();
	}

	@Nullable
	@Override
	public String getOwningGesuchId() {
		//haben wir hier nicht da das Dossier nicht zu einem Gesuch gehoert
		return null;
	}

	@Nullable
	@Override
	public String getOwningFallId() {
		return getFall().getId();
	}

	@Nullable
	@Override
	public String getOwningDossierId() {
		return getId();
	}

	/**
	 * wenn der VerantwortlicherBG oder GMDE gesetzt ist, wir er zurueckgegeben.
	 * Sonst wenn der VerantwortlicherTS gesetzt ist, wir er zurueckgegeben.
	 * Sonst wird null zurueckgegeben
	 */
	@Nullable
	public Benutzer getHauptVerantwortlicher() {
		Benutzer hauptverantwortlicher = this.getVerantwortlicherBG();
		if (hauptverantwortlicher == null) {
			hauptverantwortlicher = this.getVerantwortlicherGMDE();
			if (hauptverantwortlicher == null) {
				hauptverantwortlicher = this.getVerantwortlicherTS();
			}
		}
		return hauptverantwortlicher;
	}
}
