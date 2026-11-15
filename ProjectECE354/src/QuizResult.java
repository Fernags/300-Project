public class QuizResult {
    private Quiz quiz;
    private User student;
    private int score;

    public QuizResult(Quiz quiz, User student, int score) {
        this.quiz = quiz;
        this.student = student;
        this.score = score;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public User getStudent() {
        return student;
    }

    public int getScore() {
        return score;
    }
}
