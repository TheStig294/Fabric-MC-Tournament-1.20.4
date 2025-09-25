package net.thestig294.mctournament.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.thestig294.mctournament.MCTournament;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ModUtil {
    public static void logRegistration(String type) {
        logRegistration(type, MCTournament.MOD_ID);
    }

    public static void logRegistration(String type, String ID) {
        MCTournament.LOGGER.info("Registering {} for {}", type, ID);
    }

//    Used for the fake room code shown on the bottom right of the QuestionScreen
//    (And potentially elsewhere, so I'm shoving this in a Util class :P)
    public static String getRandomString(int length, int numberChars){
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String nums = "0123456789";
        char[] builder = new char[4];
        Random random = Random.create();

        for (int i = 0; i < length; i++) {
            if (random.nextBoolean() || numberChars == 0) {
                builder[i] = chars.charAt(random.nextInt(chars.length()));
            } else {
                builder[i] = nums.charAt(random.nextInt(nums.length()));
                numberChars--;
            }
        }

        return new String(builder);
    }

    public static int clampInt(int value, int min, int max) {
        return Math.min(max, Math.max(value, min));
    }

    public static float clampFloat(float value, float min, float max) {
        return Math.min(max, Math.max(value, min));
    }

    /**
     * Returns how close a float is from the start, to the end value, as a percentage
     * @param start starting value
     * @param end end value
     * @param value current value
     * @return Percentage progress of the value, as a float
     */
    public static float lerpPercent(float start, float end, float value) {
        float total = end - start;
        float progress = value - start;
        return progress / total;
    }

    /**
     * Returns the value from start to end, that is the passed percent along from the start to the end
     * @param start starting value
     * @param end ending value
     * @param percent current percent progress from start to end
     * @return Lerped value, that is the specified distance along from start to end
     */
    public static float lerpLinear(float start, float end, float percent) {
        float difference = end - start;
        float progress = difference * percent;
        return start + progress;
    }

    public static @Nullable ServerPlayerEntity getPlayer(String playerName) {
        if (MCTournament.server() == null || MCTournament.server().getPlayerManager() == null) return null;
        return MCTournament.server().getPlayerManager().getPlayer(playerName);
    }

    public static List<ServerPlayerEntity> getPlayers() {
        if (MCTournament.server() == null || MCTournament.server().getPlayerManager() == null) return Collections.emptyList();
        return MCTournament.server().getPlayerManager().getPlayerList();
    }

    public static void forAllPlayers(Consumer<ServerPlayerEntity> function) {
        getPlayers().forEach(function);
    }

    public static float getTicksPerSecond() {
        return MCTournament.server().getTickManager().getTickRate();
    }

    public static void runConsoleCommand(String command, @Nullable Object... args) {
        runConsoleCommand(MCTournament.server().getCommandSource(), command, args);
    }

    public static void runConsoleCommand(ServerCommandSource source, String command, @Nullable Object... args) {
        MCTournament.server().getCommandManager().executeWithPrefix(source, String.format(command, args));
    }

    public static void placeRedstoneBlock(BlockPos pos) {
        runConsoleCommand("/setblock %d %d %d minecraft:redstone_block", pos.getX(), pos.getY(), pos.getZ());
    }

    public static void teleportFacingNorth(PlayerEntity player, BlockPos pos) {
//        The "180 0" part of the /tp command forces the player to face north: yaw, pitch
        runConsoleCommand("/tp %s %s %s %s 180 0", player.getNameForScoreboard(),
                pos.getX(), pos.getY(), pos.getZ());
    }
}
