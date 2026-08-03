import {Component, ChangeDetectionStrategy} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormControl, FormsModule} from '@angular/forms';
import {By} from '@angular/platform-browser';
import {TSGesuch} from '../../../../../models/TSGesuch';
import {ZpvInitMailUniqueValidator} from './zpv-init-mail-unique.validator';

const BESITZER_MAIL = 'owner@example.com';

@Component({
    imports: [FormsModule, ZpvInitMailUniqueValidator],
    changeDetection: ChangeDetectionStrategy.Eager,
    template: `
        <input
            type="email"
            name="email"
            [(ngModel)]="email"
            isValidStekIdentifierInitMail
            [gesuch]="gesuch"
        />
    `
})
class TestHostComponent {
    public email = '';
    public gesuch: TSGesuch = {
        dossier: {
            fall: {
                besitzer: {
                    email: BESITZER_MAIL
                }
            }
        }
    } as TSGesuch;
}

describe('ZpvInitMailUniqueValidator', () => {
    let fixture: ComponentFixture<TestHostComponent>;
    let directive: ZpvInitMailUniqueValidator;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [TestHostComponent, ZpvInitMailUniqueValidator],
            imports: [FormsModule]
        }).compileComponents();

        fixture = TestBed.createComponent(TestHostComponent);
        fixture.detectChanges();

        const inputEl = fixture.debugElement.query(By.css('input'));
        directive = inputEl.injector.get(ZpvInitMailUniqueValidator);
    });

    describe('validate()', () => {
        it('should return benutzerMailForStekIdentifierNotAllowed error when the value equals the besitzer email', () => {
            const control = new FormControl(BESITZER_MAIL);

            const result = directive.validate(control);

            expect(result).toEqual({
                benutzerMailForStekIdentifierNotAllowed: {}
            });
        });

        it('should return null when the value differs from the besitzer email', () => {
            const control = new FormControl('someone-else@example.com');

            const result = directive.validate(control);

            expect(result).toBeNull();
        });

        it('should return null for an empty value', () => {
            const control = new FormControl('');

            const result = directive.validate(control);

            expect(result).toBeNull();
        });

        it('should return null for a null value', () => {
            const control = new FormControl(null);

            const result = directive.validate(control);

            expect(result).toBeNull();
        });

        it('should treat the comparison as case-insensitive', () => {
            const control = new FormControl('OWNER@example.com');

            const result = directive.validate(control);

            expect(result).toEqual({
                benutzerMailForStekIdentifierNotAllowed: {}
            });
        });
    });
});
