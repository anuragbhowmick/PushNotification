package com.vizury.PushNotification.DataLayer;

import com.vizury.PushNotification.common.PNConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by anurag on 5/8/15.
 */
public class DBConnector {

    private Logger logger = LoggerFactory.getLogger(DBConnector.class);

    String dbDriver;

    public DBConnector() {
        logger.debug("Initializing DBConnector");
        dbDriver = PNConstants.properties.getString(PNConstants.PROP_DB_DRIVER,
                PNConstants.DEFAULT_DB_DRIVER);
        try {
            Class.forName(dbDriver).newInstance();
        } catch (ClassNotFoundException e) {
            logger.error("Class not found exception in DB Connector " + e.getMessage());
        } catch (InstantiationException e) {
            logger.error("InstantiationException in DB Connector " + e.getMessage());
        } catch (IllegalAccessException e) {
            logger.error("IllegalAccessException in DB Connector " + e.getMessage());
            return;
        }
    }

    public Map<Integer, Map<String, String>> queryDB(String dbUserName, String dbPassword,
                        String dbUrl, String dbQuery) {
        Connection conn = null;
        ResultSet resultSet = null;
        Statement statement = null;
        Map<Integer, Map<String, String>> resultMap = new HashMap<Integer, Map<String, String>>();

        try {
            conn = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
            if(conn != null) {
                logger.debug("DB connection established, firing query " + dbQuery);
                statement = conn.createStatement();
                statement.executeQuery(dbQuery);
                resultSet = statement.getResultSet();
                ResultSetMetaData rsmd = resultSet.getMetaData();
                int numOfColumns = rsmd.getColumnCount();
                int count = 0;

                while(resultSet.next()) {
                    Map<String, String> rowMap = new HashMap<String, String>();
                    for (int i = 1; i <= numOfColumns; i++) {
                        rowMap.put(rsmd.getColumnName(i), resultSet.getString(i));
                    }
                    resultMap.put(count, rowMap);
                    count++;
                }
            }
        } catch (SQLException e) {
            logger.error("Error in connecting to {} DB. with message {} ", dbUrl, e.getMessage());
            e.printStackTrace();
        }
        finally {
            if (conn != null) {
                try {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    if (statement != null) {
                        statement.close();
                    }
                    conn.close();
                    logger.info("Database connection terminated");
                } catch (Exception e) {
                    logger.error("Error closing DB connection in queryDB", e);
                }
            }
        }
        return resultMap;
    }

    public void updateDB(String dbUserName, String dbPassword,
                         String dbUrl, String dbQuery) {
        Connection conn = null;
        Statement statement = null;
        try {
            conn = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
            if(conn != null) {
                logger.debug("DB connection established, firing query " + dbQuery);
                statement = conn.createStatement();
                statement.executeUpdate(dbQuery);
            }
        } catch (SQLException e) {
            logger.error("Error in connecting to {} DB ", dbUrl, e.getMessage());
            e.printStackTrace();
        }
        finally {
            if (conn != null) {
                try {
                    if (statement != null) {
                        statement.close();
                    }
                    conn.close();
                    logger.info("Database connection terminated");
                } catch (Exception e) {
                    logger.error("Error closing DB connection in updateDB", e);
                }
            }
        }
    }
}
