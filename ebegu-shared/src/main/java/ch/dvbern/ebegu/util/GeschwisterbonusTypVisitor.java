package ch.dvbern.ebegu.util;

public interface GeschwisterbonusTypVisitor<T> {

	T visitSchwyz();

	T visitSchwyz2();

	T visitLuzern();

	T visitNone();
}
