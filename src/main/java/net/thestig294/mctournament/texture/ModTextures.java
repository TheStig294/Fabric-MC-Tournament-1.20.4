package net.thestig294.mctournament.texture;

import net.minecraft.util.Identifier;
import net.thestig294.mctournament.MCTournament;

public class ModTextures {
    public static final Identifier QUESTION_TIMER_BACK = register("textures/gui/question_timer/question_timer_back.png");
    public static final Identifier[] QUESTION_TIMER_HANDS = registerHand();
    public static final int QUESTION_TIMER_HAND_COUNT = 31;

    private static Identifier[] registerHand() {
        Identifier[] handList = new Identifier[QUESTION_TIMER_HAND_COUNT];

        for (int i = 0; i < QUESTION_TIMER_HAND_COUNT; i++) {
            handList[i] = register("textures/gui/question_timer/question_timer_hand" + i + ".png");
        }

        return handList;
    }

    private static Identifier register(String name) {
        return new Identifier(MCTournament.MOD_ID, name);
    }

    public static void registerTextures() {
        MCTournament.LOGGER.info("Registering textures for " + MCTournament.MOD_ID);
    }
}
