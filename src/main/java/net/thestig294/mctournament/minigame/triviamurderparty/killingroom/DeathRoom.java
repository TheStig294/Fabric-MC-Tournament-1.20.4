package net.thestig294.mctournament.minigame.triviamurderparty.killingroom;

import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public interface DeathRoom {
    String getID();

    void begin(List<Team> deadTeams, BlockPos roomPos);
}
