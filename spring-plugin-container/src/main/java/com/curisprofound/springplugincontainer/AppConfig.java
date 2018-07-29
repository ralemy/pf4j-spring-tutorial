package com.curisprofound.springplugincontainer;

import org.pf4j.spring.SpringPluginManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class AppConfig {
    @Bean
    public SpringPluginManager pluginManager() {
        return new SpringPluginManager();
    }

    @Bean
    public RouterFunction<ServerResponse> route() {
        return RouterFunctions.route(GET("/hello")
                        .and(accept(MediaType.TEXT_PLAIN)),
                req -> ServerResponse.ok().body(Mono.just("Reactive endpoint on contaainer"), String.class));
    }

}
