package net.thestig294.mctournament.minigame.triviamurderparty.question;

public record Question(
        String question,
        String answer0,
        String answer1,
        String answer2,
        String answer3,
        int correctAnswer,
        float holdTime,
        int id
) {
    public boolean isCorrect(int answerNumber) {
        return answerNumber == this.correctAnswer;
    }

    public String getAnswer(int answerNumber) {
        return switch (answerNumber){
            case 0 -> this.answer0;
            case 1 -> this.answer1;
            case 2 -> this.answer2;
            default -> this.answer3;
        };
    }
}
