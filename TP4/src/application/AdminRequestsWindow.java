package application;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class AdminRequestsWindow {
    private final DatabaseHelper db;
    private ListView<AdminRequest> list;

    public AdminRequestsWindow(DatabaseHelper db) { this.db = db; }

    public void show() {
        Stage st = new Stage();
        list = new ListView<>(FXCollections.observableArrayList(
                db.getAdminRequests(false)));

        TextArea actionBox = new TextArea();
        actionBox.setPromptText("Document action / resolution ...");

        Button closeBtn = new Button("Close Request");
        closeBtn.setOnAction(e -> {
            AdminRequest sel = list.getSelectionModel().getSelectedItem();
            if (sel==null) return;
            if (db.closeAdminRequest(sel.getId(), "admin", actionBox.getText().trim())) {
                list.getItems().setAll(db.getAdminRequests(false));
                actionBox.clear();
            }
        });

        VBox root = new VBox(10, list, actionBox, closeBtn);
        root.setPadding(new Insets(10));
        st.setTitle("Open Admin Requests");
        st.setScene(new Scene(root, 600, 450));
        st.show();
    }
}
