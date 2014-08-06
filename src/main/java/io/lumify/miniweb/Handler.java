package io.lumify.miniweb;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Handler {
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception;
}
