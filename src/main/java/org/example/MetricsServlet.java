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
import java.lang.management.ManagementFactory;
import java.util.logging.Level;
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

        Gauge.builder("jvm_cpu_usage", ManagementFactory.getOperatingSystemMXBean(),
            osBean -> osBean.getSystemCpuLoad() * 100)
            .description("System CPU usage in percentage")
            .register(prometheusRegistry);
    }
            
    public static MeterRegistry getRegistry() {
        return prometheusRegistry;
    }
            
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        try {
            // Sécurité : Vérifier l'authentification/autorisation
            if (!isAuthorized(request)) {
                handleUnauthorizedAccess(response);
                return;
            }
            
            // Sécurité : Headers de sécurité
            setSecurityHeaders(response);
            response.setContentType("text/plain; version=0.0.4; charset=utf-8");
            
            String metricsData = prometheusRegistry.scrape();
            writeMetricsResponse(response, metricsData);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error in doGet", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Unexpected error while retrieving metrics");
            } catch (IOException ioe) {
                LOGGER.log(Level.SEVERE, "Failed to send error response", ioe);
            }
        }
    }
    
    /**
     * Définit les headers de sécurité pour la réponse.
     */
    private void setSecurityHeaders(HttpServletResponse response) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
    }
    
    /**
     * Gère l'accès non autorisé.
     */
    private void handleUnauthorizedAccess(HttpServletResponse response) {
        try {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to send access denied response", e);
        }
    }
    
    /**
     * Écrit les données de métriques dans la réponse.
     */
    private void writeMetricsResponse(HttpServletResponse response, String metricsData) {
        try {
            response.getWriter().write(metricsData);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to write metrics response", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Failed to write metrics");
            } catch (IOException ioe) {
                LOGGER.log(Level.SEVERE, "Failed to send error response after write failure", ioe);
            }
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
        if (isLocalhostAddress(remoteAddr)) {
            return true;
        }
        
        // Option 3 : Vérifier un rôle utilisateur
        return request.isUserInRole("METRICS_READER");
    }
    
    /**
     * Vérifie si l'adresse est localhost.
     */
    private boolean isLocalhostAddress(String remoteAddr) {
        return "127.0.0.1".equals(remoteAddr) || "0:0:0:0:0:0:0:1".equals(remoteAddr) 
            || "::1".equals(remoteAddr);
    }
    
    /**
     * Valide le token d'authentification.
     * À implémenter selon votre système d'authentification.
     */
    private boolean isValidToken(String token) {
        // Éviter les null pointer exceptions
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        // Exemple : vérifier contre une variable d'environnement
        String expectedToken = System.getenv("METRICS_ACCESS_TOKEN");
        return expectedToken != null && expectedToken.equals(token);
    }
}