package databasePart1;

import application.Answer;
import application.PrivateMessage;
import application.Question;
import application.User;
import application.Review;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, and handling invitation codes.
 * CHANGES MADE:
 * added new table userRoles to store multiple roles per user
 * updated 'register(User user)' to insert roles into both 'cse360users' and 'userRoles'
 * updated 'login(User user) to load all roles from 'userRoles' instead of just one
 *	(if no entry exists in userRoles, fallback to single role in cse360users)
 * added helper method 'getUserRoles(String userName)' to retrieve multiple roles
 */
public class DatabaseHelper {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase;AUTO_SERVER=TRUE"; 

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	private Connection connection = null;
	private Statement statement = null; 
	//	PreparedStatement pstmt

	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement(); 
			// You can use this command to clear the database and restart from fresh.
			//statement.execute("DROP ALL OBJECTS");

			createTables();  // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
			throw new SQLException("JDBC Driver not found", e);
		}
	}
	// Admin sets a one-time password for a user who forgot theirs
	public boolean setOneTimePassword(String userName, String otp, Timestamp expiration) {
		String query = "INSERT INTO OneTimePasswords (userName, otp, expiration) VALUES (?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, userName);
			pstmt.setString(2, otp);
			pstmt.setTimestamp(3, expiration);
			pstmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	// Validate a one-time password for a user (not expired, not used)
	public boolean validateOneTimePassword(String userName, String otp) {
		String query = "SELECT expiration FROM OneTimePasswords WHERE userName = ? AND otp = ? AND isUsed = FALSE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, userName);
			pstmt.setString(2, otp);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				Timestamp expiration = rs.getTimestamp("expiration");
				if (expiration != null && expiration.after(new Timestamp(System.currentTimeMillis()))) {
					markOneTimePasswordAsUsed(userName, otp);
					return true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// Mark a one-time password as used
	private void markOneTimePasswordAsUsed(String userName, String otp) {
		String query = "UPDATE OneTimePasswords SET isUsed = TRUE WHERE userName = ? AND otp = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, userName);
			pstmt.setString(2, otp);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	private void createTables() throws SQLException {
		String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "userName VARCHAR(255) UNIQUE, "
	            + "password VARCHAR(255), "
	            + "name VARCHAR(255), "
	            + "email VARCHAR(255), "
	            + "role VARCHAR(20))";
	    statement.execute(userTable);
	  //new table for multiple roles
	    String userRolesTable = "CREATE TABLE IF NOT EXISTS UserRoles ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "userName VARCHAR(255), "
				+ "role VARCHAR(20),"
				+ "FOREIGN KEY (userName) REFERENCES cse360users(userName))";
		statement.execute(userRolesTable);

		// Create the invitation codes table
		String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
			+ "code VARCHAR(10) PRIMARY KEY, "
			+ "email VARCHAR(255), "
			+ "expiration TIMESTAMP, "
			+ "isUsed BOOLEAN DEFAULT FALSE)";
		statement.execute(invitationCodesTable);

		// Create private messages table
		String messagesTable = "CREATE TABLE IF NOT EXISTS PrivateMessages ("
			+ "id INT AUTO_INCREMENT PRIMARY KEY, "
			+ "questionId VARCHAR(64), "
			+ "fromUser VARCHAR(255), "
			+ "toUser VARCHAR(255), "
			+ "content VARCHAR(2000), "
			+ "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
			+ "isRead BOOLEAN DEFAULT FALSE)";
		statement.execute(messagesTable);

		// Create the one-time passwords table
		String otpTable = "CREATE TABLE IF NOT EXISTS OneTimePasswords ("
			+ "userName VARCHAR(255), "
			+ "otp VARCHAR(255), "
			+ "expiration TIMESTAMP, "
			+ "isUsed BOOLEAN DEFAULT FALSE, "
			+ "PRIMARY KEY(userName, otp))";
		statement.execute(otpTable);
		
		
		String questionsTable = "CREATE TABLE IF NOT EXISTS questions ("
		    + "questionId VARCHAR(50) PRIMARY KEY, "
		    + "title VARCHAR(500) NOT NULL, "
		    + "content TEXT, "
		    + "author VARCHAR(255), "
		    + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
		    + "followUpOf VARCHAR(50),"
		    + "isResolved BOOLEAN DEFAULT FALSE, "
		    + "FOREIGN KEY (author) REFERENCES cse360users(userName),"
		 	+ "FOREIGN KEY (followUpOf) REFERENCES questions(questionId))";
		statement.execute(questionsTable);

		String questionTagsTable = "CREATE TABLE IF NOT EXISTS question_tags ("
		    + "questionId VARCHAR(50), "
		    + "tag VARCHAR(100), "
		    + "PRIMARY KEY (questionId, tag), "
		    + "FOREIGN KEY (questionId) REFERENCES questions(questionId))";
		statement.execute(questionTagsTable);

		String answersTable = "CREATE TABLE IF NOT EXISTS answers ("
		    + "answerId VARCHAR(50) PRIMARY KEY, "
		    + "questionId VARCHAR(50), "
		    + "content VARCHAR(50), "
		    + "author VARCHAR(255), "
		    + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
		    + "isAccepted BOOLEAN DEFAULT FALSE, "
		    + "isRead BOOLEAN DEFAULT FALSE, "
		    + "FOREIGN KEY (questionId) REFERENCES questions(questionId), "
		    + "FOREIGN KEY (author) REFERENCES cse360users(userName))";
		statement.execute(answersTable);
		
		//Shows a table for the pending requests
		String reviewerTables = "CREATE TABLE IF NOT EXISTS reviewer_requests ("
				+ "requestId TEXT PRIMARY KEY,"
				+ "studentUserName VARCHAR(255),"
				+ "status TEXT DEFAULT 'Pending',"
				+ "requestDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
				+ "FOREIGN KEY(studentUserName) REFERENCES cse360users(userName))";
		statement.execute(reviewerTables);
		
		//Shows a table of answer for the reviewer
		String answerReviews = "CREATE TABLE IF NOT EXISTS answer_reviews ("
				+ "reviewId VARCHAR(50) PRIMARY KEY,"
				+ "answerId VARCHAR(50),"
				+ "reviewerUserName VARCHAR(255),"
				+ "reviewContent TEXT NOT NULL,"
				+ "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
				+ "FOREIGN KEY (answerId) REFERENCES answers(answerId),"
				+ "FOREIGN KEY (reviewerUserName) REFERENCES cse360users(userName))";
		statement.execute(answerReviews);
				
	}


	// Check if the database is empty
	public boolean isDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM cse360users";
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

	// Registers a new user in the database.
	public void register(User user) throws SQLException {
		String insertUser = "INSERT INTO cse360users (userName, password, name, email, role) VALUES (?,?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
			pstmt.setString(1, user.getUserName());
	        pstmt.setString(2, user.getPassword());
	        pstmt.setString(3, user.getUserInfoName());  
	        pstmt.setString(4, user.getEmail());  
	        pstmt.setString(5, user.getRole());
	        pstmt.executeUpdate();
		}
	
	//insert into userRoles for full role list
			String insertRole = "INSERT INTO UserRoles (userName, role) VALUES (?,?)";
			try (PreparedStatement pstmt = connection.prepareStatement(insertRole)) {
		        for (String role : user.getRoles()) {
		            pstmt.setString(1, user.getUserName());
		            pstmt.setString(2, role);
		            pstmt.executeUpdate();
		        }
			}
		}
	
	// Loads all existing roles unto getRoles()
	public List<String> getRolesForUser(String userName) throws SQLException {
	    List<String> roles = new ArrayList<>();
	    String sql = "SELECT role FROM user_roles WHERE userName = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        stmt.setString(1, userName);
	        try (ResultSet rs = stmt.executeQuery()) {
	            while (rs.next()) {
	                roles.add(rs.getString("role"));
	            }
	        }
	    }
	    return roles;
	}

	// Validates a user's login credentials.
	public boolean login(User user) throws SQLException {
		String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ? AND role = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getRole());
			try (ResultSet rs = pstmt.executeQuery()) {
				if(rs.next()) {
					//login successful

					//step 2: clear and load roles from UserRoles
					user.getRoles().clear();
					List<String>roles = getUserRoles(user.getUserName());
					if(!roles.isEmpty()) {
						for (String role: roles) {
							user.addRole(role);
						} 
					}else {
						//fallback: is UserRoles is empty, use cse360users.role
							String singleRole = rs.getString("role");
							if(singleRole != null ) {
								user.addRole(singleRole);
							}

					}
					return true;
				}
			}
		}
		return false;
	}
	
	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            // If the count is greater than 0, the user exists
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // If an error occurs, assume user doesn't exist
	}
	
	// Retrieves the role of a user from the database using their UserName.
	public String getUserRole(String userName) {
	    String query = "SELECT role FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("role"); // Return the role if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null; // If no user exists or an error occurs
	}
	//Retrieves multiple roles from user from database using their Username.
		public List<String> getUserRoles(String userName){
			List<String> roles = new ArrayList<>();
			String query = "SELECT role FROM UserRoles WHERE userName = ?";
			try(PreparedStatement pstmt = connection.prepareStatement(query)){
				pstmt.setString(1,userName);
				ResultSet rs = pstmt.executeQuery();

				while(rs.next()) {
					String role = rs.getString("role");
					roles.add(role);


				}
				return roles;

			} catch (SQLException e) {
				e.printStackTrace();
			}
			return roles;
		}

	// Generates a new invitation code, associates it with an email and expiration, and inserts it into the database.
	public String generateInvitationCode(String email, Timestamp expiration) {
		if (!isValidEmail(email)) {
			throw new IllegalArgumentException("Invalid email format");
		}
		String code = UUID.randomUUID().toString().substring(0, 6); // 6-char code
		String query = "INSERT INTO InvitationCodes (code, email, expiration) VALUES (?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, code);
			pstmt.setString(2, email);
			pstmt.setTimestamp(3, expiration);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return code;
	}

	// Validates email format (simple regex)
	public static boolean isValidEmail(String email) {
		return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
	}

	// Validates expiration date string (yyyy-MM-dd HH:mm)
	public static boolean isValidExpiration(String expiration) {
		return expiration != null && expiration.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$");
	}
	
	// Validates an invitation code to check if it is unused.
	// Validates an invitation code to check if it is unused and not expired.
	public boolean validateInvitationCode(String code) {
		String query = "SELECT expiration FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, code);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				Timestamp expiration = rs.getTimestamp("expiration");
				if (expiration != null && expiration.after(new Timestamp(System.currentTimeMillis()))) {
					// Mark the code as used
					markInvitationCodeAsUsed(code);
					return true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	// Marks the invitation code as used in the database.
	private void markInvitationCodeAsUsed(String code) {
	    String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	// Closes the database connection and statement.
	public void closeConnection() {
		try{ 
			if(statement!=null) statement.close(); 
		} catch(SQLException se2) { 
			se2.printStackTrace();
		} 
		try { 
			if(connection!=null) connection.close(); 
		} catch(SQLException se){ 
			se.printStackTrace(); 
		} 
	}
	
	// Method to get all users
	public List<User> getAllUsers() throws SQLException {
		List<User> users = new ArrayList<>();
		String query = "SELECT * FROM cse360users";
		try (PreparedStatement pstmt = connection.prepareStatement(query);
				ResultSet rs = pstmt.executeQuery()) {
			while (rs.next()) {
				User user = new User(
						rs.getString("userName"),
						rs.getString("password"),
						rs.getString("name"),
						rs.getString("email"),
						rs.getString("role") // we can add other fields as needed here
				);
				users.add(user);
			}
		}
		return users;
	}
	
	// Method to delete a user
	public boolean deleteUser(String userName, String currentAdmin) throws SQLException {
		// Prevent admin from deleting themselves
		if (userName.equals(currentAdmin)) {
			return false;
		}
		
		String query = "DELETE FROM cse360users WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1,  userName);
			return pstmt.executeUpdate() > 0;
		}
	}
	
	// Method to update user roles
	public boolean updateUserRole(String userName, String newRole, String currentAdmin) throws SQLException {
		// Prevent an admin from removing their own admin role
		if (userName.equals(currentAdmin) && !newRole.contains("admin")) {
			// Check if there's at least one other admin
			if (countAdmins() <= 1) {
				return false;
			}
		}
		
		String query = "UPDATE cse360users SET role = ? WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, newRole);
			pstmt.setString(2,  userName);
			return pstmt.executeUpdate() > 0;
		}
	}
	
	private int countAdmins() throws SQLException {
		String query = "SELECT COUNT(*) FROM cse360users WHERE role = 'admin'";
		try (PreparedStatement pstmt = connection.prepareStatement(query);
				ResultSet rs = pstmt.executeQuery()) {
			if (rs.next()) {
				return rs.getInt(1);
			}
		}
		return 0;
	}
	
	public boolean updateUserRoles(String userName, List<String> newRoles, String currentAdmin) throws SQLException {
	    // Prevent an admin from removing their own admin role if they're the last admin
	    if (userName.equals(currentAdmin) && !newRoles.contains("admin")) {
	        if (countAdmins() <= 1) {
	            return false;
	        }
	    }
	    
	    // Use transaction to ensure consistency
	    connection.setAutoCommit(false);
	    try {
	        // Delete all existing roles for this user
	        String deleteQuery = "DELETE FROM UserRoles WHERE userName = ?";
	        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
	            deleteStmt.setString(1, userName);
	            deleteStmt.executeUpdate();
	        }
	        
	        // Insert the new roles
	        String insertQuery = "INSERT INTO UserRoles (userName, role) VALUES (?, ?)";
	        try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
	            for (String role : newRoles) {
	                insertStmt.setString(1, userName);
	                insertStmt.setString(2, role);
	                insertStmt.executeUpdate();
	            }
	        }
	        
	        // Update the primary role in cse360users (use the first role)
	        String updateQuery = "UPDATE cse360users SET role = ? WHERE userName = ?";
	        try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
	            updateStmt.setString(1, newRoles.isEmpty() ? null : newRoles.get(0));
	            updateStmt.setString(2, userName);
	            updateStmt.executeUpdate();
	        }
	        
	        connection.commit();
	        return true;
	    } catch (SQLException e) {
	        connection.rollback();
	        throw e;
	    } finally {
	        connection.setAutoCommit(true);
	    }
	}
	
	//ADDED HW2
	// Question-related methods
	public boolean insertQuestion(Question question) throws SQLException {
	    String query = "INSERT INTO questions (questionId, title, content, author, createdAt, followUpOf, isResolved) VALUES (?, ?, ?, ?, ?, ?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, question.getQuestionId());
	        pstmt.setString(2, question.getTitle());
	        pstmt.setString(3, question.getContent());
	        pstmt.setString(4, question.getAuthor());
	        pstmt.setTimestamp(5, Timestamp.valueOf(java.time.LocalDateTime.now()));
	        pstmt.setObject(6, question.getFollowUpOf());
	        pstmt.setBoolean(7, question.getIsResolved());
	        
	        int rowsAffected = pstmt.executeUpdate();
	        
	        // Insert tags
	        if (rowsAffected > 0) {
	            insertQuestionTags(question);
	        }
	        
	        return rowsAffected > 0;
	    }
	}

	private void insertQuestionTags(Question question) throws SQLException {
	    String deleteQuery = "DELETE FROM question_tags WHERE questionId = ?";
	    try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
	        deleteStmt.setString(1, question.getQuestionId());
	        deleteStmt.executeUpdate();
	    }
	    
	    String insertQuery = "INSERT INTO question_tags (questionId, tag) VALUES (?, ?)";
	    try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
	        for (String tag : question.getTags()) {
	            insertStmt.setString(1, question.getQuestionId());
	            insertStmt.setString(2, tag);
	            insertStmt.executeUpdate();
	        }
	    }
	}

	public boolean updateQuestion(Question question) throws SQLException {
		String query = "UPDATE questions SET title = ?, content = ?, followUpOf = ? WHERE questionId = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
		    pstmt.setString(1, question.getTitle());
		    pstmt.setString(2, question.getContent());
		    pstmt.setObject(3, question.getFollowUpOf());
		    pstmt.setString(4, question.getQuestionId());
	        
	        int rowsAffected = pstmt.executeUpdate();
	        
	        // Update tags
	        if (rowsAffected > 0) {
	            insertQuestionTags(question);
	        }
	        
	        return rowsAffected > 0;
	    }
	}

	public boolean deleteQuestion(String questionId) throws SQLException {
	    String deleteAnswersQuery = "DELETE FROM answers WHERE questionId = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(deleteAnswersQuery)) {
	        pstmt.setString(1, questionId);
	        pstmt.executeUpdate();
	    }
	    
	    String deleteTagsQuery = "DELETE FROM question_tags WHERE questionId = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(deleteTagsQuery)) {
	        pstmt.setString(1, questionId);
	        pstmt.executeUpdate();
	    }
	    
	    String deleteQuestionQuery = "DELETE FROM questions WHERE questionId = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(deleteQuestionQuery)) {
	        pstmt.setString(1, questionId);
	        return pstmt.executeUpdate() > 0;
	    }
	}

	public List<Question> getAllQuestions() throws SQLException {
	    List<Question> questions = new ArrayList<>();
	    String query = "SELECT * FROM questions ORDER BY createdAt DESC";
	    
	    try (PreparedStatement pstmt = connection.prepareStatement(query);
	         ResultSet rs = pstmt.executeQuery()) {
	        
	        while (rs.next()) {
	            Question question = new Question(
	                rs.getString("questionId"),
	                rs.getString("title"),
	                rs.getString("content"),
	                rs.getString("author"),
	                rs.getTimestamp("createdAt"),  // Use toString()
	                rs.getString("followUpOf")
	            );
	            
	            question.setIsResolved(rs.getBoolean("isResolved"));
	            
	            // Load tags for this question
	            loadQuestionTags(question);
	            questions.add(question);
	        }
	    }
	    return questions;
	}

	public List<Question> searchQuestionsByTitle(String keyword) throws SQLException {
	    List<Question> questions = new ArrayList<>();
	    String query = "SELECT * FROM questions WHERE LOWER(title) LIKE LOWER(?) ORDER BY createdAt DESC";
	    
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, "%" + keyword + "%");
	        
	        try (ResultSet rs = pstmt.executeQuery()) {
	            while (rs.next()) {
	                Question question = new Question(
	                    rs.getString("questionId"),
	                    rs.getString("title"),
	                    rs.getString("content"),
	                    rs.getString("author"),
	                    rs.getTimestamp("createdAt")  // Use toString()
	                );
		            question.setIsResolved(rs.getBoolean("isResolved"));
	                loadQuestionTags(question);
	                questions.add(question);
	            }
	        }
	    }
	    return questions;
	}
	
	public List<Question> searchQuestionsByAuthor(String author) throws SQLException {
	    List<Question> questions = new ArrayList<>();
	    String query = "SELECT * FROM questions WHERE author = ? ORDER BY createdAt DESC";
	    
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, author);
	        
	        try (ResultSet rs = pstmt.executeQuery()) {
	            while (rs.next()) {
	                Question question = new Question(
	                    rs.getString("questionId"),
	                    rs.getString("title"),
	                    rs.getString("content"),
	                    rs.getString("author"),
	                    rs.getTimestamp("createdAt")
	                );
		            question.setIsResolved(rs.getBoolean("isResolved"));
	                loadQuestionTags(question);
	                questions.add(question);
	            }
	        }
	    }
	    return questions;
	}

	public List<Question> searchQuestionsByContent(String keyword) throws SQLException {
	    List<Question> questions = new ArrayList<>();
	    String query = "SELECT * FROM questions WHERE LOWER(content) LIKE LOWER(?) ORDER BY createdAt DESC";
	    
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, "%" + keyword + "%");
	        
	        try (ResultSet rs = pstmt.executeQuery()) {
	            while (rs.next()) {
	                Question question = new Question(
	                    rs.getString("questionId"),
	                    rs.getString("title"),
	                    rs.getString("content"),
	                    rs.getString("author"),
	                    rs.getTimestamp("createdAt")
	                );
		            question.setIsResolved(rs.getBoolean("isResolved"));
	                loadQuestionTags(question);
	                questions.add(question);
	            }
	        }
	    }
	    return questions;
	}

	
	private void loadQuestionTags(Question question) throws SQLException {
	    String query = "SELECT tag FROM question_tags WHERE questionId = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, question.getQuestionId());
	        
	        try (ResultSet rs = pstmt.executeQuery()) {
	            while (rs.next()) {
	                question.addTag(rs.getString("tag"));
	            }
	        }
	    }
	}
	

	// Answer-related methods
	public boolean insertAnswer(Answer answer) throws SQLException {
	    String query = "INSERT INTO answers (answerId, questionId, content, author, createdAt, isAccepted, isRead) VALUES (?, ?, ?, ?, ?, ?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, answer.getAnswerId());
	        pstmt.setString(2, answer.getQuestionId());
	        pstmt.setString(3, answer.getContent());
	        pstmt.setString(4, answer.getAuthor());
	        pstmt.setTimestamp(5, Timestamp.valueOf(java.time.LocalDateTime.now()));
	        pstmt.setBoolean(6, answer.getIsAccepted());
	        pstmt.setBoolean(7, false);
	        
	        return pstmt.executeUpdate() > 0;
	    }
	}

	public boolean updateAnswer(Answer answer) throws SQLException {
	    String query = "UPDATE answers SET content = ?, isAccepted = ? WHERE answerId = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, answer.getContent());
	        pstmt.setBoolean(2, answer.getIsAccepted());
	        pstmt.setString(3, answer.getAnswerId());
	        
	        return pstmt.executeUpdate() > 0;
	    }
	}

	public boolean deleteAnswer(String answerId) throws SQLException {
	    String query = "DELETE FROM answers WHERE answerId = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, answerId);
	        return pstmt.executeUpdate() > 0;
	    }
	}

	public List<Answer> getAllAnswers() throws SQLException {
	    List<Answer> answers = new ArrayList<>();
	    String query = "SELECT * FROM answers ORDER BY createdAt DESC";
	    
	    try (PreparedStatement pstmt = connection.prepareStatement(query);
	         ResultSet rs = pstmt.executeQuery()) {
	        
	        while (rs.next()) {
	            Answer answer = new Answer(
	                rs.getString("answerId"),
	                rs.getString("questionId"),
	                rs.getString("content"),
	                rs.getString("author"),
	                rs.getTimestamp("createdAt").toString(),
	                rs.getBoolean("isAccepted")
	            );
	            answer.setIsRead(rs.getBoolean("isRead"));
	            answers.add(answer);
	        }
	    }
	    return answers;
	}

	public List<Answer> getAnswersForQuestion(String questionId) throws SQLException {
	    List<Answer> answers = new ArrayList<>();
	    String query = "SELECT * FROM answers WHERE questionId = ? ORDER BY createdAt DESC";
	    
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, questionId);
	        
	        try (ResultSet rs = pstmt.executeQuery()) {
	            while (rs.next()) {
	                Answer answer = new Answer(
	                    rs.getString("answerId"),
	                    rs.getString("questionId"),
	                    rs.getString("content"),
	                    rs.getString("author"),
	                    rs.getTimestamp("createdAt").toString(),
	                    rs.getBoolean("isAccepted")
	                );
	                answer.setIsRead(rs.getBoolean("isRead"));
	                answers.add(answer);
	            }
	        }
	    }
	    return answers;
	}

	public List<Answer> searchAnswersByContent(String keyword) throws SQLException {
	    List<Answer> answers = new ArrayList<>();
	    String query = "SELECT * FROM answers WHERE LOWER(content) LIKE LOWER(?) ORDER BY createdAt DESC";
	    
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, "%" + keyword + "%");
	        
	        try (ResultSet rs = pstmt.executeQuery()) {
	            while (rs.next()) {
	                Answer answer = new Answer(
	                    rs.getString("answerId"),
	                    rs.getString("questionId"),
	                    rs.getString("content"),
	                    rs.getString("author"),
	                    rs.getTimestamp("createdAt").toString(),
	                    rs.getBoolean("isAccepted")
	                );
	                answer.setIsRead(rs.getBoolean("isRead"));
	                answers.add(answer);
	            }
	        }
	    }
	    return answers;
	}
	
	// Count the unread answers
	public int countUnreadAnswers(String questionId, String author) throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM answers "
					+ "WHERE questionId = ? AND isRead = FALSE "
					+ "AND questionId IN (SELECT questionId FROM questions WHERE author = ?)";
		try(PreparedStatement pstmt =  connection.prepareStatement(query)) {
			pstmt.setString(1, questionId);
			pstmt.setString(2, author);
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) return rs.getInt("count");
		}
		return 0;
	}
	
	// Mark answers as read
	public void markAnswersAsRead(String questionId, String author) throws SQLException {
		String query = "UPDATE answers SET isRead = TRUE "
				+ "WHERE questionId = ? AND questionId IN "
				+ "(SELECT questionId FROM questions WHERE author = ?)";
		try(PreparedStatement pstmt =  connection.prepareStatement(query)) {
			pstmt.setString(1, questionId);
			pstmt.setString(2, author);
			pstmt.executeUpdate();
		}
	}  

	// Utility method to check if a question exists
	public boolean doesQuestionExist(String questionId) throws SQLException {
	    String query = "SELECT COUNT(*) FROM questions WHERE questionId = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, questionId);
	        try (ResultSet rs = pstmt.executeQuery()) {
	            return rs.next() && rs.getInt(1) > 0;
	        }
	    }
	}

	// Private messaging methods
	public boolean insertPrivateMessage(String questionId, String fromUser, String toUser, String content) throws SQLException {
		String query = "INSERT INTO PrivateMessages (questionId, fromUser, toUser, content) VALUES (?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, questionId);
			pstmt.setString(2, fromUser);
			pstmt.setString(3, toUser);
			pstmt.setString(4, content);
			return pstmt.executeUpdate() > 0;
		}
	}

	public int getUnreadCountForQuestion(String questionId, String userName) throws SQLException {
		String query = "SELECT COUNT(*) FROM PrivateMessages WHERE questionId = ? AND toUser = ? AND isRead = FALSE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, questionId);
			pstmt.setString(2, userName);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) return rs.getInt(1);
			}
		}
		return 0;
	}

	public List<PrivateMessage> getMessagesForQuestion(String questionId, String forUser) throws SQLException {
		List<PrivateMessage> messages = new ArrayList<>();
		String query = "SELECT * FROM PrivateMessages WHERE questionId = ? AND (toUser = ? OR fromUser = ?) ORDER BY createdAt ASC";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, questionId);
			pstmt.setString(2, forUser);
			pstmt.setString(3, forUser);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					PrivateMessage msg = new PrivateMessage(
						rs.getInt("id"),
						rs.getString("questionId"),
						rs.getString("fromUser"),
						rs.getString("toUser"),
						rs.getString("content"),
						rs.getTimestamp("createdAt"),
						rs.getBoolean("isRead")
					);
					messages.add(msg);
				}
			}
		}
		return messages;
	}

	public boolean markMessagesRead(String questionId, String userName) throws SQLException {
		String query = "UPDATE PrivateMessages SET isRead = TRUE WHERE questionId = ? AND toUser = ? AND isRead = FALSE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, questionId);
			pstmt.setString(2, userName);
			return pstmt.executeUpdate() > 0;
		}
	}
	
	//Updates the question to resolved
	public boolean updateQuestionResolved(String questionId, boolean isResolved) throws SQLException{
		String query = "UPDATE questions SET isResolved = ? WHERE questionId = ?";
		try(PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setBoolean(1, isResolved);
			pstmt.setString(2, questionId);
			return pstmt.executeUpdate() > 0;
		}
	}
	
	// Checks if the question is unresolved
	public List<Question> getUnresolvedQuestions() throws SQLException{
		List<Question> questions = new ArrayList<>();
		String query = "SELECT * FROM questions WHERE isResolved = FALSE ORDER BY createdAt DESC";
		
		try(PreparedStatement pstmt = connection.prepareStatement(query);
				ResultSet rs = pstmt.executeQuery()) {
			
			while(rs.next()) {
				Question question = new Question(
						rs.getString("questionId"),
						rs.getString("title"),
						rs.getString("content"),
						rs.getString("author"),
						rs.getTimestamp("createdAt")
					);
				
				question.setIsResolved(rs.getBoolean("isResolved"));

                loadQuestionTags(question);
                questions.add(question);
			}
		}
		return questions;
	}
	
	//Student submits a review role request
	public void submitReviewerRequest(String studentUserName) throws SQLException {
	    String checkQuery = "SELECT * FROM reviewer_requests WHERE studentUserName = ? AND status = 'Pending'";
	    try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
	        checkStmt.setString(1, studentUserName);
	        try (ResultSet rs = checkStmt.executeQuery()) {
	            if (rs.next()) {
	                throw new SQLException("You already have a pending request.");
	            }
	        }
	    }

	    String insertQuery = "INSERT INTO reviewer_requests (requestId, studentUserName, status) VALUES (?, ?, 'Pending')";
	    try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
	        pstmt.setString(1, java.util.UUID.randomUUID().toString());
	        pstmt.setString(2, studentUserName);
	        pstmt.executeUpdate();
	    }
	}

	// Instructor retrieves all pending requests
	public List<User> getPendingReviewerRequests() throws SQLException {
	    List<User> requests = new ArrayList<>();
	    String query = "SELECT u.userName, u.password, u.email, u.name, u.role " +
	                   "FROM cse360users AS u " +
	                   "INNER JOIN reviewer_requests AS r " +
	                   "WHERE r.status = 'Pending'";
	    try (PreparedStatement pstmt = connection.prepareStatement(query);
	         ResultSet rs = pstmt.executeQuery()) {
	        while (rs.next()) {
	            requests.add(new User(
	                rs.getString("userName"),
	                rs.getString("password"),
	                rs.getString("email"),
	                rs.getString("name"),
	                rs.getString("role")
	            ));
	        }
	    }
	    return requests;
	}

	// Instructor approves or rejects a reviewer request
	public boolean processReviewerRequest(String studentUserName, boolean approve, String instructorUserName) throws SQLException {
	    
	    if (connection == null || connection.isClosed()) {
	        connectToDatabase();
	    }

	    // Approve request: add 'Reviewer' role to student
	    if (approve) {
	        List<String> roles = getUserRoles(studentUserName);
	        if (!roles.contains("reviewer")) {
	            roles.add("reviewer");
	            updateUserRoles(studentUserName, roles, instructorUserName); // pass instructor username
	        }
	    }

	    // Update reviewer_requests table status
	    String status = approve ? "Approved" : "Rejected";
	    String query = "UPDATE reviewer_requests SET status = ? WHERE studentUserName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, status);
	        pstmt.setString(2, studentUserName);
	        return pstmt.executeUpdate() > 0;
	    }
	}

	// Insert a new review into the database
	public boolean insertReview(Review review) throws SQLException {
	    if (connection == null || connection.isClosed()) {
	    	connectToDatabase();
	    }

	    String query = "INSERT INTO answer_reviews (reviewId, answerId, reviewerUserName, reviewContent) " +
	                   "VALUES (?, ?, ?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, review.getReviewId());
	        pstmt.setString(2, review.getAnswerId());
	        pstmt.setString(3, review.getReviewer());
	        pstmt.setString(4, review.getContent());
	        return pstmt.executeUpdate() > 0;
	    }
	}
	
	public List<Review> getReviewsForAnswer(String answerId) throws SQLException {
	    if (connection == null || connection.isClosed()) {
	        connectToDatabase();
	    }

	    List<Review> reviews = new ArrayList<>();
	    String sql = "SELECT reviewId, answerId, reviewerUserName, reviewContent, createdAt "
	            + "FROM answer_reviews WHERE answerId = ? ORDER BY createdAt ASC";

	    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
	        pstmt.setString(1, answerId);
	        try (ResultSet rs = pstmt.executeQuery()) {
	            while (rs.next()) {
	                Review review = new Review(
	                    rs.getString("reviewId"),
	                    rs.getString("answerId"),
	                    rs.getString("reviewerUserName"),
	                    rs.getString("reviewContent"),
	                    rs.getTimestamp("createdAt")
	                );
	                reviews.add(review);
	            }
	        }
	    }
	    return reviews;
	}

	// TestCases Helpers to run tests
	// Allow tests / other classes to access the underlying JDBC connection
	public Connection getConnection() {
	    return this.connection;
	}

	// Convenience helper for tests to run simple update/delete SQL (cleanup)
	public void executeUpdate(String sql) throws SQLException {
	    if (connection == null || connection.isClosed()) {
	        connectToDatabase();
	    }
	    try (Statement stmt = connection.createStatement()) {
	        stmt.executeUpdate(sql);
	    }
	}

	// Optional helper to insert a user with primitives (uses your register internally)
	public boolean insertUserDirect(String userName, String password, String email, String name, String role) {
	    try {
	        application.User user = new application.User(userName, password, email, name, role);
	        // ensure role list contains the role (User constructor already does)
	        register(user);
	        return true;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
}
