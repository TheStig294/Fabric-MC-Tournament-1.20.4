package net.thestig294.mctournament.minigame.minigames;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.thestig294.mctournament.minigame.Minigame;

public class MarioKart extends Minigame {
    @Override
    public Text getName() {
        return Text.translatable("minigame.mario_kart.name");
    }

    @Override
    public void sharedInit() {

    }

    @Override
    public void sharedBegin() {

    }

    @Override
    public void sharedCleanup() {

    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientInit() {

    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientBegin() {

    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientCleanup() {

    }
}
