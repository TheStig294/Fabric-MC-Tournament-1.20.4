package net.thestig294.mctournament.texture;

import net.minecraft.util.Identifier;
import net.thestig294.mctournament.MCTournament;

public class ModTextures {
    public static final Identifier QUESTION_TIMER_FRONT = register("textures/gui/question_timer_front.png");
    public static final Identifier QUESTION_TIMER_BACK = register("textures/gui/question_timer_back.png");

    private static Identifier register(String name) {
        return new Identifier(MCTournament.MOD_ID, name);
    }

    public static void registerTextures() {
        MCTournament.LOGGER.info("Registering textures for: " + MCTournament.MOD_ID);
    }
}
