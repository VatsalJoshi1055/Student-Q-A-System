/**
 * StaffDashboard.java
 * 
 * A user interface for staff members to manage questions within the system.
 * Staff can view questions, add internal notes, delete questions,
 * view unanswered questions, notify reviewers, and inspect answers.
 * 
 * @author Vatsal Joshi
 * @version 1.0
 */

package application;

import databasePart1.DatabaseHelper;

import java.sql.SQLException;

import application.AnswersWindow;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * The StaffDashboard class provides a graphical interface for staff members
 * to review and manage questions. It allows staff to add internal notes, 
 * filter unanswered questions, notify reviewers, view answers, and delete inappropriate content.
 */
public class StaffDashboard {

    private final DatabaseHelper db;
    private final Questions questionsManager;
    private final Answers answersManager;
    private Label messageLabel;
    /**
     * Constructs the StaffDashboard with the necessary database and logic handlers.
     *
     * @param db The DatabaseHelper instance for database operations.
     * @param questions The Questions manager to handle question-related operations.
     * @param answers The Answers manager to handle answer-related operations.
     */
    public StaffDashboard(DatabaseHelper db, Questions questions, Answers answers) {
        this.db = db;
        this.questionsManager = questions;
        this.answersManager = answers;
    }

    /**
     * Displays the Staff Dashboard UI in a new window.
     * Allows staff to view questions, manage notes, and take moderation actions.
     */
    public void show() {
        Stage stage = new Stage();
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Label heading = new Label("Staff Dashboard");

        ListView<Question> questionList = new ListView<>(questionsManager.getAllQuestions());

        TextArea staffNoteArea = new TextArea();
        staffNoteArea.setPromptText("Add internal note (visible only to staff/instructors)");

        // Update the note area when a question is selected
        questionList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                staffNoteArea.setText(newVal.getStaffNote());
            } else {
                staffNoteArea.clear();
            }
        });

        // Save internal staff notes
        Button saveNoteBtn = new Button("Save Note");
        saveNoteBtn.setOnAction(e -> {
            Question selected = questionList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String note = staffNoteArea.getText().trim();
                questionsManager.updateStaffNote(selected, note);

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Note saved for: " + selected.getTitle());
                alert.show();

                questionList.refresh();
            }
        });

        // View only unanswered questions
        Button viewUnansweredBtn = new Button("View Unanswered Questions");
        viewUnansweredBtn.setOnAction(e -> {
            ObservableList<Question> unanswered = questionsManager.getUnansweredQuestions();
            questionList.setItems(unanswered);
        });

        // View all answers to the selected question
        Button viewAnswersBtn = new Button("View Answers");
        viewAnswersBtn.setOnAction(e -> {
            Question selected = questionList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                new AnswersWindow(selected, answersManager, null ).show();
            }
        });
        
        messageLabel = new Label();

        Button banUserBtn = new Button("Ban User");
        banUserBtn.setOnAction(a -> {
            try {
                Question selected = questionList.getSelectionModel().getSelectedItem();

                if (selected == null) {
                    messageLabel.setText("No question selected.");
                    return;
                }

                String role = db.getUserRole(selected.getAuthor());
                if((role.equals("staff")) || (role.equals("admin"))) {
                    messageLabel.setText("You cannot ban an admin or staff member.");
                    return;
                }

                db.userBanned(selected.getAuthor());
                questionsManager.deleteQuestion(selected);
                
                questionList.refresh();

                messageLabel.setText("User Banned.");
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        // Notify reviewers if the question is unanswered
        Button notifyReviewersBtn = new Button("Notify Reviewers");
        notifyReviewersBtn.setOnAction(e -> {
            Question selected = questionList.getSelectionModel().getSelectedItem();
            if (selected != null && selected.getAnswers().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION,
                        "Reviewers notified about unanswered question: " + selected.getTitle());
                alert.show();
            }
        });

        // Delete the selected question
        Button deleteBtn = new Button("Delete Question");
        deleteBtn.setOnAction(e -> {
            Question selected = questionList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                questionsManager.deleteQuestion(selected);
                questionList.getItems().remove(selected);
                staffNoteArea.clear();
            }
        });

        HBox actionButtons = new HBox(10, saveNoteBtn, notifyReviewersBtn, deleteBtn, viewUnansweredBtn, viewAnswersBtn, banUserBtn);
        VBox.setMargin(actionButtons, new Insets(5));

        root.getChildren().addAll(heading, questionList, staffNoteArea, actionButtons);

        Scene scene = new Scene(root, 700, 500);
        stage.setScene(scene);
        stage.setTitle("Staff Dashboard");
        stage.show();
    }
}
