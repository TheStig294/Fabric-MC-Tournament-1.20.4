package net.thestig294.mctournament.minigame.towerfall;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.thestig294.mctournament.minigame.Minigame;
import net.thestig294.mctournament.util.ModUtil;

import static net.thestig294.mctournament.minigame.MinigameVariants.registerVariant;

public class Towerfall extends Minigame {
    public static final String ID = "towerfall";

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
        return false;
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
        public static final String LAST_ONE_STANDING = registerVariant(ID, "last_one_standing");
        public static final String TEAM_DEATHMATCH = registerVariant(ID, "team_deathmatch");

        public static void register() {
            ModUtil.logRegistration("variants", ID);
        }
    }
}
