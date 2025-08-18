package net.thestig294.mctournament.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.thestig294.mctournament.MCTournament;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class ModUtilClient {
    public static @Nullable PlayerEntity clientGetPlayer(String playerName) {
        if (MCTournament.CLIENT == null || MCTournament.CLIENT.world == null) return null;
        for (final var player : clientGetPlayers()) {
            if (player.getNameForScoreboard().equals(playerName)) return player;
        }
        return null;
    }

    public static List<? extends PlayerEntity> clientGetPlayers() {
        if (MCTournament.CLIENT == null || MCTournament.CLIENT.world == null) return Collections.emptyList();
        return MCTournament.CLIENT.world.getPlayers();
    }

    public static void clientForAllPlayers(Consumer<PlayerEntity> function) {
        clientGetPlayers().forEach(function);
    }
}
