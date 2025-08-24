package application;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * 	This class holds all of the JUnit tests
 * 
 *  <p> Title: JUnitClass </p>
 *	
 * 	<p> Description: This class holds all of the JUnit tests </p>	
 * 
 * 	@author Miguel Sued
 */

public class JUnit {

	/**
	 * 	Stores a question input that is too long to be valid
	 */
	
	String maxLengthQuestion = "Grades will not be curved and will not be rounded up. Requesting curving, rounding of grades, or\r\n"
			+ "raising a grade for any reason (e.g., to help you avoid losing a scholarship) is unethical and will be\r\n"
			+ "reported. Grades reflect your performance on assignments and adherence to deadlines. Grades are\r\n"
			+ "independent of how well any other student performs in the class.\r\n"
			+ "\r\n"
			+ "The only way to “improve” your grade is to be proactive. Rather than asking for extra work to make\r\n"
			+ "up for a poor grade on an assignment, be proactive, start early, identify things that you do not know\r\n"
			+ "well, ask questions on the Ed Discussions, and ask questions in class. Asking for an extra credit\r\n"
			+ "assignment or an opportunity to do an assignment or exam over again will not be successful.";
			
	/**
	 * 	Stores an answer input that is too long to be valid
	 */
	
	String maxLengthAnswer = "Grades will not be curved and will not be rounded up. Requesting curving, rounding of grades, or\r\n"
			+ "raising a grade for any reason (e.g., to help you avoid losing a scholarship) is unethical and will be\r\n"
			+ "reported. Grades reflect your performance on assignments and adherence to deadlines. Grades are\r\n"
			+ "independent of how well any other student performs in the class.\r\n"
			+ "\r\n"
			+ "The only way to “improve” your grade is to be proactive. Rather than asking for extra work to make\r\n"
			+ "up for a poor grade on an assignment, be proactive, start early, identify things that you do not know\r\n"
			+ "well, ask questions on the Ed Discussions, and ask questions in class. Asking for an extra credit\r\n"
			+ "assignment or an opportunity to do an assignment or exam over again will not be successful.\r\n"
			+ "\r\n"
			+ "The grade of \"I\" (incomplete) can only be given when a student, who is otherwise doing good work\r\n"
			+ "(i.e., a passing grade), is unable to complete a part of work (e.g., the final exam) because of\r\n"
			+ "documented illness or other ASU-recognized extenuating circumstances beyond the student's\r\n"
			+ "control. In the latter case, the student must discuss with the instructor and complete an application\r\n"
			+ "form from the department before the work is due or as soon as the circumstances are known. Please\r\n"
			+ "see ASU grading policies at https://students.asu.edu/grades.";
	
	/**
	 * A basic test consisting of both question inputs being valid.
	 */
	
	@Test
	public void QuestionTest1() {
		assertEquals("", questionEvaluator.checkQuestion("CSE360", "What is CSE360?"));
	}

	/**
	 * Tests the requirement of the question body to end in a question mark by having an input that lacks it. 
	 */
	
	@Test
	public void QuestionTest2() {
		assertNotEquals("", questionEvaluator.checkQuestion("CSE360",  "What is CSE360"));
	}
	
	/**
	 * Tests the length requirement of the question body by having an input that is too short. 
	 */
	
	@Test
	public void QuestionTest3() {
		assertNotEquals("", questionEvaluator.checkQuestion("CSE360",  "CSE360?"));
	}
	
	/**
	 * Test if both the length and lack of question mark errors can both trigger at once. 
	 */
	
	@Test
	public void QuestionTest4() {
		assertNotEquals("", questionEvaluator.checkQuestion("CSE360",  "CSE360"));
	}
	
	/**
	 * Tests the requirement that the question body starts with a capital letter.
	 */
	
	@Test
	public void QuestionTest5() {
		assertNotEquals("", questionEvaluator.checkQuestion("CSE360",  "what is CSE360?"));
	}
	
	/**
	 * Tests the length cap of the question body by passing a very long input.
	 */
	
	@Test
	public void QuestionTest6() {
		assertNotEquals("", questionEvaluator.checkQuestion("CSE360",  maxLengthQuestion));
	}
	
	/**
	 * Tests the length limit of the question title by passing an input that is too long.
	 */
	
	@Test
	public void QuestionTest7() {
		assertNotEquals("", questionEvaluator.checkQuestion("CSE360 is the class of all time",  "What is CSE360?"));
	}
	
	/**
	 * Testing the requirement that the question title start with a capital letter.
	 */
	
	@Test
	public void QuestionTest8() {
		assertNotEquals("", questionEvaluator.checkQuestion("cse360",  "What is CSE360?"));
	}
	
	/**
	 * Verifies that an empty question body input will not be accepted into the database.
	 */
	
	@Test
	public void QuestionTest9() {
		assertNotEquals("", questionEvaluator.checkQuestion("CSE360",  ""));
	}
	
	/**
	 * Verifies that an empty question title input will not be accepted into the database. 
	 */

	@Test
	public void QuestionTest10() {
		assertNotEquals("", questionEvaluator.checkQuestion("",  "What is CSE360?"));
	}
	
	/**
	 * Verifies that the input will not be accepted into the database when both inputs are empty
	 */
	
	@Test
	public void QuestionTest11() {
		assertNotEquals("", questionEvaluator.checkQuestion("",  ""));
	}
	
	/**
	 * A basic test with a simple and valid input.
	 */
	
	@Test
	public void AnswerTest1() {
		assertEquals("", answerEvaluator.checkAnswer("It is a class."));
	}
	
	/**
	 * Tests the length requirement of answers by having an input that is too short. 
	 */
	
	@Test
	public void AnswerTest2() {
		assertNotEquals("", answerEvaluator.checkAnswer("It is a class"));
	}
	
	/**
	 * 	Tests the requirement that the answer ends in a period or a question mark.
	 */
	
	@Test
	public void AnswerTest3() {
		assertNotEquals("", answerEvaluator.checkAnswer("..."));
	}
	
	/**
	 * Tests the requirement that answers must start with a capital letter. 
	 */
	
	@Test
	public void AnswerTest4() {
		assertNotEquals("", answerEvaluator.checkAnswer("Hello"));
	}
	
	/**
	 * Tests if both the errors for length and lack of a question mark or period can trigger at once. 
	 */
	
	@Test
	public void AnswerTest5() {
		assertNotEquals("", answerEvaluator.checkAnswer("it is a class."));
	}
	
	/**
	 * Tests the length cap on answers by passing an answer that is too long.
	 */
	
	@Test
	public void AnswerTest6() {
		assertNotEquals("", answerEvaluator.checkAnswer(maxLengthAnswer));
	}
	
	/**
	 * Verifies that an empty answer input will not be accepted into the database.
	 */
	
	@Test
	public void AnswerTest7() {
		assertNotEquals("", answerEvaluator.checkAnswer(""));
	}
	
	/**
	 *	Tests the requirement that an answer can end in either a question mark or a period 
	 */
	
	@Test
	public void AnswerTest8() {
		assertEquals("", answerEvaluator.checkAnswer("Can you please clarify?"));
	}
	
	/**
	 * Tests the word filter on answers with one of the banned words. 
	 */
	
	@Test
	public void AnswerTest9() {
		assertNotEquals("", answerEvaluator.checkAnswer("I hope we get a curve."));
	}
	
	/**
	 * Tests the word filter on answers with one of the banned words. 
	 */
	
	@Test
	public void AnswerTest10() {
		assertNotEquals("", answerEvaluator.checkAnswer("You should use ChatGPT."));
	}
	
	/**
	 * Tests the word filter on answers with one of the banned words. 
	 */
	
	@Test
	public void AnswerTest11() {
		assertNotEquals("", answerEvaluator.checkAnswer("Have AI write your project."));
	}
	
	/**
	 * Tests the word filter on answers with one of the banned words. 
	 */
	
	@Test
	public void AnswerTest12() {
		assertNotEquals("", answerEvaluator.checkAnswer("Maybe you can ask for an extension."));
	}
	
	/**
	 * Tests the word filter on answers with multiple of the banned words. 
	 */
	
	@Test
	public void AnswerTest13() {
		assertNotEquals("", answerEvaluator.checkAnswer("I love curve, extension, AI and ChatGPT"));
	}
	
	/**
	 * Tests the word filter on questions with one of the banned words. 
	 */
	
	@Test
	public void QuestionTest12() {
		assertNotEquals("", questionEvaluator.checkQuestion("Success in 360",  "Can I use ChatGPT?"));
	}
	
	/**
	 * Tests the word filter on questions with one of the banned words. 
	 */
	
	@Test
	public void QuestionTest13() {
		assertNotEquals("", questionEvaluator.checkQuestion("Success in 360",  "Can I use AI?"));
	}
	
	/**
	 * Tests the word filter on questions with one of the banned words. 
	 */
	
	@Test
	public void QuestionTest14() {
		assertNotEquals("", questionEvaluator.checkQuestion("Please curve on midterm",  "I hope to boost my grade?"));
	}
	
	/**
	 * Tests the word filter on questions with one of the banned words. 
	 */
	
	@Test
	public void QuestionTest15() {
		assertNotEquals("", questionEvaluator.checkQuestion("Please extension on HW3",  "Can we get more time?"));
	}
	
	/**
	 * Tests the word filter on questions with multiple of the banned words in the title. 
	 */
	
	@Test
	public void QuestionTest16() {
		assertNotEquals("", questionEvaluator.checkQuestion("I like curve and extension",  "Can I use them to boost my grade?"));
	}
	
	/**
	 * Tests the word filter on questions with multiple of the banned words in the body. 
	 */
	
	@Test
	public void QuestionTest17() {
		assertNotEquals("", questionEvaluator.checkQuestion("Success in 360",  "Can I use AI and ChatGPT?"));
	}
}
