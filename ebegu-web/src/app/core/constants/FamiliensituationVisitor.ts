import {AbstractMandantDefaultVisitor, KiBonMandant} from '@models/mandant';

export class FamiliensituationVisitor extends AbstractMandantDefaultVisitor<any> {
    public process(mandant: KiBonMandant): any {
        return mandant.accept(this);
    }

    protected visitDefault() {
        return 'gesuch.familiensituation-default';
    }

    public visitAppenzellAusserrhoden(): any {
        return 'gesuch.familiensituation-appenzell';
    }

    public visitSchwyz(): any {
        return 'gesuch.familiensituation-schwyz';
    }
}
