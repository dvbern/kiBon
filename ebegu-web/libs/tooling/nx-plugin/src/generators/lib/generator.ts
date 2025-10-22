import * as fs from 'fs';
import * as path from 'path';
import {
    formatFiles,
    generateFiles,
    getWorkspaceLayout,
    names,
    offsetFromRoot,
    updateProjectConfiguration,
    Tree,
    readProjectConfiguration
} from '@nx/devkit';
import {
    camelize,
    capitalize,
    classify,
    dasherize
} from '@nx/devkit/src/utils/string-utils';

import {LibGeneratorSchema} from './schema';
import {NormalizedSchema, LibTypeGeneratorMap} from './generator.interface';

import {featureTypeFactory} from './types/feature';
import {patternTypeFactory} from './types/pattern';
import {dataAccessTypeFactory} from './types/data-access';
import {uiTypeFactory} from './types/ui';
import {utilTypeFactory} from './types/util';
import {utilFnTypeFactory} from './types/util-fn';
import {modelTypeFactory} from './types/model';

const LIB_TYPE_GENERATOR_MAP: LibTypeGeneratorMap = {
    feature: featureTypeFactory,
    pattern: patternTypeFactory,
    'data-access': dataAccessTypeFactory,
    ui: uiTypeFactory,
    util: utilTypeFactory,
    'util-fn': utilFnTypeFactory,
    model: modelTypeFactory
};

function normalizeOptions(
    tree: Tree,
    options: LibGeneratorSchema
): NormalizedSchema {
    const projectDirectory = `${options.feature}/${options.type}`;
    const nameDasherized = dasherize(options.name);
    const projectName = `${options.feature}-${options.type}-${nameDasherized}`;
    const projectRoot = `${getWorkspaceLayout(tree).libsDir}/${projectDirectory}/${projectName}`;
    const parsedTags = [`type:${options.type}`, `feature:${options.feature}`];

    return {
        ...options,
        nameDasherized,
        projectName,
        projectRoot,
        projectDirectory,
        parsedTags
    };
}

export default async function (tree: Tree, options: LibGeneratorSchema) {
    const normalizedOptions = normalizeOptions(tree, options);
    const {type} = normalizedOptions;
    const libTypeFactory = LIB_TYPE_GENERATOR_MAP[type];

    const {libGenerator, libDefaultOptions, generators, postprocess} =
        libTypeFactory(normalizedOptions);

    await libGenerator(tree, {
        ...libDefaultOptions,
        name: normalizedOptions.projectName,
        directory: normalizedOptions.projectRoot,
        tags: normalizedOptions.parsedTags.join(',')
    });
    const projectConfig = readProjectConfiguration(
        tree,
        normalizedOptions.projectName
    );
    // Add a dummy build target if it no target exists which configures
    // the location of the tsconfig file.
    // @see {@link file://./../../../../../../.eslintrc.json} -> enforceBuildableLibDependency
    if (
        ['test', 'build'].some(
            target => !projectConfig.targets?.[target]?.options?.tsConfig
        )
    ) {
        updateProjectConfiguration(tree, normalizedOptions.projectName, {
            ...projectConfig,
            targets: {
                build: {
                    executor: 'nx:noop',
                    options: {
                        tsConfig: `${normalizedOptions.projectRoot}/tsconfig.lib.json`
                    }
                },
                ...projectConfig.targets,
                test: {
                    ...projectConfig.targets?.test,
                    executor: '@angular-builders/custom-webpack:karma',
                    options: {
                        main: './src/test.ts',
                        customWebpackConfig: {
                            path: './extra-webpack.config.js'
                        },
                        karmaConfig: './karma.conf.js',
                        polyfills: './src/polyfills.ts',
                        tsConfig: '{projectRoot}/tsconfig.spec.json',
                        fileReplacements: [
                            {
                                replace: 'src/environments/environment.ts',
                                with: 'src/environments/environment.test.ts'
                            }
                        ]
                    }
                }
            }
        });
    }

    for (const {generator, defaultOptions} of generators) {
        await generator(tree, {
            ...defaultOptions,
            name: normalizedOptions.name,
            parent: normalizedOptions.projectName,
            directory: normalizedOptions.projectDirectory
        });
    }

    addFiles(tree, normalizedOptions);
    postprocess(tree, normalizedOptions);
    await formatFiles(tree);

    return async () => {
        console['log'](
            `\nProject: --project ${normalizedOptions.projectName}\n`
        );
        console['log'](
            `Can be used to generate additional components, service or perform other commands like`
        );
        console['log'](
            `eg "nx g remove --project ${normalizedOptions.projectName}"\n`
        );
    };
}

function addFiles(tree: Tree, options: NormalizedSchema) {
    const templateOptions = {
        ...options,
        ...names(options.name),
        offsetFromRoot: offsetFromRoot(options.projectRoot),
        classify,
        dasherize,
        capitalize,
        camelize
    };

    generateTemplateFiles(options, tree, templateOptions);
    generateTSConfigFiles(options, tree, templateOptions);
}

function generateTemplateFiles(
    options: NormalizedSchema,
    tree: Tree,
    templateOptions: any
) {
    const tplPath = path.join(__dirname, 'files', options.type);
    if (!fs.existsSync(tplPath)) {
        return;
    }

    generateFiles(
        tree,
        tplPath,
        path.join(options.projectRoot, 'src'),
        templateOptions
    );
}

function generateTSConfigFiles(
    options: NormalizedSchema,
    tree: Tree,
    templateOptions: any
) {
    const tplPath = path.join(__dirname, 'files', 'tsconfig');
    if (!fs.existsSync(tplPath)) {
        return;
    }

    generateFiles(tree, tplPath, options.projectRoot, templateOptions);
}
