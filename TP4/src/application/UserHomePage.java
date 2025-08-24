package application;

import javafx.application.Platform;
import databasePart1.*;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class UserHomePage {

    private Questions questionsManager;  
    private Answers answersManager;      

    private ListView<Question> questionListView;
    private Label messageLabel;
    private TextField questionInput;
    private TextField questionTitle;
    private ComboBox<String> resolvedComboBox;

    private final DatabaseHelper databaseHelper;

    public UserHomePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
   
    public void start(Stage primaryStage, User user) { //Function that displays the page
        try {
            Class.forName("org.h2.Driver");
            Connection connection = DriverManager.getConnection("jdbc:h2:file:./mydb;DB_CLOSE_DELAY=-1;", "sa", "");

            questionsManager = new Questions(connection);
            answersManager = new Answers(connection, databaseHelper);

            questionsManager.createTable();
            answersManager.createTable();
            
           
            questionsManager.loadAllFromDB();
            answersManager.loadAllFromDB();
            answersManager.linkAnswersToQuestions(questionsManager.getAllQuestions());

        } catch (Exception e) {
            e.printStackTrace();
        }

        primaryStage.setTitle("Discussion Application (Questions)");

        // List of questions
        questionListView = new ListView<>(questionsManager.getAllQuestions());
        questionListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Question item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String resolvedText = item.isResolved() ? " [Resolved]" : "";
                    setText(item.getTitle() + ": " +item.getText() + resolvedText + " - " + item.getAuthor());
                }
            }
        });

        questionTitle = new TextField();
        questionTitle.setPromptText("Enter a title...");
        
        questionInput = new TextField();
        questionInput.setPromptText("Enter or search a question...");
        
        Button logOutBtn = new Button("Log Out");
        logOutBtn.setMinWidth(120); // Make text fit in the button
        logOutBtn.setOnAction(e -> {
            new UserLoginPage(databaseHelper).show(primaryStage);
        });

        Button quitBtn = new Button("Quit");
        quitBtn.setMinWidth(120); // Make text fit in the button
        quitBtn.setOnAction(a -> {
            databaseHelper.closeConnection();
            Platform.exit();
        });
        
        Button searchBtn = new Button("Search Question");
        searchBtn.setOnAction(e -> {
            try {
                doAdvancedSearch();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        });
        
        Button createBtn = new Button("Create Question");
        createBtn.setOnAction(e -> createQuestion(user));

        Button updateBtn = new Button("Update Question");
        updateBtn.setOnAction(e -> updateQuestion(user));

        Button deleteBtn = new Button("Delete Question");
        deleteBtn.setOnAction(e -> deleteQuestion(user));

        Button resolvedBtn = new Button("Mark as Resolved");
        resolvedBtn.setOnAction(e -> markQuestionResolved(user));

        Button viewAnswersBtn = new Button("View Answers");
        viewAnswersBtn.setOnAction(e -> openAnswersWindow(user));
        
        // Add chat button as requested
        Button chatButton = new Button("Open Chat");
        chatButton.setMinWidth(120); // Make text fit in the button
        chatButton.setOnAction(e -> openChatWindow(user.getUserName(), "reviewerUserName")); // Replace "reviewerUserName" with actual reviewer username
        
        Button requestReviewerBtn = new Button("Request Reviewer Access");
        requestReviewerBtn.setOnAction(e -> {
            boolean requested = databaseHelper.requestReviewer(user.getUserName());
            if (requested) {
                messageLabel.setText("Reviewer access requested.");
            } else {
                messageLabel.setText("Request already submitted or failed.");
            }
        });

        HBox questionBtns = new HBox(10, createBtn, updateBtn, deleteBtn, resolvedBtn, viewAnswersBtn);
        questionBtns.setAlignment(Pos.CENTER);
        
        resolvedComboBox = new ComboBox<>();
        resolvedComboBox.getItems().addAll("Any", "Resolved", "Unresolved");
        resolvedComboBox.setValue("Any");
        
        HBox searchOptions = new HBox(10, searchBtn, resolvedComboBox);
        searchOptions.setAlignment(Pos.CENTER);

        messageLabel = new Label();

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #f0f8ff;");

        VBox questionSection = new VBox(10,
        	    new Label("Questions"),
        	    questionListView,
        	    questionTitle,
        	    questionInput,
        	    questionBtns,
        	    searchOptions,
        	    requestReviewerBtn, // New Button
        	    chatButton,
        	    logOutBtn,
        	    quitBtn
        	);

        questionSection.setMaxWidth(550);

        root.getChildren().addAll(questionSection, messageLabel);

        Scene scene = new Scene(root, 1000, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Method to open the chat window
    private void openChatWindow(String currentUser, String otherUser) {
        Stage chatStage = new Stage();
        ChatBox chatBox = new ChatBox(currentUser, otherUser, databaseHelper);
        Scene chatScene = new Scene(chatBox, 400, 300);
        chatStage.setScene(chatScene);
        chatStage.setTitle("Chat with " + otherUser);
        chatStage.show();
    }
    
    private void createQuestion(User user) { //Function to create questions
        String text = questionInput.getText().trim();
        String title = questionTitle.getText().trim();
        
        String errorMessage = questionEvaluator.checkQuestion(title, text);
        
        if (errorMessage != "") {
            messageLabel.setText(errorMessage);
            return;
        }
        
        questionsManager.createQuestion(text, user.getUserName(), title);
        messageLabel.setText("Question created.");
        questionInput.clear();
        questionTitle.clear();
        questionListView.refresh();
    }

    private void updateQuestion(User user) { //Function to update questions
        Question selected = questionListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("No question selected.");
            return;
        }
        
        String newText = questionInput.getText().trim();
        if (newText.isEmpty()) {
            messageLabel.setText("Cannot set empty question text.");
            return;
        }
        
        if ( !(selected.getAuthor().equals(user.getUserName())) ) {
        	messageLabel.setText("You are not the author of this post");
        	return;
        }
        
        questionsManager.updateQuestion(selected, newText);
        messageLabel.setText("Question updated.");
        questionInput.clear();
        questionListView.refresh();
    }

    private void deleteQuestion(User user) { //Function to delete questions
        Question selected = questionListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("No question selected.");
            return;
        }
        
        if ( !(selected.getAuthor().equals(user.getUserName()))) {
        	messageLabel.setText("You are not the author of this post");
        	return;
        }
        
        questionsManager.deleteQuestion(selected);
        messageLabel.setText("Question deleted.");
        questionListView.refresh();
    }

    private void markQuestionResolved(User user) { //Marks questions resolved
        Question selected = questionListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("No question selected.");
            return;
        }
    
        if ( !(selected.getAuthor().equals(user.getUserName())) ) {
        	messageLabel.setText("You are not the author of this post");
        	return;
        }
        
        questionsManager.markQuestionAsResolved(selected);
        messageLabel.setText("Question marked as resolved.");
        questionListView.refresh();
    }

    private void openAnswersWindow(User user) { //Opens a separate window that shows answers for each question
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
        if (text.isEmpty()) {
            messageLabel.setText("Cannot search empty question.");
            return;
        }
        ObservableList<Question> similarQuestions = questionsManager.search(text);
        if (similarQuestions.isEmpty()) {
            messageLabel.setText("Question not found.");
            return;
        }
        
        // Instead of replacing questionListView, update its items
        questionListView.getItems().clear();
        questionListView.refresh();
        questionListView.setItems(similarQuestions);
        questionListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Question item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String resolvedText = item.isResolved() ? " [Resolved]" : "";
                    setText(item.getText() + resolvedText); //+ " (Answers: " + item.getAnswers().size() + ")");
                }
            }
        });
        messageLabel.setText("Search results updated.");
    }
    
    
    private void doAdvancedSearch() throws SQLException {
        String text = questionInput.getText().trim();
        String author = "";
        String resolvedChoice = resolvedComboBox.getValue();

        Boolean resolvedFilter = null;
        if ("Resolved".equals(resolvedChoice)) {
            resolvedFilter = true;
        } else if ("Unresolved".equals(resolvedChoice)) {
            resolvedFilter = false;
        }

        try {
            ObservableList<Question> results = questionsManager.advancedSearch(
                    text, author, resolvedFilter
            );
            
            questionListView.setItems(results);
            
            messageLabel.setText("Search returned " + results.size() + " results.");
        } catch (SQLException e) {
            e.printStackTrace();
            messageLabel.setText("Search error: " + e.getMessage());
        }
    }
}