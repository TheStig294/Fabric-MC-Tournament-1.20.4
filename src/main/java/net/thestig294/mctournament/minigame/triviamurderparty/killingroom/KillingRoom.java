package net.thestig294.mctournament.minigame.triviamurderparty.killingroom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.scoreboard.Team;

public abstract class KillingRoom {
    public abstract Properties properties();

    public abstract void init();

    public abstract void begin();

    @Environment(EnvType.CLIENT)
    public abstract void clientInit();

    @Environment(EnvType.CLIENT)
    public abstract void clientBegin();

    public void setTeamDead(Team team) {

    }

    public void killTeams() {

        this.startScoreScreen();
    }

    private void startScoreScreen() {

    }

    public record Description(String description, int displaySecs){}
    public record Properties(String id, String name, Description... descriptionLines){}
}
