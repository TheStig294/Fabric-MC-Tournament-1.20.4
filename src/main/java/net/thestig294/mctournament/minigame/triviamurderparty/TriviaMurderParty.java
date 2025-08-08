package net.thestig294.mctournament.minigame.triviamurderparty;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.Minigame;
import net.thestig294.mctournament.minigame.Minigames;
import net.thestig294.mctournament.minigame.triviamurderparty.question.Questions;
import net.thestig294.mctournament.minigame.triviamurderparty.screen.QuestionScreen;
import net.thestig294.mctournament.util.ModUtil;

import static net.thestig294.mctournament.font.ModFonts.registerFont;
import static net.thestig294.mctournament.minigame.MinigameVariants.registerVariant;
import static net.thestig294.mctournament.texture.ModTextures.registerTexture;

public class TriviaMurderParty extends Minigame {
    public static final String ID = "trivia_murder_party";

    @Override
    public Text getName() {
        return Text.translatable("minigame.trivia_murder_party.name");
    }

    @Override
    public void serverInit() {
        Fonts.register();
        Textures.register();
        Variants.register();

        Questions.register();
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


    public static class Fonts {
        public static final Identifier QUESTION = registerFont("question");
        public static final Identifier QUESTION_ANSWER = registerFont("question_answer");
        public static final Identifier QUESTION_NUMBER = registerFont("question_number");

        public static void register() {
            ModUtil.logRegistration("fonts", TriviaMurderParty.ID);
        }
    }

    public static class Textures {
        public static final Identifier QUESTION_TIMER_BACK = registerTexture("textures/gui/question_timer/question_timer_back.png");
        public static final Identifier[] QUESTION_TIMER_HANDS = registerHand();
        public static final int QUESTION_TIMER_HAND_COUNT = 31;

        private static Identifier[] registerHand() {
            Identifier[] handList = new Identifier[QUESTION_TIMER_HAND_COUNT];

            for (int i = 0; i < QUESTION_TIMER_HAND_COUNT; i++) {
                handList[i] = registerTexture("textures/gui/question_timer/question_timer_hand" + i + ".png");
            }

            return handList;
        }

        public static void register() {
            ModUtil.logRegistration("textures", TriviaMurderParty.ID);
        }
    }

    public static class Variants {
        public static final String GAMING = registerVariant(Minigames.TRIVIA_MURDER_PARTY, "gaming");
        public static final String AUSSIE = registerVariant(Minigames.TRIVIA_MURDER_PARTY, "aussie");
        public static final String SILLY = registerVariant(Minigames.TRIVIA_MURDER_PARTY, "silly");
        public static final String YOGSCAST = registerVariant(Minigames.TRIVIA_MURDER_PARTY, "yogscast");

        public static void register() {
            ModUtil.logRegistration("variants", TriviaMurderParty.ID);
        }
    }
}
