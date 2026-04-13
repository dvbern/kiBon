import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {NgForm} from '@angular/forms';
import {SHARED_MODULE_OVERRIDES} from '../../../../hybridTools/mockUpgradedDirective';
import {WindowRef} from '../../../../utils/window-ref/windowRef.service';
import {SharedModule} from '../../shared.module';

import {DvMonthPickerComponent} from './dv-month-picker.component';

describe('DvMonthPickerComponent', () => {
    let component: DvMonthPickerComponent;
    let fixture: ComponentFixture<DvMonthPickerComponent>;

    beforeEach(waitForAsync(() => {
        TestBed.configureTestingModule({
            imports: [SharedModule],
            providers: [
                WindowRef,
                {provide: NgForm, useValue: new NgForm([], [])}
            ]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(DvMonthPickerComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
