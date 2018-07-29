# Spring boot app as plugin container

Using [PF4J-Spring]() package, it is rather straight-forward to add plugin support to an existing application. once more, the pom file needs to declare
dependencies on ```pf4j-spring``` and the shared interfaces

```xml
        <dependency>
            <groupId>org.pf4j</groupId>
            <artifactId>pf4j-spring</artifactId>
            <version>0.4.0</version>
        </dependency>

        <dependency>
            <groupId>com.curisprofound</groupId>
            <artifactId>shared-plugin-interfaces</artifactId>
            <version>1.1.0</version>
        </dependency>
```

the build section does not need to change, except if you decide to have an external directory added to the classpath, which comes in handy when resolving dependencies

```xml
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
				<configuration>
					<folders>
						<folder>./lib/</folder>
					</folders>
				</configuration>
			</plugin>
```

The application needs to add a Singleton for ```PluginManager``` to the dependency injection platform. this can be done alongside other configuration

```java
@Configuration
public class AppConfig {

    @Bean
    public SpringPluginManager pluginManager() {
        return new SpringPluginManager();
    }

    @Bean
    public RouterFunction<ServerResponse> route() {
        String message = "Reactive endpoint on contaainer";
        return RouterFunctions.route(
                GET("/hello")
                        .and(accept(MediaType.TEXT_PLAIN)),
                req -> ServerResponse
                       .ok()
                       .body(Mono.just(message), String.class));
    }
}
```

The above config also adds a ```/hello``` endpoint to show a use case of the container having its own endpoints.

# Configuring the Plugin

The plugin configuration is done through a class extending ```BeanFactoryAware``` and annotated with ```@Configuration```.

```java
@Configuration
public class PluginConfig implements BeanFactoryAware {


    private final SpringPluginManager pluginManager;
    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;
    private BeanFactory beanFactory;

    @Autowired
    public PluginConfig(SpringPluginManager pm, ApplicationContext applicationContext) {
        this.pluginManager = pm;
        this.applicationContext = applicationContext;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Bean
    @DependsOn("pluginManager")
    public RouterFunction<?> pluginEndpoints(PluginManager pm) {
        registerMvcEndpoints(pm);
        return getReactiveRoutes(pm);
    }
    ...
    ...
    @PreDestroy
    public void cleanup() {
        pluginManager.stopPlugins();
    }
```

The PluginManager and the ApplicationContext are auto-wired to this class, and a BeanFactory is obtained by the ```setBeanFactory``` method getting called at the initialization of the class. It is important to have a ```@PreDestroy``` annotated method to stop the plugins on plugin manager. 

This class returns a Bean that registers the MVC and reactive endpoints the plugin provides. registering the MVC endpoints is done in two steps, first we add each endpoint as a singleton using the bean factory, then we call the ```afterPropertiesSet()``` method on ```RequestMappingHandlerMapping``` classes to map them to the correct endpoints:

```java
    private void registerMvcEndpoints(PluginManager pm) {
        pm.getExtensions(PluginInterface.class).stream()
                .flatMap(g -> g.mvcControllers().stream())
                .forEach(r -> ((ConfigurableBeanFactory) beanFactory)
                        .registerSingleton(r.getClass().getName(), r));
        applicationContext
                .getBeansOfType(RequestMappingHandlerMapping.class)
                .forEach((k, v) -> v.afterPropertiesSet());
    }
```

We use another approach for the reactive endpoints. here we provide a base, which allows the user to see which plugins are in the system:

```java
    private RouterFunction<?> baseRoot(PluginManager pm) {
        return route(GET("/plugins"),
                req -> ServerResponse.ok().body(Mono.just(pluginNamesMono(pm)), String.class));
    }

    private String pluginNamesMono(PluginManager pm) {
        try {
            List<String> identityList = pm
                .getExtensions(PluginInterface.class).stream()
                .map(g-> g.getClass().getName() + ": " + g.identify())
                .collect(Collectors.toList());
            return objectMapper.writeValueAsString(identityList);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

```

to this base we add all if any router functions returned by plugins:

```java
    private RouterFunction<?> getReactiveRoutes(PluginManager pm) {
        RouterFunction<?> base = baseRoot(pm);
        RouterFunction<?> routes = pm
                .getExtensions(PluginInterface.class).stream()
                .flatMap(g -> g.reactiveRoutes().stream())
                .map(r-> (RouterFunction<ServerResponse>)r)
                .reduce((o,r )-> (RouterFunction<ServerResponse>) o.andOther(r))
                .orElse(null);
        return routes == null ? base : base.andOther(routes);
    }
```

Once the application starts, it looks under the current directory for a directory called plugins to scan for the plugins. Consult the [PF4J Documentation][] on how to specify a location using environment variables. 

Once the zip files of the simple and spring plugin samples are in the plugins directory, here is the output the application confirms the correct discovery and installation of plugins and endpoints:

```bash
$ java -jar target/spring-plugin-container-0.0.1-SNAPSHOT.jar
....
....
 Enabled plugins: []
 Disabled plugins: []
 PF4J version 2.0.0 in 'deployment' mode
 Plugin 'spring-sample-plugin@0.0.1' resolved
 Plugin 'simple-identity-plugin@0.0.1' resolved
 Start plugin 'spring-sample-plugin@0.0.1'
 Spring Sample plugin.start()
 Start plugin 'simple-identity-plugin@0.0.1'
 Simple Plugin Started
 Mapped "{[/plugin-mvc-controller],methods=[GET]}" onto public org.springframework.http.ResponseEntity<java.lang.String> com.curisprofound.springtestplugin.PluginController.greetMVC()
 Mapped (GET && /plugins) -> com.curisprofound.springplugincontainer.PluginConfig$$Lambda$238/90205195@1c72da34
   (GET && /plugin-end-point) -> com.curisprofound.springtestplugin.SpringSamplePlugin$SpringPlugin$2$$Lambda$243/280265505@6b0c2d26
 Mapped ((GET && /hello) && Accept: [text/plain]) -> com.curisprofound.springplugincontainer.AppConfig$$Lambda$244/1161667116@6e38921c
 Mapped URL path [/webjars/**] onto handler of type [class org.springframework.web.reactive.resource.ResourceWebHandler]
 Mapped URL path [/**] onto handler of type [class org.springframework.web.reactive.resource.ResourceWebHandler]
 Registering beans for JMX exposure on startup
 Started HttpServer on /0:0:0:0:0:0:0:0:8080
 Netty started on port(s): 8080
 Started SpringPluginContainerApplication in 2.71 seconds (JVM running for 3.192)
 Number of plugins found: 2
com.curisprofound.plugins.simple.SimplePlugin$SimpleIdentityPlugin:A simple plugin with no dependency on Spring
com.curisprofound.springtestplugin.SpringSamplePlugin$SpringPlugin:A plugin using Spring framework
```

the /plugins will return

```json
[
"com.curisprofound.plugins.simple.SimplePlugin$SimpleIdentityPlugin: A simple plugin with no dependency on Spring",
"com.curisprofound.springtestplugin.SpringSamplePlugin$SpringPlugin: A plugin using Spring framework"
]
```

here is the return value for the rest of the endpoints

|Endpoint| Returns|
|--------|--------|
|/hello| Reactive endpoint on contaainer|
|/plugin-mvc-controller| An endpoint defined by annotation in plugin|
|/plugin-end-point| reactive router endpoint|


[Back to Contents](../#contents)

[PF4J Documentation]:https://pf4j.org/
