package net.thestig294.mctournament.tournament;

import com.google.common.primitives.Ints;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.scoreboard.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.Minigame;
import net.thestig294.mctournament.minigame.MinigameScoreboard;
import net.thestig294.mctournament.network.ModNetworking;
import net.thestig294.mctournament.util.ModUtil;
import net.thestig294.mctournament.util.ModUtilClient;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class TournamentScoreboard {
    public static final int MAX_TEAMS = 8;
    public static final String TEAM_NAME_PREFIX = MCTournament.MOD_ID + '_';
    public static final String OBJECTIVE_NAME = MCTournament.MOD_ID + ":tournamentScore";

    private final boolean isClient;
    private boolean hooksAdded;
    private final List<Team> teams;
    private final SortedMap<Integer, String> teamCaptainNames;
    private Scoreboard scoreboard;
    private ScoreboardObjective objective;

    public TournamentScoreboard(boolean isClient) {
        this.isClient = isClient;
        this.hooksAdded = false;
        this.teams = new ArrayList<>(MAX_TEAMS);
        this.teamCaptainNames = new TreeMap<>();
    }

    /**
     * Called at the beginning of every tournament
     */
    @Environment(EnvType.CLIENT)
    public void clientInit() {
        World world = MCTournament.client().world;
        this.scoreboard = world != null ? world.getScoreboard() : null;

        ModNetworking.clientReceive(ModNetworking.SCOREBOARD_UPDATE_TEAMS, clientReceiveInfo -> {
            this.teams.clear();
            this.forAllNullableTeams(this.teams::add);
        });

        ModNetworking.clientReceive(ModNetworking.SCOREBOARD_UPDATE_TEAM_CAPTAINS, clientReceiveInfo -> {
            PacketByteBuf buffer = clientReceiveInfo.buffer();
            int noOfCaptains = buffer.readInt();

            for (int i = 0; i < noOfCaptains; i++) {
                int teamNumber = buffer.readInt();
                boolean isNull = buffer.readBoolean();
                Team team = this.getTeam(teamNumber);
                if (team == null) continue;

                if (isNull) {
                    this.setTeamCaptain(team, null);
                } else {
                    String playerName = buffer.readString();
                    PlayerEntity player = ModUtilClient.getPlayer(playerName);
                    this.setTeamCaptain(team, player);
                }
            }
        });

        ModNetworking.sendToServer(ModNetworking.SCOREBOARD_PLAYER_JOINED);
    }

    /**
     * Called at the beginning of every tournament
     */
    public void serverInit() {
        this.scoreboard = MCTournament.server().getScoreboard();

        this.objective = this.scoreboard.getNullableObjective(OBJECTIVE_NAME);
        if (this.objective != null) this.scoreboard.removeObjective(this.objective);
        this.objective = this.scoreboard.addObjective(OBJECTIVE_NAME, ScoreboardCriterion.DUMMY, Text.literal("Tournament Score"),
                ScoreboardCriterion.RenderType.INTEGER, false, null);

        this.teams.clear();
        for (int i = 0; i < MAX_TEAMS; i++) {
            Team team = this.getTeam(i);
            if (team != null) {
                this.scoreboard.removeTeam(team);
            }
            this.teams.add(this.scoreboard.addTeam(this.getTeamName(i)));
        }

        this.teamCaptainNames.clear();
        ModUtil.forAllPlayers(this::addPlayerToTeam);

        if (!this.hooksAdded) {
            this.hooksAdded = true;
            this.addHooks();
        }
    }

    public void serverEnd() {
        this.setGlobalNametagVisibility(true);
    }

    private PacketByteBuf getTeamCaptainsInfoBuffer() {
        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeInt(MAX_TEAMS);

        for (int i = 0; i < MAX_TEAMS; i++) {
            buffer.writeInt(i);

            PlayerEntity captain = this.getTeamCaptain(false, i);
            boolean isNull = captain == null;
            buffer.writeBoolean(isNull);

            if (!isNull) {
                buffer.writeString(captain.getNameForScoreboard());
            }
        }

        return buffer;
    }

    private void addHooks() {
        ModNetworking.serverReceive(ModNetworking.SCOREBOARD_PLAYER_JOINED, serverReceiveInfo -> {
            ServerPlayerEntity player = serverReceiveInfo.player();
            this.addPlayerToTeam(player);

            ModNetworking.send(ModNetworking.SCOREBOARD_UPDATE_TEAMS, player);
            ModNetworking.send(ModNetworking.SCOREBOARD_UPDATE_TEAM_CAPTAINS, player, this.getTeamCaptainsInfoBuffer());
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            Team team = player.getScoreboardTeam();

            if (team != null && this.isTeamCaptain(player)) {
                this.findNewTeamCaptain(team);
            }
        });
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

    public void forAllTeams(Consumer<Team> lambda) {
        this.forAllNullableTeams(team -> {
            if (team != null) lambda.accept(team);
        });
    }

    public void forAllNullableTeams(Consumer<Team> lambda) {
        for (int i = 0; i < MAX_TEAMS; i++) {
            lambda.accept(this.getTeam(i));
        }
    }

    public @Nullable Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    public @Nullable PlayerEntity getTeamCaptain(boolean isClient, PlayerEntity player) {
        Team team = player.getScoreboardTeam();
        return team != null ? this.getTeamCaptain(isClient, team) : null;
    }

    public @Nullable PlayerEntity getTeamCaptain(boolean isClient, Team team) {
        return this.getTeamCaptain(isClient, team.getName());
    }

    public @Nullable PlayerEntity getTeamCaptain(boolean isClient, String teamName) {
        return this.getTeamCaptain(isClient, this.getTeamNumber(teamName));
    }

    public @Nullable PlayerEntity getTeamCaptain(boolean isClient, int teamNumber) {
        return ModUtil.getPlayer(isClient, this.teamCaptainNames.get(teamNumber));
    }

    /**
     * @return A list of all team captains, in order of team name from 0 to {@code MAX_TEAMS},
     * ignoring teams with missing captains
     */
    public List<PlayerEntity> getValidTeamCaptains(boolean isClient) {
        Function<String, PlayerEntity> getPlayerFunction = isClient ? ModUtilClient::getPlayer : ModUtil::getServerPlayer;

        return this.teamCaptainNames.values().stream()
                .map(getPlayerFunction)
                .filter(Objects::nonNull)
                .toList();
    }

    public void setTeamCaptain(Team team, @Nullable PlayerEntity captain) {
        this.setTeamCaptain(team.getName(), captain);
    }

    public void setTeamCaptain(String teamName, @Nullable PlayerEntity captain)  {
        this.setTeamCaptain(this.getTeamNumber(teamName), captain);
    }

    public void setTeamCaptain(int teamNumber, @Nullable PlayerEntity captain) {
        if (captain == null) {
            this.teamCaptainNames.put(teamNumber, null);
        } else {
//            Whenever a new team captain is chosen, transfer the old captain's main minigame score over to the new captain
            if (!this.isClient) {
                String oldTeamCaptainName = this.teamCaptainNames.getOrDefault(teamNumber, null);
                if (oldTeamCaptainName != null) this.transferTeamCaptainScore(oldTeamCaptainName, captain.getNameForScoreboard());
            }

            this.teamCaptainNames.put(teamNumber, captain.getNameForScoreboard());
        }

        if (!this.isClient) {
            PacketByteBuf buffer = PacketByteBufs.create();
            int noOfCaptainUpdates = 1;
            buffer.writeInt(noOfCaptainUpdates);
            buffer.writeInt(teamNumber);

            if (captain == null) {
                buffer.writeBoolean(true);
            } else {
                buffer.writeBoolean(false);
                buffer.writeString(captain.getNameForScoreboard());
            }

            ModNetworking.broadcast(ModNetworking.SCOREBOARD_UPDATE_TEAM_CAPTAINS, buffer);
        }
    }

    public boolean isTeamCaptain(@Nullable PlayerEntity player) {
        return player != null && this.teamCaptainNames.containsValue(player.getNameForScoreboard());
    }

    public void findNewTeamCaptain(Team team) {
        for (final var playerName : team.getPlayerList()) {
            ServerPlayerEntity captain = ModUtil.getServerPlayer(playerName);

            if (captain != null) {
                this.setTeamCaptain(team, captain);
                return;
            }
        }
    }

    public void transferTeamCaptainScore(String oldCaptainName, String newCaptainName) {
        Minigame minigame = Tournament.inst().minigame();
        if (minigame.ignoreTeamCaptainScoreTransfer()) return;

        MinigameScoreboard minigameScoreboard = minigame.scoreboard();
        minigameScoreboard.setScore(newCaptainName, minigameScoreboard.getScore(oldCaptainName));
    }

    public String getTeamName(int teamNumber) {
        return TEAM_NAME_PREFIX + teamNumber;
    }

    public @Nullable Team getTeam(int teamNumber) {
        return this.scoreboard.getTeam(this.getTeamName(teamNumber));
    }

    /**
     * Gets a player's team number
     * @param player PlayerEntity
     * @return the player's team number, or -1 if the player doesn't have a team or has an invalid team name
     */
    public int getTeamNumber(PlayerEntity player) {
        Team team = player.getScoreboardTeam();
        return team == null ? -1 : this.getTeamNumber(team);
    }

    public int getTeamNumber(Team team) {
        return this.getTeamNumber(team.getName());
    }

    /**
     * Converts a team name into a team number
     * @param teamName String of valid tournament team name
     * @return Team's team number, or -1 if not a properly formatted tournament team name.
     * <p>
     * See: {@link TournamentScoreboard#getTeamName(int)}
     */
    public int getTeamNumber(String teamName) {
        Integer teamNumber = Ints.tryParse(teamName.substring(TEAM_NAME_PREFIX.length()));
        return teamNumber == null ? -1 : teamNumber;
    }

    public List<ServerPlayerEntity> getConnectedTeamMembers(Team team) {
        List<ServerPlayerEntity> teamPlayers = new ArrayList<>();

        for (final var playerName : team.getPlayerList()) {
            ServerPlayerEntity player = ModUtil.getServerPlayer(playerName);
            if ((player != null && !player.isDisconnected())) teamPlayers.add(player);
        }

        return teamPlayers;
    }

    public void forAllConnectedTeamPlayers(BiConsumer<Team, ServerPlayerEntity> lambda) {
        this.forAllTeams((team) ->
                this.getConnectedTeamMembers(team).forEach((player) ->
                        lambda.accept(team, player)));
    }

    public boolean isTeamConnected(@Nullable Team team) {
        return team != null && !this.getConnectedTeamMembers(team).isEmpty();
    }

    public @Nullable Team getRandomConnectedTeam() {
        List<Team> teamsCopy = new ArrayList<>(this.teams);
        Collections.shuffle(teamsCopy);
        return teamsCopy.stream()
                .filter(this::isTeamConnected)
                .findFirst()
                .orElse(null);
    }

    public void updateOverallScores() {
        MinigameScoreboard minigameScoreboard = Tournament.inst().minigame().scoreboard();
        float multiplier = minigameScoreboard.getScoreMultiplier();

        this.forAllTeams(team -> team.getPlayerList().forEach(playerName -> {
            int minigameScore = (int) (minigameScoreboard.getScore(playerName) * multiplier);
            this.scoreboard.getOrCreateScore(ScoreHolder.fromName(playerName), this.objective).incrementScore(minigameScore);
        }));
    }

    public void setGlobalNametagVisibility(boolean isVisible) {
        this.forAllTeams(team -> this.setNametagVisibility(team, isVisible));
    }

    public void setNametagVisibility(Team team, boolean isVisible) {
        team.setNameTagVisibilityRule(isVisible ? AbstractTeam.VisibilityRule.ALWAYS : AbstractTeam.VisibilityRule.NEVER);
    }
}
