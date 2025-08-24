package application;

public class Answer {
    private long id;
    private long questionId;
    private String text;
    private int likes;       
    private int dislikes;    
    private String author;
    private int is_review;   
    private Long parentAnswerId; 

    public Answer(long id, long questionId, String text, int likes, int dislikes,
                  String author, int is_review, Long parentAnswerId) {
        this.id = id;
        this.questionId = questionId;
        this.text = text;
        this.likes = likes;
        this.dislikes = dislikes;
        this.author = author;
        this.is_review = is_review;
        this.parentAnswerId = parentAnswerId;
    }

    
    public Answer(long id, long questionId, String text, int likes, int dislikes,
                  String author, int is_review) {
        this(id, questionId, text, likes, dislikes, author, is_review, null);
    }

    public long getId() {
        return id;
    }

    public long getQuestionId() {
        return questionId;
    }

    public String getText() {
        return text;
    }

    public void setText(String newText) {
        this.text = newText;
    }

    public int getLikes() {
        return likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    public void incrementLikes() {
        this.likes++;
    }

    public void incrementDislikes() {
        this.dislikes++;
    }

    public String getAuthor() {
        return author;
    }

    public boolean isReview() {
        return (this.is_review == 1);
    }

    public Long getParentAnswerId() {
        return parentAnswerId;
    }

   
    private double computeReviewScore() {
        int total = likes + dislikes;
        if (total == 0) {
            
            return 0.0;
        }
        double numerator = (double)likes - (double)dislikes;
        double fraction = numerator / total;  
        double rawScore = (fraction * 4) + 3; 
        rawScore = Math.max(1.0, Math.min(5.0, rawScore)); 
        return rawScore;
    }

    @Override
    public String toString() {
        if (isReview()) {
            
            int total = likes + dislikes;
            if (total == 0) {
                return "   [Review by " + author + "] " + text + " (No rating yet)";
            } else {
                double score = computeReviewScore();
                return String.format("   [Review by %s] %s [Rating: %.2f/5.00]", author, text, score);
            }
        } else {
           
            return String.format("%s [Helpful: %d, Not helpful: %d]", text, likes, dislikes);
        }
    }
}