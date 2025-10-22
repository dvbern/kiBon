import path from 'path';
import {Tree} from '@nx/devkit';
import {libraryGenerator} from '@nx/angular/generators';

import {NormalizedSchema, LibTypeGenerator} from '../generator.interface';
import {updateSpecTsConfig} from './helpers/tsconfig';

export function featureTypeFactory(): LibTypeGenerator {
    return {
        libGenerator: libraryGenerator,
        libDefaultOptions: {
            lazy: true,
            routing: true,
            standalone: true,
            style: 'less',
            skipTests: true,
            changeDetection: 'OnPush'
        },
        generators: [],
        postprocess
    };
}

function postprocess(tree: Tree, options: NormalizedSchema) {
    updateSpecTsConfig(tree, options);
    tree.delete(
        path.join(options.projectRoot, options.nameDasherized, 'README.md')
    );
    tree.delete(
        path.join(
            options.projectRoot,
            options.nameDasherized,
            'src',
            'lib',
            'lib.routes.ts'
        )
    );

    const pathToIndex = path.join(
        options.projectRoot,
        options.nameDasherized,
        'src',
        'index.ts'
    );
    const indexTsContent = tree.read(pathToIndex)?.toString();
    if (indexTsContent) {
        tree.write(
            pathToIndex,
            indexTsContent.replace(
                'lib/lib.routes',
                `lib/${options.projectName}.routes`
            )
        );
    }
}
