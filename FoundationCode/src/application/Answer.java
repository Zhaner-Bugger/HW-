// Updated Answer.java
package application;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Answer {
    private final StringProperty answerId;
    private final StringProperty questionId;
    private final StringProperty content;
    private final StringProperty author;
    private final StringProperty createdAt;
    private final StringProperty isAccepted;
    private final StringProperty isRead;
    
    public Answer(String answerId, String questionId, String content, String author, String createdAt, boolean isAccepted) {
        this.answerId = new SimpleStringProperty(answerId);
        this.questionId = new SimpleStringProperty(questionId);
        this.content = new SimpleStringProperty(content);
        this.author = new SimpleStringProperty(author);
        this.createdAt = new SimpleStringProperty(createdAt);
        this.isAccepted = new SimpleStringProperty(isAccepted ? "Yes" : "No");
        this.isRead = new SimpleStringProperty("No");
    }
    
    // New constructor for database usage
    public Answer(String answerId, String questionId, String content, String author, Timestamp createdAt, boolean isAccepted) {
        this.answerId = new SimpleStringProperty(answerId);
        this.questionId = new SimpleStringProperty(questionId);
        this.content = new SimpleStringProperty(content);
        this.author = new SimpleStringProperty(author);
        
        // Format timestamp for display
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDate = createdAt.toLocalDateTime().format(formatter);
        this.createdAt = new SimpleStringProperty(formattedDate);
        
        this.isAccepted = new SimpleStringProperty(isAccepted ? "Yes" : "No");
        this.isRead = new SimpleStringProperty("No");
    }
    
    // Getters
    public String getAnswerId() { return answerId.get(); }
    public String getQuestionId() { return questionId.get(); }
    public String getContent() { return content.get(); }
    public String getAuthor() { return author.get(); }
    public String getCreatedAt() { return createdAt.get(); }
    public boolean getIsAccepted() { return "Yes".equals(isAccepted.get()); }
    public boolean getIsRead() { return "Yes".equals(isRead.get()); }
    
    // Property getters
    public StringProperty answerIdProperty() { return answerId; }
    public StringProperty questionIdProperty() { return questionId; }
    public StringProperty contentProperty() { return content; }
    public StringProperty authorProperty() { return author; }
    public StringProperty createdAtProperty() { return createdAt; }
    public StringProperty isAcceptedProperty() { return isAccepted; }
    public StringProperty isReadProperty() { return isRead; }
    
    // Setters
    public void setContent(String content) { this.content.set(content); }
    public void setIsAccepted(boolean isAccepted) { 
        this.isAccepted.set(isAccepted ? "Yes" : "No"); 
    }
    public void setIsRead(boolean isRead) {
    	this.isRead.set(isRead ? "Yes": "No");
    }
}