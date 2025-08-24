package application;

public class answerEvaluator {
    private static String answerErrorMessage = "";

    public static String checkAnswer(String input) {
        //resets the error message for every test case
        answerErrorMessage = "";

        //checks if the answer field is empty
        if(input.length() <= 0) {
            answerErrorMessage += "Answer input field is empty; ";
            return answerErrorMessage;
        }

        //checks if the answer meets the length requirement
        if(input.length() < 5 || input.length() > 950) {
            answerErrorMessage += "Answer is not a proper length; ";
        }


        //checks if the answer ends in the proper character
        if(input.charAt(input.length() - 1) != '.' && input.charAt(input.length() - 1) != '?') {
            answerErrorMessage += "Answer must end in a . or a ?; ";
        }

        //checks if the answer starts with a capital letter
        String firstChar = input.charAt(0) + "";
        if(firstChar != firstChar.toUpperCase()) {
            answerErrorMessage += "Answer must start with upper case letter; ";
        }

        //checks if the answer contains a word that is not allowed or is unethical
        String[] bannedWords = {"ChatGPT", "extension", "curve", "AI"};

        for(int i = 0; i< bannedWords.length; i++) {
            if(input.contains(bannedWords[i])) {
                answerErrorMessage += "Your answer contains a word that is prohibited";
                break;
            }
        }

        return answerErrorMessage;
    }
}