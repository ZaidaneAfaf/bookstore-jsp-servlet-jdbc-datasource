package org.example;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class MetricsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(MetricsServlet.class.getName());
    
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static PrometheusMeterRegistry prometheusRegistry;
    private static Counter requestCounter;
    private static Counter bookAddedCounter;
    private static Counter bookUpdatedCounter;
    private static Counter bookDeletedCounter;
    private static final AtomicInteger activeThreads = new AtomicInteger();
    
    public static Counter getRequestCounter() {
        checkInitialization();
        return requestCounter;
    }
    
    public static Counter getBookAddedCounter() {
        checkInitialization();
        return bookAddedCounter;
    }
    
    public static Counter getBookUpdatedCounter() {
        checkInitialization();
        return bookUpdatedCounter;
    }
    
    public static Counter getBookDeletedCounter() {
        checkInitialization();
        return bookDeletedCounter;
    }
    
    private static void checkInitialization() {
        if (!initialized.get()) {
            synchronized (MetricsServlet.class) {
                if (!initialized.get()) {
                    initializeMetrics();
                    initialized.set(true);
                }
            }
        }
    }
        
    @Override
    public void init() throws ServletException {
        super.init();
        checkInitialization();
    }
    
    private static synchronized void initializeMetrics() {
        if (prometheusRegistry == null) {
            prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        }
        
        // Initialisation des counters
        requestCounter = Counter.builder("http_requests_total")
            .description("Total HTTP requests")
            .tag("application", "bookstore")
            .register(prometheusRegistry);
            
        bookAddedCounter = Counter.builder("books_added_total")
            .description("Total books added")
            .tag("application", "bookstore")
            .register(prometheusRegistry);
            
        bookUpdatedCounter = Counter.builder("books_updated_total")
            .description("Total books updated")
            .tag("application", "bookstore")
            .register(prometheusRegistry);
            
        bookDeletedCounter = Counter.builder("books_deleted_total")
            .description("Total books deleted")
            .tag("application", "bookstore")
            .register(prometheusRegistry);
        
        // Utilisation des binders standard pour les métriques JVM
        new JvmMemoryMetrics().bindTo(prometheusRegistry);
        new JvmThreadMetrics().bindTo(prometheusRegistry);
        new ProcessorMetrics().bindTo(prometheusRegistry);
    }
        
    public static MeterRegistry getRegistry() {
        checkInitialization();
        return prometheusRegistry;
    }
        
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        // Headers de sécurité
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        
        try {
            String metrics = prometheusRegistry.scrape();
            response.getWriter().write(metrics);
            response.getWriter().flush();
        } catch (IOException e) {
            LOGGER.severe("Error writing metrics: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new ServletException("Error writing metrics", e);
        }
    }
    
    @Override
    public void destroy() {
        super.destroy();
        if (prometheusRegistry != null) {
            prometheusRegistry.close();
        }
    }
}