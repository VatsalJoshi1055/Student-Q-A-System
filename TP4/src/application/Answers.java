package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import databasePart1.DatabaseHelper;

public class Answers {
    private final Connection connection;
    private final ObservableList<Answer> allAnswers = FXCollections.observableArrayList();
    
    
    private final DatabaseHelper dbHelper;

    public Answers(Connection connection, DatabaseHelper dbHelper) {
        this.connection = connection;
        this.dbHelper = dbHelper;
    }

    public DatabaseHelper getDatabaseHelper() {
        return dbHelper;
    }

    public void createTable() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS answers (
                  id IDENTITY PRIMARY KEY,
                  question_id BIGINT NOT NULL,
                  text VARCHAR(255) NOT NULL,
                  author VARCHAR(255) NOT NULL,
                  likes INT DEFAULT 0,
                  dislikes INT DEFAULT 0,
                  is_review INT,
                  parent_answer_id BIGINT,
                  FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
                  FOREIGN KEY (parent_answer_id) REFERENCES answers(id) ON DELETE CASCADE
                )
            """);
        }
        /*try (Statement st = connection.createStatement()) {
            st.execute("ALTER TABLE answers ADD COLUMN dislikes INT DEFAULT 0");
        } catch (SQLException e) {
            if (!e.getMessage().contains("Column \"DISLIKES\" already exists")) {
                e.printStackTrace();
            }
        }*/
        /*try(Statement st = connection.createStatement()) {
            st.execute("ALTER TABLE answers ADD COLUMN is_review INT");
        } catch (SQLException e) {
            if (!e.getMessage().contains("Column \"IS_REVIEW\" already exists")) {
                e.printStackTrace();
            }
        }

        // PARENT_ANSWER_ID
        try (Statement st = connection.createStatement()) {
            st.execute("ALTER TABLE answers ADD COLUMN parent_answer_id BIGINT");
        } catch (SQLException e) {
            if (!e.getMessage().contains("Column \"PARENT_ANSWER_ID\" already exists")) {
                e.printStackTrace();
            }
        }*/

    }

    public void loadAllFromDB() throws SQLException {
        allAnswers.clear();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("""
                 SELECT id, question_id, text, author, likes, dislikes, is_review, parent_answer_id
                 FROM answers
             """)) {
            while (rs.next()) {
                long id = rs.getLong("id");
                long qId = rs.getLong("question_id");
                String txt = rs.getString("text");
                String author = rs.getString("author");
                int likes = rs.getInt("likes");
                int dislikes = rs.getInt("dislikes");
                int isReview = rs.getInt("is_review");
                Long parentId = (rs.getObject("parent_answer_id") != null)
                                ? rs.getLong("parent_answer_id")
                                : null;

                Answer ans = new Answer(id, qId, txt, likes, dislikes, author, isReview, parentId);
                allAnswers.add(ans);
            }
        }
    }

    public void linkAnswersToQuestions(ObservableList<Question> questions) {
        for (Answer ans : allAnswers) {
            for (Question q : questions) {
                if (q.getId() == ans.getQuestionId()) {
                    q.getAnswers().add(ans);
                    break;
                }
            }
        }
    }

    public void createAnswer(Question question, String text, User user, int roleVal) {
        createAnswer(question, text, user, roleVal, null);
    }

    public void createAnswer(Question question, String text, User user, int roleVal, Long parentAnswerId) {
        try {
            long newId = insertAnswerDB(question.getId(), text, user.getUserName(), roleVal, parentAnswerId);
            Answer ans = new Answer(newId, question.getId(), text,
                                    0, 0, // likes, dislikes
                                    user.getUserName(), roleVal, parentAnswerId);
            allAnswers.add(ans);
            question.getAnswers().add(ans);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateAnswer(Answer ans, String newText) {
        try {
            String sql = "UPDATE answers SET text=? WHERE id=?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, newText);
                ps.setLong(2, ans.getId());
                ps.executeUpdate();
            }
            ans.setText(newText);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteAnswer(Answer ans, Question q) {
        try {
            String sql = "DELETE FROM answers WHERE id=?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setLong(1, ans.getId());
                ps.executeUpdate();
            }
            allAnswers.remove(ans);
            q.getAnswers().remove(ans);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Answer> getAnswersForQuestion(long questionId) {
        List<Answer> subset = new ArrayList<>();
        for (Answer ans : allAnswers) {
            if (ans.getQuestionId() == questionId) {
                subset.add(ans);
            }
        }
        return subset;
    }

   
    public void markHelpful(Answer ans) {
        try {
            String sql = "UPDATE answers SET likes = likes + 1 WHERE id=?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setLong(1, ans.getId());
                ps.executeUpdate();
            }
            ans.incrementLikes();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    public void markNotHelpful(Answer ans) {
        try {
            String sql = "UPDATE answers SET dislikes = dislikes + 1 WHERE id=?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setLong(1, ans.getId());
                ps.executeUpdate();
            }
            ans.incrementDislikes();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public List<Answer> getAllAnswers() {

        return allAnswers;
    }

    private long insertAnswerDB(long questionId, String text, String author,
                                int roleVal, Long parentAnswerId) throws SQLException {
        String sql = """
            INSERT INTO answers (question_id, text, author, is_review, parent_answer_id)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, questionId);
            ps.setString(2, text);
            ps.setString(3, author);
            ps.setInt(4, roleVal);
            if (parentAnswerId != null) {
                ps.setLong(5, parentAnswerId);
            } else {
                ps.setNull(5, java.sql.Types.BIGINT);
            }
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("Failed to insert answer.");
    }
}