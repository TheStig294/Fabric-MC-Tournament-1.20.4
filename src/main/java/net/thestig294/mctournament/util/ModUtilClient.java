package net.thestig294.mctournament.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.thestig294.mctournament.MCTournament;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ModUtilClient {
    public static @Nullable PlayerEntity getPlayer(String playerName) {
        if (MCTournament.CLIENT == null || MCTournament.CLIENT.world == null) return null;
        for (final var player : getPlayers()) {
            if (player.getNameForScoreboard().equals(playerName)) return player;
        }
        return null;
    }

    public static List<AbstractClientPlayerEntity> getPlayers() {
        if (MCTournament.CLIENT == null || MCTournament.CLIENT.world == null) return Collections.emptyList();
        return MCTournament.CLIENT.world.getPlayers();
    }

    public static void playSound(SoundEvent sound) {
        playSound(sound, 1.0f);
    }

    public static void playSound(SoundEvent sound, float pitch) {
        playSound(sound, pitch, 1.0f);
    }

    public static void playSound(SoundEvent sound, float pitch, float volume) {
        MCTournament.CLIENT.getSoundManager().play(PositionedSoundInstance.master(sound, pitch, volume));
    }

    public static float getTicksPerSecond() {
        ClientWorld world = MCTournament.CLIENT.world;
        if (world == null) return 20.0f;
        return world.getTickManager().getTickRate();
    }
}
