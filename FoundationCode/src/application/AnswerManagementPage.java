package application;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;

public class AnswerManagementPage {
    private final DatabaseHelper databaseHelper;
    private final User currentUser;
    private TableView<Answer> answerTable;
    
    public AnswerManagementPage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }
    
    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        Label titleLabel = new Label("Answer Management");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        // Create answer table
        answerTable = createAnswerTable();
        populateAnswerTable();
        
        // Buttons for CRUD operations
        HBox buttonBox = new HBox(10);
        Button addButton = new Button("Add Answer");
        Button editButton = new Button("Edit Answer");
        Button deleteButton = new Button("Delete Answer");
        Button searchButton = new Button("Search Answers");
        Button toggleAcceptedButton = new Button("Toggle Accepted");
        Button sortByAcceptedButton = new Button("Sort by Accepted");
        Button refreshButton = new Button("Refresh");
        Button backButton = new Button("Back");
        
        addButton.setOnAction(e -> showAddAnswerDialog());
        editButton.setOnAction(e -> editSelectedAnswer());
        deleteButton.setOnAction(e -> deleteSelectedAnswer());
        searchButton.setOnAction(e -> showSearchDialog());
        toggleAcceptedButton.setOnAction(e -> toggleAcceptedStatus());
        sortByAcceptedButton.setOnAction(e -> sortByAccepted());
        refreshButton.setOnAction(e -> populateAnswerTable());
        backButton.setOnAction(e -> {
            NavigationHelper.goToHomePage(currentUser.getActiveRole(), primaryStage, databaseHelper, currentUser);
        });
        
        buttonBox.getChildren().addAll(addButton, editButton, deleteButton, searchButton, 
        		toggleAcceptedButton, sortByAcceptedButton, refreshButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);
        
        layout.getChildren().addAll(titleLabel, answerTable, buttonBox);
        Scene scene = new Scene(layout, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Answer Management");
    }
    
    private TableView<Answer> createAnswerTable() {
        TableView<Answer> table = new TableView<>();
        
        TableColumn<Answer, String> idCol = new TableColumn<>("Answer ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("answerId"));
        idCol.setMinWidth(80);
        
        TableColumn<Answer, String> questionIdCol = new TableColumn<>("Question ID");
        questionIdCol.setCellValueFactory(new PropertyValueFactory<>("questionId"));
        questionIdCol.setMinWidth(80);
        
        TableColumn<Answer, String> contentCol = new TableColumn<>("Content");
        contentCol.setCellValueFactory(new PropertyValueFactory<>("content"));
        contentCol.setMinWidth(300);
        
        TableColumn<Answer, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setMinWidth(100);
        
        TableColumn<Answer, String> dateCol = new TableColumn<>("Created");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        dateCol.setMinWidth(150);
        
        TableColumn<Answer, String> acceptedCol = new TableColumn<>("Accepted");
        acceptedCol.setCellValueFactory(new PropertyValueFactory<>("isAccepted"));
        acceptedCol.setMinWidth(80);
        
        table.getColumns().addAll(idCol, questionIdCol, contentCol, authorCol, dateCol, acceptedCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }
    
    private void populateAnswerTable() {
        try {
            List<Answer> answersList = databaseHelper.getAllAnswers();
            ObservableList<Answer> answers = FXCollections.observableArrayList(answersList);
            answerTable.setItems(answers);
        } catch (Exception e) {
            showErrorAlert("Error", "Failed to load answers: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showAddAnswerDialog() {
        Dialog<Answer> dialog = new Dialog<>();
        dialog.setTitle("Add New Answer");
        dialog.setHeaderText("Enter answer details");
        
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));
        
        TextField questionIdField = new TextField();
        questionIdField.setPromptText("Enter question ID");
        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Enter answer content");
        contentArea.setPrefRowCount(4);
      //CheckBox acceptedCheckbox = new CheckBox("Mark as accepted answer");
        
        grid.add(new Label("Question ID:"), 0, 0);
        grid.add(questionIdField, 1, 0);
        grid.add(new Label("Content:"), 0, 1);
        grid.add(contentArea, 1, 1);
      //grid.add(new Label("Accepted:"), 0, 2);
      //grid.add(acceptedCheckbox, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        // Enable/Disable add button depending on whether content was entered
        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);
        
        contentArea.textProperty().addListener((observable, oldValue, newValue) -> {
            addButton.setDisable(newValue.trim().isEmpty());
        });
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                String validation = AnswerValidator.validateAnswer(contentArea.getText());
                if (!validation.isEmpty()) {
                    showErrorAlert("Validation Error", validation);
                    return null;
                }
                
                String answerId = "A" + UUID.randomUUID().toString().substring(0, 8);
                Answer answer = new Answer(answerId, questionIdField.getText(), 
                    contentArea.getText(), currentUser.getUserName(), 
                    java.time.LocalDateTime.now().toString(), 
                    false );
                
                try {
                    if (databaseHelper.insertAnswer(answer)) {
                        return answer;
                    } else {
                        showErrorAlert("Database Error", "Failed to insert answer into database");
                        return null;
                    }
                } catch (SQLException ex) {
                    showErrorAlert("Database Error", "Failed to insert answer: " + ex.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        Optional<Answer> result = dialog.showAndWait();
        result.ifPresent(answer -> {
            showInfoAlert("Success", "Answer added successfully!");
            populateAnswerTable();
        });
    }
    
    private void editSelectedAnswer() {
        Answer selected = answerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorAlert("Selection Error", "Please select an answer to edit.");
            return;
        }
      //Error if user does not own answer
        if(!currentUser.getUserName().equals(selected.getAuthor())) {
        	showErrorAlert("Permission Denied", "You can only edit your own answers");
        	return;
        }
        
        Dialog<Answer> dialog = new Dialog<>();
        dialog.setTitle("Edit Answer");
        dialog.setHeaderText("Edit answer details");
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));
        
        TextArea contentArea = new TextArea(selected.getContent());
        contentArea.setPrefRowCount(4);
      //Check box for Chosen Answer
      //CheckBox acceptedCheckbox = new CheckBox("Mark as accepted answer");
      //acceptedCheckbox.setSelected(selected.getIsAccepted());
        
        grid.add(new Label("Content:"), 0, 0);
        grid.add(contentArea, 1, 0);
      //grid.add(new Label("Accepted:"), 0, 1);
      //grid.add(acceptedCheckbox, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String validation = AnswerValidator.validateAnswer(contentArea.getText());
                if (!validation.isEmpty()) {
                    showErrorAlert("Validation Error", validation);
                    return null;
                }
                
                selected.setContent(contentArea.getText());
              //selected.setIsAccepted(acceptedCheckbox.isSelected());
                return selected;
            }
            return null;
        });
        
        Optional<Answer> result = dialog.showAndWait();
        result.ifPresent(answer -> {
            try {
                if (databaseHelper.updateAnswer(answer)) {
                    showInfoAlert("Success", "Answer updated successfully!");
                    populateAnswerTable();
                } else {
                    showErrorAlert("Error", "Failed to update answer in database");
                }
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to update answer: " + e.getMessage());
            }
        });
    }
    
    private void deleteSelectedAnswer() {
        Answer selected = answerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorAlert("Selection Error", "Please select an answer to delete.");
            return;
        }
        //Error if user does not own answer
        if(!currentUser.getUserName().equals(selected.getAuthor())) {
        	showErrorAlert("Permission Denied", "You can only delete your own answers");
        	return;
        }
        
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Delete Answer");
        confirmation.setContentText("Are you sure you want to delete this answer?");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (databaseHelper.deleteAnswer(selected.getAnswerId())) {
                    showInfoAlert("Success", "Answer deleted successfully!");
                    populateAnswerTable();
                } else {
                    showErrorAlert("Error", "Failed to delete answer from database");
                }
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to delete answer: " + e.getMessage());
            }
        }
    }
    
    private void showSearchDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Search Answers");
        dialog.setHeaderText("Enter search criteria");
        
        ButtonType searchButtonType = new ButtonType("Search", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(searchButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));
        
        TextField searchField = new TextField();
        searchField.setPromptText("Search by content");
        ComboBox<String> searchTypeCombo = new ComboBox<>();
        searchTypeCombo.getItems().addAll("Content", "Question ID", "Author");
        searchTypeCombo.setValue("Content");
        
        grid.add(new Label("Search:"), 0, 0);
        grid.add(searchField, 1, 0);
        grid.add(new Label("Search in:"), 0, 1);
        grid.add(searchTypeCombo, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == searchButtonType) {
                String searchTerm = searchField.getText();
                String searchType = searchTypeCombo.getValue();
                
                try {
                    List<Answer> searchResults;
                    switch (searchType) {
                        case "Content":
                            searchResults = databaseHelper.searchAnswersByContent(searchTerm);
                            break;
                        case "Question ID":
                            searchResults = databaseHelper.getAnswersForQuestion(searchTerm);
                            break;
                        case "Author":
                            // You would need to implement this method
                            searchResults = databaseHelper.getAllAnswers(); // Temporary
                            break;
                        default:
                            searchResults = databaseHelper.getAllAnswers();
                            break;
                    }
                    
                    ObservableList<Answer> results = FXCollections.observableArrayList(searchResults);
                    answerTable.setItems(results);
                    
                } catch (SQLException e) {
                    showErrorAlert("Search Error", "Failed to search answers: " + e.getMessage());
                }
                return searchTerm + "|" + searchType;
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
    }
    
    // Created a button to toggle Accepted answer
    private void toggleAcceptedStatus() {
        Answer selected = answerTable.getSelectionModel().getSelectedItem();
        // Checking for selected answer
        if (selected == null) { 
            showErrorAlert("Selection Error", "Please select an answer to toggle its accepted status.");
            return;
        }

        boolean currentStatus = selected.getIsAccepted();
        boolean newStatus = !currentStatus;
        // Confirmation on chosen answer
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Status Change");
        confirmation.setHeaderText("Change Accepted Status");
        confirmation.setContentText(
            "Do you want to mark this answer as " + 
            (newStatus ? "ACCEPTED?" : "NOT ACCEPTED?")
        );

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            selected.setIsAccepted(newStatus); //Changes UI for Accepter to show yes/no
            try {
                if (databaseHelper.updateAnswer(selected)) {
                    showInfoAlert("Success", "Answer marked as " + 
                        (newStatus ? "ACCEPTED" : "NOT ACCEPTED") + " successfully!");
                    populateAnswerTable();
                } else {
                    showErrorAlert("Error", "Failed to update accepted status in database.");
                }
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to update accepted status: " + e.getMessage());
            }
        } else {
            populateAnswerTable();
        }
    }
    // Sorts by Accepted answers
    private void sortByAccepted() {
        ObservableList<Answer> currentAnswers = answerTable.getItems();

        if (currentAnswers == null || currentAnswers.isEmpty()) {
            showErrorAlert("No Data", "There are no answers to sort.");
            return;
        }

        // Sort by accepted answers first, then un-accepted
        FXCollections.sort(currentAnswers, (a1, a2) -> {
            boolean a1Accepted = a1.getIsAccepted();
            boolean a2Accepted = a2.getIsAccepted();
            // true comes before false
            return Boolean.compare(a2Accepted, a1Accepted);
        });

        answerTable.setItems(currentAnswers);

        showInfoAlert("Sorted", "Answers sorted with accepted ones first.");
    }
    
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}