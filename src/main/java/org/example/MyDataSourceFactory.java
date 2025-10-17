package org.example;

import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MyDataSourceFactory {
    
    // Constructeur privé pour empêcher l'instanciation
    private MyDataSourceFactory() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    public static DataSource getDataSource() {
        Properties prop = loadDatabaseProperties();
        
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
     * Charge les propriétés de configuration de la base de données
     * @return Properties chargées depuis db.properties
     * @throws IllegalStateException si le fichier n'existe pas ou ne peut pas être chargé
     */
    private static Properties loadDatabaseProperties() {
        Properties prop = new Properties();
        
        try (InputStream input = MyDataSourceFactory.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            
            if (input == null) {
                throw new IllegalStateException("Database configuration file 'db.properties' not found in classpath");
            }
            
            prop.load(input);
            return prop;
            
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load database configuration from db.properties", ex);
        }
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