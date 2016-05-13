package ch.dvbern.ebegu.entities;

import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Entity fuer PensumFachstelle.
 */
@Audited
@Entity
public class PensumFachstelle extends AbstractPensumEntity {

	private static final long serialVersionUID = -9132257320978374570L;

	@ManyToOne(optional = false)
	private Fachstelle fachstelle;

	public Fachstelle getFachstelle() {
		return fachstelle;
	}

	public void setFachstelle(Fachstelle fachstelle) {
		this.fachstelle = fachstelle;
	}
}