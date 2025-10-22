import {Component, OnInit} from '@angular/core';
import {UIRouterGlobals} from '@uirouter/core';
import {GesuchRS} from '../../../gesuch/service/gesuchRS.rest';

@Component({
    selector: 'dv-zpv-nr-success',
    templateUrl: './zpv-nr-success.component.html',
    styleUrls: ['./zpv-nr-success.component.less'],
    standalone: false
})
export class ZpvNrSuccessComponent implements OnInit {
    public isZpvNummerErfolgreichVerknuepft: boolean;

    public constructor(
        private readonly gesuchRS: GesuchRS,
        private readonly uiRouterGlobals: UIRouterGlobals
    ) {}

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
