package net.thestig294.mctournament.tournament;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.thestig294.mctournament.minigame.Minigame;
import net.thestig294.mctournament.minigame.Minigames;
import net.thestig294.mctournament.network.ModNetworking;
import net.thestig294.mctournament.util.ModTimer;

import java.util.*;

public class Tournament {
    public static final int MINIGAME_BEGIN_DELAY_SECS = 2;

    private int round = -1;
    private Minigame minigame = null;
    private List<Identifier> minigameIDs = new ArrayList<>();
    private List<Minigame> minigames = new ArrayList<>();
    private List<String> variants = new ArrayList<>();

    private TournamentScoreboard scoreboard;
    private TournamentScoreboard clientScoreboard;

    public void serverSetup(TournamentSettings settings) {
        this.round = -1;
        this.minigameIDs = settings.getMinigames();
        this.variants = settings.getVariants();
        this.scoreboard = this.scoreboard == null ? new TournamentScoreboard(false) : this.scoreboard;
        this.scoreboard.serverInit();

        this.sharedSetup(false);
        ModNetworking.broadcast(ModNetworking.TOURNAMENT_SETUP, this.getClientInfoBuffer());
    }

    private PacketByteBuf getClientInfoBuffer() {
        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeInt(this.round);

        buffer.writeInt(this.minigameIDs.size());
        for (final var id : this.minigameIDs) {
            buffer.writeIdentifier(id);
        }

        buffer.writeInt(this.variants.size());
        for (final var variant : this.variants) {
            buffer.writeString(variant);
        }

        return buffer;
    }

    @Environment(EnvType.CLIENT)
    private void clientSetup(PacketByteBuf buffer) {
        this.round = buffer.readInt();

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
        this.clientScoreboard.clientInit();
        this.sharedSetup(true);
    }

    private void sharedSetup(boolean isClient) {
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
            this.endRound(false, this.round);
        }
    }

    private void endRound(boolean isClient, int round) {
        if (isClient) {
            this.minigame.clientEnd();
            this.round = round;
        } else {
            this.minigame.translateScores();
            this.minigame.serverEnd();
            this.round++;
            PacketByteBuf buffer = PacketByteBufs.create();
            buffer.writeInt(this.round);
            ModNetworking.broadcast(ModNetworking.TOURNAMENT_END_ROUND, buffer);
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

//        Called after a small delay to allow for packets to be sent between the server and client,
//        from the last minigame's end function, and the initial state update for the TournamentScoreboard
        ModTimer.simple(isClient, MINIGAME_BEGIN_DELAY_SECS, () -> {
            if (isClient) {
                this.minigame.clientBegin();
            } else {
                this.minigame.serverBegin();
            }
        });
    }

    public void serverInit() {
        ModNetworking.serverReceive(ModNetworking.TOURNAMENT_CLIENT_END_ROUND, serverReceiveInfo ->
                this.endRound(false, this.round));

        ModNetworking.serverReceive(ModNetworking.TOURNAMENT_SETUP, serverReceiveInfo ->
                ModNetworking.send(ModNetworking.TOURNAMENT_SETUP, serverReceiveInfo.player(), this.getClientInfoBuffer()));
    }

    @Environment(EnvType.CLIENT)
    public void clientInit() {
        ModNetworking.clientReceive(ModNetworking.TOURNAMENT_END_ROUND, clientReceiveInfo ->
                this.endRound(true, clientReceiveInfo.buffer().readInt()));

        ModNetworking.clientReceive(ModNetworking.TOURNAMENT_SETUP, clientReceiveInfo ->
                this.clientSetup(clientReceiveInfo.buffer()));

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                ModNetworking.sendToServer(ModNetworking.TOURNAMENT_SETUP));
    }


    @SuppressWarnings("unused")
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
