import {LibType} from './generator.model';

export interface LibGeneratorSchema {
    name: string;
    type: LibType;
    domain: string;
    testing: string;
}
