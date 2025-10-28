import {Component, OnInit, inject} from '@angular/core';
import {UIRouterGlobals} from '@uirouter/core';
import {GesuchRS} from '../../../gesuch/service/gesuchRS.rest';

@Component({
    selector: 'dv-zpv-nr-success',
    templateUrl: './zpv-nr-success.component.html',
    styleUrls: ['./zpv-nr-success.component.less'],
    standalone: false
})
export class ZpvNrSuccessComponent implements OnInit {
    private readonly gesuchRS = inject(GesuchRS);
    private readonly uiRouterGlobals = inject(UIRouterGlobals);

    public isZpvNummerErfolgreichVerknuepft: boolean;

    public ngOnInit(): void {
        this.gesuchRS
            .zpvNummerErfolgreichVerknuepft(
                this.uiRouterGlobals.params.gesuchstellerId
            )
            .then(
                isErfolgreich =>
                    (this.isZpvNummerErfolgreichVerknuepft = isErfolgreich)
            );
    }
}
