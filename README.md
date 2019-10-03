# Extending applications with PF4J plugins

This repository provides a tutorial and an example on how to use PF4J to 
develop an application that supports extension through the development of third-party plugins.

# User Stories:

Initially, we focus on the minimum amount of code to create a java applicaiton with plugin support. 

|Story 01 |Should be able to add functionality via plugin|
|---------|-----------------------|
|As an | Application Developer|
|I need to| support plugins in my applications|
|So that| they can be extended by third-party collaborators|

Then, we extend the functionality to include container and plugins built with
Spring boot

|Story 02 |Should be able to use Spring boot in container and plugins|
|---------|-----------------------|
|As an | Spring boot Developer|
|I need to| support plugins written based on Spring|
|So that| I can use Spring functionality such as dependency injection in my plugins|

Next, we add a new Restful endpoint using an annotated class

|Story 03 |Should be able to add to Restful endpoints from plugin |
|---------|-----------------------|
|As an | Spring boot Developer|
|I need to| be able to add new endpoints via the plugin|
|So that| provide access to the extended functionality via RESTful interfaces|

Finally, we add a reactive Restful endpoint using a Router Function

|Story 04 |Should be able to add reactive endpoints from plugin |
|---------|-----------------------|
|As an | Spring boot Developer|
|I need to| be able to add reactive endpoints via the plugin|
|So that| provide the benefits of reactive programing with my plugins|


## Contents

[Overview](#overview) ....................... Explains the reasons behind creation of this repository

[Shared Interface](./shared-interfaces).............  Allows plugins to interact with the container and eachother

[Baremetal Plugin](./simple-test-plugin)............. Minimal java code that creates a functioning plugin

[Baremetal Container](./simple-plugin-container)....... Minimal java code that creates standalone plugin container

[Spring boot Plugin](./spring-test-plugin)............ A plugin that uses Spring boot for DI and adds endpoints created with annotated classes or router functions.

[Spring boot Container](./spring-plugin-container)...... A Spring boot app with plugin support and reactive Restful service

# Overview 

Plugins are pieces of software, developed by a third-party, that extend the functionality of a given application. There are many benefits to an application that supports plugins, such as flexibility to accommodate new requirements, extensibility to integrate in new environments, and suitability in being able to create tailored solutions by including only the functionality required for a specific use case.

There are multiple ways of adding plugin support to a Java application. The powerful, age-old and very complex OSGI network to the use of simple and limited-functionality mechanisms inherent in Java 8. One good mid-way compromise which is neither too simplistic nor too complicated is Pf4J, although by no means this is to claim that it is the only or even the best solution for the problem.

[PF4J][] is a plugin framework for Java, that allows third party developers to extend the functionality of an application by adding a zip file to the plugins folder that will be loaded at 
runtime. Extensions can also be added to the application jar file if needed. the [PF4J][] Github repository has an example of how to develop such an application, along with comprehensive documentation on the framework. While these should certainly be studied, in our experience understanding the examples was not easy for the average developers and we decided to contribure by creating this tutorial.

This project demonstrates the same concepts in a more practical way. Three separate maven projects are created, and a scenario is presented on how the solution will be created and deployed. 

## Actors

In the plugin management system, two main actors are at play. the **application owner**, who creates the application which accepts the plugin, and published the interfaces that the second actor, the **plugin developer**, implements to develop extended functionality. 

## Artifacts

The solution involves three components. the first component is in the form of a java interface, which is developed by the application owner and consists of a series of mehtods that plugin developer implements to extend the application. This component should be typically published in maven repository or some similar place and is added to the other two components as a dependency.

the second component is the container, which is also developed by the application owner. Once started, the plugin container scans the plugins folder and reads and unzips the plugins in that folder, loading the classes and starting and stopping the plugins. 


the third component is the plugin itself. it is developed by the plugin developer and implements some of the shared interfaces defined in the first component and together with its own dependencies resides as a zip file in the plugins directory where the container will be looking for it.


The following table summarizes the above concepts

|Artifact| Actor| Description|
|--------|------|------------|
|Container| Application Owner| the main application, scans a directory that contains plugins as zip files, decompresses and imports them into the application|
|Shared Interfaces|Application Owner| the collection of interfaces that plugins implement. published by the application owner and included as a dependency by both the plugin-container and the plugin|
|Plugin| Plugin Developer| the implementation of one or more interfaces defined in Shared-Plugin-Interfaces. deployed as a zip file of classes and their dependencies inside the plugins directory of the Plugin-Container|

[Back to Contents](#contents)

[PF4J]: https://github.com/pf4j/pf4j







