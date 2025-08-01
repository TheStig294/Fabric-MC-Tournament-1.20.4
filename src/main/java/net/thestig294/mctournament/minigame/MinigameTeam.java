package net.thestig294.mctournament.minigame;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;

public class MinigameTeam extends Team {
    public MinigameTeam(Scoreboard scoreboard, String name) {
        super(scoreboard, name);
    }
}
