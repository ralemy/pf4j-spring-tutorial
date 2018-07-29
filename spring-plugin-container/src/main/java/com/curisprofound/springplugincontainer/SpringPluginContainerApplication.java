package com.curisprofound.springplugincontainer;

import com.curisprofound.plugins.PluginInterface;
import org.pf4j.spring.SpringPluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class SpringPluginContainerApplication {

    private static Logger log = LoggerFactory.getLogger(SpringPluginContainerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringPluginContainerApplication.class, args);
    }

    @Bean
    public ApplicationRunner run() {
        return new ApplicationRunner() {

            @Autowired
            private SpringPluginManager springPluginManager;

            @Override
            public void run(ApplicationArguments args) throws Exception {
                List<PluginInterface> plugins = springPluginManager.getExtensions(PluginInterface.class);
                log.info(String.format("Number of plugins found: %d", plugins.size()));
                plugins.forEach(c -> log.info(c.getClass().getName() + ":" + c.identify()));
            }
        };
    }
}
