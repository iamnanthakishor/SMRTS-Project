package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBConnection {

    private static final Logger logger = Logger.getLogger(DBConnection.class.getName());

    // ── Change these to match your MySQL setup ──
    private static final String URL      = "jdbc:mysql://localhost:3306/smrts_db?useSSL=false&serverTimezone=UTC";
    private static final String USER     = "root";
    private static final String PASSWORD = "2004";   // your DB password

    public static Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            return conn;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database connection failed: " + e.getMessage());
            throw new RuntimeException("Cannot connect to database. Check your settings.", e);
        }
    }
}