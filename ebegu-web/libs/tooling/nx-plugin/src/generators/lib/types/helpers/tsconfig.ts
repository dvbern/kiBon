import path from 'path';
import {updateJson, Tree} from '@nx/devkit';

import {NormalizedSchema} from '../../generator.interface';

export function updateTsConfig(tree: Tree, options: NormalizedSchema): void {
    updateJson(tree, path.join(options.projectRoot, 'tsconfig.json'), json => {
        json.compilerOptions = {
            ...json.compilerOptions,
            ...{
                forceConsistentCasingInFileNames: true,
                strict: true,
                noImplicitOverride: true,
                noImplicitReturns: true,
                noFallthroughCasesInSwitch: true
            }
        };
        json.angularCompilerOptions = {
            ...json.angularCompilerOptions,
            ...{
                enableI18nLegacyMessageIdFormat: false,
                strictInjectionParameters: true,
                strictInputAccessModifiers: true,
                strictTemplates: true
            }
        };

        return json;
    });
}
export function updateSpecTsConfig(
    tree: Tree,
    options: NormalizedSchema
): void {
    updateJson(
        tree,
        path.join(options.projectRoot, 'tsconfig.spec.json'),
        json => {
            json.compilerOptions = {
                ...json.compilerOptions,
                ...{
                    forceConsistentCasingInFileNames: true,
                    strict: true,
                    noImplicitOverride: true,
                    noImplicitReturns: true,
                    noFallthroughCasesInSwitch: true,
                    types: [
                        ...json.compilerOptions.types,
                        'jasmine',
                        'webpack-env',
                        'angular-cookies',
                        'angular-material',
                        'angular-translate',
                        'angular-mocks',
                        'jquery'
                    ]
                }
            };
            json.files = [
                '../../../../src/polyfills.ts',
                '../../../../src/test.ts'
            ];
            json.angularCompilerOptions = {
                ...json.angularCompilerOptions,
                ...{
                    enableI18nLegacyMessageIdFormat: false,
                    strictInjectionParameters: true,
                    strictInputAccessModifiers: true,
                    strictTemplates: true
                }
            };

            return json;
        }
    );
}
