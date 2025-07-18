# SPipes Forms Module

Main focus of this module is to provide mapping between scripts and forms, 
i.e. describing transformations from scripts to forms and vice versa.
Those forms should be used to edit or run scripts in a user-friendly way.

More information about the mapping can be found in [mapping scripts to forms](#mapping-scripts-to-forms) section.

## Development

## Mapping scripts to forms

Different parts of scripts are mapped to forms represented by [SForms model](https://github.com/kbss-cvut/s-forms-model) 
that can be viewed by [SForms library](https://github.com/kbss-cvut/s-forms).

### Module configuration form

Module configuration is mapped to its form as shown in the following figure: 
![mapping module configuration](./doc/mapping-scripts-to-forms.png)

The figure shows that the module configuration form is mapped to the script, more particularly  
how concrete questions within the form are mapped to concrete RDF triples within the script.
It visualizes following questions within the form:
- identityQuestion - the question created from the IRI of the module configuration
- statementQuestion - the question created from one concrete RDF statement of the module configuration
- schemaQuestion - the question created from the schema of the module configuration, i.e. module type

### Editing Test Source Code Annotated with Lombok
To edit the test source code annotated with lombok and take advantage of
the IntelliJ IDEA:
* Unmark `src/test/generated-test-sources` folder as `test source root`
* Mark `src/test/lombok` folder as `test source root`

When done editing test source code annotated with lombok revert folder marking.