import java.util.List;

/** FR-2: validates a complete attempt and awards one point per correct answer. */
public class QuizGrader {
    public QuizResult grade(Quiz quiz, User student, List<Integer> answers) {
        if (quiz == null || student == null || answers == null) {
            throw new IllegalArgumentException("A quiz, student, and answers are required.");
        }
        if (!"student".equalsIgnoreCase(student.getRole())) {
            throw new IllegalArgumentException("Select a student to submit a quiz.");
        }
        List<Question> questions = quiz.getQuestions();
        if (questions.isEmpty()) {
            throw new IllegalArgumentException("This quiz has no questions.");
        }
        if (answers.size() != questions.size()) {
            throw new IllegalArgumentException("Answer every question before submitting.");
        }

        int score = 0;
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            if (question == null || question.getCorrectAnswer() < 1
                    || question.getCorrectAnswer() > question.getChoices().length) {
                throw new IllegalArgumentException("Question " + (i + 1)
                        + " has an invalid answer key.");
            }
            Integer answer = answers.get(i);
            if (answer == null || answer < 1
                    || answer > question.getChoices().length) {
                throw new IllegalArgumentException("Choose a valid answer for question "
                        + (i + 1) + ".");
            }
            if (question.checkAnswer(answer)) {
                score++;
            }
        }
        return new QuizResult(quiz, student, score, questions.size(), answers);
    }
}
