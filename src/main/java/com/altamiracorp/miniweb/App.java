package com.altamiracorp.miniweb;

import com.altamiracorp.miniweb.Route.Method;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class App {
    private Router router;
    private Map<String, Object> config;

    public App(final ServletConfig servletConfig) {
        router = new Router(servletConfig);
        config = new HashMap<String, Object>();
    }

    public void get(String path, Handler... handlers) {
        router.addRoute(Method.GET, path, handlers);
    }

    public void get(String path, Class<? extends Handler>... classes) {
        try {
            Handler[] handlers = instantiateHandlers(classes);
            get(path, handlers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void post(String path, Handler... handlers) {
        router.addRoute(Method.POST, path, handlers);
    }

    public void post(String path, Class<? extends Handler>... classes) {
        try {
            Handler[] handlers = instantiateHandlers(classes);
            post(path, handlers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void put(String path, Handler... handlers) {
        router.addRoute(Method.PUT, path, handlers);
    }

    public void put(String path, Class<? extends Handler>... classes) {
        try {
            Handler[] handlers = instantiateHandlers(classes);
            put(path, handlers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(String path, Handler... handlers) {
        router.addRoute(Method.DELETE, path, handlers);
    }

    public void delete(String path, Class<? extends Handler>... classes) {
        try {
            Handler[] handlers = instantiateHandlers(classes);
            delete(path, handlers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object get(String name) {
        return config.get(name);
    }

    public void set(String name, Object value) {
        config.put(name, value);
    }

    public void enable(String name) {
        config.put(name, true);
    }

    public void disable(String name) {
        config.put(name, false);
    }

    public boolean isEnabled(String name) {
        Object value = config.get(name);
        if (value != null && value instanceof Boolean) {
            return (Boolean) value;
        }
        return false;
    }

    public boolean isDisabled(String name) {
        return !isEnabled(name);
    }

    public void handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        router.route(request, response);
    }

    protected Handler[] instantiateHandlers(Class<? extends Handler>[] handlerClasses) throws Exception {
        Handler[] handlers = new Handler[handlerClasses.length];
        for (int i = 0; i < handlerClasses.length; i++) {
            handlers[i] = handlerClasses[i].newInstance();
        }
        return handlers;
    }
}
