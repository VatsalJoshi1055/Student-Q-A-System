package databasePart1;

import application.AdminRequest;
import application.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, invitation codes, and
 * (new) instructor → admin request tracking.
 */
public class DatabaseHelper {

    // ─── JDBC CONFIG ────────────────────────────────────────────────────────────────
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL      = "jdbc:h2:./mydb";
    static final String USER        = "sa";
    static final String PASS        = "";

    private Connection connection;
    private Statement  statement;

    // ─── DATABASE BOOTSTRAP ────────────────────────────────────────────────────────
    public void connectToDatabase() throws SQLException {
        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("Connecting to database...");
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement  = connection.createStatement();
			// You can use this command to clear the database and restart from fresh.
			//statement.execute("DROP ALL OBJECTS");

            createTables();
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
    	// Core user table ---------------------------------------------------------
        String userTable = """
                CREATE TABLE IF NOT EXISTS cse360users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    userName VARCHAR(255) UNIQUE,
                    password VARCHAR(255),
                    role VARCHAR(20),
                    l_d_difference INT,
                    num_ratings INT,
                    reviewer_request BOOLEAN DEFAULT FALSE,
                    isBanned BOOLEAN DEFAULT FALSE
                )
            """;
            statement.execute(userTable);

        // Invitation codes --------------------------------------------------------
        statement.execute("""
            CREATE TABLE IF NOT EXISTS InvitationCodes (
                code VARCHAR(10) PRIMARY KEY,
                isUsed BOOLEAN DEFAULT FALSE
            )
        """);

        // Chat history ------------------------------------------------------------
        statement.execute("""
            CREATE TABLE IF NOT EXISTS chat_messages (
                chat_Id INT AUTO_INCREMENT PRIMARY KEY,
                student_Id VARCHAR(300),
                comment VARCHAR(1000),
                thread_Id INT,
                reviewer VARCHAR(300)
            )
        """);

        // Trusted‑reviewer relationships -----------------------------------------
        statement.execute("""
            CREATE TABLE IF NOT EXISTS trusted_relations (
                studentName  VARCHAR(255),
                reviewerName VARCHAR(255),
                PRIMARY KEY (studentName, reviewerName)
            )
        """);

        // NEW: Admin‑request tracking table ---------------------------------------
        statement.execute("""
            CREATE TABLE IF NOT EXISTS admin_requests (
                id IDENTITY PRIMARY KEY,
                description VARCHAR(1000),
                status VARCHAR(20) DEFAULT 'OPEN',
                created_by VARCHAR(255),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                closed_by VARCHAR(255),
                closed_message VARCHAR(1000),
                closed_at TIMESTAMP,
                parent_request_id INT
            )
        """);

        //createAdminUserIfNotExists();
    }

    private void createAdminUserIfNotExists() throws SQLException {
        try (ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM cse360users WHERE role='admin'")) {
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO cse360users (userName, password, role) VALUES (?,?,?)")) {
                    ps.setString(1, "admin");
                    ps.setString(2, "admin123");
                    ps.setString(3, "admin");
                    ps.executeUpdate();
                }
            }
        }
    }

    // ===== Trusted‑reviewer helpers =============================================
    public void markReviewerAsTrusted(String student, String reviewer) {
        String check = "SELECT COUNT(*) FROM trusted_relations WHERE studentName=? AND reviewerName=?";
        String insert= "INSERT INTO trusted_relations (studentName, reviewerName) VALUES (?,?)";
        try (PreparedStatement ck = connection.prepareStatement(check)) {
            ck.setString(1, student);
            ck.setString(2, reviewer);
            try (ResultSet rs = ck.executeQuery()) {
                if (rs.next() && rs.getInt(1)==0) {
                    try (PreparedStatement ins = connection.prepareStatement(insert)) {
                        ins.setString(1, student);
                        ins.setString(2, reviewer);
                        ins.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
    public boolean isReviewerTrusted(String student, String reviewer) {
        String sql = "SELECT COUNT(*) FROM trusted_relations WHERE studentName=? AND reviewerName=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, student);
            ps.setString(2, reviewer);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1)>0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ======== User‑management utils ============================================
    public boolean isDatabaseEmpty() throws SQLException {
        try (ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM cse360users")) {
            return rs.next() && rs.getInt(1)==0;
        }
    }

    public void register(User user) throws SQLException {
        String sql="INSERT INTO cse360users (userName,password,role) VALUES (?,?,?)";
        try (PreparedStatement ps=connection.prepareStatement(sql)) {
            ps.setString(1, user.getUserName());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            ps.executeUpdate();
        }
    }

    public boolean login(User user) throws SQLException {
        String sql="SELECT 1 FROM cse360users WHERE userName=? AND password=? AND role=?";
        try (PreparedStatement ps=connection.prepareStatement(sql)) {
            ps.setString(1, user.getUserName());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            try (ResultSet rs=ps.executeQuery()) { return rs.next(); }
        }
    }

    public boolean doesUserExist(String uname) {
        String sql="SELECT COUNT(*) FROM cse360users WHERE userName=?";
        try (PreparedStatement ps=connection.prepareStatement(sql)) {
            ps.setString(1, uname);
            try(ResultSet rs=ps.executeQuery()) { return rs.next() && rs.getInt(1)>0; }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public String getUserRole(String uname) {
        String sql="SELECT role FROM cse360users WHERE userName=?";
        try (PreparedStatement ps=connection.prepareStatement(sql)) {
            ps.setString(1, uname);
            try (ResultSet rs = ps.executeQuery()) { return rs.next()? rs.getString("role") : null; }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // Invitation codes -----------------------------------------------------------
    public String generateInvitationCode() {
        String code = UUID.randomUUID().toString().substring(0,4);
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO InvitationCodes (code) VALUES (?)")) {
            ps.setString(1, code);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return code;
    }
    public boolean validateInvitationCode(String code) {
        String sql="SELECT 1 FROM InvitationCodes WHERE code=? AND isUsed=FALSE";
        try (PreparedStatement ps=connection.prepareStatement(sql)) {
            ps.setString(1, code);
            try(ResultSet rs=ps.executeQuery()) {
                if (rs.next()) { markInvitationCodeAsUsed(code); return true; }
            }
        } catch(SQLException e){ e.printStackTrace(); }
        return false;
    }
    private void markInvitationCodeAsUsed(String code) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE InvitationCodes SET isUsed=TRUE WHERE code=?")) {
            ps.setString(1, code);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Role‑change helpers --------------------------------------------------------
    public boolean changeUserRole(String userName, String newRole) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE cse360users SET role=? WHERE userName=?")) {
            ps.setString(1, newRole);
            ps.setString(2, userName);
            return ps.executeUpdate()>0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // Reviewer‑request workflow --------------------------------------------------
    public boolean requestReviewer(String uname) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE cse360users SET reviewer_request=TRUE WHERE userName=? AND reviewer_request=FALSE")) {
            ps.setString(1, uname);
            return ps.executeUpdate()>0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
    public List<String> getReviewerRequests() {
        List<String> list=new ArrayList<>();
        try (PreparedStatement ps=connection.prepareStatement(
                "SELECT userName FROM cse360users WHERE reviewer_request=TRUE");
             ResultSet rs=ps.executeQuery()) {
            while(rs.next()) list.add(rs.getString(1));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
    public boolean approveReviewer(String uname) {
        try (PreparedStatement ps=connection.prepareStatement(
                "UPDATE cse360users SET role='reviewer', reviewer_request=FALSE WHERE userName=?")) {
            ps.setString(1, uname);
            return ps.executeUpdate()>0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
    public boolean denyReviewer(String uname) {
        try (PreparedStatement ps=connection.prepareStatement(
                "UPDATE cse360users SET reviewer_request=FALSE WHERE userName=?")) {
            ps.setString(1, uname);
            return ps.executeUpdate()>0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ===== NEW: Instructor → Admin request helpers ==============================
    public long createAdminRequest(String description, String instructor) {
        String sql="INSERT INTO admin_requests (description, created_by) VALUES (?,?)";
        try (PreparedStatement ps=connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, description);
            ps.setString(2, instructor);
            ps.executeUpdate();
            try(ResultSet rs=ps.getGeneratedKeys()) { if(rs.next()) return rs.getLong(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public List<AdminRequest> getAdminRequests(boolean includeClosed) {
        List<AdminRequest> list=new ArrayList<>();
        String sql="SELECT * FROM admin_requests" + (includeClosed?"":" WHERE status='OPEN'");
        try (PreparedStatement ps=connection.prepareStatement(sql);
             ResultSet rs=ps.executeQuery()) {
            while(rs.next()) list.add(mapRowToAdminRequest(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean closeAdminRequest(long id, String admin, String message) {
        String sql="UPDATE admin_requests SET status='CLOSED', closed_by=?, closed_message=?, closed_at=CURRENT_TIMESTAMP WHERE id=? AND status='OPEN'";
        try (PreparedStatement ps=connection.prepareStatement(sql)) {
            ps.setString(1, admin);
            ps.setString(2, message);
            ps.setLong(3, id);
            return ps.executeUpdate()>0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean reopenAdminRequest(long closedId, String instructor, String newDesc) {
        // ensure target is closed
        String check="SELECT 1 FROM admin_requests WHERE id=? AND status='CLOSED'";
        try (PreparedStatement ps=connection.prepareStatement(check)) {
            ps.setLong(1, closedId);
            try(ResultSet rs=ps.executeQuery()) { if(!rs.next()) return false; }
        } catch (SQLException e) { e.printStackTrace(); return false; }

        String sql="INSERT INTO admin_requests (description, created_by, parent_request_id) VALUES (?,?,?)";
        try (PreparedStatement ps=connection.prepareStatement(sql)) {
            ps.setString(1, newDesc);
            ps.setString(2, instructor);
            ps.setLong(3, closedId);
            return ps.executeUpdate()>0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private AdminRequest mapRowToAdminRequest(ResultSet rs) throws SQLException {
        return new AdminRequest(
                rs.getLong("id"),
                rs.getString("description"),
                rs.getString("status"),
                rs.getString("created_by"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getString("closed_by"),
                rs.getString("closed_message"),
                rs.getTimestamp("closed_at") == null ? null : rs.getTimestamp("closed_at").toLocalDateTime(),
                rs.getObject("parent_request_id") == null ? null : rs.getLong("parent_request_id"));
    }
    
  //marks a user as banned in the database
  	public void userBanned(String username) throws SQLException {
  		String query = "UPDATE cse360users SET isBanned = TRUE WHERE userName = ?";
  		try(PreparedStatement ps = connection.prepareStatement(query)){
  			ps.setString(1, username);
  			ps.executeUpdate();
  		} catch (SQLException e) {
  	        e.printStackTrace();
  	    }
  	}
  	
  	//checks if the user is banned when they log in
  //checks if the user is banned when they log in
    public boolean isUserBanned(User user) {
    if(user.getRole().equals("admin") || user.getRole().equals("staff")) {
    return false;
    }
    
    String query = "SELECT isBanned FROM cse360users WHERE userName = ?";
    try(PreparedStatement ps = connection.prepareStatement(query)){
    ps.setString(1, user.getUserName());
    ResultSet rs = ps.executeQuery();
    
    if(rs.next()) {
    return rs.getBoolean("isBanned");
    }
    } catch (SQLException e) {
            e.printStackTrace();
        }
    
    return false;
    }

  	
  //gets a list of chatters based on the role that is searched
  	public List<String> getChatters(String searchedRole){
  		List<String> reviewers = new ArrayList<>();
  		String query = "SELECT userName FROM cse360users WHERE role = ?";
  		try(PreparedStatement ps = connection.prepareStatement(query)){
  			ps.setString(1, searchedRole);
  			ResultSet rs = ps.executeQuery();
  			
  			while(rs.next()) {
  				String chatterName = rs.getString("userName");
  				reviewers.add(chatterName);
  			}
  		} catch (SQLException e) {
  	        e.printStackTrace();
  	    }
  		
  		return reviewers;
  	}
  	
  	//inserts a message into the chat log
  	public void insertMessage(String sender, String receiver, String message) {
  		String query = "INSERT INTO chat_messages (student_ID, reviewer, comment) VALUES (?, ?, ?)";
          try (PreparedStatement pstmt = connection.prepareStatement(query)) {
              pstmt.setString(1, sender);
              pstmt.setString(2, receiver);
              pstmt.setString(3, message);
              pstmt.executeUpdate();
          } catch (SQLException e) {
          	e.printStackTrace();
          }
  	}
  	
  	//gathers all the messages from a specific chat log
  	public List<String> getChat(String user1, String user2, String currentUser){
  		List<String> messages = new ArrayList<>();
  		
  		String sql = "SELECT student_ID, comment FROM chat_messages WHERE (student_ID = ? AND reviewer = ?) OR (student_ID = ? AND reviewer = ?)";
          try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
              pstmt.setString(1, user1);
              pstmt.setString(2, user2);
              pstmt.setString(3, user2);
              pstmt.setString(4, user1);
              ResultSet rs = pstmt.executeQuery();
              while (rs.next()) {
                  String sender = rs.getString("student_ID");
                  String msg = rs.getString("comment");
                  messages.add(sender.equals(currentUser) ? "You: " + msg : sender + ": " + msg);
              }
          } catch (SQLException e){
          	 e.printStackTrace();
          }
          
  		return messages;
  	}

    // ===== House‑keeping ========================================================
    public void closeConnection() {
        try { if(statement!=null) statement.close(); } catch (SQLException ignored) {}
        try { if(connection!=null) connection.close(); } catch (SQLException ignored) {}
    }
    
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT userName, role FROM cse360users";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String username = rs.getString("userName");
                String role = rs.getString("role");
                users.add(new User(username, "", role)); // password left blank
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

}
