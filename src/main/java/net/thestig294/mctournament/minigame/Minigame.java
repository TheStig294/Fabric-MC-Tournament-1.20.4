package net.thestig294.mctournament.minigame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.thestig294.mctournament.tournament.Tournament;

public abstract class Minigame {
    private final MinigameScoreboard scoreboard;

    public Minigame() {
        this.scoreboard = new MinigameScoreboard();
    }

    public abstract Text getName();

    public abstract void serverInit();

    public abstract void serverBegin();

    public abstract void serverEnd();

    @Environment(EnvType.CLIENT)
    public abstract void clientInit();

    @Environment(EnvType.CLIENT)
    public abstract void clientBegin();

    @Environment(EnvType.CLIENT)
    public abstract void clientEnd();

//    Must be called at the end of a minigame to clean up any events for the next minigame,
//    and for the Tournament instance to tally the scores from the minigame's scoreboard
    public void endMinigame(boolean isClient) {
        Tournament.getInstance().endRound(isClient);
    }

    public MinigameScoreboard getScoreboard() {
        return this.scoreboard;
    }
}
