package org.example;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

public class MetricsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(MetricsServlet.class.getName());
    private static final PrometheusMeterRegistry prometheusRegistry = 
        new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        
    private static Counter requestCounter;
    private static Counter bookAddedCounter;
    private static Counter bookUpdatedCounter;
    private static Counter bookDeletedCounter;
    
    public static Counter getRequestCounter() {
        return requestCounter;
    }
    
    public static Counter getBookAddedCounter() {
        return bookAddedCounter;
    }
    
    public static Counter getBookUpdatedCounter() {
        return bookUpdatedCounter;
    }
    
    public static Counter getBookDeletedCounter() {
        return bookDeletedCounter;
    }
        
    @Override
    public void init() throws ServletException {
        super.init();
        initializeMetrics();
    }
    
    private static synchronized void initializeMetrics() {
        requestCounter = Counter.builder("http_requests_total")
            .description("Total HTTP requests")
            .tag("application", "bookstore")
            .register(prometheusRegistry);
            
        bookAddedCounter = Counter.builder("books_added_total")
            .description("Total books added")
            .register(prometheusRegistry);
            
        bookUpdatedCounter = Counter.builder("books_updated_total")
            .description("Total books updated")
            .register(prometheusRegistry);
            
        bookDeletedCounter = Counter.builder("books_deleted_total")
            .description("Total books deleted")
            .register(prometheusRegistry);
            
        Gauge.builder("jvm_memory_used_bytes", Runtime.getRuntime(),
            runtime -> runtime.totalMemory() - runtime.freeMemory())
            .description("JVM memory used")
            .register(prometheusRegistry);
    }
        
    public static MeterRegistry getRegistry() {
        return prometheusRegistry;
    }
        
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain; version=0.0.4; charset=utf-8");
        try {
            response.getWriter().write(prometheusRegistry.scrape());
        } catch (IOException e) {
            String errorMsg = "Failed to write Prometheus metrics";
            LOGGER.severe(errorMsg + ": " + e.getMessage());
            throw new ServletException(errorMsg, e);
        }
    }
}