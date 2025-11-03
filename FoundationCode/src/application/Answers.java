package application;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Answers {
    private List<Answer> answers;
    
    public Answers() {
        this.answers = new ArrayList<>();
    }
    
    // CRUD Operations
    public void addAnswer(Answer answer) {
        answers.add(answer);
    }
    
    public Answer getAnswer(String answerId) {
        return answers.stream()
            .filter(a -> a.getAnswerId().equals(answerId))
            .findFirst()
            .orElse(null);
    }
    
    public void updateAnswer(Answer updatedAnswer) {
        for (int i = 0; i < answers.size(); i++) {
            if (answers.get(i).getAnswerId().equals(updatedAnswer.getAnswerId())) {
                answers.set(i, updatedAnswer);
                return;
            }
        }
    }
    
    public void deleteAnswer(String answerId) {
        answers.removeIf(a -> a.getAnswerId().equals(answerId));
    }
    
    // Search operations
    public Answers getAnswersForQuestion(String questionId) {
        Answers result = new Answers();
        result.answers = answers.stream()
            .filter(a -> a.getQuestionId().equals(questionId))
            .collect(Collectors.toList());
        return result;
    }
    
    public Answers searchByContent(String keyword) {
        Answers result = new Answers();
        result.answers = answers.stream()
            .filter(a -> a.getContent().toLowerCase().contains(keyword.toLowerCase()))
            .collect(Collectors.toList());
        return result;
    }
    
    public List<Answer> getAllAnswers() {
        return new ArrayList<>(answers);
    }
    
    public boolean isEmpty() {
        return answers.isEmpty();
    }
    
    public int size() {
        return answers.size();
    }
}