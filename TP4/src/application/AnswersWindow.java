package application;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

import databasePart1.DatabaseHelper;

public class AnswersWindow extends Stage {

    private final Question question;
    private final Answers answersManager;
    private final User currentUser;
    private final DatabaseHelper dbHelper; 

    private ListView<Answer> answersListView;
    private TextField answerInput;
    private Label messageLabel;

    public AnswersWindow(Question question, Answers answersManager, User user) {
        this.question = question;
        this.answersManager = answersManager;
        this.currentUser = user;

        this.dbHelper = (answersManager != null)
                        ? answersManager.getDatabaseHelper()
                        : null;

        setTitle("Answers for: " + question.getText());

        answersListView = new ListView<>();
        answerInput = new TextField();
        answerInput.setPromptText("Enter your answer...");

        if (question.isResolved()) {
            answerInput.setDisable(true);
        }

        loadAnswers();

        
        if (!((user.getRole().equals("reviewer")) || (user.getRole().equals("staff")))) {
            
            Button addBtn = new Button("Add Answer");
            addBtn.setDisable(question.isResolved());
            addBtn.setOnAction(e -> addAnswer(user, 0));

            Button updateBtn = new Button("Update Answer");
            updateBtn.setOnAction(e -> updateAnswer(user, 0));

            Button deleteBtn = new Button("Delete Answer");
            deleteBtn.setOnAction(e -> deleteAnswer(user, 0));

            
            Button helpfulBtn = new Button("Mark Helpful");
            helpfulBtn.setOnAction(e -> markHelpful());

            Button notHelpfulBtn = new Button("Mark Not Helpful");
            notHelpfulBtn.setOnAction(e -> markNotHelpful());

            
            Button trustBtn = new Button("Mark Reviewer as Trusted");
            trustBtn.setOnAction(e -> markReviewerAsTrusted());
            
            Button viewProfileBtn = new Button("View Profile");
            viewProfileBtn.setOnAction(e -> viewUserProfile());


            messageLabel = new Label();

            VBox layout = new VBox(10);
            layout.setAlignment(Pos.CENTER);
            layout.setPadding(new Insets(15));

            layout.getChildren().addAll(
                new Label("Answers for question: \"" + question.getText() + "\""),
                answersListView,
                answerInput,
                new HBox(10, addBtn, updateBtn, deleteBtn, helpfulBtn, notHelpfulBtn, trustBtn, viewProfileBtn),
                messageLabel
            );
            Scene scene = new Scene(layout, 700, 400);
            setScene(scene);

        } else {
            
            Button addBtn = new Button("Add Review");
            addBtn.setDisable(question.isResolved());
            addBtn.setOnAction(e -> addAnswer(user, 1));

            Button updateBtn = new Button("Update Review");
            updateBtn.setOnAction(e -> updateAnswer(user, 1));

            Button deleteBtn = new Button("Delete Review");
            deleteBtn.setOnAction(e -> deleteAnswer(user, 1));

           
            Button helpfulBtn = new Button("Mark Helpful");
            helpfulBtn.setOnAction(e -> markHelpful());

            Button notHelpfulBtn = new Button("Mark Not Helpful");
            notHelpfulBtn.setOnAction(e -> markNotHelpful());

            
            Button trustBtn = new Button("Mark Reviewer as Trusted");
            trustBtn.setOnAction(e -> markReviewerAsTrusted());
            
            Button viewProfileBtn = new Button("View Profile");
            viewProfileBtn.setOnAction(e -> viewUserProfile());


            messageLabel = new Label();

            VBox layout = new VBox(10);
            layout.setAlignment(Pos.CENTER);
            layout.setPadding(new Insets(15));

            layout.getChildren().addAll(
                new Label("Answers for question: \"" + question.getText() + "\""),
                answersListView,
                answerInput,
                new HBox(10, addBtn, updateBtn, deleteBtn, helpfulBtn, notHelpfulBtn, trustBtn, viewProfileBtn),
                messageLabel
            );
            Scene scene = new Scene(layout, 700, 400);
            setScene(scene);
        }
    }

   
    private void loadAnswers() {
        List<Answer> rawList = answersManager.getAnswersForQuestion(question.getId());
        List<Answer> displayList = new ArrayList<>();

        for (Answer ans : rawList) {
            if (!ans.isReview()) {
                
                displayList.add(ans);

                
                List<Answer> trustedReviews = new ArrayList<>();
                List<Answer> untrustedReviews = new ArrayList<>();

                for (Answer possibleReview : rawList) {
                    if (possibleReview.isReview()
                        && possibleReview.getParentAnswerId() != null
                        && possibleReview.getParentAnswerId().equals(ans.getId())) {

                        boolean isTrusted = false;
                        if (dbHelper != null && currentUser != null) {
                            
                            String reviewerName = possibleReview.getAuthor();
                            String studentName = currentUser.getUserName();
                            isTrusted = dbHelper.isReviewerTrusted(studentName, reviewerName);
                        }

                        if (isTrusted) {
                            trustedReviews.add(possibleReview);
                        } else {
                            untrustedReviews.add(possibleReview);
                        }
                    }
                }

                
                displayList.addAll(trustedReviews);
                displayList.addAll(untrustedReviews);
            }
        }

        answersListView.setItems(FXCollections.observableArrayList(displayList));
    }

    private void addAnswer(User user, int roleVal) {
        String text = answerInput.getText().trim();
        String errorMessage = answerEvaluator.checkAnswer(text);
        if (!errorMessage.isEmpty()) {
            messageLabel.setText(errorMessage);
            return;
        }

        Long parentId = null;
        if (roleVal == 1) { 
            Answer selected = answersListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                messageLabel.setText("Select an answer to review first.");
                return;
            }
            parentId = selected.getId();
        }

        answersManager.createAnswer(question, text, user, roleVal, parentId);
        messageLabel.setText(roleVal == 1 ? "Review added." : "Answer added.");
        answerInput.clear();
        refreshAnswers();
    }

    private void updateAnswer(User user, int roleVal) {
        Answer selected = answersListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("No answer selected.");
            return;
        }
        String newText = answerInput.getText().trim();
        if (newText.isEmpty()) {
            messageLabel.setText("Cannot set empty text.");
            return;
        }

       
        if (!selected.getAuthor().equals(user.getUserName()) 
            && !user.getRole().equals("admin")) {
            messageLabel.setText("You are not the author of this post");
            return;
        }

        answersManager.updateAnswer(selected, newText);
        messageLabel.setText(roleVal == 1 ? "Review updated." : "Answer updated.");
        answerInput.clear();
        refreshAnswers();
    }

    private void deleteAnswer(User user, int roleVal) {
        Answer selectedAnswer = answersListView.getSelectionModel().getSelectedItem();
        if (selectedAnswer == null) {
            messageLabel.setText("No answer selected.");
            return;
        }
        if (!selectedAnswer.getAuthor().equals(user.getUserName())
            && !user.getRole().equals("admin")) {
            messageLabel.setText("You are not the author of this post");
            return;
        }

        answersManager.deleteAnswer(selectedAnswer, question);
        messageLabel.setText(roleVal == 1 ? "Review deleted." : "Answer deleted.");
        refreshAnswers();
    }

    private void markHelpful() {
        Answer selected = answersListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("No item selected.");
            return;
        }
        answersManager.markHelpful(selected);
        messageLabel.setText("Marked as Helpful. [Helpful: " + selected.getLikes() + "]");
        refreshAnswers();
    }

    private void markNotHelpful() {
        Answer selected = answersListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("No item selected.");
            return;
        }
        answersManager.markNotHelpful(selected);
        messageLabel.setText("Marked as Not Helpful. [Not helpful: " + selected.getDislikes() + "]");
        refreshAnswers();
    }

    private void markReviewerAsTrusted() {
        Answer selected = answersListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("No item selected.");
            return;
        }
        if (!selected.isReview()) {
            messageLabel.setText("This isn't a review. You can only trust actual reviewers!");
            return;
        }

        if (currentUser == null || dbHelper == null) {
            messageLabel.setText("Database or current user info not available.");
            return;
        }

        String reviewerName = selected.getAuthor();
        
        String studentName = currentUser.getUserName();

        dbHelper.markReviewerAsTrusted(studentName, reviewerName);
        messageLabel.setText("You now trust reviewer: " + reviewerName);

        
        refreshAnswers();
    }
    
    private void viewUserProfile() {
        Answer selected = answersListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Select an answer or review first.");
            return;
        }

        String username = selected.getAuthor();
        String role = dbHelper.getUserRole(username);  // Ensure this method exists
        Double rating = null;

        if ("reviewer".equalsIgnoreCase(role)) {
            rating = computeReviewerRating(username);
        }

        new ProfilePage(username, role, rating).show();
    }

    private Double computeReviewerRating(String reviewerUsername) {
        List<Answer> reviews = answersManager.getAllAnswers().stream()
            .filter(ans -> ans.isReview() && reviewerUsername.equals(ans.getAuthor()))
            .toList();

        if (reviews.isEmpty()) return null;

        double total = 0;
        int count = 0;

        for (Answer ans : reviews) {
            int likes = ans.getLikes();
            int dislikes = ans.getDislikes();
            int totalVotes = likes + dislikes;
            if (totalVotes == 0) continue;

            double rawScore = ((double)(likes - dislikes) / totalVotes) * 4 + 3;
            total += Math.max(1.0, Math.min(5.0, rawScore));
            count++;
        }

        return count > 0 ? total / count : null;
    }


    private void refreshAnswers() {
        loadAnswers();
    }
}