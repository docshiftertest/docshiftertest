package com.docshifter.core.metric.services;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

@Service
public class MetricManagementServiceImpl implements MetricManagementService {

    private static final Logger logger = Logger.getLogger(new Object() {}.getClass().getEnclosingClass());

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

    /**
     * Retrieves the number of all tasks that have completed successfully.
     * @return success
     *
     * TODO: (Also below) Lessen hardcoding in the queries
     */
    public int successfulWfs() {
        Connection db = connect();
        Statement statement = null;
        int success=0;
        try {
            statement = db.createStatement();
            ResultSet query = statement.executeQuery(
                    "SELECT count(distinct(task_id)) FROM monitoring.monitoringqueue " +
                    "WHERE level='SUCCESS'");
            while (query.next()) {
                success = query.getInt("count");
            }
            query.close();
            statement.close();
            db.close();
        }catch (Exception e) {
                logger.warn(e);
            }

        return success;
    }

    public int allWfs() {
        Connection db = connect();
        Statement statement = null;
        int counts=0;
        try {
            statement = db.createStatement();
            ResultSet query = statement.executeQuery(
                    "SELECT count(distinct(task_id)) FROM monitoring.monitoringqueue");
            while (query.next()) {
                counts = query.getInt("count");
            }
            query.close();
            statement.close();
            db.close();
        }catch (Exception e) {
            logger.warn(e);
        }

        return counts;
    }

    /**
     * Retrieves the number of files that were processed in successful tasks.
     *
     * @return success
     */
    public int successfulFiles() {
        Connection db = connect();
        Statement statement;
        int success=0;
        try {
            statement = db.createStatement();
            ResultSet query = statement.executeQuery(
                    "SELECT message FROM monitoring.monitoringqueue WHERE " +
                    "level='INFO' AND task_id IN (SELECT task_id FROM monitoring.monitoringqueue WHERE level='SUCCESS')");
            while (query.next()) {
                Scanner scan = new Scanner(query.getString("message"));
                scan.skip("[^0-9]*"); // skips all non-integer characters
                int count = scan.nextInt();
                success = success + count;
            }
            query.close();
            statement.close();
            db.close();
        }catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }

        return success;
    }

    public int allFiles() {
        Connection db = connect();
        Statement statement = null;
        int allCounts=0;
        try {
            statement = db.createStatement();
            ResultSet query = statement.executeQuery(
                    "SELECT message FROM monitoring.monitoringqueue WHERE " +
                    "level='INFO' AND task_id IN (SELECT task_id FROM monitoring.monitoringqueue)");
            while (query.next()) {
                Scanner scan = new Scanner(query.getString("message"));
                scan.skip("[^0-9]*"); // skips all non-integer characters
                int count = scan.nextInt();
                allCounts = allCounts + count;
            }
            query.close();
            statement.close();
            db.close();
        }catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }

        return allCounts;
    }
}
