/**
 * Questions.java
 *
 * Manages all question-related operations, including loading from and writing to
 * the database, and maintaining an in-memory list of all questions.
 * Also includes utility methods to update notes, filter unanswered questions,
 * and delete or search for specific questions.
 *
 * Author: Vatsal Joshi
 * Version: 1.0
 */

package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

/**
 * The Questions class handles the storage, retrieval, and modification of
 * question data in both the database and local memory.
 */
public class Questions {
    private final Connection connection;
    private final ObservableList<Question> allQuestions = FXCollections.observableArrayList();

    /**
     * Constructs a Questions manager instance.
     *
     * @param connection The active database connection.
     */
    public Questions(Connection connection) {
        this.connection = connection;
    }

    /**
     * Creates the questions table in the database if it doesn't already exist.
     *
     * @throws SQLException if a database error occurs.
     */
    public void createTable() throws SQLException {
        try (Statement st = connection.createStatement()) {
            String sql = """
                CREATE TABLE IF NOT EXISTS questions (
                    id IDENTITY PRIMARY KEY,
                    text VARCHAR(255) NOT NULL,
                    title VARCHAR(255),
                    author VARCHAR(255),
                    resolved BOOLEAN DEFAULT FALSE,
                    staff_note VARCHAR(255) DEFAULT ''
                )
            """;
            st.executeUpdate(sql);
        }
    }

    /**
     * Loads all questions from the database into the local ObservableList.
     *
     * @throws SQLException if a database access error occurs.
     */
    public void loadAllFromDB() throws SQLException {
        allQuestions.clear();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM questions")) {
            while (rs.next()) {
                long id = rs.getLong("id");
                String txt = rs.getString("text");
                boolean res = rs.getBoolean("resolved");
                String author = rs.getString("author");
                String title = rs.getString("title");
                String note = rs.getString("staff_note");

                Question q = new Question(id, txt, author, title);
                q.setResolved(res);
                q.setStaffNote(note);
                allQuestions.add(q);
            }
        }
    }

    /**
     * Creates a new question, adds it to the database and to the in-memory list.
     *
     * @param text   The content of the question.
     * @param author The author of the question.
     * @param title  The title of the question.
     */
    public void createQuestion(String text, String author, String title) {
        try {
            long newId = insertQuestionDB(text, author, title);
            Question q = new Question(newId, text, author, title);
            allQuestions.add(q);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the content of a question in the database and memory.
     *
     * @param q       The question to update.
     * @param newText The new text to be stored.
     */
    public void updateQuestion(Question q, String newText) {
        try {
            String sql = "UPDATE questions SET text=? WHERE id=?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, newText);
                ps.setLong(2, q.getId());
                ps.executeUpdate();
            }
            q.setText(newText);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the internal staff note for a specific question.
     *
     * @param q    The question to update.
     * @param note The note to be saved.
     */
    public void updateStaffNote(Question q, String note) {
        try {
            String sql = "UPDATE questions SET staff_note=? WHERE id=?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, note);
                ps.setLong(2, q.getId());
                ps.executeUpdate();
            }
            q.setStaffNote(note);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Marks a question as resolved in both memory and database.
     *
     * @param q The question to mark as resolved.
     */
    public void markQuestionAsResolved(Question q) {
        try {
            String sql = "UPDATE questions SET resolved = ? WHERE id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setBoolean(1, true);
                ps.setLong(2, q.getId());
                ps.executeUpdate();
            }
            q.setResolved(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a question from the database and removes it from memory.
     *
     * @param q The question to delete.
     */
    public void deleteQuestion(Question q) {
        try {
            String sql = "DELETE FROM questions WHERE id=?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setLong(1, q.getId());
                ps.executeUpdate();
            }
            allQuestions.remove(q);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns all currently loaded questions.
     *
     * @return An ObservableList of all questions.
     */
    public ObservableList<Question> getAllQuestions() {
        return allQuestions;
    }

    /**
     * Returns all questions that have no associated answers.
     *
     * @return A filtered list of unanswered questions.
     */
    public ObservableList<Question> getUnansweredQuestions() {
        ObservableList<Question> unanswered = FXCollections.observableArrayList();
        for (Question q : allQuestions) {
            if (q.getAnswers().isEmpty()) {
                unanswered.add(q);
            }
        }
        return unanswered;
    }

    /**
     * Searches for questions containing the given text.
     *
     * @param text The search keyword.
     * @return An ObservableList of questions that match the search.
     * @throws SQLException if a database error occurs.
     */
    public ObservableList<Question> search(String text) throws SQLException {
        ObservableList<Question> specificQuestions = FXCollections.observableArrayList();
        String query = "SELECT * FROM questions WHERE text LIKE ?";
        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setString(1, "%" + text + "%");
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                long id = rs.getLong("id");
                String txt = rs.getString("text");
                boolean res = rs.getBoolean("resolved");
                String auth = rs.getString("author");
                String title = rs.getString("title");
                String note = rs.getString("staff_note");

                Question q = new Question(id, txt, auth, title);
                q.setResolved(res);
                q.setStaffNote(note);
                specificQuestions.add(q);
            }
        }
        return specificQuestions;
    }

    /**
     * Inserts a new question into the database and returns its generated ID.
     *
     * @param text   The question content.
     * @param author The author of the question.
     * @param title  The title of the question.
     * @return The auto-generated ID of the inserted question.
     * @throws SQLException if the insertion fails.
     */
    private long insertQuestionDB(String text, String author, String title) throws SQLException {
        String sql = "INSERT INTO questions (text, author, title) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, text);
            ps.setString(2, author);
            ps.setString(3, title);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("Failed to insert question.");
    }
    
    public ObservableList<Question> advancedSearch(String text, String author, Boolean resolved) throws SQLException {
        ObservableList<Question> results = FXCollections.observableArrayList();
        StringBuilder sql = new StringBuilder("SELECT * FROM questions WHERE 1=1 ");

        if (text != null && !text.isEmpty()) {
            sql.append("AND (title LIKE ? OR text LIKE ?) ");
        }
        if (author != null && !author.isEmpty()) {
            sql.append("AND author = ? ");
        }
        if (resolved != null) {
            sql.append("AND resolved = ? ");
        }

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (text != null && !text.isEmpty()) {
                ps.setString(paramIndex++, "%" + text + "%");
                ps.setString(paramIndex++, "%" + text + "%");
            }
            if (author != null && !author.isEmpty()) {
                ps.setString(paramIndex++, author);
            }
            if (resolved != null) {
                ps.setBoolean(paramIndex++, resolved);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String txt = rs.getString("text");
                    boolean res = rs.getBoolean("resolved");
                    String auth = rs.getString("author");
                    String title = rs.getString("title");

                    Question q = new Question(id, txt, auth, title);
                    q.setResolved(res);
                    results.add(q);
                }
            }
        }
        return results;
    }

    /**
     * Returns the total number of questions in the database.
     *
     * @return number of questions
     * @throws SQLException if a database error occurs
     */
    public int countTotalQuestions() throws SQLException {
        String sql = "SELECT COUNT(*) FROM questions";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Returns the total number of resolved questions in the database.
     *
     * @return number of resolved questions
     * @throws SQLException if a database error occurs
     */
    public int countResolvedQuestions() throws SQLException {
        String sql = "SELECT COUNT(*) FROM questions WHERE resolved = TRUE";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
