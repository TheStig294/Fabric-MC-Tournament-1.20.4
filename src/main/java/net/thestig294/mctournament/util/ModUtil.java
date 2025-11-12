package net.thestig294.mctournament.util;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.network.ModNetworking;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.Consumer;

public class ModUtil {
    private static final Map<String, ServerPlayerEntity> CACHED_PLAYERS = new HashMap<>();
    private static final Random SHARED_RANDOM = Random.create();

    public static void logRegistration(String type) {
        logRegistration(type, MCTournament.MOD_ID);
    }

    public static void logRegistration(String type, String ID) {
        MCTournament.LOGGER.info("Registering {} for {}", type, ID);
    }

//    Used for the fake room code shown on the bottom right of the QuestionScreen
//    (And potentially elsewhere, so I'm shoving this in a Util class :P)
    public static String getRandomString(boolean isClient, int length, int numberChars){
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String nums = "0123456789";
        char[] builder = new char[4];
        Random random = random(isClient);

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
     * @param playerName A string of the player's teams-safe name (See: {@link PlayerEntity#getNameForScoreboard()})
     * @return The {@link ServerPlayerEntity} of the player, or {@code null} if they cannot be found
     */
    public static @Nullable ServerPlayerEntity getServerPlayer(String playerName) {
        ServerPlayerEntity player = CACHED_PLAYERS.get(playerName);
        if (isValid(player)) return player;

        if (MCTournament.server() == null || MCTournament.server().getPlayerManager() == null) return null;
        player = MCTournament.server().getPlayerManager().getPlayer(playerName);
        CACHED_PLAYERS.put(playerName, player);
        return player;
    }

    public static @Nullable PlayerEntity getPlayer(boolean isClient, String playerName) {
        return isClient ? ModUtilClient.getPlayer(playerName) : getServerPlayer(playerName);
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

    public static void placeAirBlock(BlockPos pos) {
        runConsoleCommand("/setblock %d %d %d minecraft:air", pos.getX(), pos.getY(), pos.getZ());
    }

    @SuppressWarnings("unused")
    public static void placeBlock(BlockPos pos, Block block) {
        runConsoleCommand("/setblock %d %d %d " + Registries.BLOCK.getId(block), pos.getX(), pos.getY(), pos.getZ());
    }

    public static int directionToYaw(Direction direction) {
        return switch (direction) {
            case WEST -> 90;
            case NORTH -> 180;
            case EAST -> -90;
            default -> 0;
        };
    }

    public static void teleportFacing(PlayerEntity player, BlockPos pos, Direction direction) {
        ModUtil.runConsoleCommand("/tp %s %s %s %s %s 0", player.getNameForScoreboard(),
                pos.getX(), pos.getY(), pos.getZ(), directionToYaw(direction));
    }

    public static void setGamemode(ServerPlayerEntity player, GameMode gamemode) {
        ModUtil.runConsoleCommand("/gamemode %s %s", gamemode.getName(), player.getNameForScoreboard());
    }

    public static Pair<Integer, Integer> getMinMax(int int1, int int2) {
        int min, max;

        if (int1 < int2) {
            min = int1;
            max = int2;
        } else {
            min = int2;
            max = int1;
        }

        return new Pair<>(min, max);
    }

    public static boolean withinRange(double value, int bound1, int bound2) {
        Pair<Integer, Integer> range = getMinMax(bound1, bound2);
        return range.getLeft() <= value && value <= range.getRight();
    }

    public static List<ServerPlayerEntity> getPlayersWithinBound(BlockPos start, BlockPos end) {
        List<ServerPlayerEntity> result = new ArrayList<>();

        for (final var player : getPlayers()) {
            boolean withinX = withinRange(player.getPos().getX(), start.getX(), end.getX());
            boolean withinY = withinRange(player.getPos().getY(), start.getY(), end.getY());
            boolean withinZ = withinRange(player.getPos().getZ(), start.getZ(), end.getZ());

            if (withinX && withinY && withinZ) {
                result.add(player);
            }
        }

        return result;
    }

    public static void broadcastChatMessage(String message) {
        MCTournament.server().getPlayerManager().broadcast(Text.literal(message), false);
    }

    public static boolean isValid(@Nullable Entity entity) {
        return entity != null && !entity.isRemoved();
    }

    public static void setInvisible(ServerPlayerEntity player, boolean isInvisible) {
        if (isInvisible) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, StatusEffectInstance.INFINITE));
        } else {
            player.removeStatusEffect(StatusEffects.INVISIBILITY);
        }
    }

    public static void respawnIfDead(ServerPlayerEntity player) {
        if (player.isAlive()) return;
        player.networkHandler.player = player.server.getPlayerManager().respawnPlayer(player, false);
        ModNetworking.send(ModNetworking.REQUEST_RESPAWN, player);
    }

    public static Random random(boolean isClient) {
        return isClient ? ModUtilClient.random() : SHARED_RANDOM;
    }

    public static <T> @Nullable T getRandomElement(boolean isClient, List<T> list) {
        if (list.isEmpty()) return null;
        int size = list.size();
        if (size == 1) return list.get(0);

        return list.get(random(isClient).nextInt(size));
    }

    public static String getPositionString(int positionNum) {
        return switch (Math.abs(positionNum % 100)) {
            case 11,12,13 -> positionNum + "th";

            default -> positionNum + switch (Math.abs(positionNum % 10)) {
                case 1 -> "st";
                case 2 -> "nd";
                case 3 -> "rd";
                default -> "th";
            };
        };
    }

    public static void setTransparent(boolean isClient, PlayerEntity player, boolean isTransparent) {
        String playerName = player.getNameForScoreboard();

        if (isClient) {
            ModUtilClient.setTransparent(playerName, isTransparent);
        } else {
            ModNetworking.broadcast(ModNetworking.SET_TRANSPARENT, PacketByteBufs.create()
                    .writeString(playerName)
                    .writeBoolean(isTransparent)
            );
        }
    }
}
