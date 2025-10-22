import path from 'path';
import {formatFiles, Tree, updateJson} from '@nx/devkit';

import {FeatureGeneratorSchema} from './schema';

export default async function (tree: Tree, options: FeatureGeneratorSchema) {
    tree.write(path.join('libs', options.name, '.gitkeep'), '');

    updateJson(tree, '.eslintrc.json', content => {
        content.overrides[0].rules[
            '@nx/enforce-module-boundaries'
        ][1].depConstraints.unshift({
            sourceTag: `feature:${options.name}`,
            onlyDependOnLibsWithTags: [`feature:${options.name}`]
        });
        return content;
    });
    updateJson(
        tree,
        path.join(
            'libs',
            'tooling',
            'nx-plugin',
            'src',
            'generators',
            'lib',
            'schema.json'
        ),
        content => {
            content.properties.feature['x-prompt'].items.push({
                value: options.name,
                label: options.description
            });
            content.properties.feature['x-prompt'].items.sort(
                (a: {value: string}, b: {value: string}) => {
                    if (a.value.startsWith('app-')) {
                        return -1;
                    } else {
                        return a.value < b.value ? -1 : 1;
                    }
                }
            );
            return content;
        }
    );

    await formatFiles(tree);

    return async () => {
        console.log(
            `\n\nGenerated "feature:${options.name}" can only depend on itself out of the box, please update rules in .eslintrc.json file based on specific the use case!\n\n`
        );
    };
}
