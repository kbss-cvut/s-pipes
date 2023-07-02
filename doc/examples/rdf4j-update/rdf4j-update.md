# RDF4J update example

RDF4J update example explains how to update RDF4J repository.

## Introduction

This example contains script *rdf4j-update.sms.ttl* that contain one pipeline. It creates RDF4J repository and performs update query. For more details about script construction and execution you can see [hello-world-example](https://github.com/kbss-cvut/s-pipes/blob/main/doc/examples/hello-world/hello-world.md).

## Script structure

Script performs following steps:
1) Creates RDF4J repository using `kbss:rdf4j-create-repository` module. Repository is specified by parameters `rdf4j:p-rdf4j-server-url` and `rdf4j:p-rdf4j-repository-name`. Parameter `rdf4j:p-rdf4j-ignore-if-exists` defines behavior of the module in case when defined repository already exists:
in case if it is set to be "true", module will not do anything if repository with given ID already exists on the server.  


    :create-repository
        a kbss:rdf4j-create-repository ;
        rdf4j:p-rdf4j-server-url "http://localhost:8080/rdf4j-server/" ;
        rdf4j:p-rdf4j-repository-name "test-update" ;
        rdf4j:p-rdf4j-ignore-if-exists "true" ;
        sm:next :update ;
    .

2) Perform an update on repository using `a kbss:rdf4j-update` module. Repository is defined in the same way as in `:create-repository`. Update query is set by string in `sp:text` section.


    :update
        a kbss:rdf4j-update ;
        sm:next :update-repository_Return ;
        sml:updateQuery [
            a sp:Update ;
            sp:text """
    DELETE {
        <http://example.org/people/john>  <http://example.org/people/age> ?oldAge .
    }
    INSERT {
        <http://example.org/people/john>  <http://example.org/people/age> ?newAge .
    } WHERE {
        OPTIONAL {
        <http://example.org/people/john>  <http://example.org/people/age> ?oldAge .
        }
        BIND(COALESCE(?oldAge+1, 1) as ?newAge)
    }
            """ ;
        ];
        rdf4j:p-rdf4j-server-url "http://localhost:8080/rdf4j-server/" ;
        rdf4j:p-rdf4j-repository-name "test-update" ;
        rdfs:label "Make insert update" ;
    .

## Script execution

Let's assume that SPipes web application is running at `http://localhost:8080/s-pipes`. We can call the *pipeline* with:

    http://localhost:8080/s-pipes/service?_pId=update-repository

Note, that rdf4j server should be running on the URL that is specified in script (`http://localhost:8080/rdf4j-server/` for example).
You can see following logs while execution:

    [http-nio-8080-exec-7] INFO  c.c.s.e.ExecutionEngineImpl -  ##### create-repository
    ...
    INFO [http-nio-8080-exec-7] cz.cvut.spipes.modules.Rdf4jCreateRepositoryModule.executeSelf Server url: http://localhost:8080/rdf4j-server/ Repository name: test-update Ignore flag: true
    INFO [http-nio-8080-exec-6] cz.cvut.spipes.modules.Rdf4jCreateRepositoryModule.executeSelf Repository "test-update" already exists
    ...
    [http-nio-8080-exec-7] INFO  c.c.s.e.ExecutionEngineImpl -  ##### Make insert update
    ...
    INFO [http-nio-8080-exec-7] cz.cvut.spipes.modules.Rdf4jUpdateModule.loadConfiguration Connection created for repository test-update
    INFO [http-nio-8080-exec-7] cz.cvut.spipes.modules.Rdf4jUpdateModule.makeUpdate Update executed

This log will occur when Ignore flag is set to true and repository already exists:
`INFO [http-nio-8080-exec-6] cz.cvut.spipes.modules.Rdf4jCreateRepositoryModule.executeSelf Repository "test-update" already exists`