package net.thestig294.mctournament.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.network.ModNetworking;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class ModUtilClient {
    private static final Map<String, PlayerEntity> CACHED_PLAYERS = new HashMap<>();
    private static final Random SHARED_RANDOM = Random.create();

    public static void init() {
        ModNetworking.clientReceive(ModNetworking.REQUEST_RESPAWN, clientReceiveInfo -> {
            ClientPlayerEntity client = MCTournament.client().player;
            if (client == null) return;
            client.requestRespawn();
            MCTournament.client().setScreen(null);
        });
    }

    /**
     * A cached copy of a player's entity from their name, this is safe to use in rapidly repeated calls like tick hooks!
     * @param playerName A string of the player's teams-safe name (See: {@link PlayerEntity#getNameForScoreboard()})
     * @return The {@link PlayerEntity} of the player, or {@code null} if they cannot be found
     */
    public static @Nullable PlayerEntity getPlayer(String playerName) {
        PlayerEntity cachedPlayer = CACHED_PLAYERS.get(playerName);
        if (ModUtil.isValid(cachedPlayer)) return cachedPlayer;

        PlayerEntity player = getPlayers().stream()
                .filter(ply -> ply.getNameForScoreboard().equals(playerName))
                .findFirst()
                .orElse(null);

        CACHED_PLAYERS.put(playerName, player);
        return player;
    }

    public static List<AbstractClientPlayerEntity> getPlayers() {
        ClientWorld world = MCTournament.client().world;
        if (world == null) return Collections.emptyList();
        return world.getPlayers();
    }

    public static void playSound(SoundEvent sound) {
        playSound(sound, 1.0f);
    }

    public static void playSound(SoundEvent sound, float pitch) {
        playSound(sound, pitch, 1.0f);
    }

    public static void playSound(SoundEvent sound, float pitch, float volume) {
        MCTournament.client().getSoundManager().play(PositionedSoundInstance.master(sound, pitch, volume));
    }

    public static float getTicksPerSecond() {
        ClientWorld world = MCTournament.client().world;
        if (world == null) return 20.0f;
        return world.getTickManager().getTickRate();
    }

    public static void printChat(Text text) {
        MCTournament.client().inGameHud.getChatHud().addMessage(text);
    }

    public static Random random() {
        return SHARED_RANDOM;
    }
}
