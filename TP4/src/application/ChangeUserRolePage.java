package application;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChangeUserRolePage {
    private final DatabaseHelper db;

    public ChangeUserRolePage(DatabaseHelper db) {
        this.db = db;
    }

    public void show(Stage stage) {
        stage.setTitle("Change User Role");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label header = new Label("Change User Roles");

        ComboBox<String> userComboBox = new ComboBox<>();
        ComboBox<String> roleComboBox = new ComboBox<>();
        Label status = new Label();
        Button updateButton = new Button("Update Role");

        Map<String, String> userRoles = new HashMap<>();

        try {
            List<User> users = db.getAllUsers();
            for (User u : users) {
                userComboBox.getItems().add(u.getUserName());
                userRoles.put(u.getUserName(), u.getRole());
            }
        } catch (Exception e) {
            status.setText("Error loading users.");
            e.printStackTrace();
        }

        roleComboBox.setItems(FXCollections.observableArrayList("user", "reviewer", "staff", "admin"));

        userComboBox.setOnAction(e -> {
            String selected = userComboBox.getValue();
            if (selected != null) {
                roleComboBox.setValue(userRoles.get(selected));
            }
        });

        updateButton.setOnAction(e -> {
            String user = userComboBox.getValue();
            String newRole = roleComboBox.getValue();
            if (user != null && newRole != null) {
                if (db.changeUserRole(user, newRole)) {
                    status.setText("Role updated successfully for " + user);
                } else {
                    status.setText("Failed to update role.");
                }
            } else {
                status.setText("Select both user and role.");
            }
        });

        root.getChildren().addAll(header, userComboBox, roleComboBox, updateButton, status);

        stage.setScene(new Scene(root, 400, 300));
        stage.show();
    }
}
