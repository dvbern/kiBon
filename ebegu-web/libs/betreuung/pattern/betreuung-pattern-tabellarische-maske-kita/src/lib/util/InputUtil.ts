export abstract class InputUtil {
    static blockInvalidChars(event: KeyboardEvent) {
        if (event.code === 'KeyE') {
            event.preventDefault();
        }
    }
}
