# SPipes Debug Module

SPipes debug module provides web API tools for retrieving different information about executed pipelines and modules. 

## Development Environment Setup

The following software needs to be installed on the system for development:

- JDK 17
- Maven
- RDF4J server

## Model description

This module interacts mostly with two main entities ModuleExecution and PipelineExecution. In [main README.md](../README.md)
there is a description about what are Pipelines and Modules. So basically ModuleExecution and PipelineExecution are entities 
representing data about executed Module or Pipeline. It can contain such data as, where are stored output of modules, time, when execution
happened and a lot of different useful information.

## Hands-on tutorial

With SPipes debug module you can retrieve information about executed pipelines and modules through REST API.
You can go over hands-on tutorial to see how to use it [here](./doc/hands-on-tutorial/hands-on-tutorial.md).

## Health Check
To check that the `s-pipes-debug` is running, use path `/actuator/health` (e.g. `http://localhost:8080/s-pipes-debug/actuator/health`).
