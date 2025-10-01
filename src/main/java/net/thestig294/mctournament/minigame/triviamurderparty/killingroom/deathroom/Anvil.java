package net.thestig294.mctournament.minigame.triviamurderparty.killingroom.deathroom;

import net.thestig294.mctournament.minigame.triviamurderparty.killingroom.DeathRoom;

public class Anvil extends DeathRoom {
    @Override
    public String getID() {
        return "anvil";
    }

    @Override
    public float getDeathDelay() {
        return 6.0f;
    }

    @Override
    public void begin() {

    }
}
