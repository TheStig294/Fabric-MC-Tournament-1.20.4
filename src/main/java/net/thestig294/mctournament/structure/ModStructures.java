package net.thestig294.mctournament.structure;

import net.minecraft.block.Block;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.util.ModUtil;

public class ModStructures {
    /**
     * All jigsaw structures must name their starting pool "_start"
     */
    private static final String JIGSAW_START_POOL_NAME = "_start";
    private static final Identifier MINECRAFT_EMPTY = new Identifier("minecraft", "empty");
    public static BlockRotation JIGSAW_ROTATION_MIXIN;

    public static Structure registerStructure(String minigameID, String id, BlockPos offset) {
        return registerStructure(minigameID, id, offset.getX(), offset.getY(), offset.getZ());
    }

    public static Structure registerStructure(String minigameID, String id, int xOffset, int yOffset, int zOffset) {
        return new Structure(new Identifier(MCTournament.MOD_ID, minigameID + '/' + id), xOffset, yOffset, zOffset);
    }

    public static Identifier registerJigsawStartPool(String minigameID, String id) {
        return registerJigsawStartPool(minigameID + '/' + id + '/' + JIGSAW_START_POOL_NAME);
    }

    public static Identifier registerJigsawStartPool(String id) {
        return new Identifier(MCTournament.MOD_ID, id);
    }

    public static void registerStructures() {
        ModUtil.logRegistration("structures");
    }

    /**
     * Places a structure facing in the direction of a player,
     * it is assumed the structure was built facing north
     * @param structure Structure identifier
     * @param player Player to spawn the structure around
     */
    @SuppressWarnings("unused")
    public static void place(Structure structure, PlayerEntity player) {
        place(structure, player.getBlockPos(), player.getYaw(), (ServerWorldAccess) player.getWorld());
    }

    private static BlockRotation yawToRotation(float yaw) {
        if (yaw > -255 && yaw <= -135) {
            return BlockRotation.NONE;
        } else if (yaw <= -45) {
            return BlockRotation.CLOCKWISE_90;
        } else if (yaw <= 45) {
            return BlockRotation.CLOCKWISE_180;
        } else if (yaw <= 135) {
            return BlockRotation.COUNTERCLOCKWISE_90;
        } else {
            return BlockRotation.NONE;
        }
    }

    /**
     * Places a structure facing in a direction relative to the given yaw, in the specified dimension,
     * it is assumed the structure was built facing north
     * @param structure Structure identifier
     * @param pos World position to spawn the structure
     * @param yaw Yaw rotation of the structure, clamps to the cardinal directions with a forgiveness of 45 degrees
     * @param world A dimension's world to place the structure in
     */
    public static void place(Structure structure, BlockPos pos, float yaw, ServerWorldAccess world) {
        place(structure, pos, yawToRotation(yaw), world);
    }

    /**
     * Places a structure facing north in the overworld. It is assumed the structure was built facing north.
     * @param structure Structure identifier
     * @param pos World position to spawn the structure
     */
    public static void place(Structure structure, BlockPos pos) {
        place(structure, pos, BlockRotation.NONE, MCTournament.server().getOverworld());
    }

    private static BlockPos offsetBlockPos(BlockPos pos, int xOffset, int yOffset, int zOffset, BlockRotation rotation) {
        BlockPos offset = new BlockPos.Mutable(-xOffset, -yOffset, -zOffset).rotate(rotation);
        return pos.add(offset);
    }

    /**
     * Places a structure with the given position, rotation, and dimension
     * @param structure Structure identifier
     * @param pos World position to spawn the structure
     * @param rotation Rotation of the structure, relative to its saved orientation in its {@code .nbt} file
     * @param world A dimension's world to place the structure in
     */
    public static void place(Structure structure, BlockPos pos, BlockRotation rotation, ServerWorldAccess world) {
        BlockPos offsetPos = offsetBlockPos(pos, structure.xOffset(), structure.yOffset(), structure.zOffset(), rotation);
        StructurePlacementData placementData = new StructurePlacementData().setRotation(rotation);
        MCTournament.server().getStructureTemplateManager().getTemplateOrBlank(structure.id())
                .place(world, offsetPos, offsetPos, placementData, StructureBlockBlockEntity.createRandom(0), Block.NOTIFY_LISTENERS);
    }

    /**
     * Places a jigsaw structure around a player, spawning them on top of a jigsaw block named {@code minecraft:empty}
     * it is assumed the structure was built facing north
     * @param startPool Identifier for the start pool of the jigsaw structure
     * @param player Player to spawn the jigsaw structure around
     */
    public static void jigsawPlace(Identifier startPool, PlayerEntity player) {
        jigsawPlace(startPool, player.getBlockPos(), player.getYaw(), (ServerWorld) player.getWorld());
    }

    /**
     * Places a jigsaw structure facing in a direction relative to the given yaw, in the specified dimension,
     * it is assumed the structure was built facing north
     * @param startPool Structure identifier
     * @param pos World position to spawn the structure
     * @param yaw Yaw rotation of the structure, clamps to the cardinal directions with a forgiveness of 45 degrees
     * @param world A dimension's world to place the structure in
     */
    public static void jigsawPlace(Identifier startPool, BlockPos pos, float yaw, ServerWorld world) {
        jigsawPlace(startPool, pos, yawToRotation(yaw), world);
    }

    /**
     * Refer to {@link net.thestig294.mctournament.mixin.JigsawMixin} for what the {@code JIGSAW_ROTATION_OVERRIDE} value is doing.
     * <p>
     * Essentially, we have to mixin to override {@link StructurePoolBasedGenerator}'s {@code .generate()} function,
     * and stop it from randomly rotating the first jigsaw piece of a placed jigsaw structure,
     * and instead allow for a specified rotation.
     * </p>
     * @param startPool Identifier for the jigsaw structure
     * @param pos Position of the first jigsaw structure piece
     * @param rotation Rotation of the first jigsaw structure piece
     * @param world A dimension's world to place the structure in
     */
    public static void jigsawPlace(Identifier startPool, BlockPos pos, BlockRotation rotation, ServerWorld world) {
        RegistryKey<Registry<StructurePool>> templatePoolRegistryKey = RegistryKeys.TEMPLATE_POOL;
        Registry<StructurePool> structurePoolRegistry = MCTournament.server().getRegistryManager().get(templatePoolRegistryKey);
        RegistryEntry<StructurePool> startPoolEntry = structurePoolRegistry.getEntry(structurePoolRegistry.get(startPool));
        JIGSAW_ROTATION_MIXIN = rotation;
        StructurePoolBasedGenerator.generate(world, startPoolEntry, MINECRAFT_EMPTY, 7, pos, false);
        JIGSAW_ROTATION_MIXIN = null;
    }
}
