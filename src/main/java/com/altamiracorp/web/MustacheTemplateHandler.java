package com.altamiracorp.web;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringReader;

public abstract class MustacheTemplateHandler implements Handler {
    private final Mustache template;

    public MustacheTemplateHandler() throws IOException {
        template = getTemplate();
    }

    private Mustache getTemplate() throws IOException {
        MustacheFactory mf = new DefaultMustacheFactory();
        return mf.compile(new StringReader(getTemplateText()), "template");
    }

    protected abstract String getTemplateText() throws IOException;

    protected abstract Object getModel(HttpServletRequest request);

    protected abstract String getContentType();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        Object model = getModel(request);

        response.setContentType(getContentType());
        renderTemplate(model, response);
    }

    private void renderTemplate(Object model, HttpServletResponse response) throws IOException {
        template.execute(response.getWriter(), model).flush();
    }
}
