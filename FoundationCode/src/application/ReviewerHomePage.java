package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ReviewerHomePage {
    private final DatabaseHelper databaseHelper;
    private final User currentUser;
    
    
    public ReviewerHomePage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }
    
    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        Label titleLabel = new Label("Reviewer Home Page");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        // Button to go to review answers page
        Button reviewAnswersButton = new Button("Review Answers");
        reviewAnswersButton.setOnAction(e -> {
            new ReviewerReviewPage(databaseHelper, currentUser).show(primaryStage);
        });
        
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
        	new WelcomeLoginPage(databaseHelper).show(primaryStage,  currentUser);
        });
        
     // Logout Button
	    Button logoutButton = new Button("Logout");
	    logoutButton.setOnAction(e -> {
	    	
	    	try {
		    	// To clear session if needed
		    	DatabaseHelper dbHelper = new DatabaseHelper();
	    		// Reconnect for login screen
	    		dbHelper.connectToDatabase();
	    		
	    		// Return to selection page
	    		SetupLoginSelectionPage setupPage = new SetupLoginSelectionPage(dbHelper);
	    		setupPage.show(primaryStage);
	    		
	    	} catch(Exception ex) {
	    		ex.printStackTrace();
	    	}
	    	
	    });

        layout.getChildren().addAll(titleLabel, reviewAnswersButton, backButton);
        
        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Reviewer Home Page");
    }
}