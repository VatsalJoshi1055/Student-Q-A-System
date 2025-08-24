package application;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import databasePart1.DatabaseHelper;

/**
 * The ChatBox class displays a window to show conversations with other users.
 */
public class ChatBox extends VBox {
    private ListView<String> messageList;
    private TextField messageInput;
    private Button sendButton;
    private TextField searchField;
    private Button searchButton;
    private ListView<String> reviewerList;
    private String currentUser;
    private String otherUser;
    private VBox chatInterface;
    
    private final DatabaseHelper databaseHelper;

    public ChatBox(String currentUser, String otherUser, DatabaseHelper databaseHelper) {
        this.currentUser = currentUser;
        this.otherUser = otherUser;
        this.databaseHelper = databaseHelper;
        
        // Initialize search components
        searchField = new TextField();
        searchField.setPromptText("Search for admin...");
        searchButton = new Button("Search");
        
        reviewerList = new ListView<>();
        reviewerList.setPrefHeight(150);
        reviewerList.setVisible(false);
        
        // Set up the layout
        setSpacing(10);
        setPadding(new Insets(10));
        
        // Add search components
        HBox searchBox = new HBox(10);
        searchBox.getChildren().addAll(searchField, searchButton);
        
        // Create a container for the chat interface (initially empty)
        chatInterface = new VBox(10);
        chatInterface.setVisible(false);
        
        // Add components to the main layout
        this.getChildren().addAll(searchBox, reviewerList, chatInterface);
        
        // Set up event handlers
        searchButton.setOnAction(e -> searchReviewers());
        
        reviewerList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                startChatWithReviewer(newValue);
            }
        });
    }
    
    //searches for all available chatters
    private void searchReviewers() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            return;
        }

        List<String> reviewers = getReviewers(searchTerm);
        reviewerList.getItems().clear();
        reviewerList.getItems().addAll(reviewers);
        reviewerList.setVisible(true);
    }

    //gets a list of chatters based on the search term
    private List<String> getReviewers(String searchTerm) {
        List<String> reviewers = databaseHelper.getChatters(searchTerm);
        return reviewers;
    }

    private void startChatWithReviewer(String reviewerName) {
        this.otherUser = reviewerName;
        
        // Hide the search components
        searchField.setVisible(false);
        searchButton.setVisible(false);
        reviewerList.setVisible(false);
        
        // Initialize chat components
        messageList = new ListView<>();
        messageList.setPrefHeight(300);
        
        messageInput = new TextField();
        messageInput.setPromptText("Type your message...");
        
        sendButton = new Button("Send");
        sendButton.setOnAction(e -> sendMessage());
        
        // Set up the chat interface
        HBox inputBox = new HBox(10);
        inputBox.getChildren().addAll(messageInput, sendButton);
        
        chatInterface.getChildren().addAll(messageList, inputBox);
        chatInterface.setVisible(true);
        
        // Load existing messages
        loadMessages();
    }

    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty()) {
            insertMessage(currentUser, otherUser, message);
            messageList.getItems().add("You: " + message);
            messageInput.clear();
        }
    }

    private void loadMessages() {
        List<String> messages = getMessages(currentUser, otherUser, currentUser);
        for (String msg : messages) {
            messageList.getItems().add(msg);
        }
    }

    private void insertMessage(String sender, String receiver, String message) {
       databaseHelper.insertMessage(sender, receiver, message);
    }

    private List<String> getMessages(String user1, String user2, String currentUser) {
        List<String> messages = databaseHelper.getChat(user1, user2, currentUser);
        
        return messages;
    }
}