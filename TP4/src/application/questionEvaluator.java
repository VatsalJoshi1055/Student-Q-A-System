package application;

public class questionEvaluator {
	private static String questionErrorMessage = "";
	
	public static String checkQuestion(String title, String question) {
		//resets the error message after each test case
		questionErrorMessage = "";
		
		//checks if either the question or title fields are empty
		if(question.length() <= 0) {
			questionErrorMessage += "Question input field is empty; ";
			
			if(title.length() <= 0) {
				questionErrorMessage += "Question title field is empty; ";
			}
			
			return questionErrorMessage;
			
		} else if(title.length() <= 0) {
			questionErrorMessage += "Question title field is empty; ";
			
			return questionErrorMessage;
		}
		
		//checks if the title meets the length requirement
		if(title.length() > 20) {
			questionErrorMessage += "Question title is too long; ";
		}
		
		//checks if the question title starts with a capitol letter
		String firstChar = title.charAt(0) + "";
		if(firstChar != firstChar.toUpperCase()) {
			questionErrorMessage += "Question title must start with upper case letter; ";
		}
		
		//checks if the question ends in the proper character
		if(question.charAt(question.length() - 1) != '?') {
			questionErrorMessage += "Question must end with a ?; ";
		}
		
		//checks if the question meets the length requirement
		if(question.length() < 10 || question.length() > 450) {
			questionErrorMessage += "Question is not a proper length; ";
		}
		
		//checks if the question starts with a capital letter
		firstChar = question.charAt(0) + "";
		if(firstChar != firstChar.toUpperCase()) {
			questionErrorMessage += "Question must start with upper case letter; ";
		}
		
		return questionErrorMessage;
	}
}