package ch.dvbern.ebegu.services.steuerabfrage.nesko;

import java.net.URI;
import java.util.Objects;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.UriBuilder;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.HasStekIdentifier;
import ch.dvbern.ebegu.nesko.StekUtil;

import static ch.dvbern.ebegu.gesuchsteller.GesuchstellerUtil.isSecondGSPresent;

/**
 * Util class for the linking process of the STEK identifier
 *
 * @see <a href="https://www.belex.sites.be.ch/app/de/texts_of_law/152.052">BSG 152.052 - Verordnung über die Zentrale
 * Personenverwaltung (ZPV V)</a>
 * @see <a href="https://intra.dvbern.ch/spaces/KIB/pages/290883081/Verkn%C3%BCpfungsprozess+ZPV-Nummer">The internal
 * technical kiBon docs</a>
 */
public final class STEKIdentifierUpdateUtil {

	/**
	 * Creates the response to redirect the user to the frontend page where the result of the process is then displayed.
	 *
	 * @param request the request made to the backend during the fake login of the zpv linking process
	 * @param frontendReturnPath where in the frontend the user is to redirected
	 * @param result the {@link STEKUpdateResult} of the zpv linking process
	 * @return the {@link ResponseBuilder} with the redirect
	 */
	public static ResponseBuilder redirectToFrontend(
		HttpServletRequest request,
		String frontendReturnPath,
		STEKUpdateResult result
	) {
		var fragment = frontendReturnPath + "?zpvUpdateResult=" + result;
		var url = UriBuilder.fromUri(
			URI.create(request.getRequestURL().toString())
		)
			.scheme("https")
			.replacePath(null)
			.fragment(fragment)
			.build();
		return Response.temporaryRedirect(url);
	}

	/**
	 * Checks if the ZPV-Nummer is already set on either the {@link Benutzer} or the other {@link Gesuchsteller}.
	 * Does not check if it is already set on the {@link Gesuchsteller} on the {@link GesuchstellerContainer} that is to
	 * be
	 * edited since this value is allowed to be overwritten.
	 *
	 * @see <a href="https://www.belex.sites.be.ch/app/de/texts_of_law/152.052">BSG 152.052 - Verordnung über die
	 * Zentrale Personenverwaltung (ZPV V)</a>
	 *
	 * @param zpvNummer the ZPV-Nummer to be set
	 * @param gesuch the {@link Gesuch} to be checked
	 * @param containerIdOfGSEdited the {@link GesuchstellerContainer} on which the zpvNummer should be set
	 * @return whether the ZPV-Nummer is already used in the same gesuch
	 */
	public static boolean isZPVAlreadyUsedInGesuch(
		String zpvNummer,
		Gesuch gesuch,
		String containerIdOfGSEdited
	) {
		return isSTEKIdentifierAlreadyUsedInGesuch(
			HasStekIdentifier::getZpvNummer,
			zpvNummer,
			gesuch,
			containerIdOfGSEdited
		);
	}

	private static boolean isSTEKIdentifierAlreadyUsedInGesuch(
		@Nonnull Function<HasStekIdentifier, String> stekMapper,
		@Nonnull String identifier,
		Gesuch gesuch,
		String containerIdOfGSEdited
	) {
		if (gesuch.getDossier().getFall().getBesitzer() == null) {
			throw new IllegalStateException(
				"Besitzer of fall must not be null in stek linking process"
			);
		}
		var identifierBesitzerin = stekMapper.apply(
			gesuch.getDossier().getFall().getBesitzer()
		);

		if (isSameIdentifier(identifierBesitzerin, identifier)) {
			return true;
		}
		if (!isSecondGSPresent(gesuch)) {
			return false;
		}
		var otherGS = getOtherGS(gesuch, containerIdOfGSEdited);
		var identifierOtherGS = stekMapper.apply(otherGS);

		return isSameIdentifier(identifierOtherGS, identifier);
	}

	/**
	 * Same as {@link #isZPVAlreadyUsedInGesuch(String, Gesuch, String)}, but for the AHV-Nummer. All values are
	 * normalized to their digits before they are compared.
	 *
	 * @param ahvNummer the AHV-Nummer to be set
	 * @param gesuch the {@link Gesuch} to be checked
	 * @param containerIdOfGSEdited the {@link GesuchstellerContainer} on which the ahvNummer should be set
	 * @return whether the AHV-Nummer is already used in the same gesuch
	 */
	public static boolean isAHVAlreadyUsedInGesuch(
		String ahvNummer,
		Gesuch gesuch,
		String containerIdOfGSEdited
	) {
		return isSTEKIdentifierAlreadyUsedInGesuch(
			HasStekIdentifier::getAhvNummer,
			ahvNummer,
			gesuch,
			containerIdOfGSEdited
		);
	}

	private static boolean isSameIdentifier(
		@Nullable String storedIdentifier,
		String identifier
	) {
		return storedIdentifier != null
			&& StekUtil.normalizeStekIdentifier(storedIdentifier)
				.equals(StekUtil.normalizeStekIdentifier(identifier));
	}

	@Nullable
	private static Gesuchsteller getOtherGS(
		Gesuch gesuch,
		String gesuchstellerContainerId
	) {
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		Objects.requireNonNull(gesuch.getGesuchsteller2());

		return gesuch.getGesuchsteller1()
			.getId()
			.equals(gesuchstellerContainerId) ?
				gesuch.getGesuchsteller2().getGesuchstellerJA() :
				gesuch.getGesuchsteller1().getGesuchstellerJA();
	}

}
