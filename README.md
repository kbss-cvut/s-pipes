# SPipes

SPipes is tool to manage semantic pipelines defined in RDF inspired by [SPARQLMotion](https://sparqlmotion.org/) language. Each node in a pipeline represents some stateless transformation of data. 

## Basic concepts

All terms defined in this section refers to SPipes terminology:

- *Module type* -- A template of transformations.
- *Module* -- A stateless transformation of input data that instantiates a *module type*. 
- *Pipeline* -- Directed graph representing data flow of one program call consisting of set of *modules*. The graph must be acyclic and connected, with exactly one sink node. Each node is *module*  while edges represent execution dependencies between *modules*. The sink node represents last transformation of the program call, i.e. last transformation of a *module*.
- *Script* -- Set of *pipelines*. 
- *Function* -- Pointer to a reusable part of a *pipeline* with constraints on input of the execution. It points to a *module* that should be executed. Output of the *module* is output of the function.  
- *Engine* -- Executes transformations of input data according to a *pipeline*.


## SPipes Features

- Custom SPARQL functions
- Web and CLI interface to execute scripts
- Auditing execution, i.e. logging metadata about execution of modules, their input/output data, etc.

## Loading of Pipelines

SPipes loads pipelines by recursive traversal of configured directories, searching for ontology files represented by `.ttl` suffix. *Global scripts* are represented by suffix **.sms.ttl**. A script is identified by ontology iri in which it is defined. Ontology imports (using rdf property **owl:imports**) can be used to modularize scripts into multiple files. Script defines set of pipelines from its ontology import closure. 

## Examples

SPipes script construction, execution, and execution history tracking is explained 
in [Hello world example](doc/examples/hello-world/hello-world.md).
Script debugging is explained in [skosify example](doc/examples/skosify/skosify.md).




## Structure of Maven Submodules

### SPipes Core 

Maven module SPipes Core provides core functionality related to SPipes engine, ontology manager, auditing. It contains configuration file `config-core.properties`, where directories of scripts for loading is configured.

### SPipes Web 

Web user interface for SPipes that allows to execute any *function* defined in *global scripts*. The *function* can be called by HTTP GET request

 `$WEB_APP_URL/service?id=$FUNCTION&$PARAM_NAME_1=$PARAM_VALUE_1&$PARAM_NAME_2=$PARAM_VALUE_2...`, where 
* `$WEB_APP_URL` -- url, where SPipes is deployed , e.g. "https://localhost:8080/s-pipes".
* `$FUNCTION` -- a function defined in a global script identified by URL. In case there is no collision the  `localName` of the URL can be used. E.g. instead of using URL "http://example.org/my-function" one can use  "my-function").
* `$PARAM_NAME_1`, `$PARAM_NAME_2`, ... -- names of parameters e.g. "repositoryName". 
* `$PARAM_VALUE_1`, `$PARAM_VALUE_2`, ... -- value of parameters e.g. "myRepository".

Example call:
    `https://localhost:8080/s-pipes/service?id=my-function&repositoryName=myRepository`

In addition, there is a [list of reserved parameter names](doc/reserved-parameters.md).

### SPipes CLI

Maven module SPipes CLI provides command-line interface to SPipes engine. In addition to `config-core.properties`, directories configured to load scripts can be overridden by command-line variable SPIPES_ONTOLOGIES_PATH. E.g. in UNIX shell following command can be used:
export SPIPES_ONTOLOGIES_PATH="/home/someuser/s-pipes-scripts"

### SPipes Modules Registry

Defines dependencies of all specific *module types* that are used in Web and Cli interface at same time.

### SPipes Modules

Contains specific SPipes *module types*.

### SPipes Model

Defines Java model that is used for serialization of metadata about execution of pipelines. It is based  on JOPA (Java OWL Persistence API) for accessing OWL ontologies, where those metadata are saved.

## Development Environment Setup

The following software needs to be installed on the system for development:

- JDK 8
- Maven

## Dockerization
  The docker image of SPipes backend can be built by
  `docker build -t s-pipes-engine .`

  SPipes web can be run and exposed at the port 8080 as
  `docker run -v /home:/home -p 8080:8080 s-pipes-engine:latest` and the endpoint is http://localhost:8080/s-pipes. The `-v /home:/home`
  option mount your home to docker image - this is very convenient for testing.

Configuration properties could be overloaded by system environment such as `CONTEXTS_SCRIPTPATHS=/my/special/path`. The full build command could look like:
  `docker run -e CONTEXTS_SCRIPTPATHS=/my/special/path -v /home:/home -p 8080:8080 s-pipes-engine:latest`


## Licences of Reused software components

Beside included software dependencies by Maven, see a [list of reused software components, and their licences](./doc/licences.md).

