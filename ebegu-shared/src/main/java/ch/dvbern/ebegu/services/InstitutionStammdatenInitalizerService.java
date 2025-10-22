package ch.dvbern.ebegu.services;

import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.InstitutionStammdaten;

public interface InstitutionStammdatenInitalizerService {

	InstitutionStammdaten initInstitutionStammdatenBetreuungsgutschein();

	InstitutionStammdaten initInstitutionStammdatenTagesschule(
		@Nullable String gemeindeId
	);

	InstitutionStammdaten initInstitutionStammdatenFerieninsel(
		@Nullable String gemeindeId
	);
}
