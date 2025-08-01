package net.thestig294.mctournament.tournament;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.text.Text;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.Minigame;
import net.thestig294.mctournament.minigame.Minigames;

import java.util.List;

public class Tournament {
    private TournamentScoreboard scoreboard;
    private int round;
    private Minigame minigame;
    private List<Minigame> minigames;

    public void setup(TournamentSettings settings) {
        this.scoreboard = new TournamentScoreboard();
        this.round = 0;

        this.minigames = settings.getMinigames();
        this.updateMinigame();

        this.minigame.preHookInit();
        this.minigame.setHooks();
        this.minigame.postHookInit();
        this.minigame.begin();
    }

    public Minigame getMinigame() {
        return this.minigame;
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    private void updateMinigame() {
        if (this.minigame == null) {
            this.minigame = this.minigames.get(this.round);
        } else if (this.minigame.equals(Minigames.TOURNAMENT_END)) {
            this.endTournament();
        } else if (this.round >= this.minigames.size()) {
            this.minigame = Minigames.TOURNAMENT_END;
        } else {
            this.minigame = this.minigames.get(this.round);
        }
    }

    public void endRound() {
        this.minigame.cleanup();
        this.round++;
        this.updateMinigame();
    }

    public void endTournament() {
        MCTournament.SERVER.getPlayerManager().broadcast(Text.translatable("tournament.mctournament.end_message"), true);
    }


//    A "Bill Pugh" Singleton implementation
    private Tournament() {}
    private static class Singleton {
        private static final Tournament INSTANCE = new Tournament();
    }
    public static Tournament getInstance() {
        return Singleton.INSTANCE;
    }
}
