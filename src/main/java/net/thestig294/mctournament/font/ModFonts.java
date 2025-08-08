package net.thestig294.mctournament.font;

import net.minecraft.util.Identifier;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.util.ModUtil;

public class ModFonts {
    public static Identifier registerFont(String name){
        return new Identifier(MCTournament.MOD_ID, name);
    }

    public static void registerFonts(){
        ModUtil.logRegistration("fonts");
    }
}
