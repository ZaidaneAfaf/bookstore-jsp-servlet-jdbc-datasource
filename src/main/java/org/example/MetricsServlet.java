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

public class MetricsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
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
        
        // Sécurité : Vérifier l'authentification/autorisation
        if (!isAuthorized(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }
        
        try {
            // Sécurité : Headers de sécurité
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
            response.setContentType("text/plain; version=0.0.4; charset=utf-8");
            
            String metricsData = prometheusRegistry.scrape();
            response.getWriter().write(metricsData);
        } catch (IOException e) {
            throw new IOException("Failed to write metrics response", e);
        }
    }
    
    /**
     * Vérifie si la requête est autorisée à accéder aux métriques.
     * À adapter selon votre mécanisme d'authentification.
     */
    private boolean isAuthorized(HttpServletRequest request) {
        // Option 1 : Vérifier un token dans les headers
        String authToken = request.getHeader("Authorization");
        if (authToken != null && isValidToken(authToken)) {
            return true;
        }
        
        // Option 2 : Vérifier que la requête vient de localhost
        String remoteAddr = request.getRemoteAddr();
        if ("127.0.0.1".equals(remoteAddr) || "0:0:0:0:0:0:0:1".equals(remoteAddr)) {
            return true;
        }
        
        // Option 3 : Vérifier un rôle utilisateur
        return request.isUserInRole("METRICS_READER");
    }
    
    /**
     * Valide le token d'authentification.
     * À implémenter selon votre système d'authentification.
     */
    private boolean isValidToken(String token) {
        // TODO: Implémenter la validation du token
        // Exemple : vérifier contre une variable d'environnement
        String expectedToken = System.getenv("METRICS_ACCESS_TOKEN");
        return expectedToken != null && expectedToken.equals(token);
    }
}