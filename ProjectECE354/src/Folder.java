import java.util.ArrayList;
import java.util.List;

public class Folder {
    private final String folderName;
    private final User owner;
    private final List<Quiz> savedQuizzes = new ArrayList<>();
    private final List<QuizResult> results = new ArrayList<>();

    public Folder(String folderName) { this(folderName, null); }

    public Folder(String folderName, User owner) {
        this.folderName = folderName;
        this.owner = owner;
    }

    public void saveQuiz(Quiz quiz) { savedQuizzes.add(quiz); }
    public String getFolderName() { return folderName; }
    public List<Quiz> getSavedQuizzes() { return new ArrayList<>(savedQuizzes); }

    public void saveResult(QuizResult result) {
        if (result == null || !belongsTo(result.getStudent()))
            throw new IllegalArgumentException("Only the folder owner's results can be saved.");
        // Quiz objects identify quizzes during this in-memory session, even with equal titles.
        results.removeIf(previous -> previous.getQuiz() == result.getQuiz());
        results.add(result);
    }

    public List<QuizResult> getResults(User viewer) {
        if (!belongsTo(viewer)) return List.of();
        return List.copyOf(results);
    }

    private boolean belongsTo(User user) {
        return owner != null && user != null && "student".equals(user.getRole())
                && owner.getUserId().equals(user.getUserId());
    }
}
