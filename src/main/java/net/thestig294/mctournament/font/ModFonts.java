package net.thestig294.mctournament.font;

import net.minecraft.util.Identifier;
import net.thestig294.mctournament.MCTournament;

public class ModFonts {
    public static final Identifier QUESTION = register("question");
    public static final Identifier QUESTION_ANSWER = register("question_answer");
    public static final Identifier QUESTION_NUMBER = register("question_number");

    private static Identifier register(String name){
        return new Identifier(MCTournament.MOD_ID, name);
    }

    public static void registerFonts(){
        MCTournament.LOGGER.info("Registering custom fonts for: " + MCTournament.MOD_ID);
    }
}
