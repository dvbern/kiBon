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
import {MatOption, MatSelect, MatSelectTrigger} from '@angular/material/select';
import {
    MeldungsfensterData,
    MeldungsfensterStatus
} from '@models/meldungsfenster';
import {TranslateModule} from '@ngx-translate/core';
import {FormsModule, NgForm, ValidationErrors} from '@angular/forms';
import {ApplicationPropertyRsService} from '@utils/application-property-rs';
import {CONSTANTS} from '@models/constants';
import {AngularEditorModule, AngularEditorConfig} from '@kolkov/angular-editor';
import {DateTimePickerComponent} from '@app/shared/component/date-time-picker';
import {UiMeldungsfensterComponent} from '@app/shared/ui-meldungsfenster';
import {ErrorMessagesComponent} from '../../../app/core/component/dv-error-messages/error-messages.component';
import {TSRole} from '../../../models/enums/TSRole';
import {getTSSpracheValues, TSSprache} from '../../../models/enums/TSSprache';
import {DateUtil} from '../../../utils/date/DateUtil';
import {MaxLengthDirective} from '../../../utils/validator/max-length.directive';
import {MeldungsfensterFormMode} from './MeldungsfensterFormMode';

@Component({
    selector: 'lib-admin-ui-meldungsfenster-form',
    imports: [
        CommonModule,
        TranslateModule,
        AngularEditorModule,
        DateTimePickerComponent,
        UiMeldungsfensterComponent,
        MaxLengthDirective,
        DateTimePickerComponent,
        FormsModule,
        MatSelect,
        MatSelectTrigger,
        MatOption,
        ErrorMessagesComponent
    ],
    templateUrl: './meldungsfenster-form.component.html',
    styleUrl: './meldungsfenster-form.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true
})
export class MeldungsfensterFormComponent {
    meldungsfenster = input.required<MeldungsfensterData>();
    mode = input.required<MeldungsfensterFormMode>();
    formSubmitted = output<MeldungsfensterData>();
    userCancelled = output<void>();
    appPropService = inject(ApplicationPropertyRsService);
    form = viewChild.required(NgForm);
    statusOptions = Object.values(MeldungsfensterStatus);
    roles = Object.values(TSRole);
    activeLanguage = TSSprache.DEUTSCH;
    min = DateUtil.toNextHalfHour(new Date());
    maxLength = CONSTANTS.MAX_LENGTH_TEXT;
    minGueltigBis = computed(() =>
        DateUtil.min(this.meldungsfenster().gueltigAb, this.min)
    );

    protected readonly getTSSpracheValues = getTSSpracheValues;
    protected readonly TSSprache = TSSprache;

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
