import path from 'path';
import {Tree} from '@nx/devkit';
import {libraryGenerator} from '@nx/angular/generators';

import {NormalizedSchema, LibTypeGenerator} from '../generator.interface';
import {updateSpecTsConfig, updateTsConfig} from './helpers/tsconfig';

export function uiTypeFactory(): LibTypeGenerator {
    return {
        libGenerator: libraryGenerator,
        libDefaultOptions: {
            standalone: true,
            skipModule: true,
            displayBlock: true,
            style: 'less',
            changeDetection: 'OnPush'
        },
        generators: [],
        postprocess
    };
}

function postprocess(tree: Tree, options: NormalizedSchema) {
    updateTsConfig(tree, options);
    updateSpecTsConfig(tree, options);
    tree.delete(
        path.join(options.projectRoot, options.nameDasherized, 'README.md')
    );
}
