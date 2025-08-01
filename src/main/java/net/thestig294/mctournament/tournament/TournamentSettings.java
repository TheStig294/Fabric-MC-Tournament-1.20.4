package net.thestig294.mctournament.tournament;

import net.thestig294.mctournament.minigame.Minigame;
import net.thestig294.mctournament.minigame.Minigames;

import java.util.ArrayList;
import java.util.List;

public class TournamentSettings {
    private List<Minigame> minigames;

    public TournamentSettings() {
        this.minigames = new ArrayList<>();
    }

    public TournamentSettings minigames(List<Minigame> minigames) {
        this.minigames = minigames;
        return this;
    }

    public TournamentSettings minigames(int count) {
        this.minigames = Minigames.getRandomMinigames(count);
        return this;
    }

    public List<Minigame> getMinigames() {
        return this.minigames;
    }
}
