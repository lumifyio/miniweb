package com.altamiracorp.web;

import com.altamiracorp.web.Route.Method;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Router {
    private ServletConfig servletConfig;
    private Map<Method, List<Route>> routes = new HashMap<Method, List<Route>>();

    public Router(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
        routes.put(Method.GET, new ArrayList<Route>());
        routes.put(Method.POST, new ArrayList<Route>());
        routes.put(Method.PUT, new ArrayList<Route>());
        routes.put(Method.DELETE, new ArrayList<Route>());
    }

    public void addRoute(Method method, String path, Handler... handlers) {
        List<Route> methodRoutes = routes.get(method);
        Route route = new Route(method, path, handlers);
        methodRoutes.add(route);
    }

    public void route(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Method method = Method.valueOf(request.getMethod().toUpperCase());

        if (method == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Route route = findRoute(method, request);

        if (route == null) {
            RequestDispatcher rd = servletConfig.getServletContext().getNamedDispatcher("default");
            HttpServletRequest wrapped = new HttpServletRequestWrapper(request) {
                public String getServletPath() { return ""; }
            };
            rd.forward(wrapped, response);
        } else {
            Handler[] handlers = route.getHandlers();
            HandlerChain chain = new HandlerChain(handlers);
            chain.next(request, response);
        }
    }

    private Route findRoute(Method method, HttpServletRequest request) {
        List<Route> potentialRoutes = routes.get(method);
        for (Route route : potentialRoutes) {
            if (route.isMatch(request)) {
                return route;
            }
        }
        return null;
    }
}
