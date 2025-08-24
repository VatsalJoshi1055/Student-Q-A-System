package application;

import java.time.LocalDateTime;

public class AdminRequest {
    private long id;
    private String description;
    private String status;            // OPEN | CLOSED
    private String createdBy;
    private LocalDateTime createdAt;
    private String closedBy;
    private String closedMessage;
    private LocalDateTime closedAt;
    private Long parentRequestId;     // null unless this is a re‑opened copy

    /* ---------- ctors ---------- */
    public AdminRequest(long id, String description, String status,
                        String createdBy, LocalDateTime createdAt,
                        String closedBy, String closedMessage,
                        LocalDateTime closedAt, Long parentRequestId) {
        this.id = id;
        this.description = description;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.closedBy = closedBy;
        this.closedMessage = closedMessage;
        this.closedAt = closedAt;
        this.parentRequestId = parentRequestId;
    }

    public AdminRequest(String description, String createdBy) {
        this(-1, description, "OPEN", createdBy,
             LocalDateTime.now(), null, null, null, null);
    }

    /* ---------- getters ---------- */
    public long getId() { return id; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getClosedBy() { return closedBy; }
    public String getClosedMessage() { return closedMessage; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public Long getParentRequestId() { return parentRequestId; }

    /* ---------- helpers ---------- */
    @Override public String toString() {
        return "#" + id + "  [" + status + "]  " + description
               + "  (" + createdBy + ")";
    }
}
