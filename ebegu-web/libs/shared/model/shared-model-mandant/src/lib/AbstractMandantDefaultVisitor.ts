import {MandantVisitor} from './MandantVisitor';

export abstract class AbstractMandantDefaultVisitor<T>
    implements MandantVisitor<T>
{
    protected abstract visitDefault(): T;

    public visitAppenzellAusserrhoden(): T {
        return this.visitDefault();
    }

    public visitBern(): T {
        return this.visitDefault();
    }

    public visitLuzern(): T {
        return this.visitDefault();
    }

    public visitSchwyz(): T {
        return this.visitDefault();
    }

    public visitSolothurn(): T {
        return this.visitDefault();
    }

    public visitZug(): T {
        return this.visitDefault();
    }

    public visitDvb(): T {
        return this.visitDefault();
    }
}
