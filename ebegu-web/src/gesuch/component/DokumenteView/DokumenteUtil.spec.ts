import {DokumenteUtil} from './DokumenteUtil';

describe('DokumenteUtil', () => {
    describe('getFileExtensionWithDot', () => {
        it('should return the file extension with a dot for a standard filename', () => {
            expect(DokumenteUtil.getFileExtensionWithDot('test.docx')).toBe(
                '.docx'
            );
        });

        it('should return the file extension in lowercase', () => {
            expect(DokumenteUtil.getFileExtensionWithDot('TEST.PDF')).toBe(
                '.pdf'
            );
        });

        it('should return the last extension for filenames with multiple dots', () => {
            expect(
                DokumenteUtil.getFileExtensionWithDot('archive.tar.gz')
            ).toBe('.gz');
        });

        it('should return an empty string if there is no dot in the filename', () => {
            expect(DokumenteUtil.getFileExtensionWithDot('README')).toBe('');
        });

        it('should return an empty string if the filename is empty', () => {
            expect(DokumenteUtil.getFileExtensionWithDot('')).toBe('');
        });

        it('should return an empty string if the filename is undefined', () => {
            expect(DokumenteUtil.getFileExtensionWithDot(undefined)).toBe('');
        });

        it('should handle filenames ending with a dot', () => {
            expect(DokumenteUtil.getFileExtensionWithDot('test.')).toBe('.');
        });

        it('should return the file extension for hidden files with an extension', () => {
            expect(DokumenteUtil.getFileExtensionWithDot('.config.json')).toBe(
                '.json'
            );
        });

        it('should return the last part of the filename for filenames starting with a dot', () => {
            expect(DokumenteUtil.getFileExtensionWithDot('.gitignore')).toBe(
                '.gitignore'
            );
        });
    });
});
