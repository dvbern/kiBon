import {IComponentOptions} from 'angular';
import TSAntragDTO from '../../../models/TSAntragDTO';
import PendenzRS from '../../service/PendenzRS.rest';
import * as moment from 'moment';
import TSUser from '../../../models/TSUser';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';

let template = require('./pendenzenListView.html');
require('./pendenzenListView.less');

export class PendenzenListViewComponentConfig implements IComponentOptions {
    transclude = false;
    template = template;
    controller = PendenzenListViewController;
    controllerAs = 'vm';
}

export class PendenzenListViewController {

    private pendenzenList: Array<TSAntragDTO>;
    totalResultCount: string = '0';

    static $inject: string[] = ['PendenzRS', 'CONSTANTS', 'AuthServiceRS'];

    constructor(public pendenzRS: PendenzRS, private CONSTANTS: any, private authServiceRS: AuthServiceRS) {
        this.initViewModel();
    }

    private initViewModel() {
        // Initial werden die Pendenzen des eingeloggten Benutzers geladen
        this.updatePendenzenList(this.authServiceRS.getPrincipal().username);
    }

    private updatePendenzenList(username: string) {
        this.pendenzRS.getPendenzenListForUser(username).then((response: any) => {
            this.pendenzenList = angular.copy(response);
            if (this.pendenzenList && this.pendenzenList.length) {
                this.totalResultCount = this.pendenzenList.length.toString();
            } else {
                this.totalResultCount = '0';
            }
        });
    }

    public getPendenzenList(): Array<TSAntragDTO> {
        return this.pendenzenList;
    }

    public userChanged(user: TSUser): void {
        if (user) {
            this.updatePendenzenList(user.username);
        }
    }
}
