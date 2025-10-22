export enum TSDokumentUploadTyp {
    'PDF' = 'PDF',
    'WORD' = 'WORD',
    'IMAGE' = 'IMAGE',
    'EXCEL' = 'EXCEL',
    'ANY' = 'ANY'
}

const excelEndings = ['xls', 'xlsx', 'xlsm', 'ods'];
const imageEndings = ['jpg', 'jpeg', 'png'];
const pdfEndings = ['pdf'];
const wordEndings = ['doc', 'docx', 'odt'];

export const UploadTypeFileEndingMap: {
    [K in keyof typeof TSDokumentUploadTyp]: string[];
} = {
    ANY: [...excelEndings, ...imageEndings, ...pdfEndings, ...wordEndings],
    EXCEL: excelEndings,
    IMAGE: imageEndings,
    PDF: pdfEndings,
    WORD: wordEndings
};
