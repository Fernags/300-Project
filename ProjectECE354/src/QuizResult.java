import java.util.List;

public class QuizResult {
    private final Quiz quiz;
    private final User student;
    private final int score;
    private final int totalQuestions;
    private final List<Integer> submittedAnswers;
    private final List<AnswerReview> answerReview;

    public record AnswerReview(String questionText, List<String> choices,
                               int submittedAnswer, int correctAnswer) {
        public AnswerReview { choices = List.copyOf(choices); }
        public boolean isCorrect() { return submittedAnswer == correctAnswer; }
    }

    QuizResult(Quiz quiz, User student, int score, int totalQuestions,
               List<Integer> submittedAnswers) {
        this.quiz = quiz;
        this.student = student;
        this.score = score;
        // Keep the denominator from submission time, even if the quiz changes later.
        this.totalQuestions = totalQuestions;
        this.submittedAnswers = List.copyOf(submittedAnswers);
        List<AnswerReview> review = new java.util.ArrayList<>();
        List<Question> questions = quiz.getQuestions();
        for (int i = 0; i < totalQuestions; i++) {
            Question question = questions.get(i);
            review.add(new AnswerReview(question.getQuestionText(), List.of(question.getChoices()),
                    this.submittedAnswers.get(i), question.getCorrectAnswer()));
        }
        this.answerReview = List.copyOf(review);
    }

    public boolean canReview(User viewer) {
        return viewer != null && student.getUserId().equals(viewer.getUserId())
                && quiz.canReviewAnswers(viewer);
    }

    public List<AnswerReview> getAnswerReview(User viewer) {
        if (!canReview(viewer))
            throw new IllegalArgumentException("Answer review is not available for this profile or quiz.");
        return answerReview;
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

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public double getPercentage() {
        return 100.0 * score / totalQuestions;
    }

    public List<Integer> getSubmittedAnswers() {
        return submittedAnswers;
    }
}
