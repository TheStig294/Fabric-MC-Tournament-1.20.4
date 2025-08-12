package net.thestig294.mctournament.texture;

import net.minecraft.util.Identifier;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.util.ModUtil;

public class ModTextures {
    public static Identifier registerTexture(String name) {
        return new Identifier(MCTournament.MOD_ID, name);
    }

    public static void registerTextures() {
        ModUtil.logRegistration("textures");
    }
}
