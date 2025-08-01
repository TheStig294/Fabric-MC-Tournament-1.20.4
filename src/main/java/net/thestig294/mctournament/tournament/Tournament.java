package net.thestig294.mctournament.tournament;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.text.Text;
import net.thestig294.mctournament.minigame.Minigame;
import net.thestig294.mctournament.minigame.Minigames;
import net.thestig294.mctournament.util.ModUtil;

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
    }

    public Minigame getMinigame() {
        return this.minigame;
    }

    @SuppressWarnings("unused")
    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    public void endRound() {
        this.minigame.sharedCleanup();
        if (ModUtil.isClient()) {
            this.minigame.clientCleanup();
        }

        this.round++;
        this.updateMinigame();
    }

    private void updateMinigame() {
        if (this.minigame != null && this.minigame.equals(Minigames.TOURNAMENT_END)) {
            this.endTournament();
            return;
        } else if (this.round >= this.minigames.size()) {
            this.minigame = Minigames.TOURNAMENT_END;
        } else {
            this.minigame = this.minigames.get(this.round);
        }

        this.minigame.sharedBegin();

        if (ModUtil.isClient()) {
            this.minigame.clientBegin();
        }
    }

    public void endTournament() {
        ModUtil.SERVER.getPlayerManager().broadcast(Text.translatable("tournament.mctournament.end_message"), true);
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
