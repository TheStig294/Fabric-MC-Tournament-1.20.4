package net.thestig294.mctournament.texture;

import net.minecraft.util.Identifier;
import net.thestig294.mctournament.MCTournament;

public class ModTextures {
    public static Identifier registerTexture(String name) {
        return new Identifier(MCTournament.MOD_ID, name);
    }

    public static void registerTextures() {
        MCTournament.LOGGER.info("Registering textures for: {}", MCTournament.MOD_ID);
    }
}
