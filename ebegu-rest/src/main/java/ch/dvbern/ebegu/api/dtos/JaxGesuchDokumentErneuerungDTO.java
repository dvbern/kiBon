package ch.dvbern.ebegu.api.dtos;

import java.util.List;

import jakarta.validation.Valid;

public record JaxGesuchDokumentErneuerungDTO(@Valid List<JaxDokumentErneuerung> dokumentErneuerungen,
											 @Valid JaxId gesuchId) {
}
