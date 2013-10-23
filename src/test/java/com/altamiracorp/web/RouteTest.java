package com.altamiracorp.web;

import com.altamiracorp.web.Route.Method;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class RouteTest {
    private String path;
    private Handler handler;

    @Before
    public void before() {
        handler = mock(Handler.class);
        path = "/test";
    }

    @Test
    public void testRouteMiss() {
        Route r = new Route(Method.GET, path, handler);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/foo");
        assertFalse(r.isMatch(request));
    }

    @Test
    public void testExactRouteMatch() {
        Route r = new Route(Method.GET, path, handler);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn(path);
        assertTrue(r.isMatch(request));
    }

    @Test
    public void testRouteMatchWithComponents() {
        Route r = new Route(Method.GET, path + "/{id}", handler);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn(path + "/25");
        assertTrue(r.isMatch(request));
        verify(request).setAttribute("id", "25");
    }

    @Test
    public void testComplexComponentAttributeSetting() {
        Route r = new Route(Method.GET, path + "/{model}/edit/{_id}", handler);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn(path + "/person/edit/25");
        assertTrue(r.isMatch(request));
        verify(request).setAttribute("model", "person");
        verify(request).setAttribute("_id", "25");
    }

    @Test
    public void testWithEscapedSlash() {
        Route r = new Route(Method.GET, path + "/{id}/test", handler);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn(path + "/12%2F34/test");
        assertTrue(r.isMatch(request));
        verify(request).setAttribute("id", "12/34");
    }

    @Test
    public void testComponentAsBaseFilename() {
        Route r = new Route(Method.GET, path + "/{file}.ext", handler);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn(path + "/less.ext");
        assertTrue(r.isMatch(request));
        verify(request).setAttribute("file", "less");
    }
}
