import {
    ClosedMeldungsfenster,
    MeldungsfensterStatus
} from '@models/meldungsfenster';

export abstract class MeldungsfensterLocalStorageUtil {
    private static readonly EXPIRY_MILLI: number = 28800000; //8 hours in milliseconds
    private static readonly LOCALSTORAGE_NAME = 'closedMeldungsfenster';

    static expireMeldungsfensterAndGet() {
        const all = this.readFromLocalStorage();
        const now = new Date().getTime();

        for (const hash of all.keys()) {
            const meldungsfenster = all.get(hash)!;
            // Cleanup localStorage if bisDatum is in the past
            if (
                this.hasExpired(meldungsfenster, now) ||
                this.isWichtigWithExpiredClose(meldungsfenster, now)
            ) {
                all.delete(hash);
            }
        }
        this.updateLocalStorage(all);
        return all;
    }

    private static readFromLocalStorage(): Map<string, ClosedMeldungsfenster> {
        return new Map(
            JSON.parse(localStorage.getItem(this.LOCALSTORAGE_NAME) || '[]')
        );
    }

    private static isWichtigWithExpiredClose(
        meldungsfenster: ClosedMeldungsfenster,
        now: number
    ) {
        return (
            meldungsfenster.status === MeldungsfensterStatus.WICHTIG &&
            meldungsfenster.closeTime + this.EXPIRY_MILLI <= now
        );
    }

    private static hasExpired(
        meldungsfenster: ClosedMeldungsfenster,
        now: number
    ) {
        return (
            meldungsfenster.bisDatum &&
            new Date(meldungsfenster.bisDatum).getTime() <= now
        );
    }

    static updateLocalStorage(updated: Map<string, ClosedMeldungsfenster>) {
        localStorage.setItem(
            this.LOCALSTORAGE_NAME,
            JSON.stringify(Array.from(updated))
        );
    }
}
