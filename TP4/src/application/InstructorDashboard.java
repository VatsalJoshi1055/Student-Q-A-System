package application;

import databasePart1.DatabaseHelper;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import javafx.scene.control.TextInputDialog;
import java.util.Optional;

/**
 * The InstructorDashboard class allows instructors to review and manage questions, answers,
 * and internal feedback. It provides tools to maintain a constructive learning environment.
 */
public class InstructorDashboard {
    private final DatabaseHelper db;
    private final Questions questionsManager;
    private final Answers answersManager;

    public InstructorDashboard(DatabaseHelper db, Questions questions, Answers answers) {
        this.db = db;
        this.questionsManager = questions;
        this.answersManager = answers;
    }

    public void show() {
        Stage stage = new Stage();
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Label heading = new Label("Instructor Dashboard");

        ListView<Question> questionList = new ListView<>(questionsManager.getAllQuestions());
        TextArea staffNoteArea = new TextArea();
        staffNoteArea.setPromptText("Add or review internal feedback (staff note)");

        /* ───── Question‑selection listener ─────────────────────────────── */
        questionList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                staffNoteArea.setText(newVal.getStaffNote());
            } else {
                staffNoteArea.clear();
            }
        });

        /* ───── Existing buttons ───────────────────────────────────────── */
        Button saveNoteBtn = new Button("Save Staff Note");
        saveNoteBtn.setOnAction(e -> {
            Question selected = questionList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                questionsManager.updateStaffNote(selected, staffNoteArea.getText().trim());
                new Alert(Alert.AlertType.INFORMATION,
                          "Note saved for: " + selected.getTitle()).show();
                questionList.refresh();
            }
        });

        Button viewAnswersBtn = new Button("View Answers");
        viewAnswersBtn.setOnAction(e -> {
            Question selected = questionList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                StringBuilder sb = new StringBuilder("Answers:\n");
                for (Answer a : selected.getAnswers()) {
                    sb.append(" - ").append(a.toString()).append("\n");
                }
                Alert alert = new Alert(Alert.AlertType.INFORMATION, sb.toString());
                alert.setHeaderText("Answers for: " + selected.getTitle());
                alert.show();
            }
        });

        Button deleteQuestionBtn = new Button("Delete Question");
        deleteQuestionBtn.setOnAction(e -> {
            Question selected = questionList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                questionsManager.deleteQuestion(selected);
                questionList.getItems().remove(selected);
                staffNoteArea.clear();
            }
        });
        
        Button changeUserRole = new Button("Change user roles");
        changeUserRole.setOnAction(e -> showChangeUserRolePage());

        Button flagAnswerBtn = new Button("Flag Inappropriate Answer");
        flagAnswerBtn.setOnAction(e -> {
            Question selected = questionList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                ChoiceDialog<Answer> dialog = new ChoiceDialog<>();
                dialog.setTitle("Flag Answer");
                dialog.setHeaderText("Select answer to flag (delete)");
                dialog.getItems().addAll(selected.getAnswers());

                dialog.showAndWait().ifPresent(ans -> {
                    answersManager.deleteAnswer(ans, selected);
                    new Alert(Alert.AlertType.INFORMATION,
                              "Answer flagged and removed.").show();
                });
            }
        });

        Button viewUnansweredBtn = new Button("View Unanswered Questions");
        viewUnansweredBtn.setOnAction(e -> {
            ObservableList<Question> unanswered = questionsManager.getUnansweredQuestions();
            questionList.setItems(unanswered);
        });

        Button markResolvedBtn = new Button("Mark as Resolved");
        markResolvedBtn.setOnAction(e -> {
            Question selected = questionList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                questionsManager.markQuestionAsResolved(selected);
                new Alert(Alert.AlertType.INFORMATION,
                          "Marked question as resolved.").show();
                questionList.refresh();
            }
        });

        /* ───── NEW admin‑request buttons ──────────────────────────────── */
        Button newReqBtn = new Button("New Admin Request");
        newReqBtn.setOnAction(e -> {
            TextInputDialog dlg = new TextInputDialog();
            dlg.setTitle("New Admin Request");
            dlg.setHeaderText("Describe the request you want to send to admins");
            Optional<String> res = dlg.showAndWait();
            res.ifPresent(desc -> {
                if (desc.trim().isEmpty()) return;
                long id = db.createAdminRequest(desc.trim(), /* current user */ "instructor");
                new Alert(Alert.AlertType.INFORMATION,
                          "Request #" + id + " submitted!").show();
            });
        });

        Button reqStatusBtn = new Button("Admin Request Status");
        reqStatusBtn.setOnAction(e -> new InstructorRequestStatusWindow(db).show());

        /* ───── Unified button bar (all buttons now exist) ─────────────── */
        HBox buttonBar = new HBox(10,
                saveNoteBtn, viewAnswersBtn, flagAnswerBtn,
                deleteQuestionBtn, viewUnansweredBtn, markResolvedBtn,
                newReqBtn, reqStatusBtn, changeUserRole);         // ← new buttons added here
        VBox.setMargin(buttonBar, new Insets(5));

        root.getChildren().addAll(heading, questionList, staffNoteArea, buttonBar);

        Scene scene = new Scene(root, 800, 550);
        stage.setScene(scene);
        stage.setTitle("Instructor Dashboard");
        stage.show();
    }
    
    public void showChangeUserRolePage() {
        ChangeUserRolePage rolePage = new ChangeUserRolePage(db);
        Stage roleStage = new Stage();
        rolePage.show(roleStage);
    }

}
