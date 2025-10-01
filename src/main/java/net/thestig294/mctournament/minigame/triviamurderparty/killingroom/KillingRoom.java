package net.thestig294.mctournament.minigame.triviamurderparty.killingroom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.thestig294.mctournament.minigame.MinigameScoreboard;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.structure.ModStructures;
import net.thestig294.mctournament.structure.Structure;
import net.thestig294.mctournament.tournament.Tournament;
import net.thestig294.mctournament.tournament.TournamentScoreboard;

import java.util.List;
import java.util.function.BiConsumer;

public abstract class KillingRoom {
    private final Structure structure;
    private BlockPos position = BlockPos.ORIGIN;

    public KillingRoom() {
        this.structure = ModStructures.registerStructure(TriviaMurderParty.ID, "/killing_room/" + this.getID(), this.getStructureOffset());
    }

    public abstract void init();

    public abstract void begin();

    public abstract void timerEnd(String timerName);

    @Environment(EnvType.CLIENT)
    public abstract void clientInit();

    @Environment(EnvType.CLIENT)
    public abstract void clientBegin();

    public abstract BlockPos getStructureOffset();

    public abstract String getID();

    public abstract List<Timer> getTimers();

    public abstract float getTimerQuipLength(String timerName);

    public abstract List<Float> getDescriptionLengths();

    public Structure getStructure() {
        return this.structure;
    }

    public TournamentScoreboard tournamentScoreboard() {
        return Tournament.inst().scoreboard();
    }

    public MinigameScoreboard scoreboard() {
        return Tournament.inst().minigame().scoreboard();
    }

    public boolean isOnTrial(PlayerEntity player) {
        boolean isCorrect = this.scoreboard().getBoolean(player, TriviaMurderParty.Objectives.IS_CORRECT);
        boolean isDead = this.scoreboard().getBoolean(player, TriviaMurderParty.Objectives.IS_DEAD);

        return !isCorrect && !isDead;
    }

    public void forAllConnectedTeamPlayers(BiConsumer<Team, ServerPlayerEntity> lambda) {
        this.tournamentScoreboard().forAllConnectedTeamPlayers(lambda);
    }

    public void setTeamKillable(Team team) {
        team.getPlayerList().forEach(playerName ->
                this.scoreboard().setBoolean(playerName, TriviaMurderParty.Objectives.IS_KILLABLE, true));
    }

    public void setPosition(BlockPos killingRoomPos) {
        this.position = killingRoomPos;
    }

    public BlockPos getPosition() {
        return this.position;
    }

    public record Timer(String name, int length) {}
}
