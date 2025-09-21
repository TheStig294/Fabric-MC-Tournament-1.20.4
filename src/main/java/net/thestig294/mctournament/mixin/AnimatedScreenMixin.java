package net.thestig294.mctournament.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.thestig294.mctournament.screen.AnimatedScreen;
import net.thestig294.mctournament.util.ModTimer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Returns a player back from a paused {@link AnimatedScreen}
 */
@Mixin(MinecraftClient.class)
public class AnimatedScreenMixin {
    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen onSetScreen(Screen newScreen) {
        if (AnimatedScreen.PAUSED_SCREEN != null && newScreen == null) {
            ModTimer.simple(true, 0.1f, () -> AnimatedScreen.PAUSED_SCREEN = null);
            return AnimatedScreen.PAUSED_SCREEN;
        } else {
            return newScreen;
        }
    }
}
