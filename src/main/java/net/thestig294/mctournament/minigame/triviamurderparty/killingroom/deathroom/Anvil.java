package net.thestig294.mctournament.minigame.triviamurderparty.killingroom.deathroom;

import net.minecraft.util.math.BlockPos;
import net.thestig294.mctournament.minigame.triviamurderparty.killingroom.DeathRoom;

import java.util.Set;

public class Anvil extends DeathRoom {
    @Override
    public String getID() {
        return "anvil";
    }

    @Override
    public float getDeathDelay() {
        return 3.0f;
    }

    @Override
    public void begin(BlockPos roomPos, Set<Integer> killableTeamNumbers) {

    }
}
