package net.thestig294.mctournament.minigame.mariokart;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.thestig294.mctournament.minigame.Minigame;
import net.thestig294.mctournament.util.ModUtil;

import static net.thestig294.mctournament.minigame.MinigameVariants.registerVariant;

public class MarioKart extends Minigame {
    public static final String ID = "mario_kart";

    @Override
    public String getID() {
        return ID;
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
        public static final String YOSHIS_CIRCUIT = registerVariant(ID, "yoshis_circuit");
        public static final String COCONUT_MALL = registerVariant(ID, "coconut_mall");
        public static final String BOWSERS_CASTLE = registerVariant(ID, "bowsers_castle");
        public static final String RAINBOW_ROAD = registerVariant(ID, "rainbow_road");
        public static final String BABY_PARK = registerVariant(ID, "baby_park");

        public static void register() {
            ModUtil.logRegistration("variants", ID);
        }
    }
}
