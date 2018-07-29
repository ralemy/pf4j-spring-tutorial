# The plugin-container application

For the application to support the plugins, we need the pom file to depend at least on PF4J and the shared plugin interfaces. if you are planning to use the slf4j logger, include the slf4j-simple package 
as well. 


```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.curisprofound</groupId>
    <artifactId>simple-plugin-container</artifactId>
    <version>1.0-SNAPSHOT</version>

    <dependencies>
        <!-- https://mvnrepository.com/artifact/org.pf4j/pf4j -->
        <dependency>
            <groupId>org.pf4j</groupId>
            <artifactId>pf4j</artifactId>
            <version>2.3.0</version>
        </dependency>

        <!-- https://mvnrepository.com/artifact/org.slf4j/slf4j-simple -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>1.7.5</version>
        </dependency>

        <dependency>
            <groupId>com.curisprofound</groupId>
            <artifactId>shared-plugin-interfaces</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
<!-- build section goes here -->
</project>
```
in the build section, the following plugins are included: 

* To compile the classes (using class level 8 to be able to use lambda and other extensions):

```xml
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>8</source>
                    <target>8</target>
                </configuration>
            </plugin>
```
* to create an executable jar which looks in lib/ for its dependencies

```xml
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>2.3.1</version>
                <configuration>
                    <archive>
                        <manifest>
                            <addClasspath>true</addClasspath>
                            <classpathPrefix>lib/</classpathPrefix>
                            <mainClass>com.curisprofound.plugins.simple.Container</mainClass>
                        </manifest>
                    </archive>
                </configuration>
            </plugin>

```

* to create a zip file that would layout the application correctly, we use the maven assembly plugin:

```xml
            <plugin>
                <artifactId>maven-assembly-plugin</artifactId>
                <version>2.3</version>
                <configuration>
                    <descriptors>
                        <descriptor>
                            src/main/resources/assembly.xml
                        </descriptor>
                    </descriptors>
                    <appendAssemblyId>false</appendAssemblyId>
                </configuration>
                <executions>
                    <execution>
                        <id>make-assembly</id>
                        <phase>package</phase>
                        <goals>
                            <goal>attached</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
```

The assembly plugin will look in ```src/main/resources/assembly.xml``` to see how to create the zip file for the main application.

here, we create a lib/ subdirectory and put all the dependencies in it:

```xml
    <dependencySets>
        <dependencySet>
            <useProjectArtifact>false</useProjectArtifact>
            <outputDirectory>lib</outputDirectory>
            <includes>
                <include>*:jar:*</include>
            </includes>
        </dependencySet>
    </dependencySets>
```
we put the executable jar in the root of the unzipped directory and create an empty plugins directory where third party plugins will reside

```xml
    <fileSets>
        <fileSet>
            <directory>${project.build.directory}</directory>
            <outputDirectory/>
            <includes>
                <include>*.jar</include>
            </includes>
            <excludes>
                <exclude>*-javadoc.jar</exclude>
                <exclude>*-sources.jar</exclude>
            </excludes>
        </fileSet>
        <fileSet>
            <directory>.</directory>
            <outputDirectory>plugins</outputDirectory>
            <excludes>
                <exclude>*/**</exclude>
            </excludes>
        </fileSet>
    </fileSets>
```

So, our plugin container code requires to create a new PluginManager object,
tell it to load and start plugins and then use it to access the plugins. here
is the minimal code for that:

```java
package com.curisprofound.plugins.simple;


public class Container {

    private static final Logger log = LoggerFactory.getLogger(Container.class);

    public static void main(String[] args){

        final PluginManager pm = new DefaultPluginManager();
        pm.loadPlugins();
        pm.startPlugins();

        final List<PluginInterface> plugins = pm.getExtensions(PluginInterface.class);

        log.info(MessageFormat.format(" {0} Plugin(s) found ", String.valueOf(plugins.size())));

        plugins.forEach(g ->
                log.info(MessageFormat.format(" {0}: {1}",
                        g.getClass().getCanonicalName(),
                        g.identify())));

        pm.stopPlugins();

    }
}

```

build the project with
```bash
mvn clean package
```

this will create, among other things, a zip file in the target directory. 

```bash
unzip target/simple-plugin-container-1.0-SNAPSHOT.zip
cd target/simple-plugin-container-1.0-SNAPSHOT
java -jar simple-plugin-container-1.0-SNAPSHOT.jar
```
The output is as expected for an empty plugins directory

```bash
[main] INFO org.pf4j.DefaultPluginStatusProvider - Enabled plugins: []
[main] INFO org.pf4j.DefaultPluginStatusProvider - Disabled plugins: []
[main] INFO org.pf4j.DefaultPluginManager - PF4J version 2.3.0 in 'deployment' mode
[main] INFO org.pf4j.AbstractPluginManager - No plugins
[main] INFO com.curisprofound.plugins.simple.Container -  0 Plugin(s) found 
```

Once a plugin is created, copy it to the plugin directory. for example, the ```simple-test-plugin``` project will create a ```target/test-plugin-simple-1.0-SNAPSHOT.zip``` when built with ```mvn clean package```. Copy this file to the plugins directory and run the container again to see the plugin discovered and working

```bash
$ cd target/simple-plugin-container-1.0-SNAPSHOT
$ cp ../../../simple-test-plugin/target/test-plugin-simple-1.0-SNAPSHOT.zip ./plugins/
$ java -jar simple-plugin-container-1.0-SNAPSHOT.jar 
[main] INFO org.pf4j.DefaultPluginStatusProvider - Enabled plugins: []
[main] INFO org.pf4j.DefaultPluginStatusProvider - Disabled plugins: []
[main] INFO org.pf4j.DefaultPluginManager - PF4J version 2.3.0 in 'deployment' mode
[main] INFO org.pf4j.util.FileUtils - Expanded plugin zip 'test-plugin-simple-1.0-SNAPSHOT.zip' in 'test-plugin-simple-1.0-SNAPSHOT'
[main] INFO org.pf4j.AbstractPluginManager - Plugin 'simple-identity-plugin@0.0.1' resolved
[main] INFO org.pf4j.AbstractPluginManager - Start plugin 'simple-identity-plugin@0.0.1'
[main] INFO com.curisprofound.plugins.simple.SimplePlugin - Simple Plugin Started
[main] INFO com.curisprofound.plugins.simple.Container -  1 Plugin(s) found 
[main] INFO com.curisprofound.plugins.simple.Container -  com.curisprofound.plugins.simple.SimplePlugin.SimpleIdentityPlugin: A simple plugin with no dependency on Spring
[main] INFO org.pf4j.AbstractPluginManager - Stop plugin 'simple-identity-plugin@0.0.1'
[main] INFO com.curisprofound.plugins.simple.SimplePlugin - Simple Plugin Stopped

```

[Back to Contents](../#contents)


[PF4J]: https://github.com/pf4j/pf4j

