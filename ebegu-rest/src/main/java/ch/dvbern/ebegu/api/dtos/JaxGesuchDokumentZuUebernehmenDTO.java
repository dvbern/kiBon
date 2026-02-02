package ch.dvbern.ebegu.api.dtos;

import java.util.List;

import jakarta.validation.Valid;

public record JaxGesuchDokumentZuUebernehmenDTO(@Valid List<JaxDokumentZuUebernehmen> dokumentZuUebernehmen,
												@Valid JaxId gesuchId) {
}
