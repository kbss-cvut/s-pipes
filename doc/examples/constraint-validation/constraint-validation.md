# Constraint validation example

The following text explains how SPipes module inputs and outputs can be checked by validation constraints.

## Introduction
This document is focused mainly on constraint validation, but you can take a look at [hello-world-example](https://github.com/kbss-cvut/s-pipes/blob/main/doc/examples/hello-world/hello-world.md) for more details about script construction and execution.

## Definition of validation constraints
Each SPipes module can have any number of validation constraints on its input (see `kbss:has-input-graph-constraint`) and its output (see `kbss:has-output-graph-constraint`). Each validation constraint is a SPARQL query. Currently, we support 2 types of queries:
* `ASK` -- returns true if validation constraint is violated

  - For example, we can create output constraint validating person's age. If a person is younger than 18 years, then validation fails.
    ```
    kbss:has-output-graph-constraint [
      a sp:Ask ;
      sp:text """# Person must be at least 18 years old
         PREFIX foaf: <http://xmlns.com/foaf/0.1/>
          ASK WHERE {
              ?person foaf:age ?age .
              FILTER (?age < 18) .
          }
          """ ;
    ].
    ```
    
* `SELECT` -- returns non-empty variable bindings if validation constraint is violated. The variable binding should be used to exemplify/explain what particular entities are violating the constraint.

  - E.g. we make a constraint ensuring that person 'Martin Novak' exists, i.e. if the person does exists, the validation fails.
  ```
  kbss:has-output-graph-constraint [
    a sp:Select ;
    sp:text """# Person 'Martin Novak' does not exist.
      PREFIX foaf: <http://xmlns.com/foaf/0.1/>
      PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
      SELECT *
      WHERE{
        FILTER NOT EXISTS{
          ?person foaf:lastName "Novak";
                  foaf:firstName "Martin";
                  a foaf:Person; }
      }""" ;
  ];
  ```
## Example

Let's imagine that we have database of people, that is small enough to expect that all people will have different names. We construct pipeline that creates new person, but keep us on track if the assumption about the different names would not hold.

1) First, we import the database from a [file](./people.ttl) with ontology iri `http://onto.fel.cvut.cz/ontologies/s-pipes/examples/constraint-validation/people`.
```
:import-person-database
  a sml:ImportRDFFromWorkspace ;
  sm:next :constraint-validation_Return;
  sml:baseURI "http://onto.fel.cvut.cz/ontologies/s-pipes/examples/constraint-validation/people" ;
  sml:ignoreImports true ;
  rdfs:label "Import person database" ;
.
```

2) Afterwards we validate the output from the previous part of the script so that we know if the script properly works. We create an output graph constraint which validates
   existence of person "Pavel Hnizdo" with ASK query. If there does not exist this person then validation fails.
   In this case constraint is validated because person "Pavel Hnizdo" already exists.

```
kbss:has-output-graph-constraint [
  a sp:Ask ;
  sp:text """# Person 'Pavel Hnizdo' does not exist
    PREFIX foaf: <http://xmlns.com/foaf/0.1/>
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    ASK
    WHERE{
        ?person a foaf:Person;
        FILTER NOT EXISTS { ?person foaf:lastName "Hnizdo";
                                    foaf:firstName "Pavel" }
    }""" ;
].
```

3) We create another constraint which checks if the specified person is in the database.

```
 kbss:has-output-graph-constraint [
  a sp:Select ;
  sp:text """# Person provided in input does not exist.
    PREFIX foaf: <http://xmlns.com/foaf/0.1/>
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    SELECT *
    WHERE{
         FILTER NOT EXISTS{
            ?person foaf:lastName ?lastname;
                 	foaf:firstName ?firstname;
                 	a foaf:Person;
    	}
    }""" ;
];
 ```
Now our pipeline is prepared, and we can run pipeline.
1) We want to check if 'Pavel Hnizdo' is in our database. So we call following GET request. But we know that exactly this person is created in database, so both constraints are validated.
```
http://localhost:8080/s-pipes/service?id=constraint-validation&firstname=Pavel&lastname=Hnizdo
```

2) But we can also send another request that checks if different person with name 'Martin Novak' exists in database.

```
http://localhost:8080/s-pipes/service?id=constraint-validation&firstname=Martin&lastname=Novak
```
After pipeline execution validation constraint fails with message 'Person provided in input does not exist.' because 'Martin Novak' is not in our database.
```
Failed validation constraint : 
 # Person provided in input does not exist.

    PREFIX foaf: <http://xmlns.com/foaf/0.1/>
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    SELECT *
    WHERE{
         FILTER NOT EXISTS{
            ?person foaf:lastName ?lastname;
                 	foaf:firstName ?firstname;
                 	a foaf:Person;
    	}
    }
Evidence of the violation: 
( ?firstname = "Martin" ) ( ?lastname = "Novak" ) ( ?_pId = "constraint-validation" )
```

The final script [constraint-validation.sms.ttl](constraint-validation.sms.ttl).

### Properties
We can specify properties in `config-core.properties`.
* `execution.checkValidationContraint` -- enables constraint validation check (true|false)
* `execution.exitOnError` -- whole pipeline fails when validation constraint fails  (true|false)



### INFO
Let's assume that SPipes web application is running at `http://localhost:8080/s-pipes`. We can call the *pipeline* with:
```
http://localhost:8080/s-pipes/service?id=constraint-validation&firstname=$ARGUMENT1&lastname=$ARGUMENT2
```
where `$ARGUMENT1` is first name and `$ARGUMENT2` is last name of a person we want to check if exists in the database.
