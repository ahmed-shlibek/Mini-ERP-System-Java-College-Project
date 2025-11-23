package main.java.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Reading variable from environment
    private static final String BD_URL = System.getenv("BD_URL");
    private static final String BD_USER = System.getenv("BD_USER");
    private static final String BD_PASSWORD = System.getenv("BD_PASSWORD");
    private static final String BD_DRIVER = System.getenv("BD_DRIVER");

    static {
        try {
            Class.forName(BD_DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(BD_URL, BD_USER, BD_PASSWORD);
    }
}
