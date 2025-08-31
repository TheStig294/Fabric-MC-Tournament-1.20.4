package net.thestig294.mctournament.minigame.triviamurderparty.screen;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.thestig294.mctournament.minigame.MinigameScoreboard;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.minigame.triviamurderparty.question.Questions;
import net.thestig294.mctournament.network.ModNetworking;

import java.util.HashSet;
import java.util.Set;

public class QuestionScreenHandler {
    public static final int CORRECT_ANSWER_POINTS = 1000;

    private final TriviaMurderParty minigame;
    private final MinigameScoreboard scoreboard;
    private final Set<String> answeredPlayers;

    public QuestionScreenHandler(TriviaMurderParty minigame, MinigameScoreboard scoreboard) {
        this.minigame = minigame;
        this.scoreboard = scoreboard;
        this.answeredPlayers = new HashSet<>();

        ModNetworking.serverReceive(TriviaMurderParty.NetworkIDs.QUESTION_ANSWERED, serverReceiveInfo -> {
            PacketByteBuf buffer = serverReceiveInfo.buf();
            String playerName = buffer.readString();
            boolean isCorrect = buffer.readBoolean();
            boolean isCaptain = buffer.readBoolean();
            int answerPosition = buffer.readInt();

            if (this.answeredPlayers.contains(playerName)) return;

            if (isCorrect && isCaptain) {
                this.scoreboard.addScore(playerName, CORRECT_ANSWER_POINTS);
                this.answeredPlayers.add(playerName);
            }

            ModNetworking.broadcast(TriviaMurderParty.NetworkIDs.QUESTION_ANSWERED, PacketByteBufs.create()
                    .writeString(playerName)
                    .writeBoolean(isCorrect)
                    .writeBoolean(isCaptain)
                    .writeInt(answerPosition)
            );
        });
    }

    public void begin() {
        Questions.shuffleCategory(this.minigame.getVariant());
        this.broadcastNextQuestionScreen();
    }

    public void broadcastNextQuestionScreen() {
        this.answeredPlayers.clear();

        ModNetworking.broadcast(TriviaMurderParty.NetworkIDs.QUESTION_SCREEN, PacketByteBufs.create()
                .writeInt(Questions.getNext().id())
                .writeInt(Questions.getQuestionNumber())
        );
    }
}
