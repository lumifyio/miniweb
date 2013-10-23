package com.altamiracorp.web;

import com.altamiracorp.web.utils.UrlUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Route {
    public static enum Method {GET, POST, PUT, DELETE}

    ;

    private Method method;
    private String path;
    private Handler[] handlers;

    private Pattern componentPattern = Pattern.compile("\\{([_a-zA-Z]+)\\}");
    private String[] routePathComponents;

    public Route(Method method, String path, Handler... handlers) {
        this.method = method;
        this.path = path;
        this.handlers = handlers;
        routePathComponents = splitPathComponents(path);
    }

    public boolean isMatch(HttpServletRequest request) {
        Method requestMethod = Method.valueOf(request.getMethod().toUpperCase());
        if (!requestMethod.equals(method)) {
            return false;
        }

        String[] requestPathComponents = splitPathComponents(request.getRequestURI());
        if (requestPathComponents.length != routePathComponents.length) {
            return false;
        }

        for (int i = 0; i < routePathComponents.length; i++) {
            String routeComponent = routePathComponents[i];
            String requestComponent = UrlUtils.urlDecode(requestPathComponents[i]);

            Matcher matcher = componentPattern.matcher(routeComponent);
            if (matcher.matches()) {
                request.setAttribute(matcher.group(1), requestComponent);
            } else if (!routeComponent.equals(requestComponent)) {
                return false;
            }
        }

        return true;
    }

    public Handler[] getHandlers() {
        return handlers;
    }

    public Method getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    private String[] splitPathComponents(String path) {
        String[] components = path.split("/");
        if (components.length > 0){
            String[] lastComponents = components[components.length - 1].split("\\.");
            if (lastComponents.length > 1) {
                String[] allComponents = new String[components.length - 1 + lastComponents.length];
                for (int i = 0; i < components.length - 1; i++) {
                    allComponents[i] = components[i];
                }
                for (int i = 0; i < lastComponents.length; i++) {
                    allComponents[components.length + i - 1] = lastComponents[i];
                }
                return allComponents;
            } else {
                return components;
            }
        }
        return new String[0];
    }
}
