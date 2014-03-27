package com.altamiracorp.miniweb;

import com.altamiracorp.miniweb.Route.Method;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class RouterTest {
    private Router router;
    private Handler handler;
    private Handler exceptionThrowingHandler;
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
        exceptionThrowingHandler = mock(Handler.class);
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
        when(request.getContextPath()).thenReturn("");
        router.route(request, response);
        verify(handler).handle(eq(request), eq(response), any(HandlerChain.class));
    }

    @Test
    public void testRouteWithComponent() throws Exception {
        router.addRoute(Method.GET, path + "/{id}/text", handler);
        when(request.getMethod()).thenReturn(Method.GET.toString());
        when(request.getRequestURI()).thenReturn(path + "/25/text");
        when(request.getContextPath()).thenReturn("");
        router.route(request, response);
        verify(handler).handle(eq(request), eq(response), any(HandlerChain.class));
        verify(request).setAttribute("id", "25");
    }

    @Test
    public void testMultipleRoutesWithSamePrefix() throws Exception {
        router.addRoute(Method.GET, path + "/{id}/text", handler);
        Route routeRaw = router.addRoute(Method.GET, path + "/{id}/raw", handler);
        router.addRoute(Method.GET, path + "/{id}", handler);
        when(request.getMethod()).thenReturn(Method.GET.toString());
        when(request.getRequestURI()).thenReturn(path + "/25/raw");
        when(request.getContextPath()).thenReturn("");
        router.route(request, response);
        verify(handler).handle(eq(request), eq(response), any(HandlerChain.class));
        verify(request).setAttribute("id", "25");
        verify(request).setAttribute(Route.MATCHED_ROUTE, routeRaw);
    }

    @Test
    public void testMultipleRoutesWithSamePrefixLastMatch() throws Exception {
        router.addRoute(Method.GET, path + "/{id}/text", handler);
        router.addRoute(Method.GET, path + "/{id}/raw", handler);
        Route defaultRoute = router.addRoute(Method.GET, path + "/{id}", handler);
        when(request.getMethod()).thenReturn(Method.GET.toString());
        when(request.getRequestURI()).thenReturn(path + "/25/zzz");
        when(request.getContextPath()).thenReturn("");
        router.route(request, response);
        verify(handler).handle(eq(request), eq(response), any(HandlerChain.class));
        verify(request).setAttribute("id", "25/zzz");
        verify(request).setAttribute(Route.MATCHED_ROUTE, defaultRoute);
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
        when(request.getContextPath()).thenReturn("");
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
        when(request.getContextPath()).thenReturn("");
        router.route(request, response);
        verify(handler).handle(eq(request), eq(response), any(HandlerChain.class));
    }

    @Test
    public void testExceptionHandler() throws Exception {
        Class<? extends Exception> exClass = ArrayStoreException.class;
        router.addRoute(Method.GET, path, exceptionThrowingHandler);
        router.addExceptionHandler(exClass, new Handler[]{handler});

        when(request.getMethod()).thenReturn(Method.GET.toString());
        when(request.getRequestURI()).thenReturn(path);
        when(request.getContextPath()).thenReturn("");
        doThrow(exClass).when(exceptionThrowingHandler).handle(eq(request), eq(response), any(HandlerChain.class));
        router.route(request, response);
        verify(handler).handle(eq(request), eq(response), any(HandlerChain.class));
    }

    @Test(expected = ArrayStoreException.class)
    public void testMissingExceptionHandler() throws Exception {
        Class<? extends Exception> exClass = ArrayStoreException.class;
        router.addRoute(Method.GET, path, exceptionThrowingHandler);
        router.addExceptionHandler(RuntimeException.class, new Handler[]{handler});

        when(request.getMethod()).thenReturn(Method.GET.toString());
        when(request.getRequestURI()).thenReturn(path);
        when(request.getContextPath()).thenReturn("");
        doThrow(exClass).when(exceptionThrowingHandler).handle(eq(request), eq(response), any(HandlerChain.class));
        router.route(request, response);
    }

    @Test(expected = ArrayStoreException.class)
    public void testExceptionInExceptionHandler() throws Exception {
        Class<? extends Exception> exClass = ArrayStoreException.class;
        router.addRoute(Method.GET, path, exceptionThrowingHandler);
        router.addExceptionHandler(exClass, new Handler[] { handler });

        when(request.getMethod()).thenReturn(Method.GET.toString());
        when(request.getRequestURI()).thenReturn(path);
        when(request.getContextPath()).thenReturn("");
        doThrow(exClass).when(handler).handle(eq(request), eq(response), any(HandlerChain.class));
        doThrow(exClass).when(exceptionThrowingHandler).handle(eq(request), eq(response), any(HandlerChain.class));
        router.route(request, response);
    }
}
