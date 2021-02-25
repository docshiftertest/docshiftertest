package com.docshifter.core.metric.services;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@Service
public class MetricManagementServiceImpl implements MetricManagementService {

    private static final Logger logger = Logger.getLogger(new Object() {
    }.getClass().getEnclosingClass());

    // Establish the connection to the database.
    // TODO: Don't hardcode the database and driver
    public Connection connect() {
        Connection db = null;
        try {
            Class.forName("org.postgresql.Driver");
            db = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/MonitoringTest",
                            "postgres", "postgres");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getClass().getName() + ": " + e.getMessage());
        }
        logger.info("Opened database successfully");

        return db;
    }

    public int successfulWfs() {
        Connection db = connect();
        Statement statement = null;
        int success=0;
        try {
            statement = db.createStatement();
            ResultSet query = statement.executeQuery("SELECT count(distinct(task_id)) FROM monitoring.monitoringqueue " +
                    "WHERE level='SUCCESS'");
            while (query.next()) {
                success = query.getInt("count");
                query.close();
                statement.close();
            }
        }catch (Exception e) {
                logger.warn(e);
            }

        return success;
    }
}
