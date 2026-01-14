import {
    joinPathFragments,
    readProjectConfiguration,
    Tree,
    updateProjectConfiguration
} from '@nx/devkit';
import {
    removeDefaultFiles,
    strictAngularCompilerOptions,
    strictCompilerOptions
} from './generator.model';

export function editTsConfig(
    tree: Tree,
    projectRoot: string,
    testing: boolean
) {
    updateTsConfig(tree, projectRoot, testing);
    cleanUpTsconfigLibJson(tree, projectRoot);
}

export function editSpecSetup(
    tree: Tree,
    projectRoot: string,
    projectName: string,
    componentPath: string,
    className: string,
    fileName: string
) {
    addKarmaFiles(tree, projectRoot);
    configureKarmaTarget(tree, projectName, projectRoot);
    updateTsConfigSpec(tree, projectRoot);
    generateBasicSpecFile(tree, componentPath, className, fileName);
}

export function generateBasicSpecFile(
    tree: Tree,
    componentPath: string,
    className: string,
    fileName: string
) {
    const specPath = joinPathFragments(componentPath, `${fileName}.spec.ts`);
    const specContent = `
        import { ComponentFixture, TestBed } from '@angular/core/testing';
        import { ${className} } from './${fileName}';

        describe('${className}', () => {
          let component: ${className};
          let fixture: ComponentFixture<${className}>;

          beforeEach(async () => {
            await TestBed.configureTestingModule({
              imports: [${className}]
            }).compileComponents();

            fixture = TestBed.createComponent(${className});
            component = fixture.componentInstance;
            fixture.detectChanges();
          });

          it('should create', () => {
            expect(component).toBeTruthy();
          });
        });
    `.trim();

    tree.write(
        joinPathFragments(componentPath, `${fileName}.spec.ts`),
        specContent + '\n'
    );

    tree.write(specPath, specContent.trim() + '\n');
}

function updateTsConfig(
    tree: Tree,
    root: string,
    addTestingReference: boolean
) {
    const tsconfigPath = joinPathFragments(root, 'tsconfig.json');

    if (!tree.exists(tsconfigPath)) {
        return;
    }

    const tsconfigJson = JSON.parse(tree.read(tsconfigPath, 'utf-8') ?? '{}');

    if (!Array.isArray(tsconfigJson.references)) {
        tsconfigJson.references = [];
    }

    if (
        !tsconfigJson.references.some(
            (reference: {path: string}) =>
                reference.path === './tsconfig.lib.json'
        )
    ) {
        tsconfigJson.references.push({path: './tsconfig.lib.json'});
    }

    const specRefIndex = tsconfigJson.references.findIndex(
        (reference: {path: string}) => reference.path === './tsconfig.spec.json'
    );

    if (addTestingReference) {
        if (specRefIndex === -1) {
            tsconfigJson.references.push({path: './tsconfig.spec.json'});
        }
    } else {
        if (specRefIndex !== -1) {
            tsconfigJson.references.splice(specRefIndex, 1);
        }
    }

    addTsConfigOptions(tsconfigJson);
    tree.write(tsconfigPath, JSON.stringify(tsconfigJson, null, 2));
}

function addTsConfigOptions(tsconfigJson: any, spec?: boolean): void {
    if (spec) {
        tsconfigJson.compilerOptions = {
            ...tsconfigJson.compilerOptions,
            ...strictCompilerOptions,
            types: [
                ...tsconfigJson.compilerOptions.types,
                'webpack-env',
                'angular-cookies',
                'angular-material',
                'angular-translate',
                'angular-mocks',
                'jquery'
            ]
        };

        tsconfigJson.angularCompilerOptions = {
            ...tsconfigJson.angularCompilerOptions,
            ...strictAngularCompilerOptions
        };
    } else {
        tsconfigJson.compilerOptions = {
            ...tsconfigJson.compilerOptions,
            ...strictCompilerOptions
        };

        tsconfigJson.angularCompilerOptions = {
            ...tsconfigJson.angularCompilerOptions,
            ...strictAngularCompilerOptions
        };
    }
}

function updateTsConfigSpec(tree: Tree, root: string): void {
    const configPath = joinPathFragments(root, 'tsconfig.spec.json');
    const configJson = JSON.parse(tree.read(configPath, 'utf-8') ?? '{}');
    addTsConfigOptions(configJson, true);
    tree.write(configPath, JSON.stringify(configJson, null, 2));
}

/**
 * adds tsconfig.spec.json
 * @param tree
 * @param root
 */
export function addKarmaFiles(tree: Tree, root: string) {
    tree.write(
        joinPathFragments(root, 'tsconfig.spec.json'),
        `{
                  "extends": "./tsconfig.json",
                  "compilerOptions": {
                    "outDir": "../../dist/out-tsc",
                    "types": ["jasmine", "node"]
                  },
                  "files": ["../../../../src/polyfills.ts"],
                  "include": ["**/*.spec.ts", "**/*.d.ts"]
                }`
    );
}

export function configureKarmaTarget(
    tree: Tree,
    projectName: string,
    projectRoot: string
) {
    const config = readProjectConfiguration(tree, projectName);

    config.targets = {
        ...config.targets,

        test: {
            executor: '@angular-devkit/build-angular:karma',
            options: {
                karmaConfig: './karma.conf.js',
                polyfills: './src/polyfills.ts',
                tsConfig: projectRoot + '/tsconfig.spec.json'
            }
        }
    };

    updateProjectConfiguration(tree, projectName, config);
}

/**
 * removes nx default files, jest specifics and readme through a array const that needs adaption if more files should be removed
 * @param tree
 * @param projectRoot
 */
export function removeFiles(tree: Tree, projectRoot: string) {
    removeDefaultFiles.forEach((filename: string) => {
        const filePath = joinPathFragments(projectRoot, filename);
        if (tree.exists(filePath)) {
            tree.delete(filePath);
        }
    });
}

/**
 * removes jest references
 * @param tree
 * @param projectRoot
 */
export function cleanUpTsconfigLibJson(tree: Tree, projectRoot: string) {
    const tsconfigPath = joinPathFragments(projectRoot, 'tsconfig.lib.json');

    if (!tree.exists(tsconfigPath)) {
        return;
    }

    const tsconfig = JSON.parse(tree.read(tsconfigPath, 'utf-8') || '{}');

    // Remove jest.config.ts from exclude list
    if (Array.isArray(tsconfig.exclude)) {
        tsconfig.exclude = tsconfig.exclude.filter(
            (entry: string) => entry !== 'jest.config.ts'
        );
    }

    tree.write(tsconfigPath, JSON.stringify(tsconfig, null, 2));
}
