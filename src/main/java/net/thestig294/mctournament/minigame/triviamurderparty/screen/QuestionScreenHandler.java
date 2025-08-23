package net.thestig294.mctournament.minigame.triviamurderparty.screen;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.thestig294.mctournament.minigame.MinigameScoreboard;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.minigame.triviamurderparty.question.Questions;
import net.thestig294.mctournament.network.ModNetworking;

public class QuestionScreenHandler {
    public static final int CORRECT_ANSWER_POINTS = 1000;

    private final TriviaMurderParty minigame;
    private final MinigameScoreboard scoreboard;

    public QuestionScreenHandler(TriviaMurderParty minigame, MinigameScoreboard scoreboard) {
        this.minigame = minigame;
        this.scoreboard = scoreboard;

        ModNetworking.serverReceive(TriviaMurderParty.NetworkIDs.QUESTION_ANSWERED, serverReceiveInfo -> {
            PacketByteBuf buffer = serverReceiveInfo.buf();
            boolean isCorrect = buffer.readBoolean();
            String playerName = buffer.readString();

            if (isCorrect) {
                this.scoreboard.addScore(playerName, CORRECT_ANSWER_POINTS);
            }

            ModNetworking.broadcast(TriviaMurderParty.NetworkIDs.QUESTION_ANSWERED, PacketByteBufs.create()
                    .writeString(playerName)
                    .writeBoolean(isCorrect)
            );
        });
    }

    public void begin() {
        Questions.shuffleCategory(this.minigame.getVariant());
        this.broadcastNextQuestionScreen();
    }

    private void broadcastNextQuestionScreen() {
        ModNetworking.broadcast(TriviaMurderParty.NetworkIDs.QUESTION_SCREEN, PacketByteBufs.create()
                .writeInt(Questions.getNext().id())
                .writeInt(Questions.getQuestionNumber())
        );
    }
}
