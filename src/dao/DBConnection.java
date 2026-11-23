package dao;

import exception.DatabaseOperationException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** One place for the JDBC URL, user, password and connection handling. */
public class DBConnection {
    private static final String HOST_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "rescuegrid_db";
    private static String user = "root";
    private static String password = "dbms";

    public static void setCredentials(String u, String p) {
        user = u;
        password = p;
    }

    private static void loadDriver() throws DatabaseOperationException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new DatabaseOperationException("MySQL JDBC driver jar not found. Add mysql-connector-j to the classpath.", e);
        }
    }

    public static Connection getConnection() throws DatabaseOperationException {
        loadDriver();
        try {
            return DriverManager.getConnection(HOST_URL + DB_NAME, user, password);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Cannot connect to database: " + e.getMessage(), e);
        }
    }

    /** Creates the database and all tables using Statement.executeUpdate(). */
    public static void initializeDatabase() throws DatabaseOperationException {
        loadDriver();
        Connection con = null;
        Statement st = null;
        try {
            con = DriverManager.getConnection(HOST_URL, user, password);
            st = con.createStatement();
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Cannot create database: " + e.getMessage(), e);
        } finally {
            closeQuietly(st);
            closeQuietly(con);
        }
        String[] tables = {
            "CREATE TABLE IF NOT EXISTS users (user_id INT PRIMARY KEY, name VARCHAR(60) NOT NULL, phone VARCHAR(15), "
                + "username VARCHAR(30) NOT NULL UNIQUE, password VARCHAR(30) NOT NULL, role VARCHAR(20) NOT NULL)",
            "CREATE TABLE IF NOT EXISTS incidents (incident_id INT PRIMARY KEY, disaster_type VARCHAR(30) NOT NULL, "
                + "location VARCHAR(60) NOT NULL, description VARCHAR(200), affected_people INT, severity INT, "
                + "status VARCHAR(15), reported_at VARCHAR(40), extra VARCHAR(60))",
            "CREATE TABLE IF NOT EXISTS rescue_teams (team_id INT PRIMARY KEY, team_name VARCHAR(40) NOT NULL, "
                + "specialization VARCHAR(30), available BOOLEAN, assigned_incident_id INT)",
            "CREATE TABLE IF NOT EXISTS team_members (member_id INT PRIMARY KEY, team_id INT NOT NULL, "
                + "member_name VARCHAR(60), skill VARCHAR(30), FOREIGN KEY (team_id) REFERENCES rescue_teams(team_id))",
            "CREATE TABLE IF NOT EXISTS resources (resource_id INT PRIMARY KEY, name VARCHAR(40) NOT NULL, "
                + "category VARCHAR(20), total_qty INT, allocated_qty INT)",
            "CREATE TABLE IF NOT EXISTS relief_camps (camp_id INT PRIMARY KEY, name VARCHAR(40) NOT NULL, "
                + "location VARCHAR(60), capacity INT, occupied INT, food_needed INT, water_needed INT)",
            "CREATE TABLE IF NOT EXISTS displaced_people (person_id INT PRIMARY KEY, name VARCHAR(60), age INT, "
                + "gender VARCHAR(10), camp_id INT NOT NULL, FOREIGN KEY (camp_id) REFERENCES relief_camps(camp_id))",
            "CREATE TABLE IF NOT EXISTS rescue_operations (operation_id INT PRIMARY KEY, incident_id INT NOT NULL, "
                + "status VARCHAR(15), start_time VARCHAR(40), end_time VARCHAR(40), "
                + "FOREIGN KEY (incident_id) REFERENCES incidents(incident_id))",
            "CREATE TABLE IF NOT EXISTS operation_teams (operation_id INT, team_id INT, PRIMARY KEY (operation_id, team_id), "
                + "FOREIGN KEY (operation_id) REFERENCES rescue_operations(operation_id), "
                + "FOREIGN KEY (team_id) REFERENCES rescue_teams(team_id))",
            "CREATE TABLE IF NOT EXISTS resource_allocations (allocation_id INT PRIMARY KEY, resource_id INT NOT NULL, "
                + "target_type VARCHAR(10), target_id INT, quantity INT, allocated_at VARCHAR(40), "
                + "FOREIGN KEY (resource_id) REFERENCES resources(resource_id))"
        };
        try {
            con = getConnection();
            st = con.createStatement();
            for (String sql : tables) st.executeUpdate(sql);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Cannot create tables: " + e.getMessage(), e);
        } finally {
            closeQuietly(st);
            closeQuietly(con);
        }
    }

    /** Counts rows of a table with a plain Statement + executeQuery(). */
    public static int countRows(String table) throws DatabaseOperationException {
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            st = con.createStatement();
            rs = st.executeQuery("SELECT COUNT(*) FROM " + table);
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Count failed for " + table + ": " + e.getMessage(), e);
        } finally {
            closeQuietly(rs);
            closeQuietly(st);
            closeQuietly(con);
        }
    }

    // Overloaded close helpers: every close error is reported, never silently ignored.
    public static void closeQuietly(ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { System.out.println("[WARN] close ResultSet: " + e.getMessage()); }
    }

    public static void closeQuietly(Statement st) {
        try { if (st != null) st.close(); } catch (SQLException e) { System.out.println("[WARN] close Statement: " + e.getMessage()); }
    }

    public static void closeQuietly(Connection con) {
        try { if (con != null) con.close(); } catch (SQLException e) { System.out.println("[WARN] close Connection: " + e.getMessage()); }
    }
}
