package net.thestig294.mctournament.tournament;

import com.google.common.primitives.Ints;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.scoreboard.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.timer.Timer;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.tournamentbegin.TournamentBegin;
import net.thestig294.mctournament.network.ModNetworking;
import net.thestig294.mctournament.util.ModUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TournamentScoreboard {
    public static final int MAX_TEAMS = 2;
    public static final String TEAM_NAME_PREFIX = MCTournament.MOD_ID + '_';
    public static final String OBJECTIVE_NAME = "tournamentScore";

    private final boolean isClient;
    private final Scoreboard scoreboard;
    private final List<Team> teams;
    private final Map<Team, PlayerEntity> teamCaptains;
    private boolean hooksAdded;
    private ScoreboardObjective objective;

    public TournamentScoreboard(boolean isClient) {
        this.isClient = isClient;

        if (isClient) {
            ClientWorld world = MCTournament.CLIENT.world;
            this.scoreboard = world != null ? world.getScoreboard() : null;;
        } else {
            this.scoreboard = MCTournament.SERVER.getScoreboard();
        }

        this.teams = new ArrayList<>(MAX_TEAMS);
        this.teamCaptains = new HashMap<>();
        this.hooksAdded = false;

        if (isClient) {
            this.clientInit();
        } else {
            this.serverInit();
        }
    }

    @Environment(EnvType.CLIENT)
    public void clientInit() {
        ModNetworking.clientReceive(ModNetworking.SCOREBOARD_UPDATE_TEAMS, clientReceiveInfo -> {
            MCTournament.LOGGER.info("Client: Received team update!");
            this.teams.clear();
            for (int i = 0; i < MAX_TEAMS; i++) {
                Team team = this.scoreboard.getTeam(this.getTeamName(i));
                this.teams.add(team);
                MCTournament.LOGGER.info("Client: Added team: {} {}", i, team);
            }
        });

        ModNetworking.clientReceive(ModNetworking.SCOREBOARD_UPDATE_TEAM_CAPTAIN, clientReceiveInfo -> {
            PacketByteBuf buffer = clientReceiveInfo.buffer();
            int teamNumber = buffer.readInt();
            boolean isNull = buffer.readBoolean();
            Team team = this.getTeam(teamNumber);

            if (isNull) {
                this.setTeamCaptain(team, null);
            } else {
                String playerName = buffer.readString();
                PlayerEntity player = ModUtil.clientGetPlayer(playerName);
                this.setTeamCaptain(team, player);
            }
        });
    }

    /**
     * Called once the special {@link TournamentBegin} minigame has begun. <p>
     * Should be called in {@link TournamentBegin#serverBegin()}
     */
    public void serverInit() {
        this.objective = this.scoreboard.getNullableObjective(OBJECTIVE_NAME);
        if (this.objective != null) this.scoreboard.removeObjective(this.objective);
        this.objective = this.scoreboard.addObjective(OBJECTIVE_NAME, ScoreboardCriterion.DUMMY,
                Text.literal("Tournament Score"), ScoreboardCriterion.RenderType.INTEGER, false, null);

        this.teams.clear();
        for (int i = 0; i < MAX_TEAMS; i++) {
            Team team = this.getTeam(i);
            if (team != null) {
                this.scoreboard.removeTeam(team);
            }
            this.teams.add(this.scoreboard.addTeam(this.getTeamName(i)));
            MCTournament.LOGGER.info("Server: Added team: {} {}", i, this.teams.get(i));
        }

        this.resetTeamCaptains();
        ModUtil.forAllPlayers(this::addPlayerToTeam);

        if (!this.hooksAdded) {
            this.hooksAdded = true;
            this.addHooks();
        }

        ModUtil.createTimer("scoreboard_client_update_delay", 20,
                (server, events, time) -> this.sendFullTeamUpdate());
    }

    private void addHooks() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            this.addPlayerToTeam(handler.getPlayer());
            ModNetworking.send(ModNetworking.SCOREBOARD_UPDATE_TEAMS, handler.getPlayer());
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            Team team = player.getScoreboardTeam();

            if (team != null && this.isTeamCaptain(player)) {
                this.findNewTeamCaptain(team);
            }
        });
    }

    public void sendFullTeamUpdate() {
        MCTournament.LOGGER.info("Server: Broadcasting team update!");
        ModNetworking.broadcast(ModNetworking.SCOREBOARD_UPDATE_TEAMS);
    }

    public void addPlayerToTeam(PlayerEntity player) {
        if (player.getScoreboardTeam() != null) return;

        Team smallestTeam = this.teams.get(0);
        int smallestTeamSize = this.getConnectedTeamMembers(smallestTeam).size();
        for (final var team : this.teams) {
            int teamSize = this.getConnectedTeamMembers(team).size();
            if (smallestTeamSize > teamSize) {
                smallestTeam = team;
                smallestTeamSize = teamSize;
            }
        }

        this.setTeam(player, smallestTeam);

        if (smallestTeamSize == 0) {
            this.setTeamCaptain(smallestTeam, player);
        }
    }

    public void setTeam(PlayerEntity player, Team team) {
        if (!Objects.equals(player.getScoreboardTeam(), team)) {
            this.scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), team);

            if (this.isTeamCaptain(player)) {
                this.findNewTeamCaptain(team);
            }
        }
    }

    public boolean isClient() {
        return this.isClient;
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    public @Nullable PlayerEntity getTeamCaptain(int teamNumber) {
        return this.getTeamCaptain(this.getTeamName(teamNumber));
    }

    public @Nullable PlayerEntity getTeamCaptain(PlayerEntity player) {
        Team team = player.getScoreboardTeam();
        return team != null ? this.getTeamCaptain(team.getName()) : null;
    }

    public @Nullable PlayerEntity getTeamCaptain(String teamName) {
        return this.getTeamCaptain(this.scoreboard.getTeam(teamName));
    }

    public @Nullable PlayerEntity getTeamCaptain(Team team) {
        return this.teamCaptains.getOrDefault(team, null);
    }

    /**
     * @return A list of all team captains, in order of team name from 0 to {@code MAX_TEAMS}
     */
    public List<PlayerEntity> getTeamCaptains() {
        List<PlayerEntity> captains = new ArrayList<>();

        for (final var team : this.teams) {
            captains.add(this.getTeamCaptain(team));
        }

        return captains;
    }

    public void setTeamCaptain(String teamName, @Nullable PlayerEntity captain)  {
        this.setTeamCaptain(this.scoreboard.getTeam(teamName), captain);
    }

    public void setTeamCaptain(Team team, @Nullable PlayerEntity captain) {
        this.teamCaptains.put(team, captain);

        if (!this.isClient()) {
            PacketByteBuf buffer = PacketByteBufs.create();
            buffer.writeInt(this.getTeamNumber(team));
            if (captain == null) {
                buffer.writeBoolean(true);
            } else {
                buffer.writeBoolean(false);
                buffer.writeString(captain.getNameForScoreboard());
            }
            ModNetworking.broadcast(ModNetworking.SCOREBOARD_UPDATE_TEAM_CAPTAIN, buffer);
        }
    }

    public boolean isTeamCaptain(PlayerEntity player) {
        return this.teamCaptains.containsValue(player);
    }

    public void findNewTeamCaptain(Team team) {
        for (final var playerName : team.getPlayerList()) {
            ServerPlayerEntity captain = ModUtil.getPlayer(playerName);

            if (captain != null) {
                this.setTeamCaptain(team, captain);
                return;
            }
        }

    }

    public void resetTeamCaptains() {
        this.teamCaptains.clear();
    }

    public String getTeamName(int teamNumber) {
        return TEAM_NAME_PREFIX + teamNumber;
    }

    public @Nullable Team getTeam(int teamNumber) {
        return this.scoreboard.getTeam(this.getTeamName(teamNumber));
    }

    public int getTeamNumber(Team team) {
        return this.getTeamNumber(team.getName());
    }

    /**
     * Converts a team name into a team number
     * @param teamName
     * @return Team's team number, or -1 if not a properly formatted tournament team name.
     * <p>
     * See: {@link TournamentScoreboard#getTeamName(int)}
     */
    public int getTeamNumber(String teamName) {
        Integer teamNumber = Ints.tryParse(teamName.substring(TEAM_NAME_PREFIX.length()));
        return teamNumber == null ? -1 : teamNumber;
    }

    public List<ServerPlayerEntity> getConnectedTeamMembers(String teamName) {
        Team team = this.scoreboard.getTeam(teamName);
        return team == null ? Collections.emptyList() : getConnectedTeamMembers(team);
    }

    public List<ServerPlayerEntity> getConnectedTeamMembers(Team team) {
        List<ServerPlayerEntity> teamPlayers = new ArrayList<>();

        for (final var playerName : team.getPlayerList()) {
            ServerPlayerEntity player = ModUtil.getPlayer(playerName);
            if ((player != null && !player.isDisconnected())) teamPlayers.add(player);
        }

        return teamPlayers;
    }
}
