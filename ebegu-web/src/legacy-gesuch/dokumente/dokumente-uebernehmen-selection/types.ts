import {TSDokumentGrund} from '../../../models/TSDokumentGrund';
import {TSDokument} from '../../../models/TSDokument';

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
