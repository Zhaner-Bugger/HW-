package application;

import java.sql.SQLException;
import databasePart1.DatabaseHelper;

public class FirstUserCreationTest {
	
   /* public static void main(String arg[]) {
    	
    	//static int numPassed = 0;	// Counter of the number of passed tests
    	//static int numFailed = 0;	// Counter of the number of failed tests
    	
    	//Start of test
    	System.out.println("______________________________________");
		System.out.println("\nTesting Automation");
		
		performTestCase(1, "1ormad", "1Miliano500!");
		
		
    }
    
    public static void performTestCase(int testCase, String userName, String password) {
    	
    	System.out.println("_____________________________________\n\nTest case: " + testCase);
		System.out.println("UserName Input: \"" + userName + "\"");
		System.out.println("Password Input: \"" + password + "\"");
		System.out.println("______________");
		
		String nameResult = UserNameRecognizer.checkForValidUserName(userName);
		String passResult = PasswordEvaluator.evaluatePassword(password);
		
		if(!nameResult.isEmpty()) {
			System.out.println("Username: " + nameResult);
		} else {
			System.out.println("Valid UserName");
		}
		if(!passResult.isEmpty()) {
			System.out.println("Password: " + passResult);
		} else {
			System.out.println("Valid Password");
		}
    }
} */

public static void main(String[] args) throws SQLException {
    DatabaseHelper mockDb = new DatabaseHelper(); 
    try {
        // Connect to the database
    	mockDb.connectToDatabase();
    } catch (SQLException e) {
	    	System.out.println(e.getMessage());
	    	}

        // Now validate inputs (will be able to check the database)
        String result = SetupAccountLogic.validateInputs("Pond500", "Password123!", mockDb, "wrongCode");
        if (!result.isEmpty()) {
            System.out.println("Error: " + result);
        } else {
            System.out.println("All inputs valid");
        }
        
}
}

		