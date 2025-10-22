import {strings} from '@angular-devkit/core';
import {libraryGenerator} from '@nx/angular/generators';
import {formatFiles, joinPathFragments, names, Tree} from '@nx/devkit';
import {editSpecSetup, editTsConfig, removeFiles} from './generator.helper';
import {defaultAngularLibraryOptions} from './generator.model';
import {LibGeneratorSchema} from './schema';
import {dasherize} from '@nx/devkit/src/utils/string-utils';

export async function libGenerator(tree: Tree, options: LibGeneratorSchema) {
    const projectDirectory = `${options.domain}/${options.type}/${dasherize(options.name)}`;
    const projectRoot = joinPathFragments('libs', projectDirectory);
    const projectName = options.domain + '-' + dasherize(options.name);
    const tags = [`type:${options.type}`, `domain:${options.domain}`];
    const testing = options.testing === 'true';

    if (tree.children(projectRoot).length > 0) {
        throw new Error(`Library already exists at ${projectRoot}`);
    }
    await libraryGenerator(tree, {
        ...defaultAngularLibraryOptions,
        directory: 'libs/' + projectDirectory,
        name: projectName,
        tags: tags.join(',')
    });

    removeFiles(tree, projectRoot);
    editTsConfig(tree, projectRoot, testing);
    if (testing) {
        const componentDirName = names(
            `${options.domain}-${options.name}`
        ).fileName; // shared-test1
        const componentPath = joinPathFragments(projectRoot, 'src/lib');
        const fileName = `${componentDirName}.component`; // "shared-test1.component"
        const className = strings.classify(fileName); // "SharedTest1Component"
        editSpecSetup(
            tree,
            projectRoot,
            projectName,
            componentPath,
            className,
            fileName
        );
    }

    await formatFiles(tree);
}

export default libGenerator;
