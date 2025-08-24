package application;

import application.ReviewerApprovalWindow;        // already present
import application.AdminRequestsWindow;          // NEW window

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import databasePart1.DatabaseHelper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * AdminHomePage – admin view for questions AND now instructor admin‑requests.
 */
public class AdminHomePage {

    private Questions questionsManager;
    private Answers   answersManager;

    private ListView<Question> questionListView;
    private Label messageLabel;
    private TextField questionInput;
    private TextField questionTitle;

    private final DatabaseHelper databaseHelper;

    public AdminHomePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /* ──────────────────────────────────────────────────────────────────── */
    public void start(Stage primaryStage, User user) {
        /* ─── set‑up Questions / Answers managers ────────────────────── */
        try {
            Class.forName("org.h2.Driver");
            Connection connection = DriverManager.getConnection(
                    "jdbc:h2:file:./mydb;DB_CLOSE_DELAY=-1;", "sa", "");

            questionsManager = new Questions(connection);
            questionsManager.createTable();

            answersManager = new Answers(connection, databaseHelper);
            answersManager.createTable();

            Statement statement = connection.createStatement();
            //statement.execute("DROP TABLE answers");   // dev‑only
            //statement.execute("DROP TABLE questions");

            questionsManager.loadAllFromDB();
            answersManager.loadAllFromDB();
            answersManager.linkAnswersToQuestions(
                    questionsManager.getAllQuestions());

        } catch (Exception e) { e.printStackTrace(); }

        /* ─── Primary UI ─────────────────────────────────────────────── */
        primaryStage.setTitle("Discussion Application (Questions)");

        // list of questions
        questionListView = new ListView<>(questionsManager.getAllQuestions());
        questionListView.setCellFactory(param -> new ListCell<>() {
            @Override protected void updateItem(Question item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); }
                else {
                    String resolved = item.isResolved() ? " [Resolved]" : "";
                    setText(item.getTitle() + ": " + item.getText()
                            + resolved + " - " + item.getAuthor());
                }
            }
        });

        questionTitle = new TextField();
        questionTitle.setPromptText("Enter a title…");

        questionInput = new TextField();
        questionInput.setPromptText("Enter a question…");

        /* ─── core buttons (unchanged) ──────────────────────────────── */
        Button logOutBtn = new Button("Log Out");
        logOutBtn.setMinWidth(120);
        logOutBtn.setOnAction(e -> new UserLoginPage(databaseHelper)
                                        .show(primaryStage));

        Button quitBtn = new Button("Quit");
        quitBtn.setMinWidth(120);
        quitBtn.setOnAction(a -> { databaseHelper.closeConnection();
                                   Platform.exit(); });

        Button createBtn = new Button("Create Question");
        createBtn.setOnAction(e -> createQuestion(user));

        Button searchBtn = new Button("Search Question");
        searchBtn.setOnAction(e -> {
            try { searchQuestion(); }
            catch (SQLException ex) { ex.printStackTrace(); }
        });

        Button updateBtn = new Button("Update Question");
        updateBtn.setOnAction(e -> updateQuestion(user));

        Button deleteBtn = new Button("Delete Question");
        deleteBtn.setOnAction(e -> deleteQuestion(user));

        Button resolvedBtn = new Button("Mark as Resolved");
        resolvedBtn.setOnAction(e -> markQuestionResolved(user));

        Button viewAnswersBtn = new Button("View Answers");
        viewAnswersBtn.setOnAction(e -> openAnswersWindow(user));

        HBox questionBtns = new HBox(10, createBtn, updateBtn, deleteBtn,
                                     resolvedBtn, viewAnswersBtn, searchBtn);
        questionBtns.setAlignment(Pos.CENTER);

        messageLabel = new Label();

        /* ─── Promote‑to‑Reviewer & reviewer‑request UI (original) ───── */
        Label promoteLabel = new Label("Promote Student to Reviewer:");
        TextField userField = new TextField();
        userField.setPromptText("Enter student's username");

        Button promoteButton = new Button("Promote");
        promoteButton.setOnAction(e -> {
            String userName = userField.getText();
            if (userName.isEmpty()) {
                new Alert(Alert.AlertType.WARNING,
                          "Please enter a username.").show();
                return;
            }
            boolean success = databaseHelper.changeUserRole(userName,"reviewer");
            if (success) {
                new Alert(Alert.AlertType.INFORMATION,
                          "User promoted to reviewer successfully!").show();
                if (user.getUserName().equals(userName)) {
                    user.setRole("reviewer");
                }
            } else {
                new Alert(Alert.AlertType.ERROR,
                          "Failed to promote user. User may not exist.").show();
            }
        });

        VBox promoteBox = new VBox(10, promoteLabel, userField, promoteButton);
        promoteBox.setPadding(new Insets(10));
        promoteBox.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-padding: 10;");

        Button viewRequestsBtn = new Button("View Reviewer Requests");
        viewRequestsBtn.setMinWidth(200);
        viewRequestsBtn.setOnAction(e -> new ReviewerApprovalWindow(
                                         databaseHelper,
                                         questionsManager,
                                         answersManager).show());

        VBox reviewerRequestBox = new VBox(10,
                new Label("Pending Reviewer Requests:"), viewRequestsBtn);
        reviewerRequestBox.setPadding(new Insets(10));
        reviewerRequestBox.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-padding: 10;");

        /* ─── NEW: instructor‑admin requests UI ─────────────────────── */
        Button adminReqBtn = new Button("View Admin Requests");
        adminReqBtn.setMinWidth(200);
        adminReqBtn.setOnAction(e -> new AdminRequestsWindow(databaseHelper)
                                           .show());

        VBox adminReqBox = new VBox(10,
                new Label("Instructor Admin Requests:"), adminReqBtn);
        adminReqBox.setPadding(new Insets(10));
        adminReqBox.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-padding: 10;");

        /* ─── main Question section ─────────────────────────────────── */
        VBox questionSection = new VBox(10,
                new Label("Questions"), questionListView,
                questionTitle, questionInput, questionBtns,
                logOutBtn, quitBtn);
        questionSection.setMaxWidth(550);

        /* ─── compose root ──────────────────────────────────────────── */
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #f0f8ff;");

        root.getChildren().addAll(
                promoteBox,
                reviewerRequestBox,
                adminReqBox,          // ← makes new button visible
                questionSection,
                messageLabel);

        primaryStage.setScene(new Scene(root, 700, 500));
        primaryStage.show();
    }

    /* ─── helper methods (identical to your originals) ─────────────── */
    private void createQuestion(User user) {
        String text  = questionInput.getText().trim();
        String title = questionTitle.getText().trim();

        String errorMessage = questionEvaluator.checkQuestion(title, text);
        if (!errorMessage.isEmpty()) {
            messageLabel.setText(errorMessage);
            return;
        }
        questionsManager.createQuestion(text, user.getUserName(), title);
        messageLabel.setText("Question created.");
        questionInput.clear();
        questionListView.refresh();
    }

    private void updateQuestion(User user) {
        Question selected = questionListView.getSelectionModel().getSelectedItem();
        if (selected == null) { messageLabel.setText("No question selected."); return; }

        String newText = questionInput.getText().trim();
        if (newText.isEmpty()) { messageLabel.setText("Cannot set empty question text."); return; }

        if (!selected.getAuthor().equals(user.getUserName())) {
            messageLabel.setText("You are not the author of this post"); return;
        }

        questionsManager.updateQuestion(selected, newText);
        messageLabel.setText("Question updated.");
        questionTitle.clear(); questionInput.clear(); questionListView.refresh();
    }

    private void deleteQuestion(User user) {
        Question selected = questionListView.getSelectionModel().getSelectedItem();
        if (selected == null) { messageLabel.setText("No question selected."); return; }

        if (!selected.getAuthor().equals(user.getUserName())
            || !user.getRole().equals("admin")) {
            messageLabel.setText("You are not the author of this post"); return;
        }

        questionsManager.deleteQuestion(selected);
        messageLabel.setText("Question deleted.");
        questionListView.refresh();
    }

    private void markQuestionResolved(User user) {
        Question selected = questionListView.getSelectionModel().getSelectedItem();
        if (selected == null) { messageLabel.setText("No question selected."); return; }

        if (!selected.getAuthor().equals(user.getUserName())
            || !user.getRole().equals("admin")) {
            messageLabel.setText("You are not the author of this post"); return;
        }

        questionsManager.markQuestionAsResolved(selected);
        messageLabel.setText("Question marked as resolved.");
        questionListView.refresh();
    }

    private void openAnswersWindow(User user) {
        Question selected = questionListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("No question selected to view answers.");
            return;
        }
        new AnswersWindow(selected, answersManager, user).show();
        messageLabel.setText("Opened separate window for answers.");
    }

    private void searchQuestion() throws SQLException {
        String text = questionInput.getText().trim();
        if (text.isEmpty()) { messageLabel.setText("Cannot search empty question."); return; }

        ObservableList<Question> similar = questionsManager.search(text);
        if (similar.isEmpty()) { messageLabel.setText("Question not found."); return; }

        questionListView.getItems().clear();
        questionListView.setItems(similar);
        questionListView.setCellFactory(param -> new ListCell<>() {
            @Override protected void updateItem(Question item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else {
                    String resolved = item.isResolved() ? " [Resolved]" : "";
                    setText(item.getText() + resolved);
                }
            }
        });
        messageLabel.setText("Search results updated.");
    }
}
