package net.thestig294.mctournament.minigame.triviamurderparty.killingroom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.scoreboard.Team;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.structure.ModStructures;
import net.thestig294.mctournament.structure.Structure;

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

    public record Properties(Structure structure, String id, int timerSecs, float... descriptionLengths) {

        public Properties(int xOffset, int yOffset, int zOffset, String id, int timerSecs, float... descriptionLengths) {

            this(ModStructures.registerStructure(TriviaMurderParty.ID + "/killing_room/" + id, xOffset, yOffset, zOffset),
                    id, timerSecs, descriptionLengths);
        }
    }
}
