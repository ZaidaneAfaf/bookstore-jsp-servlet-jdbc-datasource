package org.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MyDataSourceFactory {
    
    private static HikariDataSource dataSource;
    
    // Constructeur privé pour empêcher l'instanciation
    private MyDataSourceFactory() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    public static synchronized DataSource getDataSource() {
        if (dataSource == null) {
            Properties props = new Properties();
            try (InputStream input = MyDataSourceFactory.class.getClassLoader()
                    .getResourceAsStream("db.properties")) {
                if (input == null) {
                    throw new IOException("Unable to find db.properties");
                }
                props.load(input);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to load database properties", e);
            }
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.username"));
            config.setPassword(props.getProperty("db.password"));
            config.setDriverClassName(props.getProperty("db.driver"));
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(5);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            
            dataSource = new HikariDataSource(config);
        }
        return dataSource;
    }
    
    public static synchronized void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}