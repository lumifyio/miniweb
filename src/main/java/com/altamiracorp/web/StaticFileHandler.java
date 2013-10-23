package com.altamiracorp.web;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

public class StaticFileHandler implements Handler {
    private final RequestDispatcher handler;

    public StaticFileHandler(ServletConfig config) {
        handler = config.getServletContext().getNamedDispatcher("default");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        HttpServletRequest wrapped = new HttpServletRequestWrapper(request) {
            public String getServletPath() { return ""; }
        };
        handler.forward(wrapped, response);
    }
}
