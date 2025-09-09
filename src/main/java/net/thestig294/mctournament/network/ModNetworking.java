package net.thestig294.mctournament.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.thestig294.mctournament.MCTournament;

import java.util.function.Consumer;

public class ModNetworking {
    public static final Identifier TOURNAMENT_SETUP = registerNetworkID("tournament_setup");
    public static final Identifier TOURNAMENT_END_ROUND = registerNetworkID("tournament_end_round");
    public static final Identifier TOURNAMENT_CLIENT_END_ROUND = registerNetworkID("tournament_client_end_round");
    public static final Identifier TOURNAMENT_MID_ROUND_JOIN = registerNetworkID("tournament_mid_round_join");
    public static final Identifier SCOREBOARD_UPDATE_TEAMS = registerNetworkID("scoreboard_update_teams");
    public static final Identifier SCOREBOARD_UPDATE_TEAM_CAPTAINS = registerNetworkID("scoreboard_update_team_captains");
    public static final Identifier SCOREBOARD_PLAYER_JOINED = registerNetworkID("scoreboard_player_joined");


    public static Identifier registerNetworkID(String name) {
        return new Identifier(MCTournament.MOD_ID, name);
    }

    public static void registerNetworkIDs() {
        MCTournament.LOGGER.info("Registering networking IDs for " + MCTournament.MOD_ID);
    }

    public static void broadcast(Identifier channelName) {
        broadcast(channelName, PacketByteBufs.empty());
    }

    public static void broadcast(Identifier channelName, PacketByteBuf buffer) {
        for (final var player : PlayerLookup.all(MCTournament.server())) {
            ServerPlayNetworking.send(player, channelName, buffer);
        }
    }

    public static void send(Identifier channelName, ServerPlayerEntity player) {
        send(channelName, player, PacketByteBufs.empty());
    }

    public static void send(Identifier channelName, ServerPlayerEntity player, PacketByteBuf buffer) {
        ServerPlayNetworking.send(player, channelName, buffer);
    }

    @Environment(EnvType.CLIENT)
    public static void sendToServer(Identifier channelName) {
        sendToServer(channelName, PacketByteBufs.empty());
    }

    @Environment(EnvType.CLIENT)
    public static void sendToServer(Identifier channelName, PacketByteBuf buffer) {
        ClientPlayNetworking.send(channelName, buffer);
    }

    public static void serverReceive(Identifier channelName, Consumer<ServerReceiveInfo> function) {
        ServerPlayNetworking.registerGlobalReceiver(channelName,
                (server, player, handler, buf, responseSender)
                        -> server.execute(() -> function.accept(new ServerReceiveInfo(server, player, handler, buf, responseSender))));
    }

    @Environment(EnvType.CLIENT)
    public static void clientReceive(Identifier channelName, Consumer<ClientReceiveInfo> function) {
        ClientPlayNetworking.registerGlobalReceiver(channelName,
                (client, handler, buf, responseSender)
                        -> client.execute(() -> function.accept(new ClientReceiveInfo(client, handler, buf, responseSender))));
    }

    public record ServerReceiveInfo(
            MinecraftServer client,
            ServerPlayerEntity player,
            ServerPlayNetworkHandler handler,
            PacketByteBuf buf,
            PacketSender responseSender
    ) {}

    public record ClientReceiveInfo(
            MinecraftClient client,
            ClientPlayNetworkHandler handler,
            PacketByteBuf buffer,
            PacketSender responseSender
    ) {}
}
