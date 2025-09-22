package net.thestig294.mctournament.minigame.triviamurderparty.killingroom.room;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.thestig294.mctournament.minigame.triviamurderparty.killingroom.KillingRoom;

public class Tattoos extends KillingRoom {

    @Override
    public Properties properties() {
        return new Properties(0,0,0, "tattoos", 30, 3.0f, 3.0f);
    }

    @Override
    public void init() {

    }

    @Override
    public void begin() {

    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientInit() {

    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientBegin() {

    }
}
