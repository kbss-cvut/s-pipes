# SPipes

SPipes is a tool to manage semantic pipelines defined in RDF inspired by [SPARQLMotion](https://sparqlmotion.org/) language. Each node in a pipeline represents some stateless transformation of data. [SPipes Editor](https://github.com/kbss-cvut/s-pipes-editor) is a tool for viewing/editing/executing/debugging SPipes scripts.

## Basic concepts

All terms defined in this section refer to SPipes terminology:

- *Module type* -- A template of transformations.
- *Module* -- A stateless transformation of input data that instantiates a *module type*. 
- *Pipeline* -- Directed graph representing data flow of one program call consisting of the set of *modules*. The graph must be acyclic and connected with exactly one sink node. Each node is *module*  while edges represent execution dependencies between *modules*. The sink node represents the last transformation of the program call, i.e., the last transformation of a *module*.
- *Script* -- Set of *pipelines*. 
- *Function* -- Pointer to a reusable part of a *pipeline* with constraints on input of the execution. It points to a *module* that should be executed. The output of the *module* is the output of the function.  
- *Engine* -- Executes transformations of input data according to a *pipeline*.


## SPipes Features

- Execute custom SPARQL functions within pipeline nodes
- Web and CLI interface to execute scripts
- Auditing execution, i.e., logging metadata about the execution of modules, their input/output data, etc.

## Loading of Pipelines

SPipes loads pipelines by recursive traversal of configured directories, searching for ontology files represented by `.ttl` suffix. *Global scripts* are represented by suffix **.sms.ttl**. A script is identified by ontology IRI in which it is defined. Ontology imports (using rdf property **owl:imports**) can be used to modularize scripts into multiple files. The script defines a set of pipelines from its ontology import closure. 

## Example scripts

Script construction, execution, debugging, constraint violation and many other features 
of SPipes together with specific modules are explained in [example scripts](./doc/examples/examples.md).

## Structure of Maven Submodules

### SPipes Core 

Maven module SPipes Core provides core functionality related to the SPipes engine, ontology manager, and auditing. It contains the configuration file `config-core.properties`, where directories of scripts for loading are configured.

### SPipes Web 

Web user interface for SPipes that allows to execute any *function* defined in *global scripts*. The *function* can be called by HTTP GET request

 `$WEB_APP_URL/service?_pId=$FUNCTION&$PARAM_NAME_1=$PARAM_VALUE_1&$PARAM_NAME_2=$PARAM_VALUE_2...`, where 
* `$WEB_APP_URL` -- URL, where SPipes is deployed, e.g., "https://localhost:8080/s-pipes".
* `$FUNCTION` -- a function defined in a global script identified by URL. If there is no collision, the  `localName` of the URL can be used. E.g., instead of using the URL "http://example.org/my-function," one can use "my-function").
* `$PARAM_NAME_1`, `$PARAM_NAME_2`, ... -- names of parameters e.g. "repositoryName". 
* `$PARAM_VALUE_1`, `$PARAM_VALUE_2`, ... -- value of parameters e.g. "myRepository".

Example call:
    `https://localhost:8080/s-pipes/service?_pId=my-function&repositoryName=myRepository`

In addition, there is a [list of reserved parameter names](doc/reserved-parameters.md).

### SPipes CLI

Maven module SPipes CLI provides a command-line interface to the SPipes engine. In addition to `config-core.properties`, directories configured to load scripts can be overridden by command-line variable SPIPES_ONTOLOGIES_PATH. For E.g. in the UNIX shell, the following command can be used:
export SPIPES_ONTOLOGIES_PATH="/home/someuser/s-pipes-scripts"

### SPipes Modules Registry

Defines dependencies of all specific *module types* that are used in Web and Cli interface at the same time.

### SPipes Modules

Contains specific SPipes *module types*.

### SPipes Modules Utils

Contains developer tools for working with SPipes module types. Specifically:
- `s-pipes-module-archetype` is a Maven Archetype for generating a clean template for a new module type. `bin/generate-test-module.sh` can be used for quickstarting. See the [official Maven docs](https://maven.apache.org/guides/introduction/introduction-to-archetypes.html) for more info.
- `s-pipes-module-creator-maven-plugin` is a Maven Plugin for post-processing existing modules. It takes care of updating the RDF ontologies within the module.


### SPipes Model

Defines Java model that is used for serialization of metadata about execution of pipelines. It is based  on JOPA (Java OWL Persistence API) for accessing OWL ontologies, where those metadata are saved.

## Development Environment Setup

The following software needs to be installed on the system for development:

- JDK 17 or newer
- Maven 3
- Apache Tomcat 9.0

## Dockerization

### Building the Docker Image
  The Docker image of the SPipes backend can be built using the following command:

  `docker build -t s-pipes-engine .`

### Running the Docker Container

  SPipes web can be run and exposed at port 8080 with the following command:

  `docker run -p 8080:8080 s-pipes-engine:latest` 

The endpoint will be available at http://localhost:8080/s-pipes

#### Configuration of SPipes scripts

By default, scripts are loaded from the directory `/scripts` within the filesystem of the s-pipes-engine image. 
The directory already contains the necessary definitions of reusable modules, so 
new scripts must be added to this directory to extend the scripts. All subdirectories of `/scripts` are searched recursively.
A good practice is to mount local scripts to e.g., `/scripts/root` directory of the image, e.g.:
`docker run -v ./my-scripts:/scripts/root -p 8080:8080 s-pipes-engine:latest`

Another option to configure scripts is to redefine where the SPipes engine searches the scripts using `CONTEXTS_SCRIPTPATHS`:
`docker run -e CONTEXTS_SCRIPTPATHS=/my/special/path -p 8080:8080 s-pipes-engine:latest`

This is particularly useful when one would like to share the same path between the host filesystem and the docker image as 
explained in the following section.

#### Aligning file paths between the docker service and host system

For your SPipes script files, you can align file paths between Docker services and your host system using mounting.
This allows a directory to be accessible from both Docker services and the host filesystem, 
ensuring that file paths remain the same. Consequently, you can copy an absolute path to a file from
a Docker service and open it on the host filesystem, and vice versa.

For Linux, the typical path is `/home`, while for Windows, it is `/host_mnt/c`. 
When running Docker on Windows, Docker replaces `C:` with `/host_mnt/c`. When running Docker on Windows from within 
WSL distribution `C:` is accessible through `/mnt/c`.

To mount a directory from your host machine to the Docker container, use the following command:

*Linux:*

`docker run -v /home:/home -p 8080:8080 s-pipes-engine:latest` 

*Windows:*

`docker run -v /host_mnt/c:/host_mnt/c -p 8080:8080 s-pipes-engine:latest` 

*Windows, but running inside WSL:*

`docker run -v /mnt/c:/mnt/c -p 8080:8080 s-pipes-engine:latest`

## Swagger

Swagger documents rest API. We can open Swagger UI with: `SPIPES_URL/swagger-ui.html`.

## Licences of Reused software components

Besides included software dependencies by Maven, see a [list of reused software components and their licenses](./doc/licenses.md).

