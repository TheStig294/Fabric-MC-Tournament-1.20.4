package net.thestig294.mctournament.tournament;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.Minigame;
import net.thestig294.mctournament.minigame.Minigames;
import net.thestig294.mctournament.network.ModNetworking;
import net.thestig294.mctournament.util.ModUtil;

import java.util.ArrayList;
import java.util.List;

public class Tournament {
    private TournamentScoreboard scoreboard;
    private int round;
    private Minigame minigame;
    private List<Identifier> minigameIDs;
    private List<Minigame> minigames;

    public void serverSetup(TournamentSettings settings) {
        this.minigameIDs = settings.getMinigames();

        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeInt(minigameIDs.size());
        for (final var id : this.minigameIDs) {
            buffer.writeIdentifier(id);
        }

        this.sharedSetup(false);
        ModNetworking.broadcast(ModNetworking.TOURNAMENT_SETUP, buffer);
    }

    private void sharedSetup(boolean isClient) {
        this.scoreboard = new TournamentScoreboard();
        this.round = 0;
        this.minigame = null;
        this.minigames = Minigames.get(this.minigameIDs);
        this.updateMinigame(isClient);
    }

    @Environment(EnvType.CLIENT)
    private void clientSetup(PacketByteBuf buffer) {
        int minigameCount = buffer.readInt();
        this.minigameIDs = new ArrayList<>(minigameCount);

        for (int i = 0; i < minigameCount; i++) {
            this.minigameIDs.add(buffer.readIdentifier());
        }

        this.sharedSetup(true);
    }

    public Minigame getMinigame() {
        return this.minigame;
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

//    Must be called at the end of a minigame to clean up any events for the next minigame,
//    and for the Tournament instance to tally the scores from the minigame's scoreboard.
//    Can be called from a single client, or from the server
    public void endCurrentMinigame(boolean isClient) {
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
            this.minigame.serverEnd();
//            Unfortunately, since the client and server and influence each other in singleplayer,
//            there is no guarantee that the incrementation below will occur before this message is received
            ModNetworking.broadcast(ModNetworking.TOURNAMENT_END_ROUND);
            this.round++;
        }

        this.updateMinigame(isClient);
    }

    private void updateMinigame(boolean isClient) {
        if (this.minigame != null && this.minigame.equals(Minigames.get(Minigames.TOURNAMENT_END))) {
            this.endTournament(isClient);
            return;
        } else if (this.round >= this.minigames.size()) {
//            Another bit of weirdness from singleplayer, we only want to set this once the integrated server has already run
//            But if the server is dedicated, we need to set this, since setting this on the server won't affect the client
            if (isClient || MCTournament.SERVER.isDedicated()) {
                this.minigame = Minigames.get(Minigames.TOURNAMENT_END);
            }
        } else {
            this.minigame = this.minigames.get(this.round);
        }

        if (isClient) {
            this.minigame.clientBegin();
        } else {
            this.minigame.serverBegin();
        }
    }

    public void endTournament(boolean isClient) {
        if (!isClient) {
            MCTournament.SERVER.getPlayerManager().broadcast(Text.translatable("tournament.mctournament.end_message"), true);
        } else {
            MCTournament.CLIENT.inGameHud.getChatHud().addMessage(Text.literal("Your name is: " + MCTournament.CLIENT.player.getName().getString()));
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


//    A "Bill Pugh" Singleton implementation
    private Tournament() {}
    private static class Singleton {
        private static final Tournament INSTANCE = new Tournament();
    }
    public static Tournament getInstance() {
        return Singleton.INSTANCE;
    }
}
