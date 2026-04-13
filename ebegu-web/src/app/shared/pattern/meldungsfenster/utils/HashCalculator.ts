import {MeldungsfensterData} from '../../../../../models/meldungsfenster';

export abstract class HashCalculator {
    static getHash(meldung: MeldungsfensterData): string {
        return this.djb2Hash(JSON.stringify(Object.values(meldung)));
    }

    // djb2Hash algorithm because browser dont support native hashing (f.e. with crypto)
    private static djb2Hash(str: string): string {
        let hash = 5381;
        for (let i = 0; i < str.length; i++) {
            hash = (hash * 33) ^ str.charCodeAt(i);
        }
        return (hash >>> 0).toString(16);
    }
}
