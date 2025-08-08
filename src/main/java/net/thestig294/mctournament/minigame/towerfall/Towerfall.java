package net.thestig294.mctournament.minigame.towerfall;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.thestig294.mctournament.minigame.Minigame;
import net.thestig294.mctournament.minigame.Minigames;

import static net.thestig294.mctournament.minigame.MinigameVariants.registerVariant;

public class Towerfall extends Minigame {
    @Override
    public String getID() {
        return "towerfall";
    }

    @Override
    public void serverInit() {
        Variants.register();
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
        public static final String LAST_ONE_STANDING = registerVariant(Minigames.TOWERFALL, "last_one_standing");
        public static final String TEAM_DEATHMATCH = registerVariant(Minigames.TOWERFALL, "team_deathmatch");

        public static void register() {
            Minigames.logRegistration("variants", Minigames.TOWERFALL);
        }
    }
}
