package ch.dvbern.ebegu.services;

import java.util.Collection;
import java.util.List;

import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;

@Stateless
@Local(InstitutionUpdateMailService.class)
public class InstitutionUpdateMailServiceBean implements
	InstitutionUpdateMailService {

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private MailService mailService;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void sendAuszahlungsdatenUpdatedInfo(
		InstitutionStammdaten institutionStammdaten
	) {
		List<Gemeinde> infomaGemeinden = gemeindeService
			.getGemeindenWithInfoma(
				institutionStammdaten.getInstitution().getMandant()
			);
		infomaGemeinden.stream()
			.filter(
				gemeinde -> isInstiutionActivatedForGemeinde(
					gemeinde,
					institutionStammdaten
				)
			)
			.forEach(
				gemeinde -> sendeUpdateInfoMail(
					gemeinde,
					institutionStammdaten
				)
			);
	}

	private void sendeUpdateInfoMail(
		Gemeinde gemeinde,
		InstitutionStammdaten institutionStammdaten
	) {
		benutzerService.getAktivGemeindeAdministratoren(gemeinde)
			.forEach(
				gemeindeAdmin -> mailService
					.prepareToSendInfoAuszahlungsdatenChanged(
						institutionStammdaten,
						gemeindeAdmin.getEmail()
					)
			);
	}

	private boolean isInstiutionActivatedForGemeinde(
		Gemeinde gemeinde,
		InstitutionStammdaten institutionStammdaten
	) {
		Collection<InstitutionStammdaten> activatedInstuitonenByGemeinde =
			institutionStammdatenService
				.getAllInstitutionStammdatenByGemeinde(
					gemeinde.getId()
				);

		return activatedInstuitonenByGemeinde
			.stream()
			.anyMatch(i -> i.getId().equals(institutionStammdaten.getId()));
	}
}
