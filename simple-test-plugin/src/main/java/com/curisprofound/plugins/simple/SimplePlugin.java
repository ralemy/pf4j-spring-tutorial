package com.curisprofound.plugins.simple;

import com.curisprofound.plugins.PluginInterface;
import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SimplePlugin extends Plugin {

    private static Logger log = LoggerFactory.getLogger(com.curisprofound.plugins.simple.SimplePlugin.class);

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
