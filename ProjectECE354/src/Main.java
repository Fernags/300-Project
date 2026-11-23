import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {
    private final TextField title = new TextField();
    private final VBox questionList = new VBox(18);
    private final List<QuestionForm> forms = new ArrayList<>();
    private final List<Quiz> savedQuizzes = new ArrayList<>();
    private final java.util.Map<String, Folder> resultFolders = new java.util.HashMap<>();
    private final Label status = new Label();
    private final Label count = new Label("My quizzes: 0");
    private boolean dirty;
    private boolean takingQuiz;
    private final List<User> profiles = List.of(
            new User("student-1", "Student 1", "student"),
            new User("student-2", "Student 2", "student"),
            new User("teacher-1", "Teacher", "teacher"));
    private User activeUser = profiles.get(0);
    private BorderPane root;
    private ScrollPane creationView;
    private VBox creationFooter;
    private Stage window;

    public static void main(String[] args) { launch(args); }

    @Override public void start(Stage stage) {
        window = stage;
        Label brand = new Label("Quiz Maker / Study assistant");
        brand.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #176b5c;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button myQuizzes = new Button("My quizzes");
        myQuizzes.setOnAction(e -> {
            if (confirmDiscard()) { reset(); showQuizzes(""); }
        });
        ComboBox<String> profile = new ComboBox<>();
        for (User user : profiles) profile.getItems().add(user.getName());
        profile.getSelectionModel().select(0);
        profile.setAccessibleText("Local demo profile");
        Button switchProfile = new Button("Switch profile");
        switchProfile.setOnAction(e -> {
            User selected = profiles.get(profile.getSelectionModel().getSelectedIndex());
            if (selected == activeUser) return;
            if (!confirmDiscard()) {
                profile.getSelectionModel().select(profiles.indexOf(activeUser));
                return;
            }
            activeUser = selected;
            reset();
            showQuizzes("");
        });
        Button myResults = new Button("My Results");
        myResults.setOnAction(e -> {
            if (confirmDiscard()) { reset(); showMyResults(); }
        });
        HBox header = new HBox(12, brand, spacer, profile, switchProfile, myQuizzes, myResults);
        header.setPadding(new Insets(24));
        Label heading = new Label("Create quiz");
        heading.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");
        title.setPromptText("Enter quiz title");
        title.setAccessibleText("Quiz title");
        title.textProperty().addListener((o, a, b) -> dirty = true);
        Button add = new Button("+ Add question");
        add.setOnAction(e -> addQuestion());
        VBox content = new VBox(18, heading,
                new Label("Build your questions and choose one correct answer for each."),
                new Label("Quiz title"), title, questionList, add);
        content.setPadding(new Insets(28));
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        Button save = new Button("Save quiz");
        save.setStyle("-fx-background-color: #176b5c; -fx-text-fill: white;");
        save.setOnAction(e -> saveQuiz());
        Button cancel = new Button("Cancel");
        cancel.setOnAction(e -> {
            if (confirmDiscard()) { reset(); message("Draft cleared.", false); }
        });
        status.setWrapText(true);
        VBox footer = new VBox(12, new HBox(12, save, cancel), status,
                new Label("Saved quizzes last for this session only."));
        footer.setPadding(new Insets(24));
        creationView = scroll;
        creationFooter = footer;
        root = new BorderPane(scroll, header, null, footer, null);
        root.setStyle("-fx-font-family: Arial; -fx-font-size: 15px; -fx-base: white; -fx-background-color: #f4f8f6;");
        reset();
        showQuizzes("");
        stage.setScene(new Scene(root, 960, 780));
        stage.setMinWidth(700);
        stage.setMinHeight(550);
        stage.setOnCloseRequest(e -> { if (!confirmDiscard()) e.consume(); });
        stage.show();
    }

    private void showQuizzes(String confirmation) {
        takingQuiz = false;
        List<Quiz> visible = savedQuizzes.stream().filter(q -> q.isVisibleTo(activeUser)).toList();
        long owned = savedQuizzes.stream().filter(q -> q.isOwnedBy(activeUser)).count();
        count.setText("My quizzes: " + owned);
        Label heading = new Label(activeUser.getName() + " - My quizzes");
        heading.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");
        Label notice = new Label(confirmation);
        notice.setWrapText(true);
        notice.setStyle("-fx-text-fill: #176b5c;");
        Button create = new Button("+ Create quiz");
        create.setOnAction(e -> {
            status.setText("");
            creationView.setVvalue(0);
            root.setCenter(creationView);
            root.setBottom(creationFooter);
            window.setTitle("Quiz Maker - Create quiz");
        });
        VBox list = new VBox(16, heading, new Label("Local demo profiles - no login authentication"), count, create);
        list.setPadding(new Insets(28));
        if (!confirmation.isBlank()) list.getChildren().add(notice);
        if (visible.isEmpty()) {
            Label empty = new Label("No quizzes yet. Create your first quiz to get started.");
            empty.setWrapText(true);
            list.getChildren().add(empty);
        } else {
            for (Quiz quiz : visible) {
                Label quizTitle = new Label(quiz.getTitle());
                quizTitle.setWrapText(true);
                quizTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
                int total = quiz.getQuestionCount();
                VBox card = new VBox(8, quizTitle,
                        new Label(total + (total == 1 ? " question" : " questions")));
                Button take = new Button("Take quiz");
                take.setOnAction(e -> showAttempt(quiz));
                card.getChildren().add(new Label(quiz.isOwnedBy(activeUser)
                        ? "Created by you" : "Teacher quiz: " + quiz.getCreator().getName()));
                if (quiz.canBeTakenBy(activeUser)) card.getChildren().add(take);
                if (quiz.isOwnedBy(activeUser) && "teacher".equalsIgnoreCase(activeUser.getRole())) {
                    CheckBox allowReview = new CheckBox("Allow students to review answers");
                    allowReview.setSelected(quiz.isReviewEnabled());
                    allowReview.setOnAction(e -> quiz.setReviewEnabled(activeUser, allowReview.isSelected()));
                    card.getChildren().add(allowReview);
                }
                card.setPadding(new Insets(20));
                card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #d8e4df; -fx-border-radius: 12;");
                list.getChildren().add(card);
            }
        }
        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        Label storage = new Label("Saved quizzes last for this session only.");
        VBox footer = new VBox(storage);
        footer.setPadding(new Insets(24));
        root.setCenter(scroll);
        root.setBottom(footer);
        window.setTitle("Quiz Maker - My quizzes");
    }

    private void showAttempt(Quiz quiz) {
        if (!quiz.canBeTakenBy(activeUser)) {
            showQuizzes("This profile cannot take that quiz.");
            return;
        }
        takingQuiz = true;
        Label heading = new Label(quiz.getTitle());
        heading.setWrapText(true);
        heading.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        VBox content = new VBox(18, heading, new Label("Select one answer for every question, then submit."));
        content.setPadding(new Insets(28));
        List<ToggleGroup> answers = new ArrayList<>();
        List<Question> quizQuestions = quiz.getQuestions();
        for (int i = 0; i < quizQuestions.size(); i++) {
            Question question = quizQuestions.get(i);
            Label prompt = new Label((i + 1) + ". " + question.getQuestionText());
            prompt.setWrapText(true);
            VBox card = new VBox(12, prompt);
            card.setPadding(new Insets(20));
            card.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
            ToggleGroup group = new ToggleGroup();
            answers.add(group);
            String[] choices = question.getChoices();
            for (int j = 0; j < choices.length; j++) {
                RadioButton choice = new RadioButton(choices[j]);
                choice.setWrapText(true);
                choice.setAccessibleText("Question " + (i + 1) + ", choice " + (j + 1) + ": " + choices[j]);
                choice.setToggleGroup(group);
                choice.setUserData(j + 1);
                card.getChildren().add(choice);
            }
            content.getChildren().add(card);
        }
        Label error = new Label();
        error.setWrapText(true);
        error.setStyle("-fx-text-fill: #a52a2a;");
        Button submit = new Button("Submit quiz");
        submit.setStyle("-fx-background-color: #176b5c; -fx-text-fill: white;");
        submit.setOnAction(e -> {
            List<Integer> submitted = new ArrayList<>();
            for (int i = 0; i < answers.size(); i++) {
                Toggle selected = answers.get(i).getSelectedToggle();
                if (selected == null) {
                    error.setText("Answer question " + (i + 1) + " before submitting.");
                    return;
                }
                submitted.add((Integer) selected.getUserData());
            }
            try {
                QuizResult result = new QuizGrader().grade(quiz, activeUser, submitted);
                resultFolder().saveResult(result);
                takingQuiz = false;
                showResult(result);
            } catch (IllegalArgumentException ex) { error.setText(ex.getMessage()); }
        });
        Button cancel = new Button("Cancel attempt");
        cancel.setOnAction(e -> { if (confirmDiscard()) showQuizzes("Attempt cancelled. No score was recorded."); });
        VBox footer = new VBox(12, new HBox(12, submit, cancel), error);
        footer.setPadding(new Insets(24));
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        root.setCenter(scroll);
        root.setBottom(footer);
        window.setTitle("Quiz Maker - Take quiz");
    }

    private Folder resultFolder() {
        return resultFolders.computeIfAbsent(activeUser.getUserId(),
                id -> new Folder("My Results", activeUser));
    }

    private void showMyResults() {
        takingQuiz = false;
        Label heading = new Label(activeUser.getName() + " - My Results");
        heading.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");
        VBox content = new VBox(18, heading);
        content.setPadding(new Insets(28));
        List<QuizResult> results = resultFolder().getResults(activeUser);
        if (!"student".equals(activeUser.getRole())) {
            content.getChildren().add(new Label("Saved attempts are available to student profiles."));
        } else if (results.isEmpty()) {
            content.getChildren().add(new Label("No saved results yet. Complete a quiz to save your first result."));
        }
        for (QuizResult result : results) {
            Label quizTitle = new Label(result.getQuiz().getTitle());
            quizTitle.setWrapText(true);
            quizTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
            Label score = new Label(result.getScore() + " / " + result.getTotalQuestions()
                    + " correct - " + String.format(java.util.Locale.US, "%.1f%%", result.getPercentage()));
            Button open = new Button("View result");
            open.setOnAction(e -> showResult(result));
            VBox card = new VBox(10, quizTitle, score, open);
            card.setPadding(new Insets(20));
            card.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
            content.getChildren().add(card);
        }
        Label note = new Label("Only your latest submitted attempt for each quiz is kept. Results last for this session only.");
        note.setWrapText(true);
        content.getChildren().add(note);
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        root.setCenter(scroll);
        root.setBottom(null);
        window.setTitle("Quiz Maker - My Results");
    }

    private void showResult(QuizResult result) {
        if (!resultFolder().getResults(activeUser).contains(result)) {
            showMyResults();
            return;
        }
        Label heading = new Label("Quiz results");
        heading.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");
        Label quizTitle = new Label(result.getQuiz().getTitle());
        quizTitle.setWrapText(true);
        Label score = new Label(result.getScore() + " / " + result.getTotalQuestions() + " correct");
        score.setStyle("-fx-font-size: 26px; -fx-text-fill: #176b5c;");
        Label percentage = new Label(String.format(java.util.Locale.US, "%.1f%%", result.getPercentage()));
        Button back = new Button("Back to My quizzes");
        back.setOnAction(e -> showQuizzes(""));
        Button retry = new Button("Try again");
        retry.setOnAction(e -> showAttempt(result.getQuiz()));
        Button results = new Button("My Results");
        results.setOnAction(e -> showMyResults());
        Label note = new Label("Saved to My Results for this session. A new submission replaces your previous result for this quiz.");
        note.setWrapText(true);
        VBox content = new VBox(20, heading, quizTitle, score, percentage, note, new HBox(12, back, retry, results));
        if (result.canReview(activeUser)) {
            Label reviewHeading = new Label("Answer review");
            reviewHeading.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
            content.getChildren().add(reviewHeading);
            int number = 1;
            for (QuizResult.AnswerReview answer : result.getAnswerReview(activeUser)) {
                Label prompt = new Label(number++ + ". " + answer.questionText());
                prompt.setWrapText(true);
                Label outcome = new Label(answer.isCorrect() ? "Correct" : "Incorrect");
                outcome.setStyle("-fx-font-weight: bold; -fx-text-fill: "
                        + (answer.isCorrect() ? "#176b5c;" : "#a52a2a;"));
                VBox card = new VBox(10, prompt, outcome);
                for (int i = 0; i < answer.choices().size(); i++) {
                    int choice = i + 1;
                    String marker = choice == answer.submittedAnswer() ? " (your answer)" : "";
                    if (choice == answer.correctAnswer()) marker += " (correct answer)";
                    Label option = new Label(choice + ". " + answer.choices().get(i) + marker);
                    option.setWrapText(true);
                    card.getChildren().add(option);
                }
                card.setPadding(new Insets(20));
                card.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
                content.getChildren().add(card);
            }
        } else {
            Label restricted = new Label("Answer review is not enabled for this quiz. Your score remains available.");
            restricted.setWrapText(true);
            content.getChildren().add(restricted);
        }
        content.setPadding(new Insets(28));
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        root.setCenter(scroll);
        root.setBottom(null);
        window.setTitle("Quiz Maker - Results");
    }

    private void addQuestion() {
        QuestionForm form = new QuestionForm(forms.size() + 1);
        forms.add(form);
        questionList.getChildren().add(form.card);
        dirty = true;
    }

    private void saveQuiz() {
        try {
            if (title.getText().isBlank()) throw new IllegalArgumentException("Enter a quiz title.");
            Quiz quiz = new Quiz(title.getText().trim(), activeUser);
            for (QuestionForm form : forms) quiz.addQuestion(form.toQuestion());
            savedQuizzes.add(quiz);
            count.setText("My quizzes: " + savedQuizzes.size());
            reset();
            showQuizzes("Saved: " + quiz.getTitle() + " (" + quiz.getQuestionCount() + " questions).");
        } catch (IllegalArgumentException ex) { message(ex.getMessage(), true); }
    }

    private void reset() {
        title.clear(); forms.clear(); questionList.getChildren().clear();
        addQuestion(); dirty = false;
    }

    private void message(String text, boolean error) {
        status.setText(text);
        status.setStyle("-fx-text-fill: " + (error ? "#a52a2a" : "#176b5c") + ";");
    }

    private boolean confirmDiscard() {
        if (!dirty && !takingQuiz) return true;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                takingQuiz ? "Leave this attempt? Your answers will be discarded."
                        : "Discard the unsaved quiz draft?", ButtonType.CANCEL, ButtonType.OK);
        alert.setHeaderText("Unsaved changes");
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    // All creation-screen controls are kept in Main.
    private class QuestionForm {
        final int number;
        final VBox card = new VBox(12);
        final TextField text = new TextField();
        final VBox options = new VBox(10);
        final List<TextField> choices = new ArrayList<>();
        final ToggleGroup correct = new ToggleGroup();

        QuestionForm(int number) {
            this.number = number;
            Label label = new Label("Question " + number);
            label.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
            text.setPromptText("Enter question");
            text.setAccessibleText("Question " + number);
            text.textProperty().addListener((o, a, b) -> dirty = true);
            correct.selectedToggleProperty().addListener((o, a, b) -> dirty = true);
            Button add = new Button("+ Add option");
            add.setOnAction(e -> addOption());
            card.setPadding(new Insets(22));
            card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #d8e4df; -fx-border-radius: 12;");
            card.getChildren().addAll(label, text, new Label("Answer choices - select the correct answer"), options, add);
            addOption(); addOption();
        }

        void addOption() {
            int index = choices.size() + 1;
            TextField field = new TextField();
            field.setPromptText("Choice " + index);
            field.setAccessibleText("Question " + number + " choice " + index);
            field.textProperty().addListener((o, a, b) -> dirty = true);
            RadioButton radio = new RadioButton("Correct");
            radio.setAccessibleText("Question " + number + " correct choice " + index);
            radio.setToggleGroup(correct); radio.setUserData(index);
            HBox row = new HBox(16, field, radio);
            HBox.setHgrow(field, Priority.ALWAYS);
            choices.add(field); options.getChildren().add(row); dirty = true;
        }

        Question toQuestion() {
            if (text.getText().isBlank()) throw new IllegalArgumentException("Enter text for question " + number + ".");
            String[] answers = choices.stream().map(f -> f.getText().trim()).toArray(String[]::new);
            for (String answer : answers)
                if (answer.isEmpty()) throw new IllegalArgumentException("Fill every choice for question " + number + ".");
            if (correct.getSelectedToggle() == null)
                throw new IllegalArgumentException("Select the correct answer for question " + number + ".");
            return new Question(text.getText().trim(), answers, (Integer) correct.getSelectedToggle().getUserData());
        }
    }
}
