package net.thestig294.mctournament.minigame.triviamurderparty.question;

public record Question(
        String question,
        String answer1,
        String answer2,
        String answer3,
        String answer4,
        int correctAnswer
) {
    public boolean isCorrect(int answerNumber) {
        return answerNumber == this.correctAnswer;
    }

    public String getAnswer(int answerNumber) {
        return switch (answerNumber){
            case 1 -> this.answer1;
            case 2 -> this.answer2;
            case 3 -> this.answer3;
            default -> this.answer4;
        };
    }
}
