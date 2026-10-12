export function thousandSeparator(input: any): string {
    if (input !== null && input !== undefined && input !== '') {
        return new Intl.NumberFormat('de-CH', {}).format(input);
    }
    return '';
}
