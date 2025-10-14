package org.example;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MyDataSourceFactory {
    
    private static DataSource dataSource;
    
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
            
            PoolProperties p = new PoolProperties();
            p.setUrl(props.getProperty("db.url"));
            p.setDriverClassName(props.getProperty("db.driver"));
            p.setUsername(props.getProperty("db.username"));
            p.setPassword(props.getProperty("db.password"));
            p.setJmxEnabled(true);
            p.setTestWhileIdle(false);
            p.setTestOnBorrow(true);
            p.setValidationQuery("SELECT 1");
            p.setTestOnReturn(false);
            p.setValidationInterval(30000);
            p.setTimeBetweenEvictionRunsMillis(30000);
            p.setMaxActive(100);
            p.setInitialSize(10);
            p.setMaxWait(10000);
            p.setRemoveAbandonedTimeout(60);
            p.setMinEvictableIdleTimeMillis(30000);
            p.setMinIdle(10);
            p.setLogAbandoned(true);
            p.setRemoveAbandoned(true);
            
            dataSource = new DataSource();
            dataSource.setPoolProperties(p);
        }
        return dataSource;
    }
    
    public static synchronized void closeDataSource() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}