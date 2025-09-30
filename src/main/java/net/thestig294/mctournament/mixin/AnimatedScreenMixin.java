package net.thestig294.mctournament.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.screen.AnimatedScreen;
import net.thestig294.mctournament.util.ModTimer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(MinecraftClient.class)
public class AnimatedScreenMixin {
    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen onSetScreen(Screen newScreen) {
//        Returns a player from a paused AnimatedScreen
        if (AnimatedScreen.PAUSED_SCREEN != null && newScreen == null) {
            ModTimer.simple(true, 0.1f, () -> AnimatedScreen.PAUSED_SCREEN = null);
            return AnimatedScreen.PAUSED_SCREEN;
        }

//        Closes the old AnimatedScreen if a new one of the same type is being set
        Screen oldScreen = MCTournament.client().currentScreen;
        if (oldScreen instanceof AnimatedScreen<?,?> && oldScreen.getClass().isInstance(newScreen)) {
            oldScreen.close();
        }

        return newScreen;
    }

    @Inject(method = "onResolutionChanged", at = @At("TAIL"))
    private void onResolutionChanged(CallbackInfo ci) {
        if (AnimatedScreen.ACTIVE_HUD_SCREEN != null) {
            MinecraftClient client = MCTournament.client();
            AnimatedScreen.ACTIVE_HUD_SCREEN.resize(client, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
        }
    }
}
