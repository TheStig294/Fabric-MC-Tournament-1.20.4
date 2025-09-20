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
        Screen currentScreen = MCTournament.client().currentScreen;

        if (AnimatedScreen.PAUSED_SCREEN != null && currentScreen == null) {
            MCTournament.client().setScreen(AnimatedScreen.PAUSED_SCREEN);
            AnimatedScreen.PAUSED_SCREEN = null;
        }
    }
}
