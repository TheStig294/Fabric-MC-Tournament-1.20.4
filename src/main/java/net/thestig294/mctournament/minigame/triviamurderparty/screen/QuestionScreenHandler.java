package net.thestig294.mctournament.minigame.triviamurderparty.screen;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.thestig294.mctournament.minigame.MinigameScoreboard;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.minigame.triviamurderparty.question.Questions;
import net.thestig294.mctournament.network.ModNetworking;
import net.thestig294.mctournament.tournament.Tournament;
import net.thestig294.mctournament.util.ModTimer;

import java.util.HashMap;
import java.util.Map;

public class QuestionScreenHandler {
    public static final int CORRECT_ANSWER_POINTS = 1000;
    public static final int ANSWERING_TIME_SECONDS = 20;
//    The time in seconds the game secretly lets you answer even though the question timer is visibly up
    public static final int ANSWERING_TIME_FORGIVENESS = 1;

    private final TriviaMurderParty minigame;
    private final MinigameScoreboard scoreboard;
    private final Map<String, Boolean> answeredCaptains;

    public QuestionScreenHandler(TriviaMurderParty minigame, MinigameScoreboard scoreboard) {
        this.minigame = minigame;
        this.scoreboard = scoreboard;
        this.answeredCaptains = new HashMap<>();

        ModNetworking.serverReceive(TriviaMurderParty.NetworkIDs.QUESTION_ANSWERED, serverReceiveInfo -> {
            PacketByteBuf buffer = serverReceiveInfo.buf();
            String playerName = buffer.readString();
            boolean isCorrect = buffer.readBoolean();
            boolean isCaptain = buffer.readBoolean();
            int answerPosition = buffer.readInt();

            ModNetworking.broadcast(TriviaMurderParty.NetworkIDs.QUESTION_ANSWERED, PacketByteBufs.create()
                    .writeString(playerName)
                    .writeBoolean(isCorrect)
                    .writeBoolean(isCaptain)
                    .writeInt(answerPosition)
            );

            if (isCaptain && !this.answeredCaptains.containsKey(playerName)) {
                this.answeredCaptains.put(playerName, isCorrect);
                if (isCorrect) this.scoreboard.addScore(playerName, CORRECT_ANSWER_POINTS);

//                Ending answering once all captains have answered
                int answeredPlayers = this.answeredCaptains.size();
                int playersToAnswer = Tournament.inst().scoreboard().getValidTeamCaptains().size();

                if (answeredPlayers >= playersToAnswer) {
                    this.broadcastAnsweringEnd();
                }
            }
        });
    }

    public void begin() {
        Questions.shuffleCategory(this.minigame.getVariant());
        this.broadcastNextQuestionScreen();
    }

    public void broadcastNextQuestionScreen() {
        this.answeredCaptains.clear();

        ModNetworking.broadcast(TriviaMurderParty.NetworkIDs.QUESTION_SCREEN, PacketByteBufs.create()
                .writeInt(Questions.getNext().id())
                .writeInt(Questions.getQuestionNumber())
                .writeInt(ANSWERING_TIME_SECONDS)
        );

//        Ending answering time once time runs out, and the forgiveness buffer runs out too
        ModTimer.simple(false, ANSWERING_TIME_SECONDS + ANSWERING_TIME_FORGIVENESS, this::broadcastAnsweringEnd);
    }

    private void broadcastAnsweringEnd() {
        ModNetworking.broadcast(TriviaMurderParty.NetworkIDs.QUESTION_ANSWERING_END);
    }
}
