import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    OnInit,
    ViewChild,
    ViewEncapsulation,
    inject
} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {AdminUtilKeycloakAdminRsService} from '@kibon/admin-util-keycloak-admin-rs';
import {
    TSApplicationProperty,
    TSApplicationPropertyKey
} from '@kibon/shared/model/einstellung';
import {SharedUtilApplicationPropertyRsService} from '@kibon/shared/util/application-property-rs';
import {combineLatest} from 'rxjs';
import {DvNgOkDialogComponent} from '../../../app/core/component/dv-ng-ok-dialog/dv-ng-ok-dialog.component';
import {ErrorServiceX} from '../../../app/core/errors/service/ErrorServiceX';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {SearchRS} from '../../../gesuch/service/searchRS.rest';
import {AbstractAdminViewX} from '../../abstractAdminViewX';
import {ReindexRS} from '../../service/reindexRS.rest';
import {ConfigurableEinstellung} from '../EinstellungConfigurations';

@Component({
    selector: 'dv-admin-view-x',
    templateUrl: './admin-view-x.component.html',
    styleUrls: ['./admin-view-x.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    encapsulation: ViewEncapsulation.None,
    standalone: false
})
export class AdminViewXComponent extends AbstractAdminViewX implements OnInit {
    private readonly applicationPropertyRS = inject(
        SharedUtilApplicationPropertyRsService
    );
    private readonly reindexRS = inject(ReindexRS);
    private readonly searchRS = inject(SearchRS);
    private readonly dvDialog = inject(MatDialog);
    private readonly cd = inject(ChangeDetectorRef);
    private readonly errorService = inject(ErrorServiceX);
    private readonly adminUtilKeycloakAdminService = inject(
        AdminUtilKeycloakAdminRsService
    );
    public authServiceRS = inject(AuthServiceRS);

    @ViewChild(NgForm) public form: NgForm;

    @ViewChild(MatSort, {static: true}) public sort: MatSort;

    protected readonly Date = Date;
    public displayedCollection: MatTableDataSource<TSApplicationProperty>;
    public displayedColumns: string[] = ['name', 'value', 'timestampErstellt'];
    public reindexInProgress: boolean = false;
    public mitarbeiterRechteErstellenInProgress: boolean = false;
    public recreateAlleFaelleInProgress: boolean = false;
    public changedApplicationProperties: ConfigurableEinstellung[] = [];

    public constructor() {
        super();
    }

    public ngOnInit(): void {
        this.resetForm();
    }

    public submit(): void {
        if (this.form.invalid) {
            return;
        }

        this.doSave();
    }

    private doSave(): void {
        combineLatest(
            this.changedApplicationProperties.map(
                (value: ConfigurableEinstellung) => {
                    const appProperty = new TSApplicationProperty();
                    appProperty.value = value.value;
                    appProperty.name = value.key as TSApplicationPropertyKey;

                    // testen ob aktuelles property schon gespeichert ist
                    if (appProperty.isNew()) {
                        return this.applicationPropertyRS.update(
                            appProperty.name,
                            appProperty.value
                        );
                    } else {
                        return this.applicationPropertyRS.create(
                            appProperty.name,
                            appProperty.value
                        );
                    }
                }
            )
        ).subscribe(() => {
            this.errorService.addMesageAsInfo('SPEICHERN_ERFOLGREICH');
        });

        this.changedApplicationProperties = [];
    }

    public resetForm(): void {
        this.changedApplicationProperties = [];
        this.applicationPropertyRS
            .getAllApplicationProperties()
            .subscribe(response => {
                this.displayedCollection =
                    new MatTableDataSource<TSApplicationProperty>(response);
                this.displayedCollection.sort = this.sort;
                this.cd.markForCheck();
            });
    }

    public startReindex(): void {
        // avoid sending double by keeping it disabled until reload
        this.reindexInProgress = true;
        this.reindexRS.reindex().subscribe(response => {
            this.dvDialog.open(DvNgOkDialogComponent, {
                data: {title: response}
            });
        });
    }

    public startMitarbeiterRechteErstellen(): void {
        this.mitarbeiterRechteErstellenInProgress = true;
        const processStartedDialog = this.dvDialog.open(DvNgOkDialogComponent, {
            data: {title: 'Mitarbeiterrechte erstellen gestartet'}
        });
        this.adminUtilKeycloakAdminService
            .mitarbeiterRechteErstellen()
            .subscribe({
                next: response => {
                    processStartedDialog.close();
                    this.dvDialog.open(DvNgOkDialogComponent, {
                        data: {title: response}
                    });
                    this.mitarbeiterRechteErstellenInProgress = false;
                    this.cd.markForCheck();
                },
                error: () => {
                    this.mitarbeiterRechteErstellenInProgress = false;
                    this.cd.markForCheck();
                }
            });
    }

    public doFilter(value: string): void {
        this.displayedCollection.filter = value;
    }

    public updateApplicationProperty(
        applicationProperty: ConfigurableEinstellung
    ): void {
        const index = this.changedApplicationProperties.findIndex(
            changed => changed.key === applicationProperty.key
        );
        if (index >= 0) {
            this.changedApplicationProperties[index].value =
                applicationProperty.value;
        } else {
            this.changedApplicationProperties.push(applicationProperty);
        }
    }
}
