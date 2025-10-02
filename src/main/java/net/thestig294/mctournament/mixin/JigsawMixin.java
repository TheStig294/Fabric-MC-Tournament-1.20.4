package net.thestig294.mctournament.mixin;

import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.util.BlockRotation;
import net.thestig294.mctournament.structure.ModStructures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(StructurePoolBasedGenerator.class)
public class JigsawMixin {
    @ModifyVariable(
            method = "generate(Lnet/minecraft/world/gen/structure/Structure$Context;" +
                    "Lnet/minecraft/registry/entry/RegistryEntry;" +
                    "Ljava/util/Optional;ILnet/minecraft/util/math/BlockPos;ZLjava/util/Optional;" +
                    "ILnet/minecraft/structure/pool/alias/StructurePoolAliasLookup;)" +
                    "Ljava/util/Optional;",
            at = @At("STORE"),
            ordinal = 0
    )
	private static BlockRotation blockRotation(BlockRotation originalRotation) {
        if (ModStructures.JIGSAW_ROTATION_MIXIN != null) {
            return ModStructures.JIGSAW_ROTATION_MIXIN;
        }
        return originalRotation;
	}
}