package net.thestig294.mctournament.tournament;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.Minigame;
import net.thestig294.mctournament.minigame.Minigames;
import net.thestig294.mctournament.network.ModNetworking;
import net.thestig294.mctournament.util.ModUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Tournament {
    public static final int MAX_TEAMS = 8;

    private int round;
    private Minigame minigame;
    private List<Identifier> minigameIDs;
    private List<Minigame> minigames;
    private List<String> variants;

    private ServerScoreboard scoreboard;
    private final Map<Team, PlayerEntity> teamCaptains = new HashMap<>();

    public void serverSetup(TournamentSettings settings) {
        this.minigameIDs = settings.getMinigames();
        this.variants = settings.getVariants();
        this.scoreboard = MCTournament.SERVER.getScoreboard();
        this.teamCaptains.clear();

        PacketByteBuf buffer = PacketByteBufs.create();

        buffer.writeInt(this.minigameIDs.size());
        for (final var id : this.minigameIDs) {
            buffer.writeIdentifier(id);
        }

        buffer.writeInt(this.variants.size());
        for (final var variant : this.variants) {
            buffer.writeString(variant);
        }

        this.sharedSetup(false);
        ModNetworking.broadcast(ModNetworking.TOURNAMENT_SETUP, buffer);
    }

    @Environment(EnvType.CLIENT)
    private void clientSetup(PacketByteBuf buffer) {
        int minigameCount = buffer.readInt();
        this.minigameIDs = new ArrayList<>(minigameCount);
        for (int i = 0; i < minigameCount; i++) {
            this.minigameIDs.add(buffer.readIdentifier());
        }

        int variantCount = buffer.readInt();
        this.variants = new ArrayList<>(variantCount);
        for (int i = 0; i < variantCount; i++) {
            this.variants.add(buffer.readString());
        }

        this.sharedSetup(true);
    }

    private void sharedSetup(boolean isClient) {
        this.round = -1;
        this.minigame = null;
        this.minigames = Minigames.get(this.minigameIDs);

        this.updateMinigame(isClient);
    }

//    Must be called at the end of a minigame to clean up any events for the next minigame,
//    and for the Tournament instance to tally the scores from the minigame's scoreboard.
//    Can be called from a single client, or from the server
    public void endCurrentMinigame(boolean isClient) {
        if (this.minigame == null) return;

        if (isClient) {
            ModNetworking.sendToServer(ModNetworking.TOURNAMENT_CLIENT_END_ROUND);
        } else {
            this.endRound(false);
        }
    }

    private void endRound(boolean isClient) {
        if (isClient) {
            this.minigame.clientEnd();
//            If we're not on a dedicated server, we need to update the client's copy of the current round
            if (!MCTournament.CLIENT.isInSingleplayer()) {
                this.round++;
            }
        } else {
            this.minigame.translateScores();
            this.minigame.serverEnd();
//            Unfortunately, since the client and server and influence each other in singleplayer,
//            there is no guarantee that the incrementation below will occur before this message is received
            ModNetworking.broadcast(ModNetworking.TOURNAMENT_END_ROUND);
            this.round++;
        }

        this.updateMinigame(isClient);
    }

    private void updateMinigame(boolean isClient) {
        if (this.round <= -1) {
            this.minigame = Minigames.get(Minigames.TOURNAMENT_BEGIN);
        } else if (this.round >= this.minigames.size()) {
            this.minigame = Minigames.get(Minigames.TOURNAMENT_END);
        } else {
            this.minigame = this.minigames.get(this.round);
            this.minigame.setVariant(this.variants.get(this.round));
        }

        if (isClient) {
            this.minigame.clientBegin();
        } else {
            this.minigame.serverBegin();
        }
    }

    public void serverInit() {
        ModNetworking.serverReceive(ModNetworking.TOURNAMENT_CLIENT_END_ROUND, serverReceiveInfo -> this.endRound(false));
    }

    @Environment(EnvType.CLIENT)
    public void clientInit() {
        ModNetworking.clientReceive(ModNetworking.TOURNAMENT_END_ROUND, clientReceiveInfo -> this.endRound(true));
        ModNetworking.clientReceive(ModNetworking.TOURNAMENT_SETUP, clientReceiveInfo -> this.clientSetup(clientReceiveInfo.buffer()));
    }


    public ServerScoreboard getServerScoreboard() {
        return this.scoreboard;
    }

    @Environment(EnvType.CLIENT)
    public Scoreboard getClientScoreboard() {
        ClientWorld world = MCTournament.CLIENT.world;
        return world != null ? world.getScoreboard() : null;
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

    public void setTeamCaptain(@Nullable PlayerEntity captain, String teamName)  {
        this.setTeamCaptain(captain, this.scoreboard.getTeam(teamName));
    }

    public void setTeamCaptain(@Nullable PlayerEntity captain, Team team) {
        this.teamCaptains.put(team, captain);
    }

    public boolean isTeamCaptain(PlayerEntity player) {
        return this.teamCaptains.containsValue(player);
    }

    public void findNewTeamCaptain(Team team) {
        for (final var playerName : team.getPlayerList()) {
            ServerPlayerEntity captain = ModUtil.getPlayer(playerName);

            if (captain != null) {
                this.teamCaptains.put(team, captain);
                return;
            }
        }

    }

    public void resetTeamCaptains() {
        this.teamCaptains.clear();
    }

    public Team getTeam(int teamNumber) {
        return this.scoreboard.getTeam(this.getTeamName(teamNumber));
    }

    public String getTeamName(int teamNumber) {
        return MCTournament.MOD_ID + teamNumber;
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


//    A "Bill Pugh" Singleton implementation
    private Tournament() {}
    private static class Singleton {
        private static final Tournament INSTANCE = new Tournament();
    }
    public static Tournament inst() {
        return Singleton.INSTANCE;
    }
}
