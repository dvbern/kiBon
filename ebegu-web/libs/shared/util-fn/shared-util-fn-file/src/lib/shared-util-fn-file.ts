import {
    TSDokumentUploadTyp,
    UploadTypeFileEndingMap
} from '@kibon/shared/model/enums';

export abstract class FileUtil {
    static areAllFileEndingsMatchingTypes(
        files: File[],
        allowedTypes: TSDokumentUploadTyp[]
    ): boolean {
        return files.every(file =>
            this.isFileEndingMatchingTypes(file, allowedTypes)
        );
    }

    static isFileEndingMatchingTypes(
        file: File,
        fileType: TSDokumentUploadTyp[]
    ) {
        const extension = file.name.split('.').pop()?.toLowerCase();
        return fileType.some(type =>
            UploadTypeFileEndingMap[type].includes(extension)
        );
    }
}
