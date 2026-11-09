import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    DestroyRef,
    OnInit,
    ViewChild,
    ViewEncapsulation,
    inject,
    signal
} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NgForm} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {KeycloakAdminRsService} from '@admin/util';
import {ApplicationPropertyRsService} from '@utils/application-property-rs';
import {combineLatest} from 'rxjs';
import {DvNgOkDialogComponent} from '../../../app/core/component/dv-ng-ok-dialog/dv-ng-ok-dialog.component';
import {ErrorServiceX} from '../../../app/core/errors/service/ErrorServiceX';
import {PagePasswordService} from '../../../authentication/password-dialog/page-password.service';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSApplicationProperty} from '../../../models/einstellung/TSApplicationProperty';
import {TSApplicationPropertyKey} from '../../../models/einstellung/TSApplicationPropertyKey';
import {AbstractAdminViewX} from '../../abstractAdminViewX';
import {ReindexRS} from '../../service/reindexRS.rest';
import {TSApplicationPropertyWithMetadata} from '../TSApplicationPropertyWithMetadata';
import {
    ConfigurableEinstellung,
    KibonConfiguration
} from '@admin/einstellungen';
@Component({
    selector: 'dv-admin-view-x',
    templateUrl: './admin-view-x.component.html',
    styleUrls: ['./admin-view-x.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    encapsulation: ViewEncapsulation.None,
    standalone: false
})
export class AdminViewXComponent extends AbstractAdminViewX implements OnInit {
    private readonly applicationPropertyRS = inject(
        ApplicationPropertyRsService
    );
    private readonly reindexRS = inject(ReindexRS);
    private readonly dvDialog = inject(MatDialog);
    private readonly cd = inject(ChangeDetectorRef);
    private readonly errorService = inject(ErrorServiceX);
    private readonly adminUtilKeycloakAdminService = inject(
        KeycloakAdminRsService
    );
    private readonly pagePassword = inject(PagePasswordService);
    private readonly destroyRef = inject(DestroyRef);
    public authServiceRS = inject(AuthServiceRS);

    public readonly unlocked = signal(false);

    @ViewChild(NgForm) public form: NgForm;

    @ViewChild(MatSort, {static: true}) public sort: MatSort;

    protected readonly Date = Date;
    public displayedCollection: MatTableDataSource<TSApplicationPropertyWithMetadata>;
    public displayedGroupedCollection: Map<string, KibonConfiguration[]> =
        new Map();
    public displayedColumns: string[] = ['name', 'value', 'timestampErstellt'];
    public reindexInProgress: boolean = false;
    public mitarbeiterRechteErstellenInProgress: boolean = false;
    public changedApplicationProperties: ConfigurableEinstellung[] = [];

    public constructor() {
        super();
    }

    public ngOnInit(): void {
        this.pagePassword
            .unlock$('faelle.list')
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe(() => {
                this.unlocked.set(true);
                this.resetForm();
            });
    }

    public submit(): void {
        if (!this.unlocked()) {
            return;
        }
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
            this.resetForm();
        });

        this.changedApplicationProperties = [];
    }

    public resetForm(): void {
        this.changedApplicationProperties = [];
        this.applicationPropertyRS
            .getAllApplicationProperties()
            .subscribe(response => {
                const ungrouped: TSApplicationPropertyWithMetadata[] = [];
                this.displayedGroupedCollection.clear();
                response.forEach((prop: TSApplicationPropertyWithMetadata) => {
                    if (prop.metadata.keyGroup) {
                        const list =
                            this.displayedGroupedCollection.get(
                                prop.metadata.keyGroup
                            ) ?? [];
                        list.push({
                            ...prop,
                            key: prop.name
                        });
                        this.displayedGroupedCollection.set(
                            prop.metadata.keyGroup,
                            list
                        );
                    } else {
                        ungrouped.push(prop);
                    }
                });
                this.displayedCollection =
                    new MatTableDataSource<TSApplicationPropertyWithMetadata>(
                        ungrouped
                    );

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
