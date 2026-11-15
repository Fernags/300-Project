public class Question {
    private String questionText;
    private String[] choices;
    private int correctAnswer;

    public Question(String questionText, String[] choices,
                    int correctAnswer) {
        this.questionText = questionText;
        this.choices = choices.clone();
        this.correctAnswer = correctAnswer;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String[] getChoices() {
        return choices.clone();
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }

    public boolean checkAnswer(int answer) {
        return answer == correctAnswer;
    }

    public void displayQuestion() {
        System.out.println(questionText);

        for (int i = 0; i < choices.length; i++) {
            System.out.println((i + 1) + ". " + choices[i]);
        }
    }
}
