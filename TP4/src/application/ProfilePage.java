package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ProfilePage extends Stage {

    public ProfilePage(String username, String role, Double rating) {
        setTitle("User Profile");

        Label nameLabel = new Label("Username: " + username);
        Label roleLabel = new Label("Role: " + role);
        Label ratingLabel = new Label();

        if ("reviewer".equalsIgnoreCase(role)) {
            ratingLabel.setText("Reviewer Rating: " + (rating != null ? String.format("%.2f/5.00", rating) : "No rating available"));
        }

        VBox layout = new VBox(10, nameLabel, roleLabel, ratingLabel);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        Scene scene = new Scene(layout, 300, 200);

        setScene(scene);
    }
}
