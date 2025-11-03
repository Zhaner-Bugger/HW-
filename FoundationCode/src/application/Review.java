package application;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Review {
    private final StringProperty reviewId;
    private final StringProperty answerId;
    private final StringProperty reviewer;
    private final StringProperty content;
    private final StringProperty createdAt;

    public Review(String reviewId, String answerId, String reviewer, String content, Timestamp createdAt) {
        this.reviewId = new SimpleStringProperty(reviewId);
        this.answerId = new SimpleStringProperty(answerId);
        this.reviewer = new SimpleStringProperty(reviewer);
        this.content = new SimpleStringProperty(content);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        this.createdAt = new SimpleStringProperty(createdAt.toLocalDateTime().format(formatter));
    }

    public String getReviewId() { return reviewId.get(); }
    public String getAnswerId() { return answerId.get(); }
    public String getReviewer() { return reviewer.get(); }
    public String getContent() { return content.get(); }
    public String getCreatedAt() { return createdAt.get(); }

    public StringProperty reviewIdProperty() { return reviewId; }
    public StringProperty answerIdProperty() { return answerId; }
    public StringProperty reviewerProperty() { return reviewer; }
    public StringProperty contentProperty() { return content; }
    public StringProperty createdAtProperty() { return createdAt; }

    public void setContent(String content) { this.content.set(content); }
}