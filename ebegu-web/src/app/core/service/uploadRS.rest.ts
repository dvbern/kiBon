/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import angular, {IHttpService, ILogService, IPromise, IQService} from 'angular';
import {TSDokumentTyp} from '../../../models/enums/TSDokumentTyp';
import {TSSprache} from '../../../models/enums/TSSprache';
import {TSFerienbetreuungDokument} from '../../../models/gemeindeantrag/TSFerienbetreuungDokument';
import {TSDokumentGrund} from '../../../models/TSDokumentGrund';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';

export class UploadRS {
    public static $inject = [
        '$http',
        'REST_API',
        '$log',
        'Upload',
        'EbeguRestUtil',
        '$q',
        'base64'
    ];
    public serviceURL: string;

    public constructor(
        public http: IHttpService,
        REST_API: string,
        public log: ILogService,
        private readonly upload: angular.angularFileUpload.IUploadService,
        public ebeguRestUtil: EbeguRestUtil,
        public q: IQService,
        private readonly base64: any
    ) {
        this.serviceURL = `${REST_API}upload`;
    }

    private changeFilesNames(files: File[] | FileList): File[] {
        // Convert FileList to an array if necessary, for example Luzern Ausweis Kopie upload
        const fileArray: File[] = Array.isArray(files)
            ? files
            : Array.from(files);

        return fileArray.map((file: File) => {
            return this.changeSingleFileName(file);
        });
    }

    private changeSingleFileName(file: File): File {
        const encodedName = this.base64.encode(file.name);
        return new File([file], encodedName, {type: file.type});
    }

    public uploadFile(
        files: File[],
        dokumentGrund: TSDokumentGrund,
        gesuchID: string
    ): IPromise<TSDokumentGrund> {
        const restDokumentGrund = this.ebeguRestUtil.dokumentGrundToRestObject(
            {},
            dokumentGrund
        );
        const restDokumentString = this.upload.json(restDokumentGrund);
        return this.upload
            .upload({
                method: 'POST',
                data: {
                    file: this.changeFilesNames(files),
                    dokumentGrund: restDokumentString
                },
                url: this.serviceURL,
                headers: {
                    'x-gesuchID': gesuchID,
                    'x-filename': this.encodeFileNames(files).join(';'),
                    'Content-Type': 'multipart/form-data; charset=UTF-8'
                }
            })
            .then(
                (response: any) =>
                    this.ebeguRestUtil.parseDokumentGrund(
                        new TSDokumentGrund(),
                        response.data
                    ),
                (response: any) => {
                    return this.q.reject(response);
                },
                (evt: any) => {
                    this.notifyCallbackByUpload(evt);
                }
            );
    }

    public uploadFerienbetreuungDokumente(
        files: any,
        ferienbetreuungContainerId: string
    ): IPromise<TSFerienbetreuungDokument[]> {
        const names = this.encodeFileNames(files);
        const fileWithEncodedName = this.changeFilesNames(files);
        return this.upload
            .upload({
                url: `${this.serviceURL}/ferienbetreuungDokumente/${encodeURIComponent(ferienbetreuungContainerId)}`,
                method: 'POST',
                headers: {
                    'x-filename': names.join(';')
                },
                data: {
                    file: fileWithEncodedName
                }
            })
            .then(
                (response: any) =>
                    this.ebeguRestUtil.parseFerienbetreuungDokumente(
                        response.data
                    ),
                (response: any) => {
                    return this.q.reject(response);
                },
                (evt: any) => {
                    this.notifyCallbackByUpload(evt);
                }
            );
    }

    private encodeFileNames(files: any): string[] {
        const names: string[] = [];
        for (const file of files) {
            if (file) {
                const encodedFilename = this.base64.encode(file.name);
                names.push(encodedFilename);
            }
        }
        return names;
    }

    public uploadZemisExcel(file: File): IPromise<void> {
        const fileWithEncodedName = this.changeSingleFileName(file);
        return this.upload
            .upload({
                url: `${this.serviceURL}/zemisExcel`,
                method: 'POST',
                headers: {
                    'x-filename': this.base64.encode(file.name)
                },
                data: {
                    file: fileWithEncodedName
                }
            })
            .then(
                (response: any) => response.data,
                (response: any) => {
                    return this.q.reject(response);
                }
            );
    }

    public uploadGesuchsperiodeDokument(
        file: any,
        sprache: TSSprache,
        periodeID: string,
        dokumentTyp: TSDokumentTyp
    ): IPromise<any> {
        const fileWithEncodedName = this.changeSingleFileName(file);

        return this.upload
            .upload({
                url: `${this.serviceURL}/gesuchsperiodeDokument/${sprache}/${periodeID}/${dokumentTyp}`,
                method: 'POST',
                data: {
                    file: fileWithEncodedName
                }
            })
            .then(
                (response: any) => response.data,
                (response: any) => {
                    return this.q.reject(response);
                }
            );
    }

    public uploadGemeindeGesuchsperiodeDokument(
        file: any,
        sprache: TSSprache,
        gemeindeId: string,
        periodeID: string,
        dokumentTyp: TSDokumentTyp
    ): IPromise<any> {
        const fileWithEncodedName = this.changeSingleFileName(file);

        return this.upload
            .upload({
                url: `${this.serviceURL}/gemeindeGesuchsperiodeDoku/${encodeURIComponent(gemeindeId)}/${encodeURIComponent(
                    periodeID
                )}/${sprache}/${dokumentTyp}`,
                method: 'POST',
                data: {
                    file: fileWithEncodedName
                }
            })
            .then(
                (response: any) => response.data,
                (response: any) => {
                    return this.q.reject(response);
                }
            );
    }

    public uploadVollmachtDokument(
        vollmacht: any,
        fallId: string
    ): IPromise<any> {
        const encodedFilename = this.base64.encode(vollmacht.name);
        const fileWithEncodedName = this.changeSingleFileName(vollmacht);
        return this.upload
            .upload({
                method: 'POST',
                url: `${this.serviceURL}/uploadSozialdienstFallsDokument/${encodeURIComponent(fallId)}`,
                headers: {
                    'x-filename': encodedFilename
                },
                data: {
                    file: fileWithEncodedName
                }
            })
            .then(
                (response: any) =>
                    this.ebeguRestUtil.parseSozialdienstFallDokumente(
                        response.data
                    ),
                (response: any) => {
                    console.log('Upload Vollmacht File: NOT SUCCESS');
                    return this.q.reject(response);
                }
            );
    }

    private notifyCallbackByUpload(evt: any): void {
        const loaded: number = evt.loaded;
        const total: number = evt.total;
        const progressPercentage = (100 * loaded) / total;
        console.log(`progress: ${progressPercentage}% `);
        this.q.defer().notify();
    }
}
