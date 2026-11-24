import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Automated tests: run this class; no external library is needed. */
public class QuizGraderTest {
    private static int passed;
    private static final QuizGrader GRADER = new QuizGrader();
    private static final User STUDENT = new User("s1", "Test Student", "student");

    public static void main(String[] args) {
        Quiz quiz = new Quiz("Practice");
        String[] choices = {"A", "B", "C", "D"};
        quiz.addQuestion(new Question("First", choices, 2));
        quiz.addQuestion(new Question("Second", choices, 1));
        quiz.addQuestion(new Question("Third", choices, 3));
        check(quiz.getQuestionCount() == 3 && quiz.getTitle().equals("Practice"),
                "FR-1: creation retains title and questions");
        score(quiz, List.of(2, 1, 3), 3, 100.0, "all correct");
        score(quiz, List.of(1, 2, 1), 0, 0.0, "all wrong");
        score(quiz, List.of(2, 2, 3), 2, 200.0 / 3, "mixed answers");
        reject(() -> GRADER.grade(quiz, STUDENT, List.of(2, 1)), "missing answer");
        reject(() -> GRADER.grade(quiz, STUDENT, Arrays.asList(2, null, 3)), "unselected answer");
        reject(() -> GRADER.grade(quiz, STUDENT, List.of(0, 1, 3)), "answer below range");
        reject(() -> GRADER.grade(quiz, STUDENT, List.of(5, 1, 3)), "answer above range");
        reject(() -> GRADER.grade(quiz, STUDENT, List.of(2, 1, 3, 1)), "extra answer");
        reject(() -> GRADER.grade(new Quiz("Empty"), STUDENT, List.of()), "empty quiz");
        reject(() -> GRADER.grade(null, STUDENT, List.of()), "missing quiz");
        reject(() -> GRADER.grade(quiz, null, List.of(2, 1, 3)), "missing student");
        reject(() -> GRADER.grade(quiz, STUDENT, null), "missing submission");
        reject(() -> GRADER.grade(quiz, new User("t1", "Teacher", "teacher"),
                List.of(2, 1, 3)), "non-student submission");
        Quiz invalid = new Quiz("Invalid");
        invalid.addQuestion(new Question("Question", choices, 5));
        reject(() -> GRADER.grade(invalid, STUDENT, List.of(1)), "invalid answer key");
        List<Integer> answers = new ArrayList<>(List.of(2, 1, 3));
        QuizResult result = GRADER.grade(quiz, STUDENT, answers);
        answers.set(0, 1);
        check(result.getSubmittedAnswers().get(0) == 2, "submission copied");
        try {
            result.getSubmittedAnswers().set(0, 1);
            throw new AssertionError("Stored answers must be immutable");
        } catch (UnsupportedOperationException expected) {
            check(true, "stored answers protected");
        }
        quiz.addQuestion(new Question("Later question", choices, 1));
        check(result.getTotalQuestions() == 3 && result.getPercentage() == 100.0,
                "original denominator preserved");
        check(result.getQuiz() == quiz && result.getStudent() == STUDENT, "result ownership");
        User first = new User("s1", "First", "student");
        User second = new User("s2", "Second", "student");
        User teacher = new User("t1", "Teacher", "teacher");
        Quiz personal = new Quiz("Personal", first);
        Quiz lesson = new Quiz("Lesson", teacher);
        check(personal.getCreator() == first, "creator retained");
        check(personal.isVisibleTo(first) && personal.canBeTakenBy(first), "owner can see and take quiz");
        check(!personal.isVisibleTo(second) && !personal.canBeTakenBy(second), "other student cannot access personal quiz");
        check(!personal.isVisibleTo(teacher), "teacher does not see student private quiz");
        check(lesson.canBeTakenBy(first) && lesson.canBeTakenBy(second), "both students can take teacher quiz");
        check(lesson.isVisibleTo(teacher) && !lesson.canBeTakenBy(teacher), "teacher sees own quiz without student action");
        check(!lesson.isOwnedBy(first), "teacher quiz not counted as student-owned");
        check(personal.isOwnedBy(new User("s1", "Renamed", "student")), "ownership uses stable ID");
        check(!personal.isVisibleTo(null) && !personal.canBeTakenBy(null), "missing user denied");
        check(!new Quiz("Legacy").isVisibleTo(first), "unowned quiz hidden from profile list");
        Folder folder = new Folder("Results", first);
        lesson.addQuestion(new Question("One", new String[]{"A", "B"}, 1));
        QuizResult firstAttempt = GRADER.grade(lesson, first, List.of(1));
        check(folder.getResults(first).isEmpty(), "new result folder empty");
        folder.saveResult(firstAttempt);
        check(folder.getResults(first).equals(List.of(firstAttempt)), "submitted result saved");
        check(folder.getResults(second).isEmpty() && folder.getResults(teacher).isEmpty()
                && folder.getResults(null).isEmpty(), "results hidden from other profiles");
        check(folder.getResults(new User("s1", "Renamed", "student")).size() == 1,
                "result access uses stable student ID");
        QuizResult retry = GRADER.grade(lesson, first, List.of(2));
        folder.saveResult(retry);
        check(folder.getResults(first).size() == 1 && folder.getResults(first).get(0).getScore() == 0,
                "retry replaces previous score");
        reject(() -> folder.saveResult(GRADER.grade(lesson, second, List.of(1))),
                "cannot save another student's result");
        reject(() -> folder.saveResult(null), "cannot save missing result");
        check(folder.getResults(first).equals(List.of(retry)), "rejected save preserves prior result");
        Quiz sameTitle = new Quiz("Lesson", teacher);
        sameTitle.addQuestion(new Question("Other", new String[]{"A", "B"}, 1));
        folder.saveResult(GRADER.grade(sameTitle, first, List.of(1)));
        check(folder.getResults(first).size() == 2, "different quizzes with same title remain separate");
        try {
            folder.getResults(first).clear();
            throw new AssertionError("Results must be protected");
        } catch (UnsupportedOperationException expected) {
            check(folder.getResults(first).size() == 2, "returned results cannot alter storage");
        }
        Folder secondFolder = new Folder("Results", second);
        secondFolder.saveResult(GRADER.grade(lesson, second, List.of(1)));
        check(secondFolder.getResults(second).size() == 1 && folder.getResults(first).size() == 2,
                "students keep independent attempts on the same quiz");
        check(!retry.canReview(first), "teacher answer review disabled by default");
        reject(() -> retry.getAnswerReview(first), "disabled review blocks answer details");
        reject(() -> lesson.setReviewEnabled(first, true), "student cannot enable teacher review");
        reject(() -> lesson.setReviewEnabled(new User("t2", "Other", "teacher"), true),
                "another teacher cannot change review permission");
        reject(() -> lesson.setReviewEnabled(null, true), "missing actor cannot change review permission");
        lesson.setReviewEnabled(teacher, true);
        check(retry.canReview(first), "teacher can enable review for saved attempts");
        check(!retry.canReview(second) && !retry.canReview(teacher) && !retry.canReview(null),
                "review stays private to attempt owner");
        reject(() -> retry.getAnswerReview(second), "other student cannot retrieve review");
        QuizResult.AnswerReview wrong = retry.getAnswerReview(first).get(0);
        check(!wrong.isCorrect() && wrong.submittedAnswer() == 2 && wrong.correctAnswer() == 1
                        && wrong.questionText().equals("One") && wrong.choices().equals(List.of("A", "B")),
                "review retains wrong response and correct answer");
        check(firstAttempt.getAnswerReview(first).get(0).isCorrect(), "correct response marked correct");
        lesson.addQuestion(new Question("Added later", new String[]{"X", "Y"}, 2));
        check(retry.getAnswerReview(first).size() == 1 && retry.getTotalQuestions() == 1,
                "saved review preserves submission-time questions");
        try {
            wrong.choices().set(0, "Changed");
            throw new AssertionError("Review choices must be protected");
        } catch (UnsupportedOperationException expected) {
            check(true, "review choices immutable");
        }
        try {
            retry.getAnswerReview(first).clear();
            throw new AssertionError("Review list must be protected");
        } catch (UnsupportedOperationException expected) {
            check(true, "review list immutable");
        }
        lesson.setReviewEnabled(teacher, false);
        reject(() -> retry.getAnswerReview(first), "disabling review blocks previously saved attempts");
        personal.addQuestion(new Question("Own question", new String[]{"A", "B"}, 1));
        QuizResult ownResult = GRADER.grade(personal, first, List.of(1));
        check(ownResult.canReview(first) && ownResult.getAnswerReview(first).get(0).isCorrect(),
                "student may always review own quiz");
        check(ownResult.canReview(new User("s1", "Renamed", "student")),
                "review ownership uses stable ID");
        System.out.println("All " + passed + " automated checks passed.");
    }

    private static void score(Quiz quiz, List<Integer> answers, int expected,
                              double percentage, String name) {
        QuizResult result = GRADER.grade(quiz, STUDENT, answers);
        check(result.getScore() == expected && result.getTotalQuestions() == 3
                && Math.abs(result.getPercentage() - percentage) < 0.000001, name);
    }

    private static void reject(Runnable action, String name) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            check(expected.getMessage() != null && !expected.getMessage().isBlank(), name);
            return;
        }
        throw new AssertionError("Expected rejection: " + name);
    }

    private static void check(boolean condition, String name) {
        if (!condition) {
            throw new AssertionError(name);
        }
        passed++;
        System.out.println("PASS: " + name);
    }
}
