package net.thestig294.mctournament.minigame;

import net.minecraft.text.Text;
import net.thestig294.mctournament.tournament.Tournament;

public abstract class Minigame {
    private final MinigameScoreboard scoreboard;

    public Minigame() {
        this.scoreboard = new MinigameScoreboard();
    }

    public abstract Text getName();

    public abstract void preHookInit();

    public abstract void setHooks();

    public abstract void postHookInit();

    public abstract void begin();

    public abstract void cleanup();

//    Must be called at the end of a minigame to clean up any events for the next minigame,
//    and for the Tournament instance to tally the scores from the minigame's scoreboard
    public void endMinigame() {
        Tournament.getInstance().endRound();
    }

    public MinigameScoreboard getScoreboard() {
        return this.scoreboard;
    }
}
