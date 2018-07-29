## Simple Plugin

A plugin is packaged as a zip or jar file and is put in a directory for the container to scan, decompress and use. the POM file for the plugin shall contain dependenceis required to compile it as well as the information that needs to go into the manifest. 

Minimum dependencies include the PF4J and the shared interfaces.

```xml
    <dependencies>
        <dependency>
            <groupId>org.pf4j</groupId>
            <artifactId>pf4j</artifactId>
            <version>2.3.0</version>
        </dependency>
        <dependency>
            <groupId>com.curisprofound</groupId>
            <artifactId>shared-plugin-interfaces</artifactId>
            <version>1.1.0</version>
        </dependency>
    </dependencies>
```

We also need to configure maven-jar-plugin to add plugin information to manifest:


```xml
....
   <properties>
        <plugin.id>simple-hello-plugin</plugin.id>
        <plugin.class>com.curisprofound.plugins.simple.HelloPlugin</plugin.class>
        <plugin.version>0.0.1</plugin.version>
        <plugin.provider>Curis Profound</plugin.provider>
        <plugin.dependencies/>
    </properties>

....
....

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>2.4</version>
                <configuration>
                    <archive>
                        <manifestEntries>
                            <Plugin-Id>${plugin.id}</Plugin-Id>
                            <Plugin-Class>${plugin.class}</Plugin-Class>
                            <Plugin-Version>${plugin.version}</Plugin-Version>
                            <Plugin-Provider>${plugin.provider}</Plugin-Provider>
                            <Plugin-Dependencies>${plugin.dependencies}</Plugin-Dependencies>
                        </manifestEntries>
                    </archive>
                </configuration>
            </plugin>

```

To create the zip file, maven-assembly-plugin is used. this plugin is configured
to read ```src/main/resources/assembly.xml``` file for instructions on how
to create the zip file. 

The most important point on creating the assembly file is to ensure that it does not include dependencies that are shared between the plugin and the container or other plugins. for example, the ```slf4j``` or the shared interfaces should not be included in the plugin zip file as they will be included in the container dependencies.

* we exclude the dependencies that are already declared in the container

```xml
 <dependencySets>
        <dependencySet>
            <useProjectArtifact>false</useProjectArtifact>
            <scope>runtime</scope>
            <outputDirectory>lib</outputDirectory>
            <includes>
                <include>*:jar:*</include>
            </includes>
            <excludes>
                <exclude>org.slf4j:*</exclude>
                <exclude>com.curisprofound:shared-plugin-interface</exclude>
            </excludes>
        </dependencySet>
    </dependencySets>
```

* we include the classes, not the jar file, because the unzipped directory is not in the classpath

```xml
        <fileSet>
            <directory>target/classes</directory>
            <outputDirectory>classes</outputDirectory>
        </fileSet>
```

* we include a plugin.properties file in the root of the zip file

```xml
        <fileSet>
            <directory>src/main/resources</directory>
            <outputDirectory/>
            <includes>
                <include>plugin.properties</include>
            </includes>
        </fileSet>
```

this just includes the properties we added to the manifest above

```
plugin.id=simple-hello-plugin
plugin.class=com.curisprofound.plugins.simple.HelloPlugin
plugin.version=0.0.1
plugin.provider=Curis Profound
plugin.dependencies=
```

## The plugin code

the actual plugin now can implement extension points and override start and stop hooks:

```java
package com.curisprofound.plugins.simple;

public class SimplePlugin extends Plugin {

    private static Logger log = LoggerFactory.getLogger(SimplePlugin.class);

    public SimplePlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start(){
        log.info("Simple Plugin Started");
    }

    @Override
    public void stop(){
        log.info("Simple Plugin Stopped");
    }

    @Extension
    public static class SimpleIdentityPlugin implements PluginInterface {

        public String identify() {
            return "A simple plugin with no dependency on Spring";
        }

        @Override
        public List<?> reactiveRoutes() {
            return new ArrayList<>();
        }

        @Override
        public List<Object> mvcControllers() {
            return new ArrayList<>();
        }
    }
}
```
Notice that the plugin returns empty lists (not null) for methods that it won't implement. this is a good idea to return the empty version of the object instead of null and avoid ```NullPointerException``` messsages. 


building the project with ```mvn clean package``` will create a zip file in the 
```target``` directory. in the future steps we will move this zip file into the plugins directory and run it through the container.

[Back to Contents](../#contents)

[PF4J]: https://github.com/pf4j/pf4j

