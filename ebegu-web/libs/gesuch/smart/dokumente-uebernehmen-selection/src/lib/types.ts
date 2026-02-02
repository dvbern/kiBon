import {TSDokumentGrund} from '../../../../../../src/models/TSDokumentGrund';
import {TSDokument} from '../../../../../../src/models/TSDokument';

export type GrundWithDokumentDecision = {
    grund: TSDokumentGrund;
    decision: DokumentDecision;
};

export type GrundGroupWithDokumentDecisions = {
    grund: TSDokumentGrund;
    dokumente: DokumentDecision[];
};

export type DokumentDecision = {
    dokument: TSDokument;
    erneuern: boolean;
};
