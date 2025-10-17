import org.example.MyDataSourceFactory;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

class MyDataSourceFactoryTest {

    @Test
    void testGetDataSource() {
        DataSource dataSource = MyDataSourceFactory.getDataSource();
        
        assertNotNull(dataSource, "DataSource should not be null");
        assertTrue(dataSource instanceof org.apache.commons.dbcp2.BasicDataSource);
    }

    @Test
    void testDataSourceConfiguration() {
        DataSource dataSource = MyDataSourceFactory.getDataSource();
        
        if (dataSource instanceof org.apache.commons.dbcp2.BasicDataSource) {
            org.apache.commons.dbcp2.BasicDataSource basicDataSource = 
                (org.apache.commons.dbcp2.BasicDataSource) dataSource;
            
            assertEquals(5, basicDataSource.getInitialSize());
            assertEquals(10, basicDataSource.getMaxTotal());
            assertNotNull(basicDataSource.getUrl());
            assertNotNull(basicDataSource.getUsername());
        }
    }

    @Test
    void testDataSourceNotNull() {
        DataSource dataSource = MyDataSourceFactory.getDataSource();
        assertNotNull(dataSource);
    }

    @Test
    void testDataSourceIsBasicDataSource() {
        DataSource dataSource = MyDataSourceFactory.getDataSource();
        assertTrue(dataSource instanceof org.apache.commons.dbcp2.BasicDataSource,
                "DataSource should be an instance of BasicDataSource");
    }

    @Test
    void testDataSourceProperties() {
        DataSource dataSource = MyDataSourceFactory.getDataSource();
        
        if (dataSource instanceof org.apache.commons.dbcp2.BasicDataSource) {
            org.apache.commons.dbcp2.BasicDataSource basicDataSource = 
                (org.apache.commons.dbcp2.BasicDataSource) dataSource;
            
            // Vérifier les propriétés de configuration
            assertTrue(basicDataSource.getInitialSize() > 0, 
                "Initial size should be greater than 0");
            assertTrue(basicDataSource.getMaxTotal() > 0, 
                "Max total connections should be greater than 0");
            assertTrue(basicDataSource.getInitialSize() <= basicDataSource.getMaxTotal(),
                "Initial size should be less than or equal to max total");
        }
    }
}