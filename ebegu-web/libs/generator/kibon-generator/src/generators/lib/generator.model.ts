import {libraryGenerator, UnitTestRunner} from '@nx/angular/generators';

export type LibType = 'routeable-component' | 'smart' | 'ui' | 'util' | 'model';

export const removeDefaultFiles = ['README.md', 'jest.config.ts'];

// get the correct options type from the generator itself
export type AngularLibraryGeneratorOptions = Parameters<
    typeof libraryGenerator
>[1];

export const defaultAngularLibraryOptions: Partial<AngularLibraryGeneratorOptions> =
    {
        changeDetection: 'OnPush',
        lazy: false,
        skipTests: true,
        spec: false,
        standalone: true,
        style: 'less',
        flat: true,
        routing: false,
        unitTestRunner: UnitTestRunner.None
    };

export const strictCompilerOptions = {
    forceConsistentCasingInFileNames: true,
    strict: true,
    noImplicitOverride: true,
    noImplicitReturns: true,
    noFallthroughCasesInSwitch: true
};

export const strictAngularCompilerOptions = {
    enableI18nLegacyMessageIdFormat: false,
    strictInjectionParameters: true,
    strictInputAccessModifiers: true,
    strictTemplates: true
};
