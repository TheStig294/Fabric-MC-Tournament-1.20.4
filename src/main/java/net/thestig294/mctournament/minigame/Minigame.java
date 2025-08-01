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

    public abstract void sharedInit();

    public abstract void sharedBegin();

    public abstract void sharedCleanup();

    @Environment(EnvType.CLIENT)
    public abstract void clientInit();

    @Environment(EnvType.CLIENT)
    public abstract void clientBegin();

    @Environment(EnvType.CLIENT)
    public abstract void clientCleanup();

//    Must be called at the end of a minigame to clean up any events for the next minigame,
//    and for the Tournament instance to tally the scores from the minigame's scoreboard
    @SuppressWarnings("unused")
    public void endMinigame() {
        Tournament.getInstance().endRound();
    }

    @SuppressWarnings("unused")
    public MinigameScoreboard getScoreboard() {
        return this.scoreboard;
    }
}
