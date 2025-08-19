package net.thestig294.mctournament.minigame.triviamurderparty;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.Minigame;
import net.thestig294.mctournament.minigame.Minigames;
import net.thestig294.mctournament.minigame.triviamurderparty.question.Question;
import net.thestig294.mctournament.minigame.triviamurderparty.question.Questions;
import net.thestig294.mctournament.minigame.triviamurderparty.screen.QuestionScreen;
import net.thestig294.mctournament.minigame.triviamurderparty.screen.QuestionScreenHandler;
import net.thestig294.mctournament.network.ModNetworking;

import static net.thestig294.mctournament.font.ModFonts.registerFont;
import static net.thestig294.mctournament.minigame.MinigameVariants.registerVariant;
import static net.thestig294.mctournament.network.ModNetworking.registerNetworkID;
import static net.thestig294.mctournament.texture.ModTextures.registerTexture;

public class TriviaMurderParty extends Minigame {
    private QuestionScreenHandler questionScreenHandler;

    @Override
    public String getID() {
        return "trivia_murder_party";
    }

    @Override
    public void serverInit() {
        Questions.register();
        this.questionScreenHandler = new QuestionScreenHandler(this, this.scoreboard());
    }

    @Override
    public void serverBegin() {
        this.questionScreenHandler.begin();
    }

    @Override
    public void translateScores() {

    }

    @Override
    public void serverEnd() {

    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientInit() {
        ModNetworking.clientReceive(NetworkIDs.QUESTION_SCREEN, clientReceiveInfo -> {
            PacketByteBuf buffer = clientReceiveInfo.buffer();
            int id = buffer.readInt();
            int questionNumber = buffer.readInt();

            Question question = Questions.getQuestionByID(id);
            MCTournament.CLIENT.setScreen(new QuestionScreen(question, questionNumber));
        });
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientBegin() {

    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientEnd() {

    }

    public static class NetworkIDs {
        public static final Identifier QUESTION_SCREEN = registerNetworkID("question_screen");
        public static final Identifier QUESTION_ANSWERED = registerNetworkID("question_answered");
    }

    public static class Fonts {
        public static final Identifier QUESTION = registerFont("question");
        public static final Identifier QUESTION_ANSWER = registerFont("question_answer");
        public static final Identifier QUESTION_NUMBER = registerFont("question_number");
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
    }

    public static class Variants {
        public static final String GAMING = registerVariant(Minigames.TRIVIA_MURDER_PARTY, "gaming");
        public static final String AUSSIE = registerVariant(Minigames.TRIVIA_MURDER_PARTY, "aussie");
        public static final String SILLY = registerVariant(Minigames.TRIVIA_MURDER_PARTY, "silly");
        public static final String YOGSCAST = registerVariant(Minigames.TRIVIA_MURDER_PARTY, "yogscast");
    }
}
