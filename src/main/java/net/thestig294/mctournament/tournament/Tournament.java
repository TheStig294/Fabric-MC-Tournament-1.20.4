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

//    If setup is called without saying if we're on the server or client, the tournament assumes we're server-side only
    public void setup(TournamentSettings settings) {
        this.setup(settings, false);
    }

    public void setup(TournamentSettings settings, boolean isClient) {
        this.scoreboard = new TournamentScoreboard();
        this.round = 0;

        this.minigames = settings.getMinigames();
        this.updateMinigame(isClient);
    }

    public Minigame getMinigame() {
        return this.minigame;
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    public void endRound(boolean isClient) {
        if (isClient) {
            this.minigame.clientEnd();
        } else {
            this.minigame.serverEnd();
            this.round++;
        }

        this.updateMinigame(isClient);
    }

    private void updateMinigame(boolean isClient) {
        if (this.minigame != null && this.minigame.equals(Minigames.TOURNAMENT_END)) {
            this.endTournament(isClient);
            return;
        } else if (this.round >= this.minigames.size()) {
            this.minigame = Minigames.TOURNAMENT_END;
        } else {
            this.minigame = this.minigames.get(this.round);
        }

        if (isClient) {
            this.minigame.clientBegin();
        } else {
            this.minigame.serverBegin();
        }
    }

    public void endTournament(boolean isClient) {
        if (!isClient) {
            MCTournament.SERVER.getPlayerManager().broadcast(Text.translatable("tournament.mctournament.end_message"), true);
        }
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
