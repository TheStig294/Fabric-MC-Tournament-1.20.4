package net.thestig294.mctournament;

import net.fabricmc.api.ClientModInitializer;

public class MCTournamentClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MCTournament.LOGGER.info("=====Hello Client World!=====");
    }
}
