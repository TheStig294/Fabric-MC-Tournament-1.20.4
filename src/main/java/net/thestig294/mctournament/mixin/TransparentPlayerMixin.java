package net.thestig294.mctournament.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.thestig294.mctournament.util.ModUtilClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

// Lets players be set as "transparent" rather than completely invisible to each other, with the normal invisibility effect
@Environment(EnvType.CLIENT)
@Mixin(LivingEntityRenderer.class)
public abstract class TransparentPlayerMixin {
    @ModifyConstant(
            method = {"render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"},
            constant = {@Constant(
                    floatValue = 0.15F
            )}
    )
    private float overrideInvisibilityAlpha(float original) {
        return 0.35F;
    }

    @ModifyVariable(
            method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            ordinal = 1,
            at = @At("STORE")
    )
    private <T extends LivingEntity> boolean overrideTranslucentBoolean(boolean original, @Local(argsOnly = true) T entity) {
        return entity instanceof PlayerEntity player && ModUtilClient.TRANSPARENT_PLAYERS.contains(player.getNameForScoreboard()) || original;
    }
}
