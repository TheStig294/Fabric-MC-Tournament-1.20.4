package net.thestig294.mctournament.network;

import net.minecraft.util.Identifier;
import net.thestig294.mctournament.MCTournament;

public class ModNetworking {
    public static final Identifier OPEN_QUESTION_SCREEN = register("open_question_screen");

    @SuppressWarnings("SameParameterValue")
    private static Identifier register(String name) {
        return new Identifier(MCTournament.MOD_ID, name);
    }

    public static void registerNetworkingIds() {
        MCTournament.LOGGER.info("Registering networking IDs for " + MCTournament.MOD_ID);
    }
}
