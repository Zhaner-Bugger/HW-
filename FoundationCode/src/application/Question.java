package application;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Question {
    private final StringProperty questionId;
    private final StringProperty title;
    private final StringProperty content;
    private final StringProperty author;
    private final StringProperty createdAt;
    private final StringProperty followUpOf;
    private final StringProperty isResolved;
    private final StringProperty unreadAnswers = new SimpleStringProperty("0");
    private List<String> tags = new ArrayList<>();
    private boolean isAnswered;
    
    public Question(String questionId, String title, String content, String author, String createdAt, String followUpOf, String isResolved) {
        this.questionId = new SimpleStringProperty(questionId);
        this.title = new SimpleStringProperty(title);
        this.content = new SimpleStringProperty(content);
        this.author = new SimpleStringProperty(author);
        this.createdAt = new SimpleStringProperty(createdAt);
        this.followUpOf = new SimpleStringProperty(followUpOf);
        this.isResolved = new SimpleStringProperty("No");
    }
       
    // New Questions with out follow up
    public Question(String questionId, String title, String content, String author, Timestamp createdAt) {
        this(questionId, title, content, author, createdAt, "");
    }
    
    // New constructor for database usage
    public Question(String questionId, String title, String content, String author, Timestamp createdAt, String followUpOf) {
        this.questionId = new SimpleStringProperty(questionId);
        this.title = new SimpleStringProperty(title);
        this.content = new SimpleStringProperty(content);
        this.author = new SimpleStringProperty(author);
        
        this.followUpOf = new SimpleStringProperty(followUpOf != null ? followUpOf : "");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDate = createdAt.toLocalDateTime().format(formatter);
        this.createdAt = new SimpleStringProperty(formattedDate);
		this.isResolved = new SimpleStringProperty("No");
    }
    //check if a question has been answered
   public boolean isAnswered() {
	   return isAnswered;
   }
   //set a question to answered
   public void setAnswered(boolean answered) {
	   this.isAnswered = answered;
   }
    
    // Getters
    public String getQuestionId() { return questionId.get(); }
    public String getTitle() { return title.get(); }
    public String getContent() { return content.get(); }
    public String getAuthor() { return author.get(); }
    public String getCreatedAt() { return createdAt.get(); }
    public boolean getIsResolved() { return "Yes".equals(isResolved.get()); }
    public String getUnreadAnswers() { return unreadAnswers.get(); }
    public List<String> getTags() { return tags; }
    public String getFollowUpOf() {  
    	String val = followUpOf.get();
    	return (val == null || val.isBlank()) ? null : val;
    }
    
    // Property getters for TableView
    public StringProperty questionIdProperty() { return questionId; }
    public StringProperty titleProperty() { return title; }
    public StringProperty contentProperty() { return content; }
    public StringProperty authorProperty() { return author; }
    public StringProperty createdAtProperty() { return createdAt; }
    public StringProperty isResolvedProperty() { return isResolved; }
    public StringProperty unreadAnswersProperty() { return unreadAnswers; }
    
    // Setters
    public void setTitle(String title) { this.title.set(title); }
    public void setContent(String content) { this.content.set(content); }
    public void setFollowUpOf(String followUpOf) { 
    	this.followUpOf.set(followUpOf != null ? followUpOf : "");
    	}
    public void addTag(String tag) { 
        if (tag != null && !tag.trim().isEmpty() && !tags.contains(tag)) {
            tags.add(tag);
        }
    }
    public void setIsResolved(boolean resolved) { this.isResolved.set(resolved ? "Yes" : "No"); }
    public void setUnreadAnswers(String count) { this.unreadAnswers.set(count); }
    public void removeTag(String tag) { tags.remove(tag); }
    public void clearTags() { tags.clear(); }
}