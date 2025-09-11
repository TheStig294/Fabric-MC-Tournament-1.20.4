package net.thestig294.mctournament.structure;

import net.minecraft.block.Block;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
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
    public static final Structure CORRIDOR_LOGS = registerStructure("corridor_logs", 1, 1, 2);

    public static final JigsawStartPool CORRIDOR = registerJigsawStartPool("corridor/piece_0_pool", 1, -1, 2);

    public static BlockRotation JIGSAW_ROTATION_OVERRIDE;
    public static final Identifier MINECRAFT_EMPTY = new Identifier("minecraft", "empty");

    public static Structure registerStructure(String id, int xOffset, int yOffset, int zOffset) {
        return new Structure(new Identifier(MCTournament.MOD_ID, id), xOffset, yOffset, zOffset);
    }

    public static JigsawStartPool registerJigsawStartPool(String id, int xOffset, int yOffset, int zOffset) {
        return new JigsawStartPool(new Identifier(MCTournament.MOD_ID, id), xOffset, yOffset, zOffset);
    }

    public static void registerStructures() {
        ModUtil.logRegistration("structures");
    }

    /**
     * Places a structure facing in a direction relative to the given yaw, in the overworld,
     * it is assumed the structure was built facing north
     * @param structure Structure identifier
     * @param pos World position to spawn the structure
     * @param yaw Yaw rotation of the structure, clamps to the cardinal directions with a forgiveness of 45 degrees
     */
    public static void place(Structure structure, BlockPos pos, float yaw) {
        place(structure, pos, yaw, MCTournament.server().getOverworld());
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

    public static BlockPos offsetBlockPos(BlockPos pos, int xOffset, int yOffset, int zOffset, BlockRotation rotation) {
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
        BlockPos offsetPos = offsetBlockPos(pos, structure.xOffset, structure.yOffset, structure.zOffset, rotation);
        MinecraftServer server = MCTournament.server();
        StructurePlacementData placementData = new StructurePlacementData().setRotation(rotation);
        server.getStructureTemplateManager().getTemplateOrBlank(structure.id)
                .place(world, offsetPos, offsetPos, placementData, StructureBlockBlockEntity.createRandom(0), Block.NOTIFY_LISTENERS);
    }

    /**
     * Places a jigsaw structure facing in a direction relative to the given yaw, in the overworld,
     * it is assumed the structure was built facing north
     * @param startPool Identifier for the start pool of the jigsaw structure
     * @param pos Position of the first jigsaw structure
     * @param yaw Yaw rotation of the structure, clamps to the cardinal directions with a forgiveness of 45 degrees
     */
    public static void jigsawPlace(JigsawStartPool startPool, BlockPos pos, float yaw) {
        jigsawPlace(startPool, pos, yaw, MCTournament.server().getOverworld());
    }

    /**
     * Places a jigsaw structure facing in a direction relative to the given yaw, in the specified dimension,
     * it is assumed the structure was built facing north
     * @param startPool Structure identifier
     * @param pos World position to spawn the structure
     * @param yaw Yaw rotation of the structure, clamps to the cardinal directions with a forgiveness of 45 degrees
     * @param world A dimension's world to place the structure in
     */
    public static void jigsawPlace(JigsawStartPool startPool, BlockPos pos, float yaw, ServerWorld world) {
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
    public static void jigsawPlace(JigsawStartPool startPool, BlockPos pos, BlockRotation rotation, ServerWorld world) {
        BlockPos offsetPos = offsetBlockPos(pos, startPool.xOffset, startPool.yOffset, startPool.zOffset, rotation);
        RegistryKey<Registry<StructurePool>> templatePoolRegistryKey = RegistryKeys.TEMPLATE_POOL;
        Registry<StructurePool> structurePoolRegistry = MCTournament.server().getRegistryManager().get(templatePoolRegistryKey);
        RegistryEntry<StructurePool> startPoolEntry = structurePoolRegistry.getEntry(structurePoolRegistry.get(startPool.id));
        JIGSAW_ROTATION_OVERRIDE = rotation;
        StructurePoolBasedGenerator.generate(world, startPoolEntry, MINECRAFT_EMPTY, 7, offsetPos, false);
        JIGSAW_ROTATION_OVERRIDE = null;
    }

    public record Structure(Identifier id, int xOffset, int yOffset, int zOffset){}
    public record JigsawStartPool(Identifier id, int xOffset, int yOffset, int zOffset){}
}
