package net.thestig294.mctournament.minigame.triviamurderparty.killingroom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.scoreboard.Team;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.minigame.triviamurderparty.screen.KillingRoomScreenHandler;
import net.thestig294.mctournament.structure.ModStructures;
import net.thestig294.mctournament.structure.Structure;
import net.thestig294.mctournament.tournament.Tournament;
import net.thestig294.mctournament.tournament.TournamentScoreboard;

public abstract class KillingRoom {
    private KillingRoomScreenHandler screenHandler;
    private TriviaMurderParty minigame;

    public abstract Properties properties();

    public abstract void init();

    public abstract void begin();

    public abstract void timerEnd(int timerIndex);

    @Environment(EnvType.CLIENT)
    public abstract void clientInit();

    @Environment(EnvType.CLIENT)
    public abstract void clientBegin();

    public void setScreenHandler(KillingRoomScreenHandler screenHandler) {
        this.screenHandler = screenHandler;
    }

    public KillingRoomScreenHandler screenHandler() {
        return this.screenHandler;
    }

    public TournamentScoreboard tournamentScoreboard() {
        return Tournament.inst().scoreboard();
    }

    public void setMinigame(TriviaMurderParty minigame) {
        this.minigame = minigame;
    }

    public TriviaMurderParty minigame() {
        return this.minigame;
    }

    public void setTeamDead(Team team) {

    }

    public void killTeams() {

        this.startScoreScreen();
    }

    private void startScoreScreen() {

    }

    public record Properties(Structure structure, String id, int timerLength, float timerQuipLength, Float... descriptionLengths) {

        public Properties(int xOffset, int yOffset, int zOffset, String id, int timerLength, float timerQuipLength, Float... descriptionLengths) {

            this(ModStructures.registerStructure(TriviaMurderParty.ID + "/killing_room/" + id, xOffset, yOffset, zOffset),
                    id, timerLength, timerQuipLength, descriptionLengths);
        }
    }
}
