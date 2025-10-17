package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricsServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private MetricsServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new MetricsServlet();
    }

    @Test
    void testInit() throws ServletException {
        servlet.init();
        assertNotNull(MetricsServlet.getRegistry());
        assertNotNull(MetricsServlet.getRequestCounter());
        assertNotNull(MetricsServlet.getBookAddedCounter());
        assertNotNull(MetricsServlet.getBookUpdatedCounter());
        assertNotNull(MetricsServlet.getBookDeletedCounter());
    }

    @Test
    void testDoGetFromLocalhost() throws Exception {
        servlet.init();
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        
        servlet.doGet(request, response);
        
        verify(response).setContentType("text/plain; version=0.0.4; charset=utf-8");
        verify(response).setHeader("X-Content-Type-Options", "nosniff");
        verify(response).setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        verify(response).getWriter();
    }

    @Test
    void testDoGetFromIPv6Localhost() throws Exception {
        servlet.init();
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        when(request.getRemoteAddr()).thenReturn("0:0:0:0:0:0:0:1");
        
        servlet.doGet(request, response);
        
        verify(response).getWriter();
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void testDoGetFromIPv6ShortLocalhost() throws Exception {
        servlet.init();
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        when(request.getRemoteAddr()).thenReturn("::1");
        
        servlet.doGet(request, response);
        
        verify(response).getWriter();
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void testDoGetUnauthorized() throws Exception {
        servlet.init();
        
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.isUserInRole("METRICS_READER")).thenReturn(false);
        
        servlet.doGet(request, response);
        
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
        verify(response, never()).getWriter();
    }

    @Test
    void testDoGetWithUserRole() throws Exception {
        servlet.init();
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.isUserInRole("METRICS_READER")).thenReturn(true);
        
        servlet.doGet(request, response);
        
        verify(response).setContentType("text/plain; version=0.0.4; charset=utf-8");
        verify(response).getWriter();
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void testDoGetWithWriteException() throws Exception {
        servlet.init();
        
        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        doThrow(new IOException("Write error")).when(writer).write(anyString());
        
        servlet.doGet(request, response);
        
        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());
    }

    @Test
    void testDoGetWithSendErrorException() throws Exception {
        servlet.init();
        
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.isUserInRole("METRICS_READER")).thenReturn(false);
        doThrow(new IOException("Send error")).when(response).sendError(anyInt(), anyString());
        
        // Ne devrait pas lancer d'exception même si sendError échoue
        servlet.doGet(request, response);
        
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
    }

    @Test
    void testCounters() throws ServletException {
        servlet.init();
        
        double initialRequestCount = MetricsServlet.getRequestCounter().count();
        double initialBookAddedCount = MetricsServlet.getBookAddedCounter().count();
        
        MetricsServlet.getRequestCounter().increment();
        MetricsServlet.getBookAddedCounter().increment();
        
        assertEquals(initialRequestCount + 1, MetricsServlet.getRequestCounter().count());
        assertEquals(initialBookAddedCount + 1, MetricsServlet.getBookAddedCounter().count());
    }

    @Test
    void testGetRegistry() throws ServletException {
        servlet.init();
        assertNotNull(MetricsServlet.getRegistry());
    }

    @Test
    void testSecurityHeaders() throws Exception {
        servlet.init();
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        
        servlet.doGet(request, response);
        
        verify(response).setHeader("X-Content-Type-Options", "nosniff");
        verify(response).setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
    }

    @Test
    void testDoGetGeneralException() throws Exception {
        servlet.init();
        
        when(request.getRemoteAddr()).thenThrow(new RuntimeException("Unexpected error"));
        
        // Ne devrait pas lancer d'exception
        servlet.doGet(request, response);
        
        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());
    }
}