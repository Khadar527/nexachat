package newpack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Serverjava {
    static final String JDBC_URL = "jdbc:oracle:thin:@localhost:1521:orcl";
    static final String DB_USER = "system";
    static final String DB_PASSWORD = "system";

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        System.out.println("Server is running...");

        try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("Database connection successful.");

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                    StringBuilder request = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null && !line.isEmpty()) {
                        request.append(line).append("\n");
                    }

                    boolean isOptionsRequest = request.toString().startsWith("OPTIONS");
                    boolean isPostRequest = request.toString().startsWith("POST");

                    // Handle CORS preflight request
                    if (isOptionsRequest) {
                        out.print("HTTP/1.1 204 No Content\r\n");
                        out.print("Access-Control-Allow-Origin: *\r\n");
                        out.print("Access-Control-Allow-Methods: POST, GET, OPTIONS\r\n");
                        out.print("Access-Control-Allow-Headers: Content-Type\r\n");
                        out.print("Content-Length: 0\r\n");
                        out.print("\r\n");
                        out.flush();
                        continue;
                    }

                    StringBuilder body = new StringBuilder();
                    if (request.toString().contains("Content-Length")) {
                        int contentLength = Integer.parseInt(request.toString().split("Content-Length: ")[1].split("\n")[0].trim());
                        for (int i = 0; i < contentLength; i++) {
                            body.append((char) in.read());
                        }
                    }

                    if (isPostRequest) {
                        String[] parts = body.toString().split("\"");
                        String message = parts[3];
                        String sender = parts[7];
                        String receiver = parts[11];

                        System.out.println("Received message: " + message + " from: " + sender + " to: " + receiver);

                        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO messages (sender, message, receiver) VALUES (?, ?, ?)")) {
                            statement.setString(1, sender);
                            statement.setString(2, message);
                            statement.setString(3, receiver);
                            statement.executeUpdate();
                        }

                        List<String> messages = new ArrayList<>();

                        try (PreparedStatement statement = connection.prepareStatement("SELECT sender, message, receiver, substr(timestamp, 10, 4) as time FROM messages WHERE (sender = ? AND receiver = ?) OR (receiver = ? AND sender = ?) ORDER BY timestamp ASC")) {
                            statement.setString(1, sender);
                            statement.setString(2, receiver);
                            statement.setString(3, sender);
                            statement.setString(4, receiver);
                            try (ResultSet resultSet = statement.executeQuery()) {
                                while (resultSet.next()) {
                                    String msgSender = resultSet.getString("sender");
                                    String msgMessage = resultSet.getString("message");
                                    String time = resultSet.getString("time");
                                    messages.add(msgSender + ": " + msgMessage + " " + time);
                                    System.out.println(msgSender + ": " + msgMessage + " " + time);
                                }
                            }
                        }

                        String responseMessage = String.join("\n", messages);
                        out.print("HTTP/1.1 200 OK\r\n");
                        out.print("Content-Type: text/plain\r\n");
                        out.print("Access-Control-Allow-Origin: *\r\n");
                        out.print("Content-Length: " + responseMessage.length() + "\r\n");
                        out.print("\r\n");
                        out.print(responseMessage);
                        out.flush();
                    } else {
                        List<String> messages = new ArrayList<>();
                        String sender = null;

                        // Extract sender from query string
                        String requestLine = request.toString().split("\n")[0];
                        if (requestLine.contains("?sender=")) {
                            sender = requestLine.split("\\?sender=")[1].split(" ")[0];
                        }

                        if (sender != null) {
                            try (PreparedStatement statement = connection.prepareStatement("SELECT sender, message, substr(timestamp, 10, 4) as time FROM messages WHERE sender = ? OR receiver = ? ORDER BY timestamp ASC")) {
                                statement.setString(1, sender);
                                statement.setString(2, sender);
                                try (ResultSet resultSet = statement.executeQuery()) {
                                    while (resultSet.next()) {
                                        String msgSender = resultSet.getString("sender");
                                        String msgMessage = resultSet.getString("message");
                                        String time = resultSet.getString("time");
                                        messages.add(msgSender + ": " + msgMessage + " " + time);
                                    }
                                }
                            }
                        }

                        String responseMessage = String.join("\n", messages);
                        out.print("HTTP/1.1 200 OK\r\n");
                        out.print("Content-Type: text/plain\r\n");
                        out.print("Access-Control-Allow-Origin: *\r\n");
                        out.print("Content-Length: " + responseMessage.length() + "\r\n");
                        out.print("\r\n");
                        out.print(responseMessage);
                        out.flush();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
