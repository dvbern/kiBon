export function thousandSeparator(input: any): string {
    if (input) {
        return new Intl.NumberFormat('de-CH', {}).format(input);
    }
    return '';
}
