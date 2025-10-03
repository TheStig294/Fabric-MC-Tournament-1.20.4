package net.thestig294.mctournament.command;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.network.ModNetworking;
import net.thestig294.mctournament.screen.AnimatedScreen;
import net.thestig294.mctournament.util.ModUtilClient;

@Environment(EnvType.CLIENT)
public class ModClientCommands {
    public static void registerCommands() {
        ModNetworking.clientReceive(ModNetworking.COMMAND_CLEAR_SCREEN, (clientReceiveInfo) -> executeClearScreen());
    }

    private static void executeClearScreen() {
        if (AnimatedScreen.PAUSED_SCREEN != null) {
            AnimatedScreen.PAUSED_SCREEN.close();
        }

        if (AnimatedScreen.ACTIVE_HUD_SCREEN != null) {
            AnimatedScreen.ACTIVE_HUD_SCREEN.close();
        }

        MCTournament.client().setScreen(null);

        ModUtilClient.printChat(Text.translatable("commands." + MCTournament.MOD_ID + ".clearscreen"));
    }
}
