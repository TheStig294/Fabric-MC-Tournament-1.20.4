package net.thestig294.mctournament.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.screen.AnimatedScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Returns a player back from a paused {@link AnimatedScreen}
 */
@Mixin(MinecraftClient.class)
public class AnimatedScreenMixin {
    @Inject(method = "setScreen", at = @At("RETURN"))
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        if (AnimatedScreen.PAUSED_SCREEN == null) return;

        if (screen == null) {
//            If a screen was paused, and the player is going back to the main game, set their screen
            MCTournament.client().setScreen(AnimatedScreen.PAUSED_SCREEN);
            AnimatedScreen.PAUSED_SCREEN = null;
        } else if (screen instanceof AnimatedScreen<?,?>) {
//            If a screen was paused, and a new animated screen is being set,
//            don't worry about returning to the old screen anymore
            AnimatedScreen.PAUSED_SCREEN = null;
        }
    }
}
