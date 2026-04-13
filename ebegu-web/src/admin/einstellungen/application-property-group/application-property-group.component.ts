import {
    ConfigurableEinstellung,
    EditEinstellungComponent
} from '@admin/einstellungen';
import {Component, input, output, OnInit} from '@angular/core';
import {
    MatAccordion,
    MatExpansionPanel,
    MatExpansionPanelHeader,
    MatExpansionPanelTitle
} from '@angular/material/expansion';
import {SharedModule} from '../../../app/shared/shared.module';
import {TSApplicationProperty} from '../../../models/einstellung/TSApplicationProperty';

@Component({
    selector: 'dv-application-property-group',
    imports: [
        MatExpansionPanel,
        MatAccordion,
        MatExpansionPanelHeader,
        MatExpansionPanelTitle,
        SharedModule,
        EditEinstellungComponent
    ],
    templateUrl: './application-property-group.component.html'
})
export class ApplicationPropertyGroupComponent implements OnInit {
    groupName = input.required<string>();
    applicationProperties = input.required<TSApplicationProperty[]>();
    applicationPropertyChange = output<ConfigurableEinstellung>();

    noSubGroup: TSApplicationProperty[] = [];
    groupedSubGroups: Record<string, TSApplicationProperty[]> = {};

    ngOnInit() {
        for (const prop of this.applicationProperties()) {
            if (prop.subKeyGroup) {
                if (!this.groupedSubGroups[prop.subKeyGroup]) {
                    this.groupedSubGroups[prop.subKeyGroup] = [];
                }
                this.groupedSubGroups[prop.subKeyGroup].push(prop);
            } else {
                this.noSubGroup.push(prop);
            }
        }
    }
    emitSelection(applicationProperty: ConfigurableEinstellung) {
        this.applicationPropertyChange.emit(applicationProperty);
    }
}
