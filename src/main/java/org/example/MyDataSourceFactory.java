package org.example;

import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyDataSourceFactory {
    
    private static final Logger LOGGER = Logger.getLogger(MyDataSourceFactory.class.getName());
    
    // Constructeur privé pour empêcher l'instanciation
    private MyDataSourceFactory() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    public static DataSource getDataSource() {
        Properties prop = new Properties();
        
        try (InputStream input = MyDataSourceFactory.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            
            if (input == null) {
                LOGGER.severe("Unable to find db.properties file");
                throw new IllegalStateException("Database configuration file not found");
            }
            
            prop.load(input);
            
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load database configuration", ex);
            throw new IllegalStateException("Failed to load database configuration", ex);
        }
        
        // Validation des propriétés obligatoires
        validateProperty(prop, "db.driver");
        validateProperty(prop, "db.url");
        validateProperty(prop, "db.username");
        validateProperty(prop, "db.password");
        
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(prop.getProperty("db.driver"));
        dataSource.setUrl(prop.getProperty("db.url"));
        dataSource.setUsername(prop.getProperty("db.username"));
        dataSource.setPassword(prop.getProperty("db.password"));
        dataSource.setInitialSize(5); // Initial number of connections
        dataSource.setMaxTotal(10); // Maximum number of connections
        
        return dataSource;
    }
    
    /**
     * Valide qu'une propriété existe et n'est pas vide
     */
    private static void validateProperty(Properties prop, String key) {
        String value = prop.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Missing or empty required property: " + key);
        }
    }
}