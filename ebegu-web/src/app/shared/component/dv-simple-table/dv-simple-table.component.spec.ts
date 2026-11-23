import {ChangeDetectionStrategy} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {SHARED_MODULE_OVERRIDES} from '../../../../hybridTools/mockUpgradedDirective';
import {SharedModule} from '../../shared.module';
import {DvSimpleTableColumnDefinition} from './dv-simple-table-column-definition';
import {DvSimpleTableConfig} from './dv-simple-table-config';

import {DvSimpleTableComponent} from './dv-simple-table.component';

describe('DvSimpleTableComponent', () => {
    let component: DvSimpleTableComponent;
    let fixture: ComponentFixture<DvSimpleTableComponent>;

    const data = [
        {a: 'a1', b: 'b1'},
        {a: 'a2', b: 'b2'},
        {a: 'a3', b: 'b3'},
        {a: 'a4', b: 'b4'},
        {a: 'a5', b: 'b5'},
        {a: 'a6', b: 'b6'}
    ];
    const columnDefinition: DvSimpleTableColumnDefinition[] = [
        {displayedName: 'A', attributeName: 'a'},
        {displayedName: 'B', attributeName: 'b'}
    ];

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SharedModule, BrowserAnimationsModule],
            declarations: [DvSimpleTableComponent]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .overrideComponent(DvSimpleTableComponent, {
                set: {changeDetection: ChangeDetectionStrategy.Default}
            })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(DvSimpleTableComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should display 6 rows', () => {
        fixture.componentRef.setInput('columns', columnDefinition);
        fixture.componentRef.setInput('data', data);
        fixture.detectChanges();
        const table = fixture.debugElement.query(By.css('mat-table'));
        expect(table).not.toBeNull();
        const rows = fixture.debugElement.queryAll(By.css('mat-row'));
        expect(rows.length).toEqual(6);
    });

    it('should display 5 rows on first page and 1 row on second page', () => {
        fixture.componentRef.setInput('columns', columnDefinition);
        fixture.componentRef.setInput(
            'config',
            new DvSimpleTableConfig('a', 'asc', false, 5)
        );
        fixture.componentRef.setInput('data', data);
        fixture.detectChanges();
        let rows = fixture.debugElement.queryAll(By.css('mat-row'));
        expect(rows.length).toEqual(5);

        // navigate like a user would; the click handler marks the OnPush view dirty
        const pageLinks = fixture.debugElement.queryAll(
            By.css('.pagination a')
        );
        pageLinks[1].nativeElement.click();
        fixture.detectChanges();
        rows = fixture.debugElement.queryAll(By.css('mat-row'));
        expect(rows.length).toEqual(1);
    });
});
