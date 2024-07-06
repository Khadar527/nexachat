package newpack;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class LoginPack
 */

public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Login() {
        super();
        // TODO Auto-generated constructor stub
    }

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Failed to load JDBC driver for SQL Server", e);
        }
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String mail = request.getParameter("username");
        String password = request.getParameter("mail");
       // Cookie ck=new Cookie("mailid",mail);
		//response.addCookie(ck);
        try {
            // Load the PostgreSQL JDBC driver

            // Establish the database connection
            try (Connection connection = getConnection()) {
                // Replace with your SQL query
                String selectQuery = "select * from usertable where name=? and mailid=?";

                try (PreparedStatement pstmt = connection.prepareStatement(selectQuery)){
                		pstmt.setString(1, mail);
                		pstmt.setString(2, password);
                        try(ResultSet rs = pstmt.executeQuery()) {

                        	if(rs.next()) {
                    			RequestDispatcher rd=request.getRequestDispatcher("Homepage");
                    			rd.forward(request, response);
                    		
                    	}
                    		else {
                    			out.println("<html><body>");
                    			out.println("<script>alert('Invalid Username / Password')</script></body></html>");
                    			RequestDispatcher rd=request.getRequestDispatcher("Login.html");
                    			rd.include(request, response);
                    		}

                        }
                } catch (SQLException e) {
                    // Log the exception
                    
                    out.println("Error executing SQL query: " + e.getMessage());
                }

            } catch (Exception e) {
                // Log the exception
               
                out.println("Error establishing connection: " + e.getMessage());
            }
        } catch (Exception e) {
            // Log the exception
            
            out.println("Unexpected error: " + e.getMessage());
        }
    }

    

    private Connection getConnection() throws SQLException, URISyntaxException {
    	 String jdbcUrl = "jdbc:sqlserver://mychatdata.database.windows.net:1433;database=r2schoolss;user=system@mychatdata;password=tiger@23;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30";
         String user = "system";
         String password = "tiger@23";
        
        return DriverManager.getConnection(jdbcUrl, user, password);
   }

	
}
