package net.thestig294.mctournament.minigame.triviamurderparty.killingroom.room;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.thestig294.mctournament.minigame.triviamurderparty.killingroom.KillingRoom;

public class Tattoos extends KillingRoom {

    @Override
    public Properties properties() {
        return new Properties("tattoos", "screen.mctournament.killing_room_tattoos",
                new Description("screen.mctournament.killing_room_tattoos_description_1", 3),
                new Description("screen.mctournament.killing_room_tattoos_description_2", 3)
        );
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
