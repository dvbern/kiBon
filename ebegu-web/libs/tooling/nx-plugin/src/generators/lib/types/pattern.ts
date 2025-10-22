import path from 'path';
import {Tree} from '@nx/devkit';
import {libraryGenerator} from '@nx/angular/generators';

import {NormalizedSchema, LibTypeGenerator} from '../generator.interface';
import {updateSpecTsConfig, updateTsConfig} from './helpers/tsconfig';

export function patternTypeFactory(): LibTypeGenerator {
    return {
        libGenerator: libraryGenerator,
        libDefaultOptions: {
            skipModule: true,
            flat: true,
            style: 'none',
            skipSelector: true,
            skipTests: true,
            inlineStyle: true,
            inlineTemplate: true
        },
        generators: [],
        postprocess
    };
}

function postprocess(tree: Tree, options: NormalizedSchema) {
    updateTsConfig(tree, options);
    updateSpecTsConfig(tree, options);
    tree.delete(
        path.join(
            options.projectRoot,
            options.nameDasherized,
            'src',
            'lib',
            options.projectName + '.component.ts'
        )
    );
    tree.delete(
        path.join(options.projectRoot, options.nameDasherized, 'README.md')
    );
}
