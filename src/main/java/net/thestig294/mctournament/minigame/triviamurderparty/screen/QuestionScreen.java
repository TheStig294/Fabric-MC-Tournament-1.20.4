package net.thestig294.mctournament.minigame.triviamurderparty.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.minigame.triviamurderparty.question.Question;
import net.thestig294.mctournament.minigame.triviamurderparty.question.Questions;
import net.thestig294.mctournament.network.ModNetworking;
import net.thestig294.mctournament.tournament.Tournament;
import net.thestig294.mctournament.util.ModColors;
import net.thestig294.mctournament.util.ModUtil;
import net.thestig294.mctournament.minigame.triviamurderparty.widget.*;
import net.thestig294.mctournament.util.ModUtilClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class QuestionScreen extends Screen {
    private static final float TITLE_FADE_TIME = 0.5f;
    private static final float TITLE_HOLD_TIME = 4.0f;
    private static final int TITLE_SHAKE_MAX = 5;
    private static final int TITLE_SHAKE_FREQUENCY = 5;
    private static final float WELCOME_FADE_TIME = 1.0f;

    private static final float SCREEN_IN_TIME = 1.0f;
    private static final float HUD_IN_TIME = 1.0f;

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
    private State state;
    private float stateEndTime;
    private float stateStartTime;
    private float stateProgress;
    private int stateProgressPercent;
    private boolean firstStateTick;
    private boolean firstState;

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Map<String, Boolean> playerAnswers;

    private final List<QuestionText> titleWidgets;
    private QuestionBox leftBoxWidget;
    private QuestionBox rightBoxWidget;
    private final List<QuestionPlayer> playerWidgets;
    private QuestionText questionNumberWidget;
    private QuestionText questionWidget;
    private final List<QuestionButton> answerWidgets;
    private final List<QuestionText> answeredCountWidgets;
    private QuestionTimer timerWidget;
    private final List<QuestionText> roomCodeWidgets;

    public QuestionScreen(Question question, int questionNumber) {
        this(question, questionNumber, State.SCREEN_IN);
    }

    public QuestionScreen(Question question, int questionNumber, State startingState) {
        super(Text.empty());
        this.parent = MCTournament.CLIENT.currentScreen;
        this.question = question;
        this.questionNumber = questionNumber;

        this.uptimeSecs = 0.0f;
        this.state = startingState;
        this.stateEndTime = 0.0f;
        this.stateStartTime = 0.0f;
        this.stateProgress = 0.0f;
        this.stateProgressPercent = 0;
        this.firstStateTick = true;
        this.firstState = true;

        this.playerAnswers = new HashMap<>();

        this.titleWidgets = new ArrayList<>();
        this.playerWidgets = new ArrayList<>();
        this.answerWidgets = new ArrayList<>();
        this.answeredCountWidgets = new ArrayList<>();
        this.roomCodeWidgets = new ArrayList<>();
    }

    @Override
    protected void init() {
        super.init();

        this.titleWidgets.add(this.addDrawableChild(new QuestionText(this.width / 2, this.height / 5, "TRIVIA",
                TriviaMurderParty.Fonts.QUESTION_NUMBER, 20, ModColors.YELLOW, this.width, this.textRenderer)));
        this.titleWidgets.add(this.addDrawableChild(new QuestionText(this.width / 2, this.height * 2 / 5, "MURDER",
                TriviaMurderParty.Fonts.QUESTION_NUMBER, 20, ModColors.RED, this.width, this.textRenderer)));
        this.titleWidgets.add(this.addDrawableChild(new QuestionText(this.width / 2, this.height * 3 / 5, "PARTY",
                TriviaMurderParty.Fonts.QUESTION_NUMBER, 20, ModColors.LIGHT_BLUE, this.width, this.textRenderer)));

        this.leftBoxWidget = this.addDrawableChild(new QuestionBox(-this.width / 2,0, this.width / 2, this.height, ModColors.BLACK));
        this.rightBoxWidget = this.addDrawableChild(new QuestionBox(this.width,0, this.width, this.height, ModColors.BLACK));

        List<PlayerEntity> teamCaptains = Tournament.inst().clientScoreboard().getValidTeamCaptains();
        for (int i = 0; i < teamCaptains.size(); i++) {
            this.playerWidgets.add(this.addDrawableChild(new QuestionPlayer(i * this.width / 8, 0,
                    this.width / 8, this.height / 3, teamCaptains.get(i), this.textRenderer)));
        }

        this.questionNumberWidget = this.addDrawableChild(new QuestionText(this.width / 2, this.height / 2,
                Integer.toString(this.questionNumber), TriviaMurderParty.Fonts.QUESTION_NUMBER, 20, ModColors.YELLOW,
                this.width / 2, this.textRenderer));

        this.questionWidget = this.addDrawableChild(new QuestionText(this.width / 3, this.height / 2,
                this.question.question(), TriviaMurderParty.Fonts.QUESTION,25, ModColors.WHITE, this.width * 3 / 5,
                this.textRenderer));

        this.answerWidgets.add(this.addDrawableChild(new QuestionButton(this, this.width * 2/3,
                this.height / 2 - 30, 140, 20, this.textRenderer, 1, this.question, 0)));
        this.answerWidgets.add(this.addDrawableChild(new QuestionButton(this, this.width * 2/3,
                this.height / 2, 140, 20, this.textRenderer, 2, this.question, 1)));
        this.answerWidgets.add(this.addDrawableChild(new QuestionButton(this, this.width * 2/3,
                this.height / 2 + 30, 140, 20, this.textRenderer, 3, this.question, 2)));
        this.answerWidgets.add(this.addDrawableChild(new QuestionButton(this, this.width * 2/3,
                this.height / 2 + 60, 140, 20, this.textRenderer, 4, this.question, 3)));

        this.answeredCountWidgets.add(this.addDrawableChild(new QuestionText(25, this.height - 45,
                "ANSWER\nNOW!", TriviaMurderParty.Fonts.QUESTION_ANSWER, 15, ModColors.RED, 100, this.textRenderer)));
        this.answeredCountWidgets.add(this.addDrawableChild(new QuestionText( 25, this.height - 30,
                "0", TriviaMurderParty.Fonts.QUESTION_NUMBER, 20, ModColors.ORANGE, 100, this.textRenderer)));
        this.answeredCountWidgets.add(this.addDrawableChild(new QuestionText( 25, this.height,
                "ANSWERED", TriviaMurderParty.Fonts.QUESTION_ANSWER, 10, ModColors.GREY, 100, this.textRenderer)));

        this.timerWidget = this.addDrawableChild(new QuestionTimer(this.width / 3, this.height - 64,
                64, 64, 20, 1.0f, TIMER_MOVE_TIME));

        this.roomCodeWidgets.add(this.addDrawableChild(new QuestionText( this.width - 40, this.height - 12,
                "MINECRAFT.TV", TriviaMurderParty.Fonts.QUESTION_ANSWER, 10, ModColors.GREY, 100, this.textRenderer)));
        this.roomCodeWidgets.add(this.addDrawableChild(new QuestionText( this.width - 20, this.height,
                ModUtil.getRandomString(4, 2), TriviaMurderParty.Fonts.QUESTION_ANSWER, 10, ModColors.RED,
                100, this.textRenderer)));

        this.handleRefresh();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        if (this.stateEndTime <= this.uptimeSecs) {
            this.nextState();
        }

//        Minecraft runs at 20 ticks per second
        this.uptimeSecs += delta / 20.0f;
        this.stateProgress = ModUtil.lerpPercent(this.stateStartTime, this.stateEndTime, this.uptimeSecs);
        this.stateProgressPercent = (int) (this.stateProgress * 100);

        this.renderState();
    }

    private float animate(float start, float end) {
        return ModUtil.lerpLinear(start, end, this.stateProgress);
    }

    private int animate(int start, int end) {
        return (int) animate(((float) start), ((float) end));
    }

    @SuppressWarnings("SameParameterValue")
    private void everyStatePercent(int percent, Runnable function) {
        if (this.stateProgressPercent % percent == 0) function.run();
    }

    private void ifFirstStateTick(Runnable function) {
        if (this.firstStateTick) function.run();
    }

    private void setAlpha(List<? extends Element> widgets, float alpha) {
        for (final var child : widgets) {
            if (child instanceof ClickableWidget widget) {
                widget.setAlpha(alpha);
            }
        }
    }

    private void renderState() {
        switch (this.state) {
            case TITLE_IN -> {
                this.ifFirstStateTick(() -> {
                    this.leftBoxWidget.setPosition(0,0);
                    this.rightBoxWidget.setPosition(this.width / 2, 0);
                    this.leftBoxWidget.setAlpha(1.0f);
                    this.rightBoxWidget.setAlpha(1.0f);
                });
                this.setAlpha(this.titleWidgets, this.animate(0.0f, 1.0f));
            }
            case TITLE_HOLD -> this.everyStatePercent(TITLE_SHAKE_FREQUENCY, () -> this.titleWidgets.forEach(widget -> {
                widget.setX(widget.getOriginalX() + Random.create().nextBetween(-TITLE_SHAKE_MAX, TITLE_SHAKE_MAX));
                widget.setY(widget.getOriginalY() + Random.create().nextBetween(-TITLE_SHAKE_MAX, TITLE_SHAKE_MAX));
            }));
            case TITLE_OUT -> {
                this.ifFirstStateTick(() -> this.titleWidgets.forEach(widget -> {
                    widget.setX(widget.getOriginalX());
                    widget.setY(widget.getOriginalY());
                }));

                this.setAlpha(this.titleWidgets, this.animate(1.0f, 0.0f));
            }
            case WELCOME_IN -> {
                this.ifFirstStateTick(() -> this.questionNumberWidget.setText("WELCOME"));
                this.questionNumberWidget.setAlpha(this.animate(0.0f, 1.0f));
            }
            case WELCOME_HOLD -> {}
            case WELCOME_OUT -> this.questionNumberWidget.setAlpha(this.animate(1.0f, 0.0f));
            case SCREEN_IN -> {
                this.ifFirstStateTick(() -> {
                    this.leftBoxWidget.setAlpha(1.0f);
                    this.rightBoxWidget.setAlpha(1.0f);
                });
                this.leftBoxWidget.setX(this.animate(0, this.leftBoxWidget.getOriginalX()));
                this.rightBoxWidget.setX(this.animate(this.width / 2, this.rightBoxWidget.getOriginalX()));
            }
            case HUD_IN -> {
                this.playerWidgets.forEach(widget -> {
                    widget.setY(this.animate(-widget.getOriginalY() - widget.getHeight(), widget.getOriginalY()));
                    widget.setAlpha(this.animate(0.0f, 1.0f));
                });
                this.answeredCountWidgets.forEach(widget -> {
                    widget.setY(this.animate(this.height, widget.getOriginalY()));
                    widget.setAlpha(this.animate(0.0f, 1.0f));
                });
                this.roomCodeWidgets.forEach(widget -> {
                    widget.setY(this.animate(this.height, widget.getOriginalY()));
                    widget.setAlpha(this.animate(0.0f, 1.0f));
                });
            }
            case QUESTION_NUMBER_IN -> {
                this.ifFirstStateTick(() -> this.questionNumberWidget.setInt(this.questionNumber));
                this.questionNumberWidget.setAlpha(this.animate(0.0f, 1.0f));
            }
            case QUESTION_NUMBER_HOLD -> {}
            case QUESTION_NUMBER_OUT -> this.questionNumberWidget.setAlpha(this.animate(1.0f, 0.0f));
            case QUESTION_IN -> {
                this.ifFirstStateTick(() -> this.questionWidget.setPosition(this.width / 2, this.height));

                this.questionWidget.setY(this.animate(this.height, this.questionWidget.getOriginalY()));
                this.questionWidget.setAlpha(this.animate(0.0f, 1.0f));
            }
            case QUESTION_HOLD -> {}
            case QUESTION_OUT -> this.questionWidget.setX(this.animate(this.width / 2, this.questionWidget.getOriginalX()));
            case TIMER_IN -> {
                this.answerWidgets.forEach(widget -> {
                    widget.setX(this.animate(this.width + widget.getWidth(), widget.getOriginalX()));
                    widget.setAlpha(this.animate(0.0f, 1.0f));
                });
                this.timerWidget.setY(this.animate(this.height, this.timerWidget.getOriginalY()));
                this.timerWidget.setAlpha(this.animate(0.0f, 1.0f));
                this.timerWidget.reset();
            }
            case ANSWERING -> {}
            case TIMER_OUT -> {}
            case ANSWER_PRE_QUIP -> {}
            case ANSWER_IN -> {}
            case ANSWER_HOLD -> {}
            case ANSWER_POST_QUIP -> {}
            case REVEAL_CORRECT -> {}
            case REVEAL_INCORRECT -> {}
            case INCORRECT_QUIP -> {}
            case KILLING_ROOM_TRANSITION_MOVE -> {}
            case KILLING_ROOM_TRANSITION_LIGHTS -> {}
            case ALL_CORRECT_LOOP_BACK -> {}
        }

        this.firstStateTick = false;
    }

    private void handleRefresh() {
        this.setAlpha(this.children(), 0.0f);

        switch (this.state) {
            case TITLE_IN, TITLE_HOLD -> this.setAlpha(this.titleWidgets, 1.0f);
            case TITLE_OUT -> {}
            case WELCOME_IN, WELCOME_HOLD -> {
                this.questionNumberWidget.setAlpha(1.0f);
                this.questionNumberWidget.setText("WELCOME");
            }
            case WELCOME_OUT -> this.questionNumberWidget.setText("WELCOME");
            case SCREEN_IN, HUD_IN, QUESTION_NUMBER_IN, QUESTION_NUMBER_HOLD, QUESTION_NUMBER_OUT -> {
                this.setAlpha(this.playerWidgets, 1.0f);
                this.setAlpha(this.answeredCountWidgets, 1.0f);
                this.setAlpha(this.roomCodeWidgets, 1.0f);
            }
            case QUESTION_IN, QUESTION_HOLD, QUESTION_OUT -> {
                this.setAlpha(this.playerWidgets, 1.0f);
                this.setAlpha(this.answeredCountWidgets, 1.0f);
                this.setAlpha(this.roomCodeWidgets, 1.0f);
                this.questionWidget.setAlpha(1.0f);
            }
            case TIMER_IN, ANSWERING -> {
                this.setAlpha(this.playerWidgets, 1.0f);
                this.setAlpha(this.answeredCountWidgets, 1.0f);
                this.setAlpha(this.roomCodeWidgets, 1.0f);
                this.questionWidget.setAlpha(1.0f);
                this.setAlpha(this.answerWidgets, 1.0f);
                this.timerWidget.setAlpha(1.0f);
                this.timerWidget.reset(0.0f, 0);
            }
            case TIMER_OUT, ANSWER_PRE_QUIP -> {
                this.setAlpha(this.playerWidgets, 1.0f);
                this.setAlpha(this.answeredCountWidgets, 1.0f);
                this.setAlpha(this.roomCodeWidgets, 1.0f);
                this.questionWidget.setAlpha(1.0f);
                this.setAlpha(this.answerWidgets, 1.0f);
            }
            case ANSWER_IN -> {}
            case ANSWER_HOLD -> {}
            case ANSWER_POST_QUIP -> {}
            case REVEAL_CORRECT -> {}
            case REVEAL_INCORRECT -> {}
            case INCORRECT_QUIP -> {}
            case KILLING_ROOM_TRANSITION_MOVE -> {}
            case KILLING_ROOM_TRANSITION_LIGHTS -> {}
            case ALL_CORRECT_LOOP_BACK -> {}
        }
    }

    @SuppressWarnings("unused")
    float getQuip(QuipType quipType) {
        return 1.0f;
    }

    private void nextState() {
        if (!this.firstState) this.state = this.state.next();
        this.firstState = false;
        float lastEndTime = this.stateEndTime;

        this.stateEndTime = this.uptimeSecs + switch (this.state) {
            case TITLE_IN, TITLE_OUT -> TITLE_FADE_TIME;
            case TITLE_HOLD -> TITLE_HOLD_TIME;
            case WELCOME_IN, WELCOME_OUT -> WELCOME_FADE_TIME;
            case WELCOME_HOLD -> this.getQuip(QuipType.WELCOME);
            case SCREEN_IN -> SCREEN_IN_TIME;
            case HUD_IN -> HUD_IN_TIME;
            case QUESTION_NUMBER_IN, QUESTION_NUMBER_OUT -> QUESTION_NUMBER_FADE_TIME;
            case QUESTION_NUMBER_HOLD -> QUESTION_NUMBER_HOLD_TIME;
            case QUESTION_IN, QUESTION_OUT -> QUESTION_ZOOM_TIME;
            case QUESTION_HOLD -> this.question.holdTime();
            case TIMER_IN, TIMER_OUT -> TIMER_MOVE_TIME;
            case ANSWERING -> QUESTION_ANSWER_TIME;
            case ANSWER_PRE_QUIP -> this.getQuip(QuipType.PRE_ANSWER);
            case ANSWER_IN -> ANSWER_ZOOM_TIME;
            case ANSWER_HOLD -> ANSWER_HOLD_TIME;
            case ANSWER_POST_QUIP -> this.getQuip(QuipType.POST_ANSWER);
            case REVEAL_CORRECT -> CORRECT_REVEAL_TIME;
            case REVEAL_INCORRECT -> INCORRECT_REVEAL_TIME;
            case INCORRECT_QUIP -> this.getQuip(QuipType.INCORRECT);
            case KILLING_ROOM_TRANSITION_MOVE -> KILLING_ROOM_TRANSITION_MOVE_TIME;
            case KILLING_ROOM_TRANSITION_LIGHTS -> KILLING_ROOM_TRANSITION_LIGHTS_TIME;
            case ALL_CORRECT_LOOP_BACK -> ALL_CORRECT_LOOP_BACK_TIME;
        };

        this.stateStartTime = lastEndTime;
        this.firstStateTick = true;
    }

    public static void clientInit() {
        ModNetworking.clientReceive(TriviaMurderParty.NetworkIDs.QUESTION_SCREEN, clientReceiveInfo -> {
            PacketByteBuf buffer = clientReceiveInfo.buffer();
            int id = buffer.readInt();
            int questionNumber = buffer.readInt();

            Question question = Questions.getQuestionByID(id);
            MCTournament.CLIENT.setScreen(new QuestionScreen(question, questionNumber));
        });

        ModNetworking.clientReceive(TriviaMurderParty.NetworkIDs.QUESTION_ANSWERED, clientReceiveInfo -> {
            PacketByteBuf buffer = clientReceiveInfo.buffer();
            String playerName = buffer.readString();
            boolean isCorrect = buffer.readBoolean();
            boolean isCaptain = buffer.readBoolean();
            int answerPosition = buffer.readInt();

            ClientPlayerEntity clientPlayer = MCTournament.CLIENT.player;
            PlayerEntity answeredPlayer = ModUtilClient.getPlayer(playerName);
            if (clientPlayer == null) return;

            if (MCTournament.CLIENT.currentScreen instanceof QuestionScreen questionScreen) {
//                See this: https://stackoverflow.com/questions/27482579/how-is-this-private-variable-accessible
//                Java be wildin'
                questionScreen.playerAnswers.put(playerName, isCorrect);

                if (isCaptain) {
                    for (final var playerWidget : questionScreen.playerWidgets) {
                        if (playerWidget.getPlayer().getNameForScoreboard().equals(playerName)) {
                            playerWidget.setAnswerState(QuestionPlayer.AnswerState.ANSWERED);
                            ModUtilClient.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK);
                            break;
                        }
                    }
                    if (clientPlayer.isTeammate(answeredPlayer)) {
                        questionScreen.lockButtons();
                        questionScreen.answerWidgets.get(answerPosition).setSelectedAnswer();
                    }

                } else if (clientPlayer.isTeammate(answeredPlayer)) {
                    for (final var answerWidget : questionScreen.answerWidgets) {
                        answerWidget.removePlayerHead(answeredPlayer);
                    }
                    questionScreen.answerWidgets.get(answerPosition).setPlayerHead(answeredPlayer);
                }
            }
        });
    }

    @Override
    public void close() {
        MCTournament.CLIENT.setScreen(this.parent);
    }

    public void lockButtons() {
        this.answerWidgets.forEach(QuestionButton::lock);
    }

    public enum State {
        TITLE_IN, // Entrypoint for a new quiz
        TITLE_HOLD,
        TITLE_OUT,
        WELCOME_IN,
        WELCOME_HOLD,
        WELCOME_OUT,
        SCREEN_IN, // Entrypoint for returning from a killing room
        HUD_IN,
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
            return true;
        }

        public State next() {
            return switch (this) {
                case REVEAL_CORRECT -> this.isPlayerIncorrect() ? REVEAL_INCORRECT : ALL_CORRECT_LOOP_BACK;
                case INCORRECT_QUIP -> Random.create().nextBoolean() ? KILLING_ROOM_TRANSITION_MOVE : KILLING_ROOM_TRANSITION_LIGHTS;
                case KILLING_ROOM_TRANSITION_MOVE -> KILLING_ROOM_TRANSITION_MOVE;
                case KILLING_ROOM_TRANSITION_LIGHTS -> KILLING_ROOM_TRANSITION_LIGHTS;
                case ALL_CORRECT_LOOP_BACK -> QUESTION_NUMBER_IN;
                default -> values()[this.ordinal() + 1];
            };
        }
    }

    public enum QuipType {
        WELCOME,
        PRE_ANSWER,
        POST_ANSWER,
        INCORRECT
    }
}
