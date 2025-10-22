import path from 'path';
import {Tree, updateJson} from '@nx/devkit';
import {libraryGenerator} from '@nx/angular/generators';

import {NormalizedSchema, LibTypeGenerator} from '../generator.interface';

export function dataAccessTypeFactory(): LibTypeGenerator {
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
    updateJson(
        tree,
        path.join(
            options.projectRoot,
            options.nameDasherized,
            '.eslintrc.json'
        ),
        json => {
            json.overrides = [
                {
                    files: ['*.ts'],
                    rules: {}
                }
            ];
            return json;
        }
    );
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
