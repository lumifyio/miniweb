package com.altamiracorp.web;

import com.altamiracorp.web.Route.Method;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class RouterTest {
    private Router router;
    private Handler handler;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletConfig servletConfig;
    private RequestDispatcher requestDispatcher;
    private ServletContext servletContext;
    private String path = "/foo";

    @Before
    public void before() {
        servletConfig = mock(ServletConfig.class);
        router = new Router(servletConfig);
        handler = mock(Handler.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        requestDispatcher = mock(RequestDispatcher.class);
        servletContext = mock(ServletContext.class);

        when(servletConfig.getServletContext()).thenReturn(servletContext);
    }

    @Test
    public void testSimpleRoute() throws Exception {
        router.addRoute(Method.GET, path, handler);
        when(request.getMethod()).thenReturn(Method.GET.toString());
        when(request.getRequestURI()).thenReturn(path);
        router.route(request, response);
        verify(handler).handle(eq(request), eq(response), any(HandlerChain.class));
    }

    @Test
    public void testRouteWithComponent() throws Exception {
        router.addRoute(Method.GET, path + "/{id}/text", handler);
        when(request.getMethod()).thenReturn(Method.GET.toString());
        when(request.getRequestURI()).thenReturn(path + "/25/text");
        router.route(request, response);
        verify(handler).handle(eq(request), eq(response), any(HandlerChain.class));
        verify(request).setAttribute("id", "25");
    }

    @Test
    public void testRouteMissingDueToMethod() throws Exception {
        router.addRoute(Method.GET, path, handler);
        when(request.getMethod()).thenReturn(Method.POST.toString());
        when(request.getRequestURI()).thenReturn(path);
        when(servletContext.getNamedDispatcher(anyString())).thenReturn(requestDispatcher);
        router.route(request, response);
        verify(requestDispatcher).forward(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    public void testRouteMissingDueToPath() throws Exception {
        router.addRoute(Method.GET, path, handler);
        when(request.getMethod()).thenReturn(Method.GET.toString());
        when(request.getRequestURI()).thenReturn(path + "extra");
        when(servletContext.getNamedDispatcher(anyString())).thenReturn(requestDispatcher);
        router.route(request, response);
        verify(requestDispatcher).forward(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    public void testMultipleRouteHandlers() throws Exception {
        Handler h2 = new TestHandler();
        router.addRoute(Method.GET, path, h2, handler);
        when(request.getMethod()).thenReturn(Method.GET.toString());
        when(request.getRequestURI()).thenReturn(path);
        router.route(request, response);
        verify(handler).handle(eq(request), eq(response), any(HandlerChain.class));
    }
}
