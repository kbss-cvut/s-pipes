# SPipes

SPipes is tool to manage semantic pipelines defined in RDF. Each node in a pipeline represents some stateless transformation of data. 

## Basic concepts

All terms defined in this section refers to SPipes terminology.

**_Module type_** -- A template of transformations.

**_Module_** -- A stateless transformation of input data that instantiates a _module type_. 

**_Pipeline_** -- Directed graph representing data flow of one program call consisting of set of _modules_. The graph must be acyclic and connected, with exactly one sink node. Each node is _module_  while edges represent execution dependencies between _modules_. The sink node represents last transformation of the program call, i.e. last transformation of a _module_.

**_Script_** -- Set of _pipelines_. 

**_Function_** -- Pointer to a reusable part of a _pipeline_ with constraints on input of the execution. It points to a _module_ that should be executed. Output of the _module_ is output of the function.  

**_Engine_** -- Executes transformations of input data according to a _pipeline_.


## SPipes Features

- Custom sparql functions
- Web and CLI interface to execute scripts
- Auditing execution, i.e. logging metadata about execution of modules, their input/output data, etc.

## Loading of pipelines

SPipes loads pipelines by recursive traversal of configured directories, searching for ontology files represented by **.ttl** suffix. **Global scripts** are represented by suffix **.sms.ttl**. A script is identified by ontology iri in which it is defined. Ontology imports (using rdf property **owl:imports**) can be used to modularize scripts into multiple files. Script defines set of pipelines from its ontology import closure. 

## Structure of Maven Submodules

### SPipes Core 

Maven module SPipes Core provides core functionality related to SPipes engine, ontology manager, auditing. It contains configuration file `config-core.properties`, where directories of scripts for loading is configured.

### SPipes Web 

Web user interface for SPipes that allows to execute any _function_ defined in _global scripts_. The function can be called by http GET request :
 _$webApp_/service?id=_$functionName_&_$paramName1_=_paramValue1_&_$paramName2_=_paramValue2_ ...

_$webApp_ -- url, where SPipes is deployed , e.g. "https://localhost:8080/s-pipes".

_$functionName_ -- name of a function defined in a global script. In case of name collision whole url of the function can be used. E.g. "my-function" can be used instead of full iri  "http://example.org/my-function".

_$paramName_ -- name of parameter e.g. "repositoryName". 

_$paramName_ -- name of parameter e.g. "myRepository".

Example call:
https://localhost:8080/s-pipes/service?id=my-function&repositoryName=myRepository


### SPipes CLI

Maven module SPipes Cli provides command-line interface to SPipes engine. In addition to `config-core.properties`, directories configured to load scripts can be overriden by command-line variable SPIPES_ONTOLOGIES_PATH. E.g. in UNIX shell following command can be used:
export SPIPES_ONTOLOGIES_PATH="/home/someuser/s-pipes-scripts"

### SPipes Modules Registry

Defines dependencies of all specific module types that are used in Web and Cli interface at same time.

### SPipes Model

Defines Java model that is used for serialization of metadata about execution of pipelines. It is based  on JOPA (Java OWL Persistence API) for accessing OWL ontologies, where those metadata are saved.

### SPipes Modules

TOOD variables + graphs, ApplyConstruct

## Development Environment Setup

The following software needs to be installed on the system for development:

- JDK 8
- Maven

## Some Notes on Development

