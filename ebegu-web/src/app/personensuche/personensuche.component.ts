import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {Observable} from 'rxjs';
import {GesuchModelManager} from '../../gesuch/service/gesuchModelManager';
import {TSEWKPerson} from '../../models/TSEWKPerson';
import {ErrorService} from '../core/errors/service/ErrorService';
import {EwkRS} from '../core/service/ewkRS.rest';

@Component({
    selector: 'dv-personensuche',
    templateUrl: './personensuche.component.html',
    styleUrls: ['./personensuche.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class PersonensucheComponent {
    private readonly ewkRS = inject(EwkRS);
    private readonly gesuchModelManager = inject(GesuchModelManager);
    private readonly errorService = inject(ErrorService);

    public personen$: Observable<TSEWKPerson[]> = null;

    public isGesuchsteller1New(): boolean {
        if (
            this.gesuchModelManager.getGesuch() &&
            this.gesuchModelManager.getGesuch().gesuchsteller1
        ) {
            return this.gesuchModelManager.getGesuch().gesuchsteller1.isNew();
        }
        return true;
    }

    public searchGesuchsteller(): void {
        this.errorService.clearAll();
        this.personen$ = this.ewkRS.sucheInEwk(this.getGesuchId());
    }

    private getGesuchId(): string | undefined {
        return this.gesuchModelManager.getGesuch()?.id;
    }

    public getShortDescription(ewkPerson: TSEWKPerson): string {
        return [
            ewkPerson.vorname,
            ewkPerson.nachname,
            ewkPerson.geburtsdatum?.format('DD.MM.YYYY'),
            ewkPerson.adresse?.ort
        ]
            .filter(token => !!token)
            .join(', ');
    }
}
