package net.thestig294.mctournament.tournament;

import net.minecraft.util.Identifier;
import net.thestig294.mctournament.minigame.Minigames;

import java.util.ArrayList;
import java.util.List;

public class TournamentSettings {
    private List<Identifier> minigameIDs;

    public TournamentSettings() {
        this.minigameIDs = new ArrayList<>();
    }

    public TournamentSettings minigames(List<Identifier> minigames) {
        this.minigameIDs = minigames;
        return this;
    }

    public TournamentSettings minigames(int count) {
        this.minigameIDs = Minigames.getRandomMinigames(count);
        return this;
    }

    public List<Identifier> getMinigames() {
        return this.minigameIDs;
    }
}
