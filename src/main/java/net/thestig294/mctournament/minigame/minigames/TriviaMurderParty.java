package net.thestig294.mctournament.minigame.minigames;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.Minigame;
import net.thestig294.mctournament.screen.QuestionScreen;

public class TriviaMurderParty extends Minigame {
    @Override
    public Text getName() {
        return Text.translatable("minigame.trivia_murder_party.name");
    }

    @Override
    public void serverInit() {

    }

    @Override
    public void serverBegin() {

    }

    @Override
    public void serverEnd() {

    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientInit() {

    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientBegin() {
        MCTournament.CLIENT.setScreen(new QuestionScreen(Text.translatable("screen.mctournament.question"), MCTournament.CLIENT.currentScreen));
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientEnd() {

    }
}
