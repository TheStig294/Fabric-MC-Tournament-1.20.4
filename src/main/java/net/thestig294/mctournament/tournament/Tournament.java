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
    private boolean isActive = false;
    private Minigame minigame = null;
    private List<Identifier> minigameIDs = new ArrayList<>();
    private List<Minigame> minigames = new ArrayList<>();
    private List<String> variants = new ArrayList<>();

    private TournamentScoreboard scoreboard;
    private TournamentScoreboard clientScoreboard;

    public void serverSetup(TournamentSettings settings) {
        this.round = -1;
        this.isActive = true;
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
        this.isActive = true;

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
        if (!this.isActive) return;

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
            this.minigame.serverPreEnd();
            this.minigame.serverEnd();
            this.round++;

            ModNetworking.broadcast(ModNetworking.TOURNAMENT_END_ROUND, PacketByteBufs.create()
                    .writeInt(this.round));
        }

        this.updateMinigame(isClient);
    }

    private void updateMinigame(boolean isClient) {
        if (this.round <= -1) {
            this.minigame = Minigames.get(Minigames.TOURNAMENT_BEGIN);
        } else if (this.round == this.minigames.size()) {
            this.minigame = Minigames.get(Minigames.TOURNAMENT_END);
        } else if (this.minigame != null && this.minigame.equals(Minigames.get(Minigames.TOURNAMENT_END))) {
            this.isActive = false;
            return;
        } else {
            this.minigame = this.minigames.get(this.round);
            this.minigame.setVariant(this.variants.get(this.round));
        }


//        Called after a small delay to allow for packets to be sent between the server and client,
//        from the last minigame's end function, and the initial state update for the TournamentScoreboard
//        Caching the value of the current minigame so that it is not changed by the time the timer runs...
        Minigame minigame = this.minigame;

        ModTimer.simple(isClient, MINIGAME_BEGIN_DELAY_SECS, () -> {
            if (isClient) {
                minigame.clientBegin();
            } else {
                minigame.serverPreBegin();
                minigame.serverBegin();
            }
        });
    }

    public void serverInit() {
        ModNetworking.serverReceive(ModNetworking.TOURNAMENT_CLIENT_END_ROUND, serverReceiveInfo ->
                this.endRound(false, this.round));

        ModNetworking.serverReceive(ModNetworking.TOURNAMENT_MID_ROUND_JOIN, serverReceiveInfo -> {
            if (!this.isActive) return;
            ModNetworking.send(ModNetworking.TOURNAMENT_SETUP, serverReceiveInfo.player(), this.getClientInfoBuffer());
        });
    }

    @Environment(EnvType.CLIENT)
    public void clientInit() {
        ModNetworking.clientReceive(ModNetworking.TOURNAMENT_END_ROUND, clientReceiveInfo ->
                this.endRound(true, clientReceiveInfo.buffer().readInt()));

        ModNetworking.clientReceive(ModNetworking.TOURNAMENT_SETUP, clientReceiveInfo ->
                this.clientSetup(clientReceiveInfo.buffer()));

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                ModNetworking.sendToServer(ModNetworking.TOURNAMENT_MID_ROUND_JOIN));
    }


    public TournamentScoreboard scoreboard() {
        return this.scoreboard;
    }

    @Environment(EnvType.CLIENT)
    public TournamentScoreboard clientScoreboard() {
        return this.clientScoreboard;
    }

    public Minigame minigame() {
        return this.minigame;
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
