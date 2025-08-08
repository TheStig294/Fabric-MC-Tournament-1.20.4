package net.thestig294.mctournament.minigame.triviamurderparty;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.Minigame;
import net.thestig294.mctournament.minigame.Minigames;
import net.thestig294.mctournament.minigame.triviamurderparty.question.Questions;
import net.thestig294.mctournament.minigame.triviamurderparty.screen.QuestionScreen;

import static net.thestig294.mctournament.minigame.Minigames.registerVariant;

public class TriviaMurderParty extends Minigame {
    @Override
    public Text getName() {
        return Text.translatable("minigame.trivia_murder_party.name");
    }

    @Override
    public void serverInit() {
        Questions.registerQuestions();
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
        Questions.shuffleCategory(this.getVariant());
        MCTournament.CLIENT.setScreen(new QuestionScreen(Questions.getNext(), Questions.getQuestionNumber()));
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientEnd() {

    }

    public static class Variants {
        public static final String GAMING = registerVariant(Minigames.TRIVIA_MURDER_PARTY, "gaming");
        public static final String AUSSIE = registerVariant(Minigames.TRIVIA_MURDER_PARTY, "aussie");
        public static final String SILLY = registerVariant(Minigames.TRIVIA_MURDER_PARTY, "silly");
        public static final String YOGSCAST = registerVariant(Minigames.TRIVIA_MURDER_PARTY, "yogscast");
    }
}
