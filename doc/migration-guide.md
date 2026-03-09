# SPipes Migration Guide

This guide helps you migrate your SPipes scripts (TTL files) and configurations when upgrading between versions, 
particularly focusing on major architectural changes.

## Version 0.7.0 - Removed Modules

### Overview
Use of module `kbss-module:tarql` requires refactoring in SPipes script files.

### Note
The Tarql module (`kbss-module:tarql`) is no longer available. Wherever possible replace 
it with `kbss-module:tabular` for CSV processing.

## Version 0.6.0

### Overview
No changes needed in SPipes script files for this version upgrade.

## Version 0.5.0 - Core Vocabulary of Scripts Changed

### Overview
Existing SPipes script files should be refactored to replace complex RDF-based SPIN expressions with simple text-based ones.
More info can be found in https://github.com/kbss-cvut/lkpr-model/issues/17.

### What to Change

SPIN expressions were previously described using complex RDF structures. 
These should be replaced with text expressions using the `sp:text` property and `sp:Expression` type.

Detail steps are:
- Remove SPIN RDF SPARQL queries
- Refactor SPIN RDF functions (SPARQL functions) to SHACL RDF functions
- Refactor SPIN expressions to new expression
- Update prefixes and imports
- Replace calls to spin functions

#### Remove SPIN RDF SPARQL queries
- remove sp:where and sp:templates triples
- remove `rdfs:comment` if it is sibling property of `rdf:type` with value `sp:Ask`, `sp:Select` or `sp:Construct`
- sp:text should have SPARQL string literal equivalent to the removed sp:where and sp:templates triples.

**Example from hello-world.sms.ttl:**
```turtle
:construct-greeting
  a sml:ApplyConstruct ;
  sml:constructQuery [
      a sp:Construct ;
      sp:templates (
          [
            sp:object [
                sp:varName "greetingMessage" ;
              ] ;
            sp:predicate :is-greeted-by-message ;
            sp:subject [
                sp:varName "person" ;
              ] ;
          ]
        ) ;
      sp:where (
          [
            rdf:type sp:Bind ;
            sp:expression [
                rdf:type sp:iri ;
                sp:arg1 [
                    rdf:type sp:concat ;
                    sp:arg1 [
                        rdf:type sp:str ;
                        sp:arg1 [
                            sp:varName "personId" ;
                          ] ;
                      ] ;
                  ] ;
              ] ;
            sp:variable [
                sp:varName "person" ;
              ] ;
          ]
          [
            rdf:type sp:Bind ;
            sp:expression [
                rdf:type sp:concat ;
                sp:arg1 "Hello " ;
                sp:arg2 [
                    sp:varName "personName" ;
                  ] ;
                sp:arg3 "." ;
              ] ;
            sp:variable [
                sp:varName "greetingMessage" ;
              ] ;
          ]
        ) ;
        rdfs:comment "construct greeting message" ;
    ] ;
  sml:replace true ;
.
```

to new string-based representation:

```turtle
:construct-greeting
  a sml:ApplyConstruct ;
  sml:constructQuery [
      a sp:Construct ;
      sp:text """#construct greeting message
CONSTRUCT {
    ?person :is-greeted-by-message ?greetingMessage .
} WHERE {
    BIND(iri(concat(str(:), ?personId)) as ?person)
    BIND(concat("Hello ", ?personName, ".") as ?greetingMessage)
}
""" ;
    ] ;
  sml:replace true ;
.
```



#### Refactor SPIN RDF functions (SPARQL functions) to SHACL RDF functions
- `rdf:type spin:Function` → `rdf:type sh:SPARQLFunction`
- `spin:body` → `sh:select`
- `spin:constraint` → `sh:parameter`
    - replace spin:constraint:
      ```
      spin:constraint [
          rdf:type spl:Argument ;
          spl:predicate sp:arg1 ;
          spl:valueType xsd:string ;
          rdfs:comment "Sparql endpoint uri (e.g. http://onto.fel.cvut.cz/openrdf-sesame/repositories/test-repo)." ;
      ] ;
      ```
      with sh:parameter:
      ```
      sh:parameter [
          sh:path kbss-shaclf:arg1 ; # the prefix of the base namespace of the script
          sh:datatype xsd:string ;
          sh:description "Sparql endpoint uri (e.g. http://onto.fel.cvut.cz/openrdf-sesame/repositories/test-repo)."@en ;
      ] ;
      ```
    - Within `spin:constraint` replace:
        - `spl:predicate` → `sh:path`
        - `spl:valueType` → `sh:datatype`
        - `rdfs:comment` → `sh:description`
        - `spl:optional` → `sh:optional`
        - ...
   For more information, see revised TTL test files in s-pipes project and [SPIN to SHACL migration guide](https://spinrdf.org/spin-shacl.html#rules-sparql).

#### Refactor SPIN expressions to new expression form
SPIN RDF representation in `sml:value` and module properties.

**_Example for sml:value_** - expected in `BindWithConstant` modules but not limited to them:
```
:some_resource sml:value [
      rdf:type sp:concat ;
      sp:arg1 "Hello " ;
      sp:arg2 [
          sp:varName "name"^^xsd:string ;
        ] ;
    ] ;
```
to new string-based representation

```
:some_resource sml:value [ a sp:Expression ;
                sp:text """concat("Hello ", ?name)""" ;
             ] ;
```

_**Example 1 for module properties**_ - assigned a variable.
```
td:deploy-to-safety-performance-repository km-rdf4j:p-rdf4j-server-url [
      sp:varName "safetyPerformanceRdf4jServer" ;
    ] ;
```
to new string-based representation
```
td:deploy-to-safety-performance-repository km-rdf4j:p-rdf4j-server-url [
       a sp:Expression ;
       sp:text "?safetyPerformanceRdf4jServer" ;
    ] ;
```
_**Example 2 for module properties**_ - assigned return value of function call.

```
td:rdfize-input-data km-tabular:data-prefix [
      a sp:str ;
      sp:arg1 : ;
    ] ;
```
to new string-based representation
```
td:rdfize-input-data km-tabular:data-prefix [
      a sp:Expression ;
      sp:text  "str(:)" ;
    ] ;
```
_**Example 3**_ - more complete example from hello world script.
```turtle
@prefix sp: <http://spinrdf.org/sp#> .
@prefix sml: <http://topbraid.org/sparqlmotionlib#> .

:bind-person-name
  a sml:BindWithConstant ;
  sml:value [
    a sp:concat ;
    sp:arg1 [
      sp:varName "firstName" ;
    ] ;
    sp:arg2 " " ;
    sp:arg3 [
      sp:varName "lastName" ;
    ] ;
  ] ;
.
```

to new string-based representation

```turtle
@prefix sp: <http://spinrdf.org/sp#> .
@prefix sml: <http://topbraid.org/sparqlmotionlib#> .

:bind-person-name
  a sml:BindWithConstant ;
  sml:value [ a sp:Expression ;
              sp:text """concat(?firstName, " ", ?lastName)""" ;
    ] ;
.
```


#### Update prefixes and imports
- Add `@prefix sh: <http://www.w3.org/ns/shacl#> .` to script file
- Add `owl:imports <http://www.w3.org/ns/shacl> ;` to ontology resource in script file
- Remove `owl:imports <http://spinrdf.org/spin> ;`

#### Replace calls to spin functions
- Replace calls to spin functions in `sp:text` literals with calls to ARQ, SHACL, or custom functions. For example, replace `sp:concat` with `concat`.
