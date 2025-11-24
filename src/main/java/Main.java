package main.java;
import main.java.database.DBConnection;

import java.util.*;
import java.sql.*;

public class Main {
    public static void main(String[] args) {

        System.out.println("Starting application...");

        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                System.out.println("SUCCESS: Connected to Aiven MySQL Database!");
            }
            else {
                System.out.println("ERROR: Connection object is null.");
            }
        } catch (Exception e) {
            System.err.println("CONNECTION FAILED! Check Environment Variables and Aiven access.");
            e.printStackTrace();
        }
    }
}