import java.util.ArrayList;
import java.util.List;

public class Quiz {
    private String title;
    private List<Question> questions;

    public Quiz(String title) {
        this.title = title;
        this.questions = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public void addQuestion(Question question) {
        questions.add(question);
    }

    public List<Question> getQuestions() {
        return new ArrayList<>(questions);
    }

    public int getQuestionCount() {
        return questions.size();
    }

    public void displayQuiz() {
        System.out.println("Quiz: " + title);

        for (int i = 0; i < questions.size(); i++) {
            System.out.println("\nQuestion " + (i + 1));
            questions.get(i).displayQuestion();
        }
    }
}

