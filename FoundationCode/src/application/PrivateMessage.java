package application;

import java.sql.Timestamp;

public class PrivateMessage {
    private final int id;
    private final String questionId;
    private final String fromUser;
    private final String toUser;
    private final String content;
    private final Timestamp createdAt;
    private final boolean isRead;

    public PrivateMessage(int id, String questionId, String fromUser, String toUser, String content, Timestamp createdAt, boolean isRead) {
        this.id = id;
        this.questionId = questionId;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.content = content;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }

    public int getId() { return id; }
    public String getQuestionId() { return questionId; }
    public String getFromUser() { return fromUser; }
    public String getToUser() { return toUser; }
    public String getContent() { return content; }
    public Timestamp getCreatedAt() { return createdAt; }
    public boolean isRead() { return isRead; }
}
