import {
    ChangeDetectionStrategy,
    Component,
    computed,
    inject,
    input,
    output,
    Signal,
    viewChild
} from '@angular/core';
import {CommonModule} from '@angular/common';
import {
    MeldungsfensterData,
    MeldungsfensterStatus
} from '@kibon/shared-model-meldungsfenster';
import {TranslateModule} from '@ngx-translate/core';
import {NgForm, ValidationErrors} from '@angular/forms';
import {SharedUtilApplicationPropertyRsService} from '@kibon/shared/util/application-property-rs';
import {CONSTANTS} from '@kibon/shared/model/constants';
import {AngularEditorModule, AngularEditorConfig} from '@kolkov/angular-editor';
import {getTSSpracheValues, TSRole, TSSprache} from '@kibon/shared/model/enums';
import {SharedUiDateTimePickerComponent} from '@kibon/shared-ui-date-time-picker';
import {SharedUiMeldungsfensterComponent} from '@kibon/shared-ui-meldungsfenster';
import {DateUtil} from '@kibon/shared/util-fn/date';
import {MaxLengthDirective} from '@kibon/shared-util-validator';
import {MeldungsfensterFormMode} from './MeldungsfensterFormMode';
import {SharedModule} from '../../../../../../../src/app/shared/shared.module';

@Component({
    selector: 'lib-admin-ui-meldungsfenster-form',
    imports: [
        CommonModule,
        SharedModule,
        TranslateModule,
        AngularEditorModule,
        SharedUiDateTimePickerComponent,
        SharedUiMeldungsfensterComponent,
        MaxLengthDirective
    ],
    templateUrl: './admin-ui-meldungsfenster-form.component.html',
    styleUrl: './admin-ui-meldungsfenster-form.component.less',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true
})
export class AdminUiMeldungsfensterFormComponent {
    meldungsfenster = input.required<MeldungsfensterData>();
    mode = input.required<MeldungsfensterFormMode>();
    formSubmitted = output<MeldungsfensterData>();
    userCancelled = output<void>();
    appPropService = inject(SharedUtilApplicationPropertyRsService);
    form = viewChild.required(NgForm);
    statusOptions = Object.values(MeldungsfensterStatus);
    roles = Object.values(TSRole);
    activeLanguage = TSSprache.DEUTSCH;
    min = DateUtil.toNextHalfHour(new Date());
    maxLength = CONSTANTS.MAX_LENGTH_TEXT;
    minGueltigBis = computed(() =>
        DateUtil.min(this.meldungsfenster().gueltigAb, this.min)
    );

    editorConfig: Signal<AngularEditorConfig> = computed(() => ({
        editable: this.mode() !== 'READONLY',
        spellcheck: true,
        height: 'auto',
        minHeight: '200px',
        maxHeight: 'auto',
        width: 'auto',
        minWidth: '0',
        translate: 'yes',
        enableToolbar: this.mode() !== 'READONLY',
        showToolbar: this.mode() !== 'READONLY',
        placeholder: 'Enter text here...',
        defaultParagraphSeparator: '',
        defaultFontName: '',
        defaultFontSize: '2',
        sanitize: true,
        toolbarPosition: 'top',
        toolbarHiddenButtons: [
            [
                'undo',
                'redo',
                'strikeThrough',
                'subscript',
                'superscript',
                'justifyLeft',
                'justifyCenter',
                'justifyRight',
                'justifyFull',
                'textColor',
                'fontName',
                'heading'
            ],
            [
                'backgroundColor',
                'customClasses',
                'insertImage',
                'insertVideo',
                'insertHorizontalRule',
                'removeFormat',
                'toggleEditorMode'
            ]
        ]
    }));

    onLanguageClick(language: TSSprache): void {
        this.activeLanguage = language;
    }

    selectAllRoles() {
        this.meldungsfenster().zielgruppe = this.roles;
        this.markZielgruppeInputAsTouched();
    }

    unselectAllRoles() {
        this.meldungsfenster().zielgruppe = [];
        this.markZielgruppeInputAsTouched();
    }

    private markZielgruppeInputAsTouched() {
        this.form().controls['zielgruppe'].markAsTouched();
    }

    hasEditorContent(content: string): boolean {
        return content?.trim().length > 0;
    }

    isFormWithFrenchValid(): boolean {
        return (
            this.hasEditorContent(this.meldungsfenster().inhaltDe) &&
            this.hasEditorContent(this.meldungsfenster().inhaltFr) &&
            this.form().valid === true
        );
    }

    isFormWithoutFrenchValid(): boolean {
        return (
            this.hasEditorContent(this.meldungsfenster().inhaltDe) &&
            this.form().valid === true
        );
    }
    protected readonly getTSSpracheValues = getTSSpracheValues;
    protected readonly TSSprache = TSSprache;

    isGueltigAbDisabled() {
        if (this.mode() === 'READONLY') {
            return true;
        }
        if (this.mode() === 'CREATE') {
            return false;
        }
        return this.meldungsfenster().gueltigAb < this.min;
    }

    getAngularEditorError(
        content: string,
        otherErrors: ValidationErrors | null
    ): ValidationErrors | null {
        return this.hasEditorContent(content)
            ? otherErrors
            : {required: true, ...otherErrors};
    }
}
