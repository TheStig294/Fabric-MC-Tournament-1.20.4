package net.thestig294.mctournament.minigame.triviamurderparty.killingroom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.BlockPos;
import net.thestig294.mctournament.minigame.MinigameScoreboard;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.minigame.triviamurderparty.screen.KillingRoomScreenHandler;
import net.thestig294.mctournament.structure.ModStructures;
import net.thestig294.mctournament.structure.Structure;
import net.thestig294.mctournament.tournament.Tournament;
import net.thestig294.mctournament.tournament.TournamentScoreboard;

import java.util.List;

public abstract class KillingRoom {
    private final Structure structure;
    private KillingRoomScreenHandler screenHandler;
    private TriviaMurderParty minigame;

    public KillingRoom() {
        this.structure = ModStructures.registerStructure(TriviaMurderParty.ID + "/killing_room/" + this.getID(), this.getStructureOffset());
    }

    public abstract void init();

    public abstract void begin();

    public abstract void timerEnd(int timerIndex);

    @Environment(EnvType.CLIENT)
    public abstract void clientInit();

    @Environment(EnvType.CLIENT)
    public abstract void clientBegin();

    public abstract BlockPos getStructureOffset();

    public abstract String getID();

    public abstract List<Integer> getTimerLengths();

    public abstract float getTimerQuipLength();

    public abstract List<Float> getDescriptionLengths();

    public Structure getStructure() {
        return this.structure;
    }

    public KillingRoomScreenHandler screenHandler() {
        return this.screenHandler;
    }

    public TournamentScoreboard tournamentScoreboard() {
        return Tournament.inst().scoreboard();
    }

    public TriviaMurderParty minigame() {
        return this.minigame;
    }

    public MinigameScoreboard scoreboard() {
        return this.minigame.scoreboard();
    }

    public void setScreenHandler(KillingRoomScreenHandler screenHandler) {
        this.screenHandler = screenHandler;
    }

    public void setMinigame(TriviaMurderParty minigame) {
        this.minigame = minigame;
    }

    public boolean isOnTrial(PlayerEntity player) {
        return this.screenHandler.isOnTrial(player);
    }

    public void setTeamDead(Team team) {

    }

    public void killTeams() {

        this.startScoreScreen();
    }

    private void startScoreScreen() {

    }
}
