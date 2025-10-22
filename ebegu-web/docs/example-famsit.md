# Example for a new Familiensituation

To create a new Feature, we need to perform the following steps:

- Creating the feature / the scope of the feature.
- Create the actual parts (building blocks) of the feature, such as a feature component, ui components, services, etc.

In this document, we're going to see how we can do this with our custom generator and also have a peek into how
this feature can then be lazy loaded.

## Generating the feature

<b>Important: The following commands must be run in the root directory (ebegu-web). Otherwise,
the components generated will be in the current working directory of your terminal.
You can avoid this by creating a run configuration in your IDE</b>

Consider the following case: A new mandant wants to use kiBon and one of it's features is a new familiensituation.
The way we think in kiBon, this familiensituation is a new feature, that can be enabled/disabled. Therefore, let's
create the scope for this new feature:

```bash
npm run g:feature

> kibon@XXXX.X.X-SNAPSHOT g:feature
> nx generate @dv/tooling/nx-plugin:feature

 NX  Generating @dv/tooling/nx-plugin:feature

? What is the feature scope name (use singular form)? » familiensituation-xy
? What is the feature scope description (used during lib generation, use format [name] - description])? » [familiensituation-xy] - Familiensituation of Mandant xy

CREATE libs/familiensituation-xy/.gitkeep
UPDATE .eslintrc.json
UPDATE libs/tooling/nx-plugin/src/generators/lib/schema.json
UPDATE tsconfig.base.json

Generated "feature:familiensituation-xy" can only depend on itself out of the box, please update rules in .eslintrc.json file based on specific the use case!
```

Note, that we again use the name in the description. This will help us later when generating libs since we will see the description
of a feature scope.

What did this command actually do? As we can see in the console output, it created a folder for the feature in the libs directory.
Further, it updated the eslint rules and the schema of our generator. Let's first check the eslint rule update:

```json
"depConstraints": [
                            {
                                "sourceTag": "feature:familiensituation-xy",
                                "onlyDependOnLibsWithTags": [
                                    "feature:familiensituation-xy"
                                ]
                            },
                            ...
```

As you can see, the generator added a new rule, that familiensituation-xy libs can only use other libs from the same feature scope.
Let's update this to also allow libs from the shared feature scope:

```json
"depConstraints": [
                            {
                                "sourceTag": "feature:familiensituation-xy",
                                "onlyDependOnLibsWithTags": [
                                    "feature:familiensituation-xy",
                                    "feature:shared"
                                ]
                            },
                            ...
```

Next, let's check out the schema:

```json
"items": [
                    {
                        "value": "familiensituation-xy",
                        "label": "[familiensituation-xy] - Familiensituation of Mandant xy"
                    },
                    ...,
                    {
                        "value": "shared",
                        "label": "[Shared] - libs that can be used from other features"
                    }
                ]
```

In our list of features to choose from, our familiensituation-xy was added.

## Creating the root feature component

Now we can create our feature:

```bash
npm run g

? What is the library type? ...
Feature - lazy loaded feature (route) consumed by an app
Pattern - eager feature, eg connection status, tracing or interceptor
UI - standalone (simple / view / presentational) UI component, eg calendar or toggle
Util - Angular based util (service), eg validator, mapper, or logger
Util Function - TypeScript based util (function), eg data transformation or calculation
Model - TypeScript interfaces, types, enums and consts

```

At this point, we want to create the entire feature we can route to. So let's select the first option (Feature).

```bash
√ What is the library type? · feature
? For which feature should the library be created? ...
[familiensituation-xy] - Familiensituation of Mandant xy
[Shared] - libs that can be used from other features
```

Here, we select the scope of our library. The component that is about to be created containing the routes of our feature
and its content must only be used within its feature. Therefore, let's select the first option again. We now can name
our root component. In this example, we use the same name as for the feature. Since our naming convention is {featurename}-feature-{libname},
this leads to a somewhat silly looking name. Feel free to use a different name like "root" or "main":

```bash
√ What is the library type? · feature
√ For which feature should the library be created? · familiensituation-xy
? What is the library name (use singular form)? · familiensituation-xy
```

You should now see a lot of creation output in the console. A lot of it is boilerplate code for the project setup, but
probably most important for you is the creation of the component. You can now use this component as you would use any other
component. Now you can start coding as you are used to. Just remember to use the nx generator instead of the angular generator
directly.

#### Creating a UI-Component

Let's say, we want to display a warning specifically to this feature, depending on two flags. Instead of running the
Angular generator (ng g), we now use our nx generator (npm run g). This time, we select UI. The reasoning is, it is neither
a full-fetched feature, nor is it a pattern since it is just a dumb UI-component (nothing will be injected)
responsible for displaying a warning. Therefore we choose UI and name it e.g. "warning":

```bash
npm run g

> kibon@XXXX.X.X-SNAPSHOT g
> nx generate @dv/tooling/nx-plugin:lib


 NX  Generating @dv/tooling/nx-plugin:lib

√ What is the library type? · ui
√ For which feature should the library be created? · familiensituation-xy
? What is the library name (use singular form)? » warning
```

As you can see, the ui component was generated in the ui folder of our feature. You can now use the component in
the root component as you are used to:

```angular2html
<lib-familiensituation-xy-ui-warning></lib-familiensituation-xy-ui-warning>
```

### Routing / Lazy loading the feature

<b>This is currently only possible within Angular2 RoutingStates. Lazy Loading Angular components/modules from AngularJS is not possible</B>

We want to lazy load all our feature. Unfortunately, the ui router we are using does not
allow us to generate this easily for us, we have to do it manually. The generator has created a route
file and registered it globally. This is a reasonable default, but probably not applicable in the wider
kiBon context (the feature could be part of the gesuch as it is in this example, therefore it is a subfeature of
the gesuch feature. The gesuch feature is not yet an nx feature, therefore we have to add it to the gesuch routes.
Again, remember that this only works for Angular2+ Routes).

Even more unfortunate, the ui router does not support lazy loading standalone components, only modules. Luckily, there
is an easy workaround: We just create a module for our feature root component. To keep the overview, let's add it manually
to our route file:

```typescript
import {NgModule} from '@angular/core';
import {Ng2StateDeclaration} from '@uirouter/angular';
import {UIRouterUpgradeModule} from '@uirouter/angular-hybrid';
import {FamiliensituationXyFeatureFamiliensituationXyComponent} from './familiensituation-xy-feature-familiensituation-xy/familiensituation-xy-feature-familiensituation-xy.component';

const states: Ng2StateDeclaration[] = [];

@NgModule({imports: [FamiliensituationXyFeatureFamiliensituationXyComponent]})
export class FamiliensituationXyLazyLoadingModule {}

@NgModule({
    imports: [UIRouterUpgradeModule.forChild({states})],
    exports: [UIRouterUpgradeModule]
})
export class FamiliensituationXyFeatureFamiliensituationXyRoutingModule {}
```

Note, that we removed the state for the root component but keep the RoutingModule, since we might add child routes
for sub-features of the familiensituation. Now we can define a state that loads this module lazily:

```typescript
const state: NgHybridStateDeclaration = {
    name: 'gesuch.familiensituation',
    url: '/familiensituation-xy',
    loadChildren: () =>
        import('familiensituation-xy/feature/familiensituation-xy').then(
            mod =>
                mod.FamiliensituationXyFeatureFamiliensituationXyRoutingModule
        )
};
```
