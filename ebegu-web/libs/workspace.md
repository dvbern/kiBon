# Workspace

## Generators & Executors

This workspace provides a set of generators and executors to automate the creation of new projects and libraries
which follow desired architecture to ensure maintainability and consistency across projects in this DV workspace.

Learn more about [@dv/tooling/nx-plugin](libs/tooling/nx-plugin/README.md).

Learn more about generators and executors in general in [Nx documentation](https://nx.dev/plugins/recipes/local-generators).

## Customization

### Generator default options

Some of the used generator options can be customized in the [nx.json](nx.json) file within the `generators` property.
This can be useful if you add additional Angular libraries which bring their own generator, and you want to customize their default options.

Additionally, default options can be evolved also for custom `@dv/tooling/nx-plugin` generators
by adjusting `libDefaultOptions` in the `libs/tooling/nx-plugin/src/generators/lib/types/<type>.ts` files
per library type.
