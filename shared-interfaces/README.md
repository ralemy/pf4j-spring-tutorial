# The Shared Interfaces component

This component will only contain the interfaces that the plugins are supposed to implement. What is important to keep in mind is that it should try not to force dependencies on plugins. For example, if it is defining an interface that will
be implemented by Spring-based and non-Spring-based plugins, it should not use Spring specific classes and types as parameters or return values. 

It is noteworthy that more than one shared interface component can exist, and while the container will reference all of them, the plugins need only reference
the ones they are implementing.

For the purposes of our tutorial, we have a very small pom file.


```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.curis-profound</groupId>
    <artifactId>shared-plugin-interfaces</artifactId>
    <version>1.1.0</version>


    <dependencies>
        <!-- https://mvnrepository.com/artifact/org.pf4j/pf4j -->
        <dependency>
            <groupId>org.pf4j</groupId>
            <artifactId>pf4j</artifactId>
            <version>2.3.0</version>
        </dependency>
    </dependencies>

</project>
```

the ```org.pf4j``` dependency is the only one necessay, and will provide the superclass for interface to define:

```java
package com.curisprofound.plugins;

import org.pf4j.ExtensionPoint;
import java.util.List;

public interface PluginInterface extends ExtensionPoint {
    String identify();
    List<?> reactiveRoutes();
    List<Object> mvcControllers();
}
``` 

Three methods are defined. the first one is used by plugins to introduce themselves to the application with a human-readable phrase. The second one is for Spring-based plugins to add new reactive endpoints using ```RouterFunction``` objects, and the third endpoint is again for Spring-based plugins to add Restful endpoints using annotated classes.


and we use a simple command to generate the jar file:

```bash
mvn clean package
```

The jar is then published to a repository and can be included in dependent projects in a standard way

```xml
<dependency>
        <dependency>
            <groupId>com.curisprofound</groupId>
            <artifactId>shared-plugin-interfaces</artifactId>
            <version>1.1.0</version>
        </dependency>
</dependency>
```

If you have the jar locally, you can add it to the local maven repo so the other projects could include it as if it was received from upstream. 

```bash
mvn install:install-file \
 -Dfile=target/shared-plugin-interfaces-1.1.0.jar \
 -DgroupId=com.curisprofound \
 -DartifactId=shared-plugin-interfaces \
 -Dversion=1.1.0 \
 -Dpackaging=jar
```

[Back to Contents](../#contents)

[PF4J]: https://github.com/pf4j/pf4j

