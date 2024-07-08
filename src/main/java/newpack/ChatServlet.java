package newpack;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ChatServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String JDBC_URL = "jdbc:sqlserver://mychatdata.database.windows.net:1433;database=r2schoolss;user=system@mychatdata;password=tiger@23;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";
    //private static final String JDBC_URL = "jdbc:oracle:thin:@localhost:1521:orcl";
    private static final String DB_USER = "system";
    private static final String DB_PASSWORD = "tiger@23";

    /*static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        	//Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Failed to load JDBC driver for SQL Server", e);
        }
    }*/
    
    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            System.out.println("JDBC driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to load JDBC driver: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("Failed to load JDBC driver for SQL Server", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        StringBuilder requestBody = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error reading request body: " + ex.getMessage());
            return;
        }

        String json = requestBody.toString();
        json = json.substring(1, json.length() - 1);

        String[] parts = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

        String message = "";
        String sender = "";
        String receiver = "";

        for (String part : parts) {
            String[] keyValue = part.split(":");
            if (keyValue.length < 2) continue;

            String key = keyValue[0].trim().replaceAll("\"", "");
            String value = keyValue[1].trim().replaceAll("\"", "");

            if (key.equalsIgnoreCase("message")) {
                message = value;
            } else if (key.equalsIgnoreCase("sender")) {
                sender = value;
            } else if (key.equalsIgnoreCase("receiver")) {
                receiver = value;
            }
        }

        if (sender.isEmpty() || receiver.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid JSON data: missing required fields.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
            String sql = "INSERT INTO messages (sender, message, receiver) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, sender);
                stmt.setString(2, message);
                stmt.setString(3, receiver);
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error storing message: " + ex.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        String sender = request.getParameter("sender");
        String receiver = request.getParameter("receiver");

        if (sender == null || receiver == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Sender and receiver parameters are required.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT sender, message FROM messages WHERE (sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?) ORDER BY timestamp ASC";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, sender);
                stmt.setString(2, receiver);
                stmt.setString(3, receiver);
                stmt.setString(4, sender);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String msgSender = rs.getString("sender");
                        String message = rs.getString("message");
                        response.getWriter().println(msgSender + ": " + message);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error retrieving messages: " + ex.getMessage());
        }
    }
}
