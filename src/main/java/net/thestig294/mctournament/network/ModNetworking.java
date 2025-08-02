package net.thestig294.mctournament.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.thestig294.mctournament.MCTournament;

public class ModNetworking {
    public static final Identifier TOURNAMENT_END_ROUND = register("tournament_end_round");
    public static final Identifier OPEN_QUESTION_SCREEN = register("open_question_screen");

    private static Identifier register(String name) {
        return new Identifier(MCTournament.MOD_ID, name);
    }

    public static void registerNetworkingIds() {
        MCTournament.LOGGER.info("Registering networking IDs for " + MCTournament.MOD_ID);
    }

    public static void broadcast(Identifier channelName) {
        broadcast(channelName, PacketByteBufs.empty());
    }

    public static void broadcast(Identifier channelName, PacketByteBuf buffer) {
        for (final var player : MCTournament.SERVER.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, channelName, buffer);
        }
    }
}
