import {Tree} from '@nx/devkit';
import {Generator} from 'nx/src/config/misc-interfaces';

import {LibGeneratorSchema} from './schema';
import {Schema} from '@nx/angular/src/generators/library/schema';

export type LibType =
    | 'feature'
    | 'pattern'
    | 'data-access'
    // | 'event'
    | 'ui'
    | 'util'
    // | 'util-data-access'
    | 'util-fn'
    | 'model';

export interface NormalizedSchema extends LibGeneratorSchema {
    nameDasherized: string;
    projectName: string;
    projectRoot: string;
    projectDirectory: string;
    parsedTags: string[];
}

export interface DefaultOptions {
    [key: string]: string | number | boolean;
}

export interface LibTypeGenerator {
    libGenerator: Generator<Schema>;
    libDefaultOptions: DefaultOptions;
    generators: {
        generator: Generator<Schema>;
        defaultOptions: DefaultOptions;
    }[];
    postprocess(tree: Tree, options: NormalizedSchema): void;
}

export type LibTypeGeneratorMap = {
    [key in LibType]: (options: NormalizedSchema) => LibTypeGenerator;
};
