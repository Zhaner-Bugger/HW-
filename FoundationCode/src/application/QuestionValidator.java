package application;

public class QuestionValidator {
    
    public static String validateQuestion(String title, String content) {
        if (title == null || title.trim().isEmpty()) {
            return "Question title cannot be empty";
        }
        if (title.length() > 500) {
            return "Question title too long (max 500 characters)";
        }
        if (content == null || content.trim().isEmpty()) {
            return "Question content cannot be empty";
        }
        if (content.length() > 10000) {
            return "Question content too long (max 10000 characters)";
        }
        return ""; // No errors
    }
    
    public static String validateTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return "Tag cannot be empty";
        }
        if (tag.length() > 100) {
            return "Tag too long (max 100 characters)";
        }
        if (!tag.matches("^[a-zA-Z0-9\\s\\-]+$")) {
            return "Tag can only contain letters, numbers, spaces, and hyphens";
        }
        return "";
    }
}