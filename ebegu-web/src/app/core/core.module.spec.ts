import {CoreModule} from './core.module';

xdescribe('CoreModule', () => {
    let coreModule: CoreModule;

    beforeEach(() => {
        coreModule = new CoreModule();
    });

    it('should create an instance', () => {
        expect(coreModule).toBeTruthy();
    });
});
