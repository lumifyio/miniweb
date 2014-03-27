package com.altamiracorp.miniweb;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

public class StaticFileHandler implements Handler {
    private final RequestDispatcher handler;
    private final String pathInfo;

    public StaticFileHandler(ServletConfig config) {
        this(config, null);
    }

    public StaticFileHandler(ServletConfig config, String pathInfo) {
        this.handler = config.getServletContext().getNamedDispatcher("default");
        this.pathInfo = pathInfo;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        HttpServletRequest wrapped = new HttpServletRequestWrapper(request) {
            @Override
            public String getServletPath() {
                return "";
            }

            @Override
            public String getPathInfo() {
                if (pathInfo == null) {
                    return super.getPathInfo();
                } else {
                    return pathInfo;
                }
            }
        };
        handler.forward(wrapped, response);
    }
}
