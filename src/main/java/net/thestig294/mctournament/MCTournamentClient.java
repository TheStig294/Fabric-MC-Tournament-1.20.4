package net.thestig294.mctournament;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.thestig294.mctournament.minigame.Minigames;
import net.thestig294.mctournament.tournament.Tournament;

public class MCTournamentClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MCTournament.LOGGER.info("=====Hello Client World!=====");
        MCTournament.CLIENT = MinecraftClient.getInstance();

        Minigames.registerMinigames(true);
        Tournament.getInstance().clientInit();
    }
}
