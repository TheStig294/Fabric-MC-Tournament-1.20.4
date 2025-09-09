package net.thestig294.mctournament;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.thestig294.mctournament.minigame.Minigames;
import net.thestig294.mctournament.tournament.Tournament;
import net.thestig294.mctournament.util.ModTimer;

public class MCTournamentClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MCTournament.LOGGER.info("=====Hello Client World!=====");
        MCTournament.setClient(MinecraftClient.getInstance());

        Minigames.registerMinigames(true);
        Tournament.inst().clientInit();

        ModTimer.init(true);
    }
}
