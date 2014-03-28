package com.altamiracorp.miniweb;

import com.altamiracorp.miniweb.Route.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(Router.class);

    private ServletConfig servletConfig;
    private Map<Method, List<Route>> routes = new HashMap<Method, List<Route>>();
    Map<Class<? extends Exception>, Handler[]> exceptionHandlers = new HashMap<Class<? extends Exception>, Handler[]>();

    public Router(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
        routes.put(Method.GET, new ArrayList<Route>());
        routes.put(Method.POST, new ArrayList<Route>());
        routes.put(Method.PUT, new ArrayList<Route>());
        routes.put(Method.DELETE, new ArrayList<Route>());
    }

    public Route addRoute(Method method, String path, Handler... handlers) {
        List<Route> methodRoutes = routes.get(method);
        Route route = new Route(method, path, handlers);
        methodRoutes.add(route);
        return route;
    }

    public void addExceptionHandler(Class<? extends Exception> exceptionClass, Handler[] handlers) {
        exceptionHandlers.put(exceptionClass, handlers);
    }

    public void route(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            routeWithExceptionHandling(request, response);
        } catch (Exception ex) {
            Handler[] handlers = exceptionHandlers.get(ex.getClass());
            if (handlers != null && handlers.length > 0) {
                LOGGER.error("Caught exception in route", ex);
                dispatch(handlers, request, response);
            } else {
                throw ex;
            }
        }
    }

    private void routeWithExceptionHandling(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Method method = Method.valueOf(request.getMethod().toUpperCase());

        if (method == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String relativeUri = requestURI.substring(contextPath.length());
        if (relativeUri.length() == 0) {
            response.sendRedirect(contextPath + '/');
            return;
        }

        Route route = findRoute(method, request, relativeUri);

        if (route == null) {
            RequestDispatcher rd = servletConfig.getServletContext().getNamedDispatcher("default");
            HttpServletRequest wrapped = new HttpServletRequestWrapper(request) {
                public String getServletPath() {
                    return "";
                }
            };
            rd.forward(wrapped, response);
        } else {
            Handler[] handlers = route.getHandlers();
            dispatch(handlers, request, response);
        }
    }

    private void dispatch(Handler[] handlers, HttpServletRequest request, HttpServletResponse response) throws Exception {
        HandlerChain chain = new HandlerChain(handlers);
        chain.next(request, response);
    }

    private Route findRoute(Method method, HttpServletRequest request, String relativeUri) {
        List<Route> potentialRoutes = routes.get(method);
        for (Route route : potentialRoutes) {
            if (route.isMatch(request, relativeUri)) {
                return route;
            }
        }
        return null;
    }
}
