package net.thestig294.mctournament.tournament.specialminigame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.scoreboard.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.thestig294.mctournament.minigame.Minigame;
import net.thestig294.mctournament.network.ModNetworking;
import net.thestig294.mctournament.tournament.Tournament;
import net.thestig294.mctournament.util.ModUtil;

import java.util.*;

/**
 * A special minigame that gets automatically played at the start of every tournament
 */
public class TournamentBegin extends Minigame {
    private ServerScoreboard scoreboard;
    private ScoreboardObjective objective;
    private boolean hooksAdded;
    private List<Team> teams;

    @Override
    public String getID() {
        return "tournament_begin";
    }

    @Override
    public void serverInit() {
        this.scoreboard = Tournament.inst().getServerScoreboard();
        this.hooksAdded = false;
        this.teams = new ArrayList<>(Tournament.MAX_TEAMS);
    }

    @Override
    public void serverBegin() {
        if (this.objective != null) this.scoreboard.removeObjective(this.objective);
        this.objective = this.scoreboard.addObjective("tournamentScore", ScoreboardCriterion.DUMMY,
                Text.literal("Tournament Score"), ScoreboardCriterion.RenderType.INTEGER, false, null);

        while (!this.teams.isEmpty()) {
            this.scoreboard.removeTeam(this.teams.getLast());
            this.teams.removeLast();
        }
        for (int i = 0; i < Tournament.MAX_TEAMS; i++) {
            this.teams.add(this.scoreboard.addTeam(Tournament.inst().getTeamName(i)));
        }

        Tournament.inst().resetTeamCaptains();
        ModUtil.forAllPlayers(this::addPlayerToTeam);

        if (!this.hooksAdded) {
            ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
                this.addPlayerToTeam(handler.getPlayer());

            });

            ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
                ServerPlayerEntity player = handler.getPlayer();
                Team team = player.getScoreboardTeam();

                if (team != null && Tournament.inst().isTeamCaptain(player)) {
                    Tournament.inst().findNewTeamCaptain(team);
                }
            });

            this.hooksAdded = true;
        }
    }

    private void addPlayerToTeam(ServerPlayerEntity player) {
        if (player.getScoreboardTeam() != null) return;

        Team smallestTeam = this.teams.getFirst();
        int smallestTeamSize = Tournament.inst().getConnectedTeamMembers(smallestTeam).size();
        for (final var team : this.teams) {
            int teamSize = Tournament.inst().getConnectedTeamMembers(team).size();
            if (smallestTeamSize > teamSize) {
                smallestTeam = team;
                smallestTeamSize = teamSize;
            }
        }

        this.setTeam(player, smallestTeam);

        if (smallestTeamSize == 0) {
            Tournament.inst().setTeamCaptain(player, smallestTeam);
        }
    }

    private void setTeam(ServerPlayerEntity player, Team team) {
        if (!Objects.equals(player.getScoreboardTeam(), team)) {
            this.scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), team);

            if (Tournament.inst().isTeamCaptain(player)) {
                Tournament.inst().findNewTeamCaptain(team);
            }
        }
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

//    The tournament scoreboard is used directly instead
    @Override
    public void translateScores() {
    }


    public static class NetworkIDs {
        public static final Identifier LATE_CONNECTED_PLAYER = ModNetworking.registerNetworkID("late_connected_player");
    }
}
