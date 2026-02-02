package ch.dvbern.ebegu.dokumente;

import ch.dvbern.ebegu.entities.Dokument;
import ch.dvbern.ebegu.entities.DokumentGrund;

public record DokumentZuUebernehmen(DokumentGrund grund, Dokument dokument) {
}
