package net.thestig294.mctournament.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;
import net.thestig294.mctournament.MCTournament;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ModUtil {
    private static final Map<String, ServerPlayerEntity> CACHED_PLAYERS = new HashMap<>();

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

    /**
     * A cached copy of a player's entity from their name, this is safe to use in rapidly repeated calls like tick hooks!
     * @param playerName A string of the player's scoreboard-safe name (See: {@link PlayerEntity#getNameForScoreboard()})
     * @return The {@link ServerPlayerEntity} of the player, or {@code null} if they cannot be found
     */
    public static @Nullable ServerPlayerEntity getPlayer(String playerName) {
        ServerPlayerEntity player = CACHED_PLAYERS.get(playerName);
        if (isValid(player)) return player;

        if (MCTournament.server() == null || MCTournament.server().getPlayerManager() == null) return null;
        player = MCTournament.server().getPlayerManager().getPlayer(playerName);
        CACHED_PLAYERS.put(playerName, player);
        return player;
    }

    /**
     * An unmodifiable list of all players on the server, use {@link List}'s copy constructor if you want to modify.
     * @return A reference to the internal list of players
     */
    public static @Unmodifiable List<ServerPlayerEntity> getPlayers() {
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

    public static int directionToYaw(Direction direction) {
        return switch (direction) {
            case WEST -> -90;
            case NORTH -> 180;
            case EAST -> 90;
            default -> 0;
        };
    }

    public static void teleportFacing(PlayerEntity player, BlockPos pos, Direction direction) {
        player.requestTeleport(pos.getX(), pos.getY(), pos.getZ());
        player.setYaw(directionToYaw(direction));
    }

    public static void setGamemode(ServerPlayerEntity player, GameMode gamemode) {
        MCTournament.server().getPlayerInteractionManager(player).changeGameMode(gamemode);
    }

    public static List<ServerPlayerEntity> getPlayersWithinBound(BlockPos start, BlockPos end) {
        return getPlayers().stream().filter(player -> {
            BlockPos pos = player.getBlockPos();
            return start.getX() <= pos.getX() && pos.getX() <= end.getX() &&
                    start.getY() <= pos.getY() && pos.getY() <= end.getY() &&
                    start.getZ() <= pos.getZ() && pos.getZ() <= end.getZ();
        }).toList();
    }

    public static void chatMessage(String message) {
        printMessage(message, false);
    }

    public static void printMessage(String message, boolean overlay) {
        MCTournament.server().getPlayerManager().broadcast(Text.literal(message), overlay);
    }

    public static boolean isValid(@Nullable Entity entity) {
        return entity != null && !entity.isRemoved();
    }
}
