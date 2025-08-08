package net.thestig294.mctournament.minigame.mariokart;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.thestig294.mctournament.minigame.Minigame;
import net.thestig294.mctournament.minigame.Minigames;

import static net.thestig294.mctournament.minigame.Minigames.registerVariant;

public class MarioKart extends Minigame {
    @Override
    public Text getName() {
        return Text.translatable("minigame.mario_kart.name");
    }

    @Override
    public void serverInit() {

    }

    @Override
    public void serverBegin() {

    }

    @Override
    public void serverEnd() {

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
    public void clientEnd() {

    }

    public static class Variants {
        public static final String YOSHIS_CIRCUIT = registerVariant(Minigames.MARIO_KART, "yoshis_circuit");
        public static final String COCONUT_MALL = registerVariant(Minigames.MARIO_KART, "coconut_mall");
        public static final String BOWSERS_CASTLE = registerVariant(Minigames.MARIO_KART, "bowsers_castle");
        public static final String RAINBOW_ROAD = registerVariant(Minigames.MARIO_KART, "rainbow_road");
        public static final String BABY_PARK = registerVariant(Minigames.MARIO_KART, "baby_park");
    }
}
