# DV kiBon Workspace

Welcome to DV kiBon frontend Workspace!

<!-- toc -->

- [Getting Started](#getting-started)
- [Architecture](#architecture)
- [Adding Code (Features, Data-Access, etc)](#adding-code-features-data-access-etc)
    - [Moving and removing of libraries](#moving-and-removing-of-libraries)
- [Troubleshooting](#troubleshooting)

<!-- tocstop -->

## Getting Started

Install global `nx` CLI with `npm i -g nx` as it will make running of some commands easier.
Install the dependencies with `npm ci`

| Task  | All             |
| ----- | --------------- |
| Serve | `npm run start` |
| Build | `npm run build` |
| Lint  | `npm run lint`  |
| Test  | `npm run test`  |

## Architecture

kiBon uses a very strict but also robust software architecture, i.e. the arrangement of the files and their affiliations are predefined and compliance with this structure is also verified with validators.

The structure might look like this

- ` (App)` -> _`<ui-view>`_

    - `Cockpit (Feature)` / `<dv-gesuch-app-pattern-main-layout>`
    - `Gesuch Form (Feature)` -> `<dv-gesuch-app-pattern-gesuch-step-layout>`._`<ui-view>`_
        - `shared/Gesuch Form Person (Feature)`
        - `shared/Gesuch Form Ausbildung (Feature)`
        - `shared/Gesuch Form Eltern (Feature)`
        - ...

More details about this structure can be found [here](docs/architecture.md).

## Adding Code (Features, Data-Access, etc)

To ensure that the architecture is being upheld correctly, a multitude of generators and other costumization tools can be used.

Use the tools that are mentioned in the [Workspace](docs/workspace.md) documentation.

See the [familiensituation for new mandant example](docs/example-famsit.md) to see how the generator can be used
to generate a new feature with new components.

### Import path prefix

Currently, we're limited that NX creates a path in the `tsconfig.base.json` file without the @dv prefix.
You'll have to add this manually. Open the file, locate `compilerOptions`, then the `paths`section.
There, you'll see a path like

```json
    "compilerOptions": {
        ...
        "paths": {
            "@dv/tooling/nx-plugin": ["libs/tooling/nx-plugin/src/index.ts"],
            ...
            "shared/your-feature/your-name": ["libs/shared/feature/terst/src/index.ts"],
        }
    },
```

Change this to

```json
    "compilerOptions": {
        ...
        "paths": {
            "@dv/tooling/nx-plugin": ["libs/tooling/nx-plugin/src/index.ts"],
            ...
            "@dv/shared/your-feature/your-name": ["libs/shared/feature/terst/src/index.ts"],
        }
    },
```

## Troubleshooting

NX monorepo is a great piece of technology, but it is not perfect. Even though caching leads to great performance
it can also lead to inconsistent state, especially when removing, moving or renaming projects and files.
If you run into any issues try to run `nx reset` (or `npm run reset) and then try to run original command again.
If the problem still persists then it's most likely a real problem which than has to be solved.

### Moving and removing of libraries

Sometimes it will make a sense to move (or rename) a library or remove it completely.
For that we should always use the `nx` commands instead of just moving or removing the library folder as
that will automate most of the adjustments that need to be performed in order for the workspace to work properly.

- `nx g remove --project <project-name>` - will remove the project and its alias from root `tsconfig.json` file
- `nx g move --project <project-name> --destination <scope>/<type>/<new-name>` - will move the project and adjust its alias from root `tsconfig.json` file

After that we should always run `npm run validate -- --fix` and perform any manual adjustments as listed in the output.
