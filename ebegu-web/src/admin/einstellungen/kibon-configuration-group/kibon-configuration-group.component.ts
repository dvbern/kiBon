import {
    ConfigurableEinstellung,
    EditEinstellungComponent,
    KibonConfiguration
} from '@admin/einstellungen';
import {
    Component,
    input,
    output,
    ChangeDetectionStrategy,
    computed
} from '@angular/core';
import {
    MatAccordion,
    MatExpansionPanel,
    MatExpansionPanelHeader,
    MatExpansionPanelTitle
} from '@angular/material/expansion';
import {SharedModule} from '../../../app/shared/shared.module';
import {groupBySubGroup} from '../KibonConfigurationOrder';

/**
 * Displays a group of kiBon configurations as an accordion expansion panel.
 *
 * A kiBon configuration is either an Einstellung or an ApplicationProperty.
 * Configurations may or may not be grouped under subgroups.
 */
@Component({
    selector: 'kib-kibon-configuration-group',
    imports: [
        MatExpansionPanel,
        MatAccordion,
        MatExpansionPanelHeader,
        MatExpansionPanelTitle,
        SharedModule,
        EditEinstellungComponent
    ],
    changeDetection: ChangeDetectionStrategy.Eager,
    templateUrl: './kibon-configuration-group.component.html',
    styleUrl: './kibon-configuration-group.component.scss'
})
export class KibonConfigurationGroup {
    /**
     * The group name that is displayed as an accordion header. This input will be translated
     */
    groupName = input.required<string>();

    /**
     * The configurations belonging to one group
     */
    configurations = input.required<KibonConfiguration[]>();

    /**
     * Triggers when any configuration changes. The changed value is output
     */
    configurationChange = output<ConfigurableEinstellung>();

    groupElements = computed(() => groupBySubGroup(this.configurations()));

    noSubGroup = computed(() => this.groupElements().noSubGroup);
    subGroups = computed(() => this.groupElements().subGroups);

    emitSelection(config: ConfigurableEinstellung) {
        this.configurationChange.emit(config);
    }
}
