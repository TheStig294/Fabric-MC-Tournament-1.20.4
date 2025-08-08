package net.thestig294.mctournament.minigame.triviamurderparty.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.minigame.triviamurderparty.question.Question;
import net.thestig294.mctournament.util.ModUtil;
import net.thestig294.mctournament.minigame.triviamurderparty.widget.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionScreen extends Screen {
    private static final float QUESTION_NUMBER_FADE_TIME = 1.0f;
    private static final float QUESTION_NUMBER_HOLD_TIME = 3.0f;
    private static final float QUESTION_ZOOM_TIME = 1.0f;
    private static final float TIMER_MOVE_TIME = 1.0f;
    private static final float QUESTION_ANSWER_TIME = 20.0f;
    private static final float ANSWER_ZOOM_TIME = 0.5f;
    private static final float ANSWER_HOLD_TIME = 3.0f;
    private static final float CORRECT_REVEAL_TIME = 3.0f;
    private static final float INCORRECT_REVEAL_TIME = 3.0f;
    private static final float KILLING_ROOM_TRANSITION_MOVE_TIME = 2.0f;
    private static final float KILLING_ROOM_TRANSITION_LIGHTS_TIME = 5.0f;
    private static final float ALL_CORRECT_LOOP_BACK_TIME = 1.0f;

    private final Screen parent;
    private final Question question;
    private final int questionNumber;

    private float uptimeSecs;
    private float uptimeSeconds;
    private QuestionScreen.State state;
    private float stateEndTime;
    private float stateStartTime;
    private float stateProgress;
    private boolean firstStateTick;

    private List<QuestionPlayer> playerWidgets;
    private QuestionText questionNumberWidget;
    private QuestionText questionWidget;
    private List<QuestionButton> answerWidgets;
    private List<QuestionText> answeredCountWidgets;
    private QuestionTimer timerWidget;
    private List<QuestionText> roomCodeWidgets;

    public QuestionScreen(Question question, int questionNumber) {
        super(Text.empty());
        this.parent = MCTournament.CLIENT.currentScreen;
        this.question = question;
        this.questionNumber = questionNumber;

        this.uptimeSecs = 0.0f;
        this.uptimeSeconds = 0.0f;
        this.state = State.QUESTION_NUMBER_IN;
        this.stateEndTime = 0.0f;
        this.stateStartTime = 0.0f;
        this.stateProgress = 0.0f;
        this.firstStateTick = true;

        this.playerWidgets = new ArrayList<>();
        this.answerWidgets = new ArrayList<>();
        this.answeredCountWidgets = new ArrayList<>();
        this.roomCodeWidgets = new ArrayList<>();
    }

    @Override
    protected void init() {
        super.init();

        if (MCTournament.CLIENT.world == null) return;
        int i = 0;
        for (final var player : MCTournament.CLIENT.world.getPlayers()) {
            this.playerWidgets.add(this.addDrawableChild(new QuestionPlayer(i * this.width / 8, 5,
                    this.width / 8, this.height / 3, player)));
            i++;
        }

        this.questionNumberWidget = this.addDrawableChild(new QuestionText(this.width / 2, this.height / 2,
                Integer.toString(this.questionNumber), TriviaMurderParty.Fonts.QUESTION_NUMBER, 20, Color.YELLOW,
                this.width / 2, this.textRenderer));

        this.questionWidget = this.addDrawableChild(new QuestionText(this.width / 30, this.height / 3,
                this.question.question(), TriviaMurderParty.Fonts.QUESTION,25, Color.WHITE, this.width * 3 / 5,
                this.textRenderer));

        this.answerWidgets.add(this.addDrawableChild(new QuestionButton(this, this.width * 2/3,
                this.height / 2 - 30, 140, 20, this.textRenderer, 1, this.question)));
        this.answerWidgets.add(this.addDrawableChild(new QuestionButton(this, this.width * 2/3,
                this.height / 2, 140, 20, this.textRenderer, 2, this.question)));
        this.answerWidgets.add(this.addDrawableChild(new QuestionButton(this, this.width * 2/3,
                this.height / 2 + 30, 140, 20, this.textRenderer, 3, this.question)));
        this.answerWidgets.add(this.addDrawableChild(new QuestionButton(this, this.width * 2/3,
                this.height / 2 + 60, 140, 20, this.textRenderer, 4, this.question)));

        this.answeredCountWidgets.add(this.addDrawableChild(new QuestionText(5, this.height - 65,
                "ANSWER\nNOW!", TriviaMurderParty.Fonts.QUESTION_ANSWER, 15, Color.RED, 100, this.textRenderer)));
        this.answeredCountWidgets.add(this.addDrawableChild(new QuestionText( 15, this.height - 40,
                "0", TriviaMurderParty.Fonts.QUESTION_NUMBER, 20, Color.ORANGE, 100, this.textRenderer)));
        this.answeredCountWidgets.add(this.addDrawableChild(new QuestionText( 0, this.height - 15,
                "ANSWERED", TriviaMurderParty.Fonts.QUESTION_ANSWER, 10, Color.GRAY, 100, this.textRenderer)));

        this.timerWidget = this.addDrawableChild(new QuestionTimer(this.width / 3, this.height - 64,
                64, 64, 20, 1.0f, 0));

        this.roomCodeWidgets.add(this.addDrawableChild(new QuestionText( this.width - 75, this.height - 30,
                "MINECRAFT.TV", TriviaMurderParty.Fonts.QUESTION_ANSWER, 10, Color.GRAY, 100, this.textRenderer)));
        this.roomCodeWidgets.add(this.addDrawableChild(new QuestionText( this.width - 40, this.height - 15,
                ModUtil.getRandomString(4, 2), TriviaMurderParty.Fonts.QUESTION_ANSWER, 10, Color.RED,
                100, this.textRenderer)));

        for (final var child : this.children()) {
            if (child instanceof ClickableWidget widget) {
                widget.setAlpha(0.0f);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
//        Minecraft runs at 20 ticks per second
        this.uptimeSecs += delta / 20.0f;
        this.stateProgress = ModUtil.lerp(this.stateStartTime, this.stateEndTime, this.uptimeSecs);

        this.handleState();
        this.firstStateTick = false;

        if (this.stateEndTime > this.uptimeSecs) {
            this.updateState();
        }
    }

    private void handleState() {
        switch (this.state) {
            case QUESTION_NUMBER_IN -> this.questionNumberWidget.setAlpha(this.stateProgress);
            case QUESTION_NUMBER_HOLD -> {}
            case QUESTION_NUMBER_OUT -> this.questionNumberWidget.setAlpha(1 - this.stateProgress);
        }
    }

    private void updateState() {
        this.state = this.state.next();
        float lastEndTime = this.stateEndTime;

        this.stateEndTime = this.uptimeSecs + switch (this.state) {
            case QUESTION_NUMBER_IN, QUESTION_NUMBER_OUT -> QUESTION_NUMBER_FADE_TIME;
            case QUESTION_NUMBER_HOLD -> QUESTION_NUMBER_HOLD_TIME;
            case QUESTION_IN, QUESTION_OUT -> QUESTION_ZOOM_TIME;
            case QUESTION_HOLD -> this.question.holdTime();
            case TIMER_IN, TIMER_OUT -> TIMER_MOVE_TIME;
            case ANSWERING -> QUESTION_ANSWER_TIME;
            case ANSWER_PRE_QUIP -> this.answerPreQuipTime;
            case ANSWER_IN -> ANSWER_ZOOM_TIME;
            case ANSWER_HOLD -> ANSWER_HOLD_TIME;
            case ANSWER_POST_QUIP -> this.answerPostQuipTime;
            case REVEAL_CORRECT -> CORRECT_REVEAL_TIME;
            case REVEAL_INCORRECT -> INCORRECT_REVEAL_TIME;
            case INCORRECT_QUIP -> this.incorrectQuipTime;
            case KILLING_ROOM_TRANSITION_MOVE -> KILLING_ROOM_TRANSITION_MOVE_TIME;
            case KILLING_ROOM_TRANSITION_LIGHTS -> KILLING_ROOM_TRANSITION_LIGHTS_TIME;
            case ALL_CORRECT_LOOP_BACK -> ALL_CORRECT_LOOP_BACK_TIME;
        };

        this.stateStartTime = lastEndTime;
        this.firstStateTick = true;
    }

    @Override
    public void close() {
        MCTournament.CLIENT.setScreen(this.parent);
    }

    public enum State {
        QUESTION_NUMBER_IN,
        QUESTION_NUMBER_HOLD,
        QUESTION_NUMBER_OUT,
        QUESTION_IN,
        QUESTION_HOLD,
        QUESTION_OUT,
        TIMER_IN,
        ANSWERING,
        TIMER_OUT,
        ANSWER_PRE_QUIP,
        ANSWER_IN,
        ANSWER_HOLD,
        ANSWER_POST_QUIP,
        REVEAL_CORRECT,
        REVEAL_INCORRECT,
        INCORRECT_QUIP,
        KILLING_ROOM_TRANSITION_MOVE,
        KILLING_ROOM_TRANSITION_LIGHTS,
        ALL_CORRECT_LOOP_BACK;

        private boolean isPlayerIncorrect() {

        }

        public State next() {
            return switch (this) {
                case REVEAL_CORRECT -> this.isPlayerIncorrect() ? REVEAL_INCORRECT : ALL_CORRECT_LOOP_BACK;
                case INCORRECT_QUIP -> Random.create().nextBoolean() ? KILLING_ROOM_TRANSITION_MOVE : KILLING_ROOM_TRANSITION_LIGHTS;
                case KILLING_ROOM_TRANSITION_MOVE -> KILLING_ROOM_TRANSITION_MOVE;
                case KILLING_ROOM_TRANSITION_LIGHTS -> KILLING_ROOM_TRANSITION_LIGHTS;
                case ALL_CORRECT_LOOP_BACK -> QUESTION_IN;
                default -> values()[this.ordinal() + 1];
            };
        }
    }
}
