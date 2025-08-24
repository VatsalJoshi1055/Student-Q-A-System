package application;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Lets instructors view the status of their admin requests,
 * read the admin's close‑out note, and optionally reopen a request.
 */
public class InstructorRequestStatusWindow {
    private final DatabaseHelper db;

    public InstructorRequestStatusWindow(DatabaseHelper db) {
        this.db = db;
    }

    public void show() {
        Stage st = new Stage();
        st.setTitle("My Admin Requests – Status");

        /* -------- list + details pane -------------------------------- */
        ListView<AdminRequest> list = new ListView<>(
                FXCollections.observableArrayList(db.getAdminRequests(true)));

        TextArea details = new TextArea();
        details.setEditable(false);
        details.setWrapText(true);
        details.setPromptText("Select a request to see its details…");

        list.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, req) -> {
            if (req == null) {
                details.clear();
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("ID: ").append(req.getId())
              .append("\nStatus: ").append(req.getStatus())
              .append("\nCreated by: ").append(req.getCreatedBy())
              .append(" on ").append(req.getCreatedAt());

            if ("CLOSED".equals(req.getStatus())) {
                sb.append("\n\n--- Admin Close‑out Note ---\n")
                  .append(req.getClosedMessage()==null ?
                          "(no message)" : req.getClosedMessage())
                  .append("\n\nClosed by: ").append(req.getClosedBy())
                  .append(" on ").append(req.getClosedAt());
            }

            if (req.getParentRequestId() != null) {
                sb.append("\n\n(Reopened copy of request #")
                  .append(req.getParentRequestId()).append(")");
            }

            details.setText(sb.toString());
        });

        /* -------- reopen controls ------------------------------------ */
        TextField newDesc = new TextField();
        newDesc.setPromptText("New / updated description…");

        Button reopenBtn = new Button("Re‑open Selected CLOSED Request");
        reopenBtn.setOnAction(e -> {
            AdminRequest sel = list.getSelectionModel().getSelectedItem();
            if (sel == null || !"CLOSED".equals(sel.getStatus())) return;

            String desc = newDesc.getText().trim();
            if (desc.isEmpty()) {
                new Alert(Alert.AlertType.WARNING,
                          "Please enter an updated description.").show();
                return;
            }
            boolean ok = db.reopenAdminRequest(sel.getId(), "instructor", desc);
            if (ok) {
                list.setItems(FXCollections.observableArrayList(
                        db.getAdminRequests(true)));
                newDesc.clear();
            }
        });

        /* -------- layout --------------------------------------------- */
        VBox rightPane = new VBox(10, new Label("Details:"), details,
                                  new Label("Update description then Re‑open:"),
                                  newDesc, reopenBtn);
        rightPane.setPadding(new Insets(10));

        SplitPane split = new SplitPane(list, rightPane);
        split.setDividerPositions(0.4);

        Scene sc = new Scene(split, 800, 500);
        st.setScene(sc);
        st.show();
    }
}
