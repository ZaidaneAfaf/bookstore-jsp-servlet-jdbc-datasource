package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
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
        when(request.getMethod()).thenReturn("GET");
        // Simuler une requête depuis localhost
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        
        servlet.service(request, response);
        
        verify(response).setContentType("text/plain; version=0.0.4; charset=utf-8");
        verify(response).getWriter();
        
        String output = stringWriter.toString();
        assertNotNull(output);
        assertTrue(!output.isEmpty());
    }

    @Test
    void testDoGetUnauthorized() throws Exception {
        servlet.init();
        
        when(request.getMethod()).thenReturn("GET");
        // Simuler une requête depuis une adresse non autorisée
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.isUserInRole("METRICS_READER")).thenReturn(false);
        
        servlet.service(request, response);
        
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
}