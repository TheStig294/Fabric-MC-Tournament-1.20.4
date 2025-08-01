package net.thestig294.mctournament;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.thestig294.mctournament.util.ModUtil;

public class MCTournamentClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MCTournament.LOGGER.info("=====Hello Client World!=====");
        ModUtil.CLIENT = MinecraftClient.getInstance();
    }
}
