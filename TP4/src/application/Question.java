/**
 * Question.java
 *
 * Represents a question posted by a user, containing text, author, title,
 * a list of associated answers, and an internal note field visible to staff.
 *
 * Author: Vatsal Joshi
 * Version: 1.0
 */

package application;

import java.util.ArrayList;
import java.util.List;

/**
 * The Question class stores metadata and content for a user-submitted question.
 * It also supports tracking answers, resolution status, and internal staff notes.
 */
public class Question {
    private long id;
    private String text;
    private final List<Answer> answers;
    private boolean resolved;
    private String author;
    private String title;
    private String staffNote = "";

    /**
     * Constructs a new Question.
     *
     * @param id     The unique identifier for the question.
     * @param text   The content of the question.
     * @param author The user who submitted the question.
     * @param title  The title of the question.
     */
    public Question(long id, String text, String author, String title) {
        this.id = id;
        this.text = text;
        this.answers = new ArrayList<>();
        this.resolved = false;
        this.author = author;
        this.title = title;
    }

    /**
     * Returns the ID of the question.
     *
     * @return The question's ID.
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the full text content of the question.
     *
     * @return The question text.
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the author of the question.
     *
     * @return The username of the author.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Returns the title of the question.
     *
     * @return The question title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Updates the text of the question.
     *
     * @param newText The new question text.
     */
    public void setText(String newText) {
        this.text = newText;
    }

    /**
     * Returns the list of answers associated with this question.
     *
     * @return A list of answers.
     */
    public List<Answer> getAnswers() {
        return answers;
    }

    /**
     * Returns whether the question has been marked as resolved.
     *
     * @return true if resolved, false otherwise.
     */
    public boolean isResolved() {
        return resolved;
    }

    /**
     * Sets the resolution status of the question.
     *
     * @param resolved true to mark as resolved, false to mark as unresolved.
     */
    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    /**
     * Returns the staff note associated with the question.
     *
     * @return The internal note added by a staff member.
     */
    public String getStaffNote() {
        return staffNote;
    }

    /**
     * Sets or updates the internal staff note for the question.
     *
     * @param staffNote The note to store.
     */
    public void setStaffNote(String staffNote) {
        this.staffNote = staffNote;
    }

    /**
     * Returns a formatted string representation of the question.
     * Includes title, text, author, and staff note if present.
     *
     * @return A user-readable summary of the question.
     */
    @Override
    public String toString() {
        String noteSnippet = (staffNote != null && !staffNote.isBlank()) ? " [Note: " + staffNote + "]" : "";
        return title + ": " + text + " (by " + author + ")" + noteSnippet;
    }
}
