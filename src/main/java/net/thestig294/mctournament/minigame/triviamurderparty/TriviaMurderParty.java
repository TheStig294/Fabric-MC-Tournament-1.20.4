package net.thestig294.mctournament.minigame.triviamurderparty;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.thestig294.mctournament.minigame.Minigame;
import net.thestig294.mctournament.minigame.Minigames;
import net.thestig294.mctournament.minigame.triviamurderparty.killingroom.KillingRooms;
import net.thestig294.mctournament.minigame.triviamurderparty.question.Questions;
import net.thestig294.mctournament.minigame.triviamurderparty.screen.KillingRoomScreen;
import net.thestig294.mctournament.minigame.triviamurderparty.screen.KillingRoomScreenHandler;
import net.thestig294.mctournament.minigame.triviamurderparty.screen.QuestionScreen;
import net.thestig294.mctournament.minigame.triviamurderparty.screen.QuestionScreenHandler;
import net.thestig294.mctournament.structure.JigsawStartPool;

import static net.thestig294.mctournament.font.ModFonts.registerFont;
import static net.thestig294.mctournament.minigame.MinigameVariants.registerVariant;
import static net.thestig294.mctournament.network.ModNetworking.registerNetworkID;
import static net.thestig294.mctournament.structure.ModStructures.registerJigsawStartPool;
import static net.thestig294.mctournament.texture.ModTextures.registerTexture;

public class TriviaMurderParty extends Minigame {
    public static final String ID = "trivia_murder_party";
    public static final int KILLING_ROOM_OFFSET = 20;

    private QuestionScreenHandler questionScreenHandler;
    private KillingRoomScreenHandler killingRoomScreenHandler;

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public float getScoreMultiplier() {
        return 1.0f;
    }

    @Override
    public boolean ignoreTeamCaptainScoreTransfer() {
        return false;
    }

    @Override
    public void serverInit() {
        Questions.register();
        this.questionScreenHandler = new QuestionScreenHandler(this);
        this.killingRoomScreenHandler = new KillingRoomScreenHandler(this);
    }

    @Override
    public void serverBegin() {
        this.hideNametags();
        this.questionScreenHandler.begin(this.getPosition());
        KillingRooms.begin(false, this);
        this.killingRoomScreenHandler.begin(this.getPosition().west(KILLING_ROOM_OFFSET));
    }

    public void startKillingRoom() {
        this.killingRoomScreenHandler.broadcastNextKillingRoom();
    }

    public boolean isPlayerCorrect(PlayerEntity player) {
        return this.scoreboard().getBoolean(player, Objectives.IS_CORRECT);
    }

    public boolean isPlayerDead(PlayerEntity player) {
        return this.scoreboard().getBoolean(player, Objectives.IS_DEAD);
    }

    public void setPlayerCorrect(PlayerEntity player, boolean isCorrect) {
        this.scoreboard().setBoolean(player, Objectives.IS_CORRECT, isCorrect);
    }

    public void setPlayerDead(PlayerEntity player, boolean isDead) {
        this.scoreboard().setBoolean(player, Objectives.IS_DEAD, isDead);
    }

    @Override
    public void serverEnd() {

    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientInit() {
        QuestionScreen.clientInit();
        KillingRoomScreen.clientInit();
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientBegin() {
        KillingRooms.begin(true, this);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientEnd() {

    }

    public static class Objectives {
        public static final String IS_CORRECT = "is_correct";
        public static final String IS_DEAD = "is_dead";
    }

    public static class Structures {
        public static final JigsawStartPool CORRIDOR = registerJigsawStartPool(ID, "corridor", 0, 0, 0);
    }

    public static class NetworkIDs {
        public static final Identifier QUESTION_SCREEN = registerNetworkID("question_screen");
        public static final Identifier QUESTION_ANSWERED = registerNetworkID("question_answered");
        public static final Identifier QUESTION_ANSWERING_END = registerNetworkID("question_answering_end");
        public static final Identifier QUESTION_ANSWERING_BEGIN = registerNetworkID("question_answering_begin");
        public static final Identifier QUESTION_ALL_CORRECT_LOOP_BACK = registerNetworkID("question_all_correct_loop_back");
        public static final Identifier QUESTION_TRIGGER_LIGHTS_OFF = registerNetworkID("question_trigger_lights_off");
        public static final Identifier QUESTION_MOVE_PLAYER = registerNetworkID("question_move_player");
        public static final Identifier QUESTION_SCREEN_START_KILLING_ROOM = registerNetworkID("question_screen_start_killing_room");

        public static final Identifier KILLING_ROOM_SCREEN = registerNetworkID("killing_room_screen");
        public static final Identifier KILLING_ROOM_BEGIN = registerNetworkID("killing_room_begin");
        public static final Identifier KILLING_ROOM_TIMER_UP = registerNetworkID("killing_room_timer_up");
    }

    public static class Fonts {
        public static final Identifier QUESTION = registerFont("question");
        public static final Identifier QUESTION_ANSWER = registerFont("question_answer");
        public static final Identifier QUESTION_NUMBER = registerFont("question_number");
    }

    public static class Textures {
        public static final Identifier QUESTION_TIMER_BACK = registerTexture("textures/gui/question_timer/question_timer_back.png");
        public static final Identifier[] QUESTION_TIMER_HANDS = registerHand();
        public static final int QUESTION_TIMER_HAND_COUNT = 60;
        public static final Identifier QUESTION_CROSS = registerTexture("textures/gui/question_cross.png");
        public static final Identifier QUESTION_TICK = registerTexture("textures/gui/question_tick.png");

        private static Identifier[] registerHand() {
            Identifier[] handList = new Identifier[QUESTION_TIMER_HAND_COUNT];

            for (int i = 0; i <= QUESTION_TIMER_HAND_COUNT; i++) {
                handList[i] = registerTexture("textures/gui/question_timer/question_timer_hand" + i + ".png");
            }

            return handList;
        }
    }

    @SuppressWarnings("unused")
    public static class Variants {
        public static final String GAMING = registerVariant(Minigames.TRIVIA_MURDER_PARTY, "gaming");
        public static final String AUSSIE = registerVariant(Minigames.TRIVIA_MURDER_PARTY, "aussie");
        public static final String SILLY = registerVariant(Minigames.TRIVIA_MURDER_PARTY, "silly");
        public static final String YOGSCAST = registerVariant(Minigames.TRIVIA_MURDER_PARTY, "yogscast");
    }
}
