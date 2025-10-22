package ch.dvbern.ebegu.util.mandant;

public abstract class AbstractMandantDefaultVisitor<T> implements
	MandantVisitor<T> {

	protected abstract T visitDefault();

	@Override
	public T visitBern() {
		return visitDefault();
	}

	@Override
	public T visitLuzern() {
		return visitDefault();
	}

	@Override
	public T visitSolothurn() {
		return visitDefault();
	}

	@Override
	public T visitAppenzellAusserrhoden() {
		return visitDefault();
	}

	@Override
	public T visitSchwyz() {
		return visitDefault();
	}

	@Override
	public T visitZug() {
		return visitDefault();
	}

	@Override
	public T visitDvb() {
		return visitDefault();
	}
}
