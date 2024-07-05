package newpack;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestPostgresConnection {
    public static void main(String[] args) {
        String JDBC_URL = "jdbc:postgresql://35.192.222.218:5432/r2schools";
        String username = "postgres";
        String password = "adminuser";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, username, password)) {
            System.out.println("Connection to PostgreSQL established!");
        } catch (SQLException e) {
            System.err.println("Connection failed:");
            e.printStackTrace();
        }
    }
}
