package net.thestig294.mctournament.minigame.triviamurderparty.screen;

import net.minecraft.util.math.BlockPos;
import net.thestig294.mctournament.minigame.MinigameScoreboard;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;

public class KillingRoomScreenHandler {
    private final TriviaMurderParty minigame;
    private final MinigameScoreboard scoreboard;
    private BlockPos roomStartingPos;

    public KillingRoomScreenHandler(TriviaMurderParty minigame, MinigameScoreboard scoreboard) {
        this.minigame = minigame;
        this.scoreboard = scoreboard;
        this.roomStartingPos = BlockPos.ORIGIN;
    }

    public void begin(BlockPos pos) {
        this.roomStartingPos = pos;
    }

    public void broadcastNextKillingRoom() {

    }
}
