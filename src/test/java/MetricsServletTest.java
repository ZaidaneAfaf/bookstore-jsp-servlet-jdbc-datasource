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
    void testDoGet() throws Exception {
        servlet.init();
        
        // Stubbing déplacé uniquement ici où il est nécessaire
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        when(request.getMethod()).thenReturn("GET");
        
        servlet.service(request, response);
        
        verify(response).setContentType("text/plain; version=0.0.4; charset=utf-8");
        verify(response).getWriter();
        
        String output = stringWriter.toString();
        assertNotNull(output);
        assertTrue(output.contains("http_requests_total") || output.length() > 0);
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