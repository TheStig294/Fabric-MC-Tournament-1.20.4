package net.thestig294.mctournament.minigame.mariokart;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.thestig294.mctournament.minigame.Minigame;
import net.thestig294.mctournament.minigame.Minigames;

import static net.thestig294.mctournament.minigame.MinigameVariants.registerVariant;

public class MarioKart extends Minigame {
    @Override
    public String getID() {
        return "mario_kart";
    }

    @Override
    public float getScoreMultiplier() {
        return 1.0f;
    }

    @Override
    public boolean ignoreTeamCaptainScoreTransfer() {
        return true;
    }

    @Override
    public void serverInit() {
        Variants.register();
    }

    @Override
    public void serverBegin() {

    }

    @Override
    public void serverPreEnd() {

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


    @SuppressWarnings("unused")
    public static class Variants {
        public static final String YOSHIS_CIRCUIT = registerVariant(Minigames.MARIO_KART, "yoshis_circuit");
        public static final String COCONUT_MALL = registerVariant(Minigames.MARIO_KART, "coconut_mall");
        public static final String BOWSERS_CASTLE = registerVariant(Minigames.MARIO_KART, "bowsers_castle");
        public static final String RAINBOW_ROAD = registerVariant(Minigames.MARIO_KART, "rainbow_road");
        public static final String BABY_PARK = registerVariant(Minigames.MARIO_KART, "baby_park");

        public static void register() {
            Minigames.logRegistration("variants", Minigames.MARIO_KART);
        }
    }
}
