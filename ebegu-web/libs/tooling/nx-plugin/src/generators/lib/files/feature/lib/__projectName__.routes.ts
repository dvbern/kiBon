import {NgModule} from '@angular/core';
import {Ng2StateDeclaration} from '@uirouter/angular';
import {UIRouterUpgradeModule} from '@uirouter/angular-hybrid';
import {<%= classify(projectName) %>Component} from './<%= dasherize(projectName) %>/<%= dasherize(projectName) %>.component';

const states: Ng2StateDeclaration[] = [
    {
        name: '<%= feature %>.<%= camelize(nameDasherized) %>',
        url: '/<%= camelize(nameDasherized) %>',
        data: {},
        component: <%= classify(projectName) %>Component,
    }
];

@NgModule({
    imports: [UIRouterUpgradeModule.forChild({states})],
    exports: [UIRouterUpgradeModule]
})
export class <%= classify(projectName) %>RoutingModule {}
