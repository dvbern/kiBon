# DV kiBon Workspace

Welcome to DV kiBon frontend Workspace!

<!-- toc -->

- [Getting Started](#getting-started)
- [Architecture](#architecture)
- [Adding Code (Features, Data-Access, etc)](#adding-code-features-data-access-etc)
- [Troubleshooting](#troubleshooting)

<!-- tocstop -->

## Getting Started

Install global `nx` CLI with `npm i -g nx` as it will make running of some commands easier.
Install the dependencies with `npm ci`

-> When encountering problems with installing:

1. Ensure you have the correct node and npm versions installed
2. rm -rf node_modules package-lock.json
3. npm cache clean --force
4. npm i --force

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

## Troubleshooting

If you run into any issues try to run `ng reset` (or `npm run reset) and then try to run original command again.
If the problem still persists, then it's most likely a real problem which then has to be solved.
