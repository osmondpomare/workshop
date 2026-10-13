package org.bintec.bco.wks901.sef.config;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.bintec.bco.wks901.sef.service.ReversalService;

public class ConfigServices extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(ReversalService.class);
        return classes;
    }
}
