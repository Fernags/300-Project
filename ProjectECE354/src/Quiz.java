import java.util.ArrayList;
import java.util.List;

public class Quiz {
    private final User creator;
    private boolean reviewEnabled;
    private String title;
    private List<Question> questions;

    public Quiz(String title) { this(title, null); }

    public Quiz(String title, User creator) {
        this.creator = creator;
        this.title = title;
        this.questions = new ArrayList<>();
    }

    public User getCreator() { return creator; }

    public boolean isOwnedBy(User user) {
        return user != null && creator != null && creator.getUserId().equals(user.getUserId());
    }

    public boolean isVisibleTo(User user) {
        return user != null && (isOwnedBy(user) ||
                ("student".equalsIgnoreCase(user.getRole()) && creator != null
                        && "teacher".equalsIgnoreCase(creator.getRole())));
    }

    public boolean canBeTakenBy(User user) {
        return user != null && "student".equalsIgnoreCase(user.getRole()) && isVisibleTo(user);
    }

    public boolean isReviewEnabled() { return reviewEnabled; }

    public void setReviewEnabled(User actor, boolean enabled) {
        if (!isOwnedBy(actor) || !"teacher".equalsIgnoreCase(actor.getRole()))
            throw new IllegalArgumentException("Only the teacher who created this quiz can change answer review.");
        reviewEnabled = enabled;
    }

    public boolean canReviewAnswers(User user) {
        return canBeTakenBy(user) && (isOwnedBy(user) || reviewEnabled);
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
