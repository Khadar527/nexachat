package newpack;

import java.io.IOException;
import java.io.PrintWriter;
//import java.net.PasswordAuthentication;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class AddedAccount extends HttpServlet {
    private static final long serialVersionUID = 1L;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Failed to load JDBC driver for PostgreSQL", e);
        }
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        String s1 = request.getParameter("myname");
        String s2 = request.getParameter("mymail");
        String s3 = request.getParameter("mymobilenum");
        String s4 = request.getParameter("myedu");
        //String s5 = request.getParameter("mygender");
        String s6 = request.getParameter("myskills");
        String s7 = request.getParameter("myintrests");
        //String s8 = request.getParameter("mycity");
        String s9 = request.getParameter("projects");
        //UUID userId = UUID.randomUUID();
        //String userIds = userId.toString();
        String s11 = request.getParameter("designation");
        //LocalDate date = LocalDate.now();
       // String today = date.toString();
        ////Cookie ck = new Cookie("mailid", userIds);
        //response.addCookie(ck);

        try (Connection connection = getConnection()) {
            String insertQuery = "INSERT INTO usertable(name, mailid, mobile, intrests, graduation, skill, favdesignation, projects) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                preparedStatement.setString(1, s1);
                preparedStatement.setString(2, s2);
                preparedStatement.setString(3, s3);
                preparedStatement.setString(4, s7);
                preparedStatement.setString(5, s4);
                preparedStatement.setString(6, s6);
                preparedStatement.setString(7, s11);
                preparedStatement.setString(8, s9);
                
                
                //preparedStatement.setString(11, userIds);

                preparedStatement.executeUpdate();

                //sendEmail(s1, s2);

                out.println("<!DOCTYPE html>"
                		+ "<html><head><meta name='viewport' content='width=device-width, initial-scale=1'><link rel='stylesheet' href='https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/css/bootstrap.min.css'>"
                		+ "  <script src='https://cdn.jsdelivr.net/npm/jquery@3.7.1/dist/jquery.slim.min.js'></script><script src='https://cdn.jsdelivr.net/npm/popper.js@1.16.1/dist/umd/popper.min.js'></script><script src='https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/js/bootstrap.bundle.min.js'></script></head><body>"
                		+ ""
                		+ "<div class='container text-center' style='margin-top: 20%;'><h2>Loading...</h2><div class='spinner-border' style='height: 200px; width: 200px; color: dodgerblue;'></div></div>"
                		+ ""
                		+ "<script>"
                		+ "alert('You have been created account Successfully');"
                		+ "  setTimeout(function() {"
                		+ "    window.location.href = 'Login.html';"
                		+ "  }, 3000); "
                		+ "</script>"
                		+ ""
                		+ "</body>"
                		+ "</html>"
                		+ "");
            }
        } catch (Exception e) {
            logError("Unexpected error", e);
            out.println("Unexpected error: " + e.getMessage());
        }
    }

    private void logError(String message, Exception e) {
        Logger logger = Logger.getLogger(AddedAccount.class.getName());
        logger.log(Level.SEVERE, message, e);
    }

    private Connection getConnection() throws SQLException, URISyntaxException {
    	
         String jdbcUrl = "jdbc:postgresql://35.192.222.218:5432/r2schools";
         String user = "postgres";
         String password = "adminuser";
 
        return DriverManager.getConnection(jdbcUrl, user, password);
    }
}
   /* private void sendEmail(String name, String email) throws javax.mail.MessagingException {
        String hostname = "smtp.gmail.com";
        final String username = "skhadar527@gmail.com";
        final String password = "your-email-password"; // Use your actual password or an app-specific password

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", hostname);
        props.put("mail.smtp.port", "587");
        props.put("mail.debug", "true");

        javax.mail.Authenticator auth = new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };

        javax.mail.Session session = Session.getInstance(props, auth);

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("skhadar527@gmail.com"));
        InternetAddress[] address = { new InternetAddress(email) };
        msg.setRecipients(MimeMessage.RecipientType.TO, address);
        msg.setSubject("Account Created: " + name);
        msg.setText("You have been created Account. Please confirm your mail by clicking on the link https://myjobcareer-f817cd4f079f.herokuapp.com/Account_Confirmation");

        Transport.send(msg);
    }
}*/
