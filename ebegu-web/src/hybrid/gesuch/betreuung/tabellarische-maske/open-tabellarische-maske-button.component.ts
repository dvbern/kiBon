import {
    Component,
    inject,
    Input,
    OnInit,
    ChangeDetectionStrategy
} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {TranslatePipe} from '@ngx-translate/core';
import {LoadingButtonDirective} from '../../../../app/shared/directive/loading-button.directive';
import {TSDateRange} from '../../../../models/entity/TSDateRange';
import {TSGesuchsperiode} from '../../../../models/entity/TSGesuchsperiode';
import {TSBetreuungsangebotTyp} from '../../../../models/enums/TSBetreuungsangebotTyp';
import {TSBetreuung} from '../../../../models/TSBetreuung';
import {TSBetreuungspensum} from '../../../../models/TSBetreuungspensum';
import {TSBetreuungspensumContainer} from '../../../../models/TSBetreuungspensumContainer';
import {TabellarischeMaskeKitaComponent} from './angebote/tabellarische-maske-kita/tabellarische-maske-kita.component';
import {TabellarischeMaskeTfoComponent} from './angebote/tabellarische-maske-tfo/tabellarische-maske-tfo.component';
import {TabellarischeMaskeDialogData} from './types/types';
import {TabellarischeMaskeMittagstischComponent} from './angebote/tabellarische-maske-mittagstisch/tabellarische-maske-mittagstisch.component';
import {ErrorServiceX} from '../../../../app/core/errors/service/ErrorServiceX';
import {EbeguUtil} from '../../../../utils/EbeguUtil';

@Component({
    templateUrl: 'open-tabellarische-maske-button.component.html',
    imports: [LoadingButtonDirective, TranslatePipe],
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './open-tabellarische-maske-button.component.scss'
})
export class OpenTabellarischeMaskeButtonComponent implements OnInit {
    // we cannot use signal input, because then we cannot downgrade the component
    @Input({required: true}) buttonDisabled!: boolean;
    @Input({required: true}) betreuung!: TSBetreuung;
    @Input({required: true}) gesuchsperiode!: TSGesuchsperiode;
    @Input({required: true}) multiplierKita!: number;
    @Input({required: true}) multiplierTfo!: number;
    @Input({required: true}) formDirty!: boolean;
    @Input({required: true}) anwesenheitstageMonatActivated!: boolean;

    private readonly dialog = inject(MatDialog);
    private readonly errorService = inject(ErrorServiceX);

    private componentMap: {[key: string]: {component: any; multiplier: number}};

    public ngOnInit(): void {
        this.componentMap = {
            KITA: {
                component: TabellarischeMaskeKitaComponent,
                multiplier: this.multiplierKita
            },
            TAGESFAMILIEN: {
                component: TabellarischeMaskeTfoComponent,
                multiplier: this.multiplierTfo
            },
            MITTAGSTISCH: {
                component: TabellarischeMaskeMittagstischComponent,
                multiplier: 1
            }
        };
    }

    openDialog() {
        if (this.hasOnlyEmptyBetreuungspensen(this.betreuung)) {
            this.errorService.addMesageAsError(
                'INFO_EMPTY_BETREUUNG_TABELLARISCHE_MASKE'
            );
            return;
        }
        if (
            this.formDirty ||
            this.hasAnyEmptyBetreuungspensen(this.betreuung)
        ) {
            this.errorService.addMesageAsError(
                'INFO_FORM_DIRTY_TABELLARISCHE_MASKE'
            );
            return;
        }
        const componentToOpen =
            this.componentMap[this.betreuung.getAngebotTyp()];

        this.dialog
            .open(componentToOpen.component, {
                width: '80wh',
                height: '80vh',
                data: {
                    betreuung: this.betreuung,
                    gesuchsperiode: this.gesuchsperiode,
                    einstellungen: {
                        multiplier: componentToOpen.multiplier
                    },
                    templateVariables: {
                        kindName: this.betreuung.kindFullname,
                        institutionName:
                            this.betreuung.institutionStammdaten.institution
                                .name
                    }
                } satisfies TabellarischeMaskeDialogData
            })
            .afterClosed()
            .subscribe((res: undefined | TSBetreuungspensum[]) => {
                if (!res) {
                    return;
                }
                this.betreuung.betreuungspensumContainers = res
                    .filter(betreuungspensum => {
                        return this.isNotZeroPensum(
                            betreuungspensum,
                            this.betreuung.getAngebotTyp()
                        );
                    })
                    .map(betreuungspensum =>
                        this.fillNullValues(
                            this.betreuung.getAngebotTyp(),
                            betreuungspensum
                        )
                    )
                    .map(
                        betreuungspensum =>
                            new TSBetreuungspensumContainer(
                                undefined,
                                betreuungspensum
                            )
                    );
            });
    }

    private hasAnyEmptyBetreuungspensen(betreuung: TSBetreuung): boolean {
        return betreuung.betreuungspensumContainers
            .map(container => container.betreuungspensumJA)
            .reduce((hasEmpty, current) => {
                return (
                    hasEmpty ||
                    this.isIncompletePensum(current, betreuung.getAngebotTyp())
                );
            }, false);
    }

    private isIncompletePensum(
        pensum: TSBetreuungspensum,
        angebotTyp: TSBetreuungsangebotTyp
    ) {
        switch (angebotTyp) {
            case TSBetreuungsangebotTyp.KITA:
            // we should check for the optional field addable by einstellung
            case TSBetreuungsangebotTyp.TAGESFAMILIEN:
                return (
                    EbeguUtil.isNullOrUndefined(pensum.pensum) ||
                    EbeguUtil.isNullOrUndefined(
                        pensum.monatlicheBetreuungskosten
                    ) ||
                    this.isIncompleteGueltigkeit(pensum.gueltigkeit)
                );
            case TSBetreuungsangebotTyp.MITTAGSTISCH:
                return (
                    EbeguUtil.isNullOrUndefined(pensum.tarifProHauptmahlzeit) ||
                    EbeguUtil.isNullOrUndefined(
                        pensum.monatlicheHauptmahlzeiten
                    ) ||
                    this.isIncompleteGueltigkeit(pensum.gueltigkeit)
                );
            default:
                throw new Error(
                    'Tabellarische Maske not implemented for ' + angebotTyp
                );
        }
    }

    private isIncompleteGueltigkeit(gueltigkeit: TSDateRange) {
        return (
            EbeguUtil.isNullOrUndefined(gueltigkeit) ||
            EbeguUtil.isNullOrUndefined(gueltigkeit.gueltigAb)
        );
    }

    private isNotZeroPensum(
        betreuungspensum: TSBetreuungspensum,
        angebotTyp: TSBetreuungsangebotTyp
    ) {
        switch (angebotTyp) {
            case TSBetreuungsangebotTyp.KITA:
            case TSBetreuungsangebotTyp.TAGESFAMILIEN:
                return (
                    this.isNotUndefinedOrZero(betreuungspensum.pensum) ||
                    this.isNotUndefinedOrZero(
                        betreuungspensum.monatlicheBetreuungskosten
                    )
                );
            case TSBetreuungsangebotTyp.MITTAGSTISCH:
                return (
                    this.isNotUndefinedOrZero(
                        betreuungspensum.monatlicheHauptmahlzeiten
                    ) ||
                    this.isNotUndefinedOrZero(
                        betreuungspensum.tarifProHauptmahlzeit
                    )
                );
            default:
                throw new Error(
                    'Tabellarische Maske not implemented for ' + angebotTyp
                );
        }
    }

    private isNotUndefinedOrZero(value: number | null | undefined) {
        return EbeguUtil.isNotNullOrUndefined(value) && value !== 0;
    }

    private hasOnlyEmptyBetreuungspensen(betreuung: TSBetreuung) {
        for (const pensum of betreuung.betreuungspensumContainers.map(
            container => container.betreuungspensumJA
        )) {
            if (!this.isIncompletePensum(pensum, betreuung.getAngebotTyp())) {
                return false;
            }
        }
        return true;
    }

    private fillNullValues(
        angebotstyp: TSBetreuungsangebotTyp,
        betreuungspensum: TSBetreuungspensum
    ) {
        switch (angebotstyp) {
            case TSBetreuungsangebotTyp.KITA:
                this.fillPensumNullValues(betreuungspensum);
                break;
            case TSBetreuungsangebotTyp.TAGESFAMILIEN:
                this.fillPensumNullValues(betreuungspensum);
                if (
                    this.anwesenheitstageMonatActivated &&
                    EbeguUtil.isNullOrUndefined(betreuungspensum.betreuteTage)
                ) {
                    betreuungspensum.betreuteTage = 0;
                }
                break;
            case TSBetreuungsangebotTyp.MITTAGSTISCH:
                if (
                    EbeguUtil.isNullOrUndefined(
                        betreuungspensum.monatlicheHauptmahlzeiten
                    )
                ) {
                    betreuungspensum.monatlicheHauptmahlzeiten = 0;
                }
                if (
                    EbeguUtil.isNullOrUndefined(
                        betreuungspensum.tarifProHauptmahlzeit
                    )
                ) {
                    betreuungspensum.tarifProHauptmahlzeit = 0;
                }
        }
        return betreuungspensum;
    }

    private fillPensumNullValues(betreuungspensum: TSBetreuungspensum) {
        if (EbeguUtil.isNullOrUndefined(betreuungspensum.pensum)) {
            betreuungspensum.pensum = 0;
        }
        if (
            EbeguUtil.isNullOrUndefined(
                betreuungspensum.monatlicheBetreuungskosten
            )
        ) {
            betreuungspensum.monatlicheBetreuungskosten = 0;
        }
    }
}
