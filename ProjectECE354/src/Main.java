
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Quiz Maker & Study Assistant ===");

        System.out.print("Enter quiz title: ");
        String title = scanner.nextLine();

        Quiz quiz = new Quiz(title);

        boolean addingQuestions = true;

        while (addingQuestions) {
            System.out.print("\nEnter question: ");
            String questionText = scanner.nextLine();

            String[] choices = new String[4];

            for (int i = 0; i < 4; i++) {
                System.out.print("Choice " + (i + 1) + ": ");
                choices[i] = scanner.nextLine();
            }

            System.out.print("Correct answer (1-4): ");
            int correctAnswer = Integer.parseInt(
                    scanner.nextLine());

            Question question = new Question(
                    questionText, choices, correctAnswer);

            quiz.addQuestion(question);

            System.out.print("Add another question? (yes/no): ");
            String answer = scanner.nextLine();

            if (!answer.equalsIgnoreCase("yes")) {
                addingQuestions = false;
            }
        }

        System.out.println("\n=== Created Quiz ===");
        quiz.displayQuiz();

        scanner.close();
    }
}