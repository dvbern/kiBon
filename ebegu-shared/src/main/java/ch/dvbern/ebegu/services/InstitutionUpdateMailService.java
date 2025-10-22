package ch.dvbern.ebegu.services;

import ch.dvbern.ebegu.entities.InstitutionStammdaten;

public interface InstitutionUpdateMailService {

	void sendAuszahlungsdatenUpdatedInfo(
		InstitutionStammdaten institutionStammdaten
	);
}
