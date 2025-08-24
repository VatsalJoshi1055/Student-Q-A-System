/**
 * StaffFeatureTests.java
 *
 * JUnit tests for verifying staff-role features in the question management system.
 *
 * Author: Vatsal Joshi
 */

package test;

import application.Question;
import application.Questions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for verifying the staff-role features implemented in the Questions class.
 */
public class StaffFeatureTests {

    private static Connection connection;
    private static Questions questions;

    /**
     * Initializes the in-memory database and creates the questions table.
     *
     * @throws Exception if setup fails due to database issues.
     */
    @BeforeAll
    public static void setupDatabase() throws Exception {
        Class.forName("org.h2.Driver");
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
        questions = new Questions(connection);
        questions.createTable();
    }

    /**
     * Clears all existing questions before each test to ensure test isolation.
     */
    @BeforeEach
    public void clearQuestions() {
        List<Question> allQuestions = new ArrayList<>(questions.getAllQuestions());
        for (Question q : allQuestions) {
            questions.deleteQuestion(q);
        }
    }

    /**
     * Tests that a question is successfully created and added to the in-memory list.
     */
    @Test
    public void testCreateAndGetQuestion() {
        questions.createQuestion("Test text", "staff", "Test title");
        assertEquals(1, questions.getAllQuestions().size(), "Question should be added");
    }

    /**
     * Tests that staff notes can be updated and retrieved properly for a question.
     */
    @Test
    public void testUpdateStaffNote() {
        questions.createQuestion("Test note text", "staff", "Note title");
        Question q = questions.getAllQuestions().get(0);
        questions.updateStaffNote(q, "This is a test note.");
        assertEquals("This is a test note.", q.getStaffNote(), "Note should match");
    }

    /**
     * Tests that unanswered questions are correctly filtered.
     */
    @Test
    public void testGetUnansweredQuestions() {
        questions.createQuestion("Unanswered text", "staff", "Unanswered title");
        Question q = questions.getAllQuestions().get(0);
        assertTrue(questions.getUnansweredQuestions().contains(q), "Should be in unanswered");
    }

    /**
     * Tests that a question can be deleted from the in-memory list and database.
     */
    @Test
    public void testDeleteQuestion() {
        questions.createQuestion("Delete me", "staff", "Delete title");
        Question q = questions.getAllQuestions().get(0);
        questions.deleteQuestion(q);
        assertFalse(questions.getAllQuestions().contains(q), "Question should be deleted");
    }

    /**
     * Closes the in-memory database connection after all tests.
     *
     * @throws Exception if closing the connection fails.
     */
    @AfterAll
    public static void cleanup() throws Exception {
        connection.close();
    }
}
