package net.thestig294.mctournament.minigame.triviamurderparty.killingroom.deathroom;

import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.BlockPos;
import net.thestig294.mctournament.minigame.triviamurderparty.killingroom.DeathRoom;

import java.util.List;

public class Anvil implements DeathRoom {
    @Override
    public String getID() {
        return "anvil";
    }

    @Override
    public void begin(List<Team> deadTeams, BlockPos roomPos) {

    }
}
