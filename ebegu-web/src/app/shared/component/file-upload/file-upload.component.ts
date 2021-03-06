/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
import {
    Component,
    ChangeDetectionStrategy,
    Output,
    EventEmitter,
    Input,
    OnChanges,
    SimpleChanges, OnInit
} from '@angular/core';
import {Moment} from 'moment';
import {TSFile} from '../../../../models/TSFile';
import {DateUtil} from '../../../../utils/DateUtil';
import {ApplicationPropertyRS} from '../../../core/rest-services/applicationPropertyRS.rest';

@Component({
    selector: 'dv-file-upload',
    templateUrl: './file-upload.component.html',
    styleUrls: ['./file-upload.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FileUploadComponent implements OnChanges, OnInit {

    @Input() public title: string;
    @Input() public readOnly: boolean;
    @Input() public readOnlyDelete: boolean;
    @Output() public readonly download: EventEmitter<any> = new EventEmitter();
    @Output() public readonly delete: EventEmitter<any> = new EventEmitter();
    @Output() public readonly uploadFile: EventEmitter<any> = new EventEmitter();

    @Input() public files: TSFile[];

    public allowedMimetypes: string = '';

    public constructor(
        private readonly applicationPropertyRS: ApplicationPropertyRS,
    ) {}

    public ngOnInit(): void {
        this.applicationPropertyRS.getAllowedMimetypes().then(response => {
            if (response !== undefined) {
                this.allowedMimetypes = response;
            }
        });
    }

    public onDownload<T extends TSFile>(file: T, attachment: boolean): void {
        this.download.emit([file, attachment]);
    }

    public onDelete<T extends TSFile>(file: T): void {
        this.delete.emit(file);
    }

    public onUploadFile(event: any): void {
        this.uploadFile.emit(event);
    }

    public formatDate(timestampUpload: Moment): string {
        return DateUtil.momentToLocalDateFormat(timestampUpload, 'DD.MM.YYYY');
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.files) {
            this.files = changes.files.currentValue;
        }
    }

    public click(fileInput: HTMLInputElement): void {
        if (!this.readOnly) {
            fileInput.click();
        }
    }
}
