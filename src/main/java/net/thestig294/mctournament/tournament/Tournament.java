package net.thestig294.mctournament.tournament;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.Minigame;
import net.thestig294.mctournament.minigame.Minigames;
import net.thestig294.mctournament.network.ModNetworking;

import java.util.*;

public class Tournament {
    private int round;
    private Minigame minigame;
    private List<Identifier> minigameIDs;
    private List<Minigame> minigames;
    private List<String> variants;

    private TournamentScoreboard scoreboard;
    private TournamentScoreboard clientScoreboard;

    public void serverSetup(TournamentSettings settings) {
        this.minigameIDs = settings.getMinigames();
        this.variants = settings.getVariants();
        this.scoreboard = this.scoreboard == null ? new TournamentScoreboard(false) : this.scoreboard;
        this.scoreboard.serverInit();

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

        this.clientScoreboard = this.clientScoreboard == null ? new TournamentScoreboard(true) : this.clientScoreboard;
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


    public TournamentScoreboard serverScoreboard() {
        return this.scoreboard;
    }

    @Environment(EnvType.CLIENT)
    public TournamentScoreboard clientScoreboard() {
        return this.clientScoreboard;
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
