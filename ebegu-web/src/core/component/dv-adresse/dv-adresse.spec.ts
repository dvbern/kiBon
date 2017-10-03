import '../../../bootstrap.ts';
import 'angular-mocks';
import {EbeguWebCore} from '../../core.module';
import IScope = angular.IScope;

describe('dvAdresse', function () {

    beforeEach(angular.mock.module(EbeguWebCore.name));

    let component: any;
    let scope: IScope;
    let $componentController: any;


    beforeEach(angular.mock.inject(function ($injector: any) {
        $componentController = $injector.get('$componentController');
        let $rootScope = $injector.get('$rootScope');
        scope = $rootScope.$new();
    }));

    it('should be defined', function () {
        /*
         To initialise your component controller you have to setup your (mock) bindings and
         pass them to $componentController.
         */
        let bindings: {};
        component = $componentController('dvAdresse', {$scope: scope}, bindings);
        expect(component).toBeDefined();
    });
});
