import java.util.ArrayList;
import java.util.List;

public class Folder {
    private String folderName;
    private List<Quiz> savedQuizzes;

    public Folder(String folderName) {
        this.folderName = folderName;
        this.savedQuizzes = new ArrayList<>();
    }

    public void saveQuiz(Quiz quiz) {
        savedQuizzes.add(quiz);
    }

    public String getFolderName() {
        return folderName;
    }

    public List<Quiz> getSavedQuizzes() {
        return new ArrayList<>(savedQuizzes);
    }
}
