package application;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Questions {
    private List<Question> questions;
    
    public Questions() {
        this.questions = new ArrayList<>();
    }
    
    // CRUD Operations
    public void addQuestion(Question question) {
        questions.add(question);
    }
    
    public Question getQuestion(String questionId) {
        return questions.stream()
            .filter(q -> q.getQuestionId().equals(questionId))
            .findFirst()
            .orElse(null);
    }
    
    public void updateQuestion(Question updatedQuestion) {
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).getQuestionId().equals(updatedQuestion.getQuestionId())) {
                questions.set(i, updatedQuestion);
                return;
            }
        }
    }
    
    // Updated delete to remove follow up question form main question
    public void deleteQuestion(String questionId) {
        List<String> toDelete = new ArrayList<>();
        toDelete.add(questionId);

        // Recursively find all follow-ups
        for (int i = 0; i < toDelete.size(); i++) {
            String currentId = toDelete.get(i);
            questions.stream()
                .filter(q -> currentId.equals(q.getFollowUpOf()))
                .forEach(q -> toDelete.add(q.getQuestionId()));
        }

        questions.removeIf(q -> toDelete.contains(q.getQuestionId()));
    }

    // Search operations
    public Questions searchByTitle(String keyword) {
        Questions result = new Questions();
        result.questions = questions.stream()
            .filter(q -> q.getTitle().toLowerCase().contains(keyword.toLowerCase()))
            .collect(Collectors.toList());
        return result;
    }
    
    public Questions searchByAuthor(String author) {
        Questions result = new Questions();
        result.questions = questions.stream()
            .filter(q -> q.getAuthor().equalsIgnoreCase(author))
            .collect(Collectors.toList());
        return result;
    }
    
    public List<Question> getAllQuestions() {
        return new ArrayList<>(questions);
    }
    public Questions getUnresolvedQuestions() {
    	Questions result = new Questions();
    	result.questions = questions.stream().filter(q -> !q.getIsResolved()).collect(java.util.stream.Collectors.toList());
    	return result;
    }
     
    // Helper to retrieve follow up questions
    public List<Question> getFollowUps(String parentQuestionId) {
        return questions.stream()
            .filter(q -> parentQuestionId.equals(q.getFollowUpOf()))
            .collect(Collectors.toList());
    }
    
    public boolean isEmpty() {
        return questions.isEmpty();
    }
    
    public int size() {
        return questions.size();
    }
}