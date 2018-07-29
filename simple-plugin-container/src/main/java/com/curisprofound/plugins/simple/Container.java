package com.curisprofound.plugins.simple;

import com.curisprofound.plugins.PluginInterface;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.List;

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
