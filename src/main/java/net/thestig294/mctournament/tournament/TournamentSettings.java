package net.thestig294.mctournament.tournament;

import net.minecraft.util.Identifier;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.MinigameVariants;
import net.thestig294.mctournament.minigame.Minigames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TournamentSettings {
    private List<Identifier> minigameIDs;
    private List<String> variants;

    public TournamentSettings() {
        this.minigameIDs = new ArrayList<>();
        this.variants = new ArrayList<>();
    }

    public TournamentSettings minigames(Identifier... minigames) {
        this.minigameIDs = Arrays.asList(minigames);
        return this;
    }

    public TournamentSettings minigames(int count) {
        this.minigameIDs = Minigames.getRandomMinigames(count);
        return this;
    }

    public TournamentSettings variants(String... variants) {
        this.variants = Arrays.asList(variants).subList(0, this.minigameIDs.size());

        for (int i = 0; i < this.variants.size(); i++) {
//            Have to do this in Java just to test if 2 strings are equal lol...
            if (Objects.equals(this.variants.get(i), MinigameVariants.RANDOM)) {
                if (i >= this.minigameIDs.size()) {
                    MCTournament.LOGGER.error("""
                            Tried to set a random variant without specifying minigame. Setting to default.
                            Call TournamentSettings.minigames() first!
                            """);
                    this.variants.set(i, MinigameVariants.DEFAULT);
                } else {
                    this.variants.set(i, MinigameVariants.getRandomVariant(this.minigameIDs.get(i)));
                }
            }
        }

        return this;
    }

    public List<Identifier> getMinigames() {
        return this.minigameIDs;
    }

    public List<String> getVariants() {
        if (this.variants.size() < this.minigameIDs.size()) {
            int paddingAmount = this.minigameIDs.size() - this.variants.size();

            for (int i = 0; i < paddingAmount; i++){
                this.variants.add(MinigameVariants.DEFAULT);
            }
        }

        return this.variants;
    }
}
