# Constraint validation example

The following text explains how SPipes module inputs and outputs can be checked by validation constraints.

## Introduction
This document is focused mainly on constraint validation, but you can take a look at [hello-world-example](https://github.com/kbss-cvut/s-pipes/blob/main/doc/examples/hello-world/hello-world.md) for more details about script construction and execution.

## Definition of validation constraints
There are two types of queries we can validate constraints with:
* `ASK` -- returns true if the condition in the body is met by at least one result set
* `SELECT` -- returns variable bindings as its result

## Example
We create simple script which shows the example of constraint validation usage. The script only changes the last name of person and meanwhile validates constraints.

1) First, we construct simple data about person with name "Pavel Hnizdo" within RDF language.
```
:createPersonTriples
    rdf:type sml:ApplyConstruct ;
    sm:next :changeLastName;
    sml:constructQuery [
        a sp:Construct ;
        sp:text """
          PREFIX foaf: <http://xmlns.com/foaf/0.1/>
          PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
          CONSTRUCT {
                <http://example.org/person1>    
                        a foaf:Person;
                        foaf:firstName "Pavel";
                        foaf:lastName "Hnizdo";
                        rdf:id "1".
          } WHERE {}
        """ ;
  ];
  rdfs:label "Create person triples";
```

2) Afterwards we validate the output from the previous part of the script. We create an output graph constraint which validates 
existence of person "Pavel Hnizdo" with ASK query. If there does not exist this person then validation fails.
In this case constraint is validated because person "Pavel Hnizdo" already exists.

```
kbss:has-output-graph-constraint [
  a sp:Ask ;
  sp:text """# Person 'Pavel Hnizdo' with id '1' does not exist
    PREFIX foaf: <http://xmlns.com/foaf/0.1/>
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    ASK
    WHERE{
        ?person a foaf:Person;
                rdf:id "1".
        FILTER NOT EXISTS { ?person foaf:lastName "Hnizdo";
                                    foaf:firstName "Pavel" }
    }""" ;
].
```

3) In next part we change last name of the person from "Hnizdo" to "Novak".
```
:changeLastName
    rdf:type sml:ApplyConstruct;
    sm:next :constraint-validation_Return;
    sml:constructQuery [
      a sp:Construct ;
      sp:text """
        PREFIX foaf: <http://xmlns.com/foaf/0.1/>
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        CONSTRUCT {
            ?person foaf:firstName ?firstName;
                    foaf:lastName ?lastName;
                    a foaf:Person;
                    rdf:id ?id
        } WHERE {
            ?person a foaf:Person;
                    rdf:id ?id;
                    foaf:firstName ?firstName;
                    foaf:lastName ?oldLastName
            BIND(IF(?oldLastName = "Hnizdo" && ?firstName = "Pavel", "Novak", ?oldLastName) as ?lastName )
        }
        """ ;
  ];
  sml:replace true;
  rdfs:label "Change last name";
  ```

4) However, before renaming our person we add another input constraint which checks if there exists "Pavel Novak". 
Constraint is validated because person "Pavel Novak" does not exist yet (name change will be executed after this constraint check as you can see bellow in the picture). 
```
kbss:has-input-graph-constraint [
  a sp:Ask ;
  sp:text """# Person 'Pavel Novak' with id '1' already exists
    PREFIX foaf: <http://xmlns.com/foaf/0.1/>
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    ASK
    WHERE{
        ?person a foaf:Person;
                rdf:id "1";
                foaf:lastName "Novak";
                foaf:firstName "Pavel"
    }""" ;
];
```

5) Finally, we create last output constraint which checks if person with id '1', first name 'Pavel' and last name different from 'Hnizdo' exists.
In our case validation fails because we renamed 'Hnizdo' to 'Novak', we can also see evidence of violation from the error log at the end of this document.
```
 kbss:has-output-graph-constraint [
   a sp:Select ;
   sp:text """# Person with id '1', first name 'Pavel' and last name different than 'Hnizdo' exists
     PREFIX foaf: <http://xmlns.com/foaf/0.1/>
     PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
     SELECT *
     WHERE{
       ?person a foaf:Person;
               foaf:lastName ?lastName;
               foaf:firstName "Pavel;
               rdf:id "1".
        FILTER NOT EXISTS { ?person foaf:lastName "Hnizdo" }
    }""" ;
 ].
 ```

The final script [constraint-validation.sms.ttl](constraint-validation.sms.ttl) can be visualized as follows:

![graphical notation](constraint-validation-graphical-notion.svg)

### Properties
We can specify properties in `config-core.properties`.
* `execution.checkValidationContraint` -- enables constraint validation check (true|false)
* `execution.exitOnError` -- whole pipeline fails when validation constraint fails  (true|false)



### INFO
Let's assume that SPipes web application is running at `http://localhost:8080/s-pipes`. We can call the *pipeline* with:
```
http://localhost:8080/s-pipes/service?id=constraint-validation
```

The call produces error because output constraint was not validated as shown bellow where we can see main error message, failed query and for select query also evidence of the violation:
```
04-03-2022 15:30:49.342 [http-nio-8080-exec-2] ERROR c.c.s.modules.AbstractModule - Validation of constraint failed for the constraint "Person with id '1', first name 'Pavel' and last name different than 'Hnizdo' exists".
Failed validation constraint : 
 # Person with id '1', first name 'Pavel' and last name different than 'Hnizdo' exists
     PREFIX foaf: <http://xmlns.com/foaf/0.1/>
     PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
     SELECT *
     WHERE{
       ?person a foaf:Person;
               foaf:lastName ?lastName;
               foaf:firstName "Pavel";
               rdf:id "1".
        FILTER NOT EXISTS { ?person foaf:lastName "Hnizdo" }
    }
Evidence of the violation: 
( ?lastName = "Novak" ) ->  ->  -> ( ?person = <http://example.org/person1> ) -> ( ?_pId = "constraint-validation" )

```