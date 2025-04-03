# Hands-on tutorial for SPipes Debug Module

This tutorial will guide you through the process of using SPipes Debug Module. 
You will learn how to retrieve information about executed pipelines and modules through REST API.

It is recommended to use some JSON formatter in browser 

It is  recommended to install some JSON formatter for your browser 
(e.g. [JsonDiscovery](https://chrome.google.com/webstore/detail/jsondiscovery/pamhglogfolfbmlpnenhpeholpnlcclo)), 
as it will be hard to orient with lots of data.

To start the tutorial follow the steps:
1) `cd ${project-root}/deploy/`
2) `docker-compose up`


# API
- Rdf4j server - localhost:1234/db-server
- SPipes core engine - localhost:1234/s-pipes
- SPipes debug engine - localhost:1234/s-pipes-debug

For list of all endpoints, check out Swagger API here:
- http://localhost:1234/s-pipes-debug/swagger-ui.html

# Related resources
The SPipes debug API implements HATEOAS technology. HATEOAS allows you to find other endpoints
associated with the entity that was returned to the user.

When using the SPipes debug API, pay attention to the has_related_resources field, which describes all possible
endpoints related to the returned entity, their purpose, and sometimes possible parameters that can be used in the request.


# Introduction

The scenario will be based on the hello world example, directly from the SPipes project.
You can find all the information about the script [here](../../../doc/examples/hello-world/hello-world.md).
Please make sure that you have a basic understanding of how the hello world example works.

SPipes debug API interacts mostly with two main entities 
ModuleExecution and PipelineExecution. In main [README.md](../../../README.md) there is a description about what are
Pipelines and Modules.
So, basically ModuleExecution and PipelineExecution are entities representing data about executed Module or Pipeline.
It can contain such data as, where are stored output of modules,
the time when execution happened, and a lot of different useful information.

# Scenario 1: Run Hello World Example
### In this scenario you will run the "Hello, World!" script and check its correct execution using Swagger-UI and GraphDB

1) Run the hello world example script with the following URL:
   http://localhost:1234/s-pipes/service?_pId=execute-greeting&firstName=TestName&lastName=TestSurname
   You should be able to see the greeting message "Hello TestName TestSurname." as part of the output JSON-LD.

2) Check the following link to the GraphDB:
   http://localhost:1234/db-server/graphs
   The number of graphs should not be one. If it’s so, we can continue. Otherwise, you messed something up at step 1.

3) Using the [debug API](http://localhost:1234/s-pipes-debug/swagger-ui/index.html), try to look through all pipeline executions that were executed. 

   It can be found under **Get all pipeline executions**

4) If you see one execution, then the scenario is successfully passed.

## Expected results:

Greeting message in JSON-LD format: "Hello TestName TestSurname."

More than one graph in GraphDB

One executed pipeline through debug API

# Scenario 2: Modify and Compare Executions
### In this scenario you will modify the "Hello, World!" script and compare the two pipelines

1) Let’s change a bit our script: open [doc/examples/hello-world/hello-world.sms.ttl](../../../doc/examples/hello-world/hello-world.sms.ttl) and change

   BIND(concat("Hello ", ?personName, ".") as ?greetingMessage) 

   to
   
   BIND(concat("Hello ", ?personName, "!") as ?greetingMessage)

2) Run the script one more time in the same way as scenario 1 (point 1).

3) Using the debug API, try to look through all pipeline executions that were executed.

4) Now, from the response, you should get 2 pipeline executions.

5) We changed our script. Let’s see, what the difference is between the two pipeline executions.
   Try to compare pipelines and find in which module, the first difference between pipeline executions was found.

   Get Pipeline IDs from the **Get all pipeline executions** and use them to run **Compare pipeline executions**, then check **difference_found_in**

## Expected results:

Greeting message in JSON-LD format: "Hello TestName TestSurname!"

Two executed pipeline through debug API

Difference found in **construct-greeting** module

# Scenario 3: Analyze Executed Modules
### In this scenario you will analyse executed modules of your previously executed pipeline

1) We already have some pipeline executions, so we don’t need to run anything else.

2) Using the debug API, try to display all executed modules in any of your pipeline executions. Use ID of one of your completed pipelines.
   
   This can be done with **Get all module executions in pipeline execution**

3) Sort modules by duration and find which module was the slowest.

   Set **duration** as **orderBy** parameter. Another supported orders are **input-triples**, **output-triples** and **start-time**. **orderType** can be **ASC** (ascending) or **DESC** (descending)

4) Sort modules by a count of output triples and find which module produced the biggest amount of triples.

## Expected results:

**express-greeting_Return** is the slowest module

**express-greeting_Return** and **construct-greeting** produce the same amount of triplets - one triplet

# Scenario 4: Investigate Triples and Variables
### In this scenario you will analyse which module creates specific triplet and variable

1) When we executed the first execution, the response looked similar to this:

```
@id: "http://onto.fel.cvut.cz/ontologies/s-pipes/hello-world-example-0.1/testname-testsurname",
is-greeted-by-message: "Hello TestName TestSurname.",
@context: {
is-greeted-by-message: { @id:"http://onto.fel.cvut.cz/ontologies/s-pipes/hello-world-example-0.1/is-greeted-by-message"
```
Its RDF representation looks like this and follows the ?s ?p ?o structure:
```
<http://onto.fel.cvut.cz/ontologies/s-pipes/hello-world-example-0.1/testname-testsurname> 
<http://onto.fel.cvut.cz/ontologies/s-pipes/hello-world-example-0.1/is-greeted-by-message> "Hello TestName TestSurname."
```

2) Using the debug API, try to find which module produced this triple first. Use **Find module execution that created a triple**. That RDF representation can be used as a **graphPattern**.
   Be sure to choose the correct **executionId** matching your pattern, so it should be ID of the Pipeline that you have executed first. 
   If you have chosen the ID of the second Pipeline, you can change **.** for **!** in the Object and use that RDF as a pattern.
   
   You do not have to use the full pattern. It follows the structure ?s ?p ?o, so you can try queries like this:
```
?s ?p "Hello TestName TestSurname."
```
or 
```
<http://onto.fel.cvut.cz/ontologies/s-pipes/hello-world-example-0.1/testname-testsurname> ?p ?o
```
or any other arrangement. But be mindful about the order:
```
?s "Hello TestName TestSurname." ?p
```
will not match any triplet.

3) Using the debug API, find out which module first produced the variable **lastName**. Use **Find module execution where a variable was created**.

## Expected results:

**construct-greeting** module created that triplet

**bind-person-name** produced the variable **lastName**