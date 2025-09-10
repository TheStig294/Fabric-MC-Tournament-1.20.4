package net.thestig294.mctournament.structure;

import net.minecraft.block.Block;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.util.ModUtil;

public class ModStructures {
    public static final Structure CORRIDOR_LOGS = registerStructure("corridor_logs", 1, 1, 2);

    public static Structure registerStructure(String id, int xOffset, int yOffset, int zOffset) {
        return new Structure(new Identifier(MCTournament.MOD_ID, id), xOffset, yOffset, zOffset);
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

    /**
     * Places a structure facing in a direction relative to the given yaw, in the specified dimension,
     * it is assumed the structure was built facing north
     * @param structure Structure identifier
     * @param pos World position to spawn the structure
     * @param yaw Yaw rotation of the structure, clamps to the cardinal directions with a forgiveness of 45 degrees
     * @param world A dimension's world to place the structure in
     */
    public static void place(Structure structure, BlockPos pos, float yaw, ServerWorldAccess world) {
        BlockRotation rotation;

        if (yaw > -255 && yaw <= -135) {
            rotation = BlockRotation.NONE;
        } else if (yaw <= -45) {
            rotation = BlockRotation.CLOCKWISE_90;
        } else if (yaw <= 45) {
            rotation = BlockRotation.CLOCKWISE_180;
        } else if (yaw <= 135) {
            rotation = BlockRotation.COUNTERCLOCKWISE_90;
        } else {
            rotation = BlockRotation.NONE;
        }

        place(structure, pos, rotation, world);
    }

    /**
     * Places a structure with the given position, rotation, and dimension
     * @param structure Structure identifier
     * @param pos World position to spawn the structure
     * @param rotation Rotation of the structure, relative to its saved orientation in its {@code .nbt} file
     * @param world A dimension's world to place the structure in
     */
    public static void place(Structure structure, BlockPos pos, BlockRotation rotation, ServerWorldAccess world) {
        BlockPos offset = new BlockPos.Mutable(structure.xOffset, structure.yOffset, structure.zOffset).rotate(rotation);
        BlockPos offsetPos = pos.add(offset);

        MinecraftServer server = MCTournament.server();
        StructurePlacementData placementData = new StructurePlacementData().setRotation(rotation);
        server.getStructureTemplateManager().getTemplateOrBlank(structure.id)
                .place(world, offsetPos, offsetPos, placementData, StructureBlockBlockEntity.createRandom(0), Block.NOTIFY_LISTENERS);
    }

    public record Structure(Identifier id, int xOffset, int yOffset, int zOffset){}
}
