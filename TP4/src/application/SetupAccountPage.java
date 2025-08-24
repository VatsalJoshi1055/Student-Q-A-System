package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

import databasePart1.*;

/**
 * SetupAccountPage class handles the account setup process for new users.
 * Users provide their userName, password, a valid invitation code, and select their role to register.
 */
public class SetupAccountPage {

    private final DatabaseHelper databaseHelper;

    public SetupAccountPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
     * Displays the Setup Account page in the provided stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    public void show(Stage primaryStage) {
        // Input fields for userName, password, and invitation code
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Username");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);

        TextField inviteCodeField = new TextField();
        inviteCodeField.setPromptText("Enter Invitation Code");
        inviteCodeField.setMaxWidth(250);

        // Dropdown for selecting user role
        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("user", "reviewer", "instructor", "staff");
        roleBox.setValue("user"); // Default selection
        roleBox.setMaxWidth(250);

        // Label to display error messages for invalid input or registration issues
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Button setupButton = new Button("Setup");

        setupButton.setOnAction(a -> {
            String userName = userNameField.getText();
            String password = passwordField.getText();
            String code = inviteCodeField.getText();
            String selectedRole = roleBox.getValue();

            StringBuilder errorMessages = new StringBuilder();

            try {
                String usernameError = UserNameRecognizer.checkForValidUserName(userName);
                if (!usernameError.isEmpty()) {
                    errorMessages.append("Username Error: ").append(usernameError).append("\n");
                }

                String passwordError = PasswordEvaluator.evaluatePassword(password);
                if (!passwordError.isEmpty()) {
                    errorMessages.append("Password Error: ").append(passwordError).append("\n");
                }

                if (errorMessages.length() > 0) {
                    errorLabel.setText(errorMessages.toString().trim());
                    return;
                }

                if (!databaseHelper.doesUserExist(userName)) {
                    if (databaseHelper.validateInvitationCode(code)) {
                        User user = new User(userName, password, selectedRole);
                        databaseHelper.register(user);
                        new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
                    } else {
                        errorLabel.setText("Please enter a valid invitation code.");
                    }
                } else {
                    errorLabel.setText("This username is taken! Please use another one to set up an account.");
                }

            } catch (SQLException e) {
                errorLabel.setText("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(userNameField, passwordField, inviteCodeField, roleBox, setupButton, errorLabel);

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    }
}
