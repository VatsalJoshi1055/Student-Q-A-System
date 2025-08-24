package application;

import databasePart1.DatabaseHelper;
import java.util.List;
import application.Question;
import application.Answer;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

public class ReviewerApprovalWindow {

    private final DatabaseHelper db;
    private final Questions questionsManager;
    private final Answers answersManager;

    public ReviewerApprovalWindow(DatabaseHelper db, Questions q, Answers a) {
        this.db = db;
        this.questionsManager = q;
        this.answersManager = a;
    }

    public void show() {
        Stage stage = new Stage();
        VBox root = new VBox(15);
        root.setPadding(new Insets(15));

        ListView<String> studentList = new ListView<>();
        List<String> requests = db.getReviewerRequests();
        studentList.setItems(FXCollections.observableArrayList(requests));

        Label infoLabel = new Label("Select a student to view their questions and answers.");

        TextArea contentArea = new TextArea();
        contentArea.setEditable(false);
        contentArea.setWrapText(true);

        Button approveBtn = new Button("Approve");
        Button denyBtn = new Button("Deny");

        approveBtn.setDisable(true);
        denyBtn.setDisable(true);

        studentList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            contentArea.clear();
            if (newVal != null) {
                StringBuilder content = new StringBuilder("Questions and Answers by " + newVal + ":\n\n");

                for (Question q : questionsManager.getAllQuestions()) {
                    if (q.getAuthor().equals(newVal)) {
                        content.append("Q: ").append(q.getTitle()).append(" - ").append(q.getText()).append("\n");
                        for (Answer a : q.getAnswers()) {
                            if (a.getAuthor().equals(newVal)) {
                                content.append("  ↳ A: ").append(a.getText()).append("\n");
                            }
                        }
                        content.append("\n");
                    }
                }

                contentArea.setText(content.toString());

                approveBtn.setDisable(false);
                denyBtn.setDisable(false);
            }
        });

        approveBtn.setOnAction(e -> {
            String selected = studentList.getSelectionModel().getSelectedItem();
            if (selected != null && db.approveReviewer(selected)) {
                studentList.getItems().remove(selected);
                contentArea.clear();
            }
        });

        denyBtn.setOnAction(e -> {
            String selected = studentList.getSelectionModel().getSelectedItem();
            if (selected != null && db.denyReviewer(selected)) {
                studentList.getItems().remove(selected);
                contentArea.clear();
            }
        });

        HBox btnBox = new HBox(10, approveBtn, denyBtn);
        VBox.setMargin(btnBox, new Insets(10, 0, 0, 0));

        root.getChildren().addAll(infoLabel, studentList, contentArea, btnBox);

        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.setTitle("Review Reviewer Requests");
        stage.show();
    }
}