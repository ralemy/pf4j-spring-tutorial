# Spring-enabled plugins

Developing plugins as Spring boot applications has a number of advantages, including the utility of 
Spring functionality, Dependency injection and auto-wiring, reactive libraries, etc. 

Plugins should also be able to define new endpoints in the container, whether the old-style annotated 
controllers or the new ```RouterFunction``` style for reactive endpoints.  

## Pom file

The pom file needs to include the shared interface and the [pf4j-spring](https://github.com/pf4j/pf4j-spring) package as dependencies. 
The rest of the dependencies would be the same that the Spring boot application would have required
in standalone mode. 

The build section needs to be customized to create the correct plugin structure. 

* The ant-run plugin opens the jar and puts the classes in a separate directory

```xml
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-antrun-plugin</artifactId>
                <version>1.6</version>
                <executions>
                    <execution>
                        <id>unzip jar file</id>
                        <phase>package</phase>
                        <configuration>
                            <target>
                                <unzip src="target/${project.artifactId}-${project.version}.${project.packaging}"
                                       dest="target/plugin-classes"/>
                            </target>
                        </configuration>
                        <goals>
                            <goal>run</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
```

* The jar plugin sets the parameters for Manifest file

```xml
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

* The maven-assemply-plugin uses the ```src/main/resources/assembly.xml``` to create the zip file

The ```assembly.xml``` file needs to exclude any packages used by the container,
and put the ```plugin.properties``` file in the root directory.

```xml
<assembly>
    <id>plugin</id>
    <formats>
        <format>zip</format>
    </formats>
    <includeBaseDirectory>false</includeBaseDirectory>
    <dependencySets>
        <dependencySet>
            <useProjectArtifact>false</useProjectArtifact>
            <scope>runtime</scope>
            <outputDirectory>lib</outputDirectory>
            <includes>
                <include>org.pf4j:*</include>
            </includes>
            <excludes>
                <exclude>org.springframework:*</exclude>
                <exclude>org.slf4j:*</exclude>
                <exclude>com.curisprofound:shared-plugin-interfaces</exclude>
            </excludes>
        </dependencySet>
    </dependencySets>
    <fileSets>
        <fileSet>
            <directory>target/plugin-classes</directory>
            <outputDirectory>classes</outputDirectory>
        </fileSet>
        <fileSet>
            <directory>src/main/resources</directory>
            <outputDirectory/>
            <includes>
                <include>plugin.properties</include>
            </includes>
        </fileSet>
    </fileSets>
</assembly>
```

# Configuration 

Using the ```@Configuration``` annotation, a class would be marked as configuration and can provide beans for later auto-wiring

```java
//in GreetProvider.java

public class GreetProvider {
    public String provide(){
        return "A plugin using Spring framework";
    }
}

//in ApplicationConfiguration.java
@Configuration
public class ApplicationConfiguration {
    @Bean
    public GreetProvider greetProvider(){
        return new GreetProvider();
    }
}
```

Restful endpoints can be created in annotated classes

```java
@Controller
@RequestMapping("/plugin-mvc-controller")
public class PluginController {
    @GetMapping
    public ResponseEntity<String> greetMVC(){
        String message = "An endpoint defined by annotation in plugin";
        return ResponseEntity.ok().body(message);
    }
}
```

The plugin is implemented as an extension to the ```SpringPlugin```, and has 
methods to start, stop, and most importantly, create application context which 
will be used to register the annotated configuration class:

```java
public class SpringSamplePlugin extends SpringPlugin {

    private static final Logger log = LoggerFactory.getLogger(SpringSamplePlugin.class);

    public SpringSamplePlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        log.info("Spring Sample plugin.start()");
    }

    @Override
    public void stop() {
        log.info("Spring Sample plugin.stop()");
        super.stop(); // to close applicationContext
    }

    @Override
    protected ApplicationContext createApplicationContext() {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.setClassLoader(getWrapper().getPluginClassLoader());
        applicationContext.register(ApplicationConfiguration.class);
        applicationContext.refresh();
        return applicationContext;
    }
...
...
}
```

The interfaces to implement are included inside the plugin as static classes implementing the interface and annotated with ```@Extension```:

```java

    @Extension(ordinal = 1)
    public static class SpringPlugin implements PluginInterface {

        @Autowired
        private GreetProvider greetProvider;



        @Override
        public String identify() {
            return greetProvider.provide();
        }

        @Override
        public List<Object> mvcControllers() {
            return new ArrayList<Object>() {{
                add(new PluginController());
            }};
        }

        @Override
        public List<RouterFunction<?>> reactiveRoutes() {
            return new ArrayList<RouterFunction<?>>() {{
                add(route(GET("/plugin-end-point"),
                        req -> ServerResponse
                               .ok()
                               .body(Mono.just("reactive router endpoint"),
                                     String.class)));
            }};
        }
    }
```

Here the ```mvcControllers()``` returns a List of annotated classes to be used as restcontrollers and the ```reactiveRoutes()``` returns a list of ```RouterFunction``` objects to create the reactive endpoints. needless to say, 
a plugin can return an empty list if it doesn't have any endpoints to add.

[Back to Contents](../#contents)
