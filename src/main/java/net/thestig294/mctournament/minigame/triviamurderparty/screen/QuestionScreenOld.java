package net.thestig294.mctournament.minigame.triviamurderparty.screen;

import it.unimi.dsi.fastutil.floats.FloatConsumer;
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
import net.thestig294.mctournament.minigame.triviamurderparty.widget.*;
import net.thestig294.mctournament.network.ModNetworking;
import net.thestig294.mctournament.tournament.Tournament;
import net.thestig294.mctournament.util.ModColors;
import net.thestig294.mctournament.util.ModUtil;
import net.thestig294.mctournament.util.ModUtilClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

@Environment(EnvType.CLIENT)
public class QuestionScreenOld extends Screen {
    private static final float TITLE_FADE_TIME = 0.5f;
    private static final float TITLE_HOLD_TIME = 4.0f;
    private static final int TITLE_SHAKE_MAX = 5;
    private static final int TITLE_SHAKE_FREQUENCY = 5;
    private static final float WELCOME_FADE_TIME = 1.0f;

    private static final float SCREEN_IN_TIME = 1.0f;
    private static final float HUD_IN_TIME = 1.0f;

    private static final float QUESTION_NUMBER_FADE_TIME = 1.0f;
    private static final float QUESTION_NUMBER_HOLD_TIME = 1.0f;
    private static final float QUESTION_ZOOM_TIME = 1.0f;
    private static final float TIMER_MOVE_TIME = 1.0f;
    private static final float ANSWER_ZOOM_TIME = 0.2f;
    private static final int ANSWER_SIZE_MULTIPLIER = 2;
    private static final float ANSWER_HOLD_TIME = 3.0f;
    private static final float CORRECT_REVEAL_TIME = 0.5f;
    private static final float CORRECT_POINTS_TIME = 1.0f;
    private static final float INCORRECT_REVEAL_TIME = 0.5f;
    private static final float INCORRECT_CROSSES_TIME = 0.5f;
    private static final float KILLING_ROOM_TRANSITION_MOVE_TIME = 1.0f;
    private static final float KILLING_ROOM_TRANSITION_LIGHTS_TIME = 1.0f;
    private static final float KILLING_ROOM_TRANSITION_HOLD_TIME = 3.0f;
    private static final float ALL_CORRECT_LOOP_BACK_TIME = 1.0f;
    private static final float ALL_CORRECT_LOOP_BACK_HOLD_TIME = 1.0f;

    private final Screen parent;
    private final Question question;
    private final int questionNumber;
    private final int answeringTimeSeconds;
    private int answeredCount;
    private final Map<String, Integer> captainAnswers;

    private float uptimeSecs;
    private State state;
    private float stateEndTime;
    private float stateStartTime;
    private float stateProgress;
    private int stateProgressPercent;
    private boolean firstStateTick;
    private boolean firstState;
    private int timerTicksLeft;

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

    public QuestionScreenOld(Question question, int questionNumber, int answeringTimeSeconds, State startingState) {
        super(Text.empty());
        this.parent = MCTournament.client().currentScreen;
        this.question = question;
        this.questionNumber = questionNumber;
        this.answeringTimeSeconds = answeringTimeSeconds;
        this.answeredCount = 0;
        this.captainAnswers = new HashMap<>();

        this.uptimeSecs = 0.0f;
        this.state = startingState;
        this.stateEndTime = 0.0f;
        this.stateStartTime = 0.0f;
        this.stateProgress = 0.0f;
        this.stateProgressPercent = 0;
        this.firstStateTick = true;
        this.firstState = true;

        this.titleWidgets = new ArrayList<>();
        this.playerWidgets = new ArrayList<>();
        this.answerWidgets = new ArrayList<>();
        this.answeredCountWidgets = new ArrayList<>();
        this.roomCodeWidgets = new ArrayList<>();
    }

    @Override
    protected void init() {
        super.init();

        this.titleWidgets.clear();
        this.titleWidgets.add(this.addDrawableChild(new QuestionText(this.width / 2, this.height / 5, "TRIVIA",
                TriviaMurderParty.Fonts.QUESTION_NUMBER, 20, ModColors.YELLOW, this.width, this.textRenderer)));
        this.titleWidgets.add(this.addDrawableChild(new QuestionText(this.width / 2, this.height * 2 / 5, "MURDER",
                TriviaMurderParty.Fonts.QUESTION_NUMBER, 20, ModColors.RED, this.width, this.textRenderer)));
        this.titleWidgets.add(this.addDrawableChild(new QuestionText(this.width / 2, this.height * 3 / 5, "PARTY",
                TriviaMurderParty.Fonts.QUESTION_NUMBER, 20, ModColors.LIGHT_BLUE, this.width, this.textRenderer)));

        this.questionNumberWidget = this.addDrawableChild(new QuestionText(this.width / 2, this.height / 2,
                Integer.toString(this.questionNumber), TriviaMurderParty.Fonts.QUESTION_NUMBER, 20, ModColors.YELLOW,
                this.width / 2, this.textRenderer));

        this.leftBoxWidget = this.addDrawableChild(new QuestionBox(-this.width / 2,0, this.width / 2, this.height, ModColors.BLACK));
        this.rightBoxWidget = this.addDrawableChild(new QuestionBox(this.width,0, this.width, this.height, ModColors.BLACK));

        this.playerWidgets.clear();
        List<PlayerEntity> teamCaptains = Tournament.inst().clientScoreboard().getValidTeamCaptains();
        for (int i = 0; i < teamCaptains.size(); i++) {
            QuestionPlayer player = new QuestionPlayer(i * this.width / 8, 0,
                    this.width / 8, this.height / 3, teamCaptains.get(i), this.textRenderer);
            player.setTickWidget(this.addDrawableChild(new QuestionImage(player.getX() + player.getWidth() / 4, player.getY() + player.getHeight() * 2/3,
                    player.getWidth() / 2, player.getWidth() / 2, TriviaMurderParty.Textures.QUESTION_TICK)));
            player.setCrossWidget(this.addDrawableChild(new QuestionImage(player.getX() + player.getWidth() / 4, player.getY(),
                    player.getWidth() / 2, player.getWidth() / 2, TriviaMurderParty.Textures.QUESTION_CROSS)));
            this.playerWidgets.add(this.addDrawableChild(player));
        }

        this.questionWidget = this.addDrawableChild(new QuestionText(this.width / 3, this.height / 2,
                this.question.question(), TriviaMurderParty.Fonts.QUESTION,25, ModColors.WHITE, this.width * 3 / 5,
                this.textRenderer));

        this.answerWidgets.clear();
        this.answerWidgets.add(this.addDrawableChild(new QuestionButton(this, this.width * 2/3,
                this.height / 2 - 30, 140, 20, this.textRenderer, 0, this.question)));
        this.answerWidgets.add(this.addDrawableChild(new QuestionButton(this, this.width * 2/3,
                this.height / 2, 140, 20, this.textRenderer, 1, this.question)));
        this.answerWidgets.add(this.addDrawableChild(new QuestionButton(this, this.width * 2/3,
                this.height / 2 + 30, 140, 20, this.textRenderer, 2, this.question)));
        this.answerWidgets.add(this.addDrawableChild(new QuestionButton(this, this.width * 2/3,
                this.height / 2 + 60, 140, 20, this.textRenderer, 3, this.question)));

        this.answeredCountWidgets.clear();
        this.answeredCountWidgets.add(this.addDrawableChild(new QuestionText(25, this.height - 45,
                "ANSWER\nNOW!", TriviaMurderParty.Fonts.QUESTION_ANSWER, 15, ModColors.RED, 100, this.textRenderer)));
        this.answeredCountWidgets.add(this.addDrawableChild(new QuestionText( 25, this.height - 30,
                "0", TriviaMurderParty.Fonts.QUESTION_NUMBER, 20, ModColors.ORANGE, 100, this.textRenderer)));
        this.answeredCountWidgets.add(this.addDrawableChild(new QuestionText( 25, this.height,
                "ANSWERED", TriviaMurderParty.Fonts.QUESTION_ANSWER, 10, ModColors.GREY, 100, this.textRenderer)));

        this.timerWidget = this.addDrawableChild(new QuestionTimer(this.width / 3, this.height - 64,
                64, 64, this.answeringTimeSeconds, 1.0f, TIMER_MOVE_TIME));

        this.roomCodeWidgets.clear();
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

        this.uptimeSecs += delta / ModUtilClient.getTicksPerSecond();
        this.stateProgress = ModUtil.lerpPercent(this.stateStartTime, this.stateEndTime, this.uptimeSecs);
        this.stateProgressPercent = (int) (this.stateProgress * 100);

        this.renderState();
    }

    private void animate(IntConsumer lambda, int start, int end) {
        lambda.accept((int) ModUtil.lerpLinear(start, end, this.stateProgress));
    }

    private void animate(FloatConsumer lambda, float start, float end) {
        lambda.accept(ModUtil.lerpLinear(start, end, this.stateProgress));
    }

    private void listAnimateAlpha(List<? extends Element> widgets, float start, float end) {
        for (final var child : widgets) {
            if (child instanceof ClickableWidget widget) {
                this.animate(widget::setAlpha, start, end);
            }
        }
    }

    public void setListAlpha(List<? extends Element> widgets, float alpha) {
        for (final var child : widgets) {
            if (child instanceof ClickableWidget widget) {
                widget.setAlpha(alpha);
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void everyStatePercent(int percent, Runnable function) {
        if (this.stateProgressPercent % percent == 0) function.run();
    }

    private void ifFirstStateTick(Runnable function) {
        if (this.firstStateTick) function.run();
    }

    private void renderState() {
        switch (this.state) {
            case TITLE_IN -> {
                this.ifFirstStateTick(this::resetBoxWidgets);
                this.listAnimateAlpha(this.titleWidgets, 0.0f, 1.0f);
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

                this.listAnimateAlpha(this.titleWidgets, 1.0f, 0.0f);
            }
            case WELCOME_IN -> {
                this.ifFirstStateTick(() -> this.questionNumberWidget.setText("screen.mctournament.question_welcome"));
                this.animate(this.questionNumberWidget::setAlpha, 0.0f, 1.0f);
            }
            case WELCOME_QUIP -> {}
            case WELCOME_OUT -> this.animate(this.questionNumberWidget::setAlpha, 1.0f, 0.0f);
            case SCREEN_IN -> {
                this.ifFirstStateTick(this::resetBoxWidgets);
                this.animate(this.leftBoxWidget::setX, 0, this.leftBoxWidget.getOriginalX());
                this.animate(this.rightBoxWidget::setX, this.width / 2, this.rightBoxWidget.getOriginalX());
            }
            case HUD_IN -> {
                this.playerWidgets.forEach(widget -> {
                    this.animate(widget::setY, -widget.getOriginalY() - widget.getHeight(), widget.getOriginalY());
                    this.animate(widget::setAlpha, 0.0f, 1.0f);
                });
                this.answeredCountWidgets.forEach(widget -> {
                    this.animate(widget::setY, this.height, widget.getOriginalY());
                    this.animate(widget::setAlpha, 0.0f, 1.0f);
                });
                this.roomCodeWidgets.forEach(widget -> {
                    this.animate(widget::setY, this.height, widget.getOriginalY());
                    this.animate(widget::setAlpha, 0.0f, 1.0f);
                });
            }
            case QUESTION_NUMBER_IN -> {
                this.ifFirstStateTick(() -> this.questionNumberWidget.setInt(this.questionNumber));
                this.animate(this.questionNumberWidget::setAlpha, 0.0f, 1.0f);
            }
            case QUESTION_NUMBER_HOLD -> {}
            case QUESTION_NUMBER_OUT -> this.animate(this.questionNumberWidget::setAlpha, 1.0f, 0.0f);
            case QUESTION_IN -> {
                this.ifFirstStateTick(() -> this.questionWidget.setPosition(this.width / 2, this.height));
                this.animate(this.questionWidget::setY, this.height, this.questionWidget.getOriginalY());
                this.animate(this.questionWidget::setAlpha, 0.0f, 1.0f);
            }
            case QUESTION_HOLD -> {}
            case QUESTION_OUT -> this.animate(this.questionWidget::setX, this.width / 2, this.questionWidget.getOriginalX());
            case TIMER_IN -> {
                this.answerWidgets.forEach(widget -> {
                    this.animate(widget::setX, this.width + widget.getWidth(), widget.getOriginalX());
                    this.animate(widget::setAlpha, 0.0f, 1.0f);
                });
                this.animate(this.timerWidget::setY, this.height, this.timerWidget.getOriginalY());
                this.animate(this.timerWidget::setAlpha, 0.0f, 1.0f);
                this.timerWidget.reset();
            }
            case ANSWERING -> {
                this.ifFirstStateTick(() -> ModNetworking.sendToServer(TriviaMurderParty.NetworkIDs.QUESTION_ANSWERING_BEGIN));
                this.timerTicksLeft = this.timerWidget.getTicksLeft();
            }
            case TIMER_OUT -> {
                this.ifFirstStateTick(() -> {
                    this.lockButtons();
                    this.resetAnsweredPlayers();
                    ModUtilClient.playSound(SoundEvents.BLOCK_BELL_USE);
                });
                this.animate(this.timerWidget::setY, this.timerWidget.getOriginalY(), this.height);
                this.animate(this.timerWidget::setAlpha, 1.0f, 0.0f);
                this.timerWidget.reset(0.0f, 0);
            }
            case ANSWER_PRE_QUIP -> {}
            case ANSWER_IN -> this.answerWidgets.forEach(widget -> {
                if (widget.isCorrect()) {
                    this.animate(widget::setX, widget.getOriginalX(), this.getFinalAnswerX(widget));
                    this.animate(widget::setY, widget.getOriginalY(), this.getFinalAnswerY(widget));
                    this.animate(widget::setWidth, widget.getOriginalWidth(), widget.getOriginalWidth() * ANSWER_SIZE_MULTIPLIER);
                    this.animate(widget::setHeight, widget.getOriginalHeight(), widget.getOriginalHeight() * ANSWER_SIZE_MULTIPLIER);
                } else {
                    this.animate(widget::setAlpha, 1.0f, 0.0f);
                }
            });
            case ANSWER_HOLD, ANSWER_POST_QUIP -> {}
            case REVEAL_CORRECT -> {
                this.ifFirstStateTick(() -> this.playerWidgets.forEach(widget -> {
                    if (!this.isPlayerCorrect(widget)) return;
                    widget.setAnswerState(QuestionPlayer.AnswerState.CORRECT);
                    widget.setBottomText("$" + QuestionScreenHandler.CORRECT_ANSWER_POINTS);
                    widget.setBottomTextAlpha(0.0f);
                }));
                this.playerWidgets.forEach(widget -> {
                    if (!this.isPlayerCorrect(widget)) return;
                    this.animate(widget::setBottomTextAlpha, 0.0f, 1.0f);
                    QuestionImage tick = widget.getTickWidget();
                    if (tick == null) return;
                    this.animate(tick::setX, tick.getOriginalX() - (tick.getOriginalWidth() / 2), tick.getOriginalX());
                    this.animate(tick::setY, tick.getOriginalY() - (tick.getOriginalHeight() / 2), tick.getOriginalY());
                    this.animate(tick::setWidth, tick.getOriginalWidth() * 2, tick.getOriginalWidth());
                    this.animate(tick::setHeight, tick.getOriginalHeight() * 2, tick.getOriginalHeight());
                    this.animate(tick::setAlpha, 0.0f, 1.0f);
                });
            }
            case REVEAL_CORRECT_POINTS -> this.playerWidgets.forEach(widget -> {
                if (!this.isPlayerCorrect(widget)) return;
                QuestionImage tick = widget.getTickWidget();
                if (tick == null) return;
                this.animate(tick::setAlpha, 1.0f, 0.0f);
            });
            case REVEAL_INCORRECT -> this.playerWidgets.forEach(widget -> {
                if (!this.isPlayerCorrect(widget)) return;
                this.animate(widget::setY, widget.getOriginalY(), -widget.getHeight() * 2);
                this.animate(widget::setBottomTextAlpha, 1.0f, 0.0f);
            });
            case REVEAL_INCORRECT_CROSSES -> {
                this.ifFirstStateTick(() -> this.playerWidgets.forEach(widget ->
                        widget.setAnswerState(QuestionPlayer.AnswerState.INCORRECT)));
                this.playerWidgets.forEach(widget -> {
                    if (this.isPlayerCorrect(widget)) return;
                    QuestionImage cross = widget.getCrossWidget();
                    if (cross == null) return;
                    this.animate(cross::setX, cross.getOriginalX() - (cross.getOriginalWidth() / 2), cross.getOriginalX());
                    this.animate(cross::setY, cross.getOriginalY() - (cross.getOriginalHeight() / 2), cross.getOriginalY());
                    this.animate(cross::setWidth, cross.getOriginalWidth() * 2, cross.getOriginalWidth());
                    this.animate(cross::setHeight, cross.getOriginalHeight() * 2, cross.getOriginalHeight());
                    this.animate(cross::setAlpha, 0.0f, 1.0f);
                });
            }
            case INCORRECT_QUIP -> {}
            case KILLING_ROOM_TRANSITION_MOVE -> this.children().forEach(child -> {
                this.ifFirstStateTick(() -> ModNetworking.sendToServer(TriviaMurderParty.NetworkIDs.QUESTION_MOVE_PLAYER));
                if (child instanceof QuestionBox widget) {
                    this.animate(widget::setAlpha, 0.0f, 1.0f);
                    this.animate(widget::setX, this.width / 2, 0);
                    this.animate(widget::setY, this.height / 2, 0);
                    this.animate(widget::setWidth, 0, this.width);
                    this.animate(widget::setHeight, 0, this.height);
                } else if ((child instanceof QuestionWidget widget) && widget.getAlpha() > 0.0f) {
                    this.animate(widget::setAlpha, 1.0f, 0.0f);
                }
            });
            case KILLING_ROOM_TRANSITION_LIGHTS -> {
                this.ifFirstStateTick(() -> ModNetworking.sendToServer(TriviaMurderParty.NetworkIDs.QUESTION_TRIGGER_LIGHTS_OFF));
                this.children().forEach(child -> {
                        if ((child instanceof QuestionWidget widget) && widget.getAlpha() > 0.0f) {
                        this.animate(widget::setAlpha, 1.0f, 0.0f);
                    }
                });
            }
            case KILLING_ROOM_TRANSITION_HOLD -> {}
            case ALL_CORRECT_LOOP_BACK -> {
                this.animate(this.questionWidget::setX, this.questionWidget.getOriginalX(), -this.questionWidget.getWidth());
                for (final var widget : this.answerWidgets) {
                    if (widget.isCorrect()) {
                        this.animate(widget::setX, this.getFinalAnswerX(widget), this.width + (widget.getWidth() / 4));
                        break;
                    }
                }
            }
            case ALL_CORRECT_LOOP_BACK_HOLD -> this.ifFirstStateTick(() -> ModNetworking.sendToServer(TriviaMurderParty.NetworkIDs.QUESTION_MOVE_PLAYER));
            case CLOSING_SCREEN -> this.ifFirstStateTick(() -> ModNetworking.sendToServer(TriviaMurderParty.NetworkIDs.QUESTION_SCREEN_START_KILLING_ROOM));
        }

        this.firstStateTick = false;
    }

    private void handleRefresh() {
        this.setListAlpha(this.children(), 0.0f);

        switch (this.state) {
            case TITLE_IN, TITLE_HOLD -> {
                this.resetBoxWidgets();
                this.setListAlpha(this.titleWidgets, 1.0f);
            }
            case TITLE_OUT -> this.resetBoxWidgets();
            case WELCOME_IN, WELCOME_QUIP -> {
                this.questionNumberWidget.setAlpha(1.0f);
                this.questionNumberWidget.setText("screen.mctournament.question_welcome");
            }
            case WELCOME_OUT -> this.questionNumberWidget.setText("screen.mctournament.question_welcome");
            case SCREEN_IN -> {}
            case HUD_IN, QUESTION_NUMBER_IN, QUESTION_NUMBER_HOLD, QUESTION_NUMBER_OUT -> {
                this.resetMainHUD();
                this.questionWidget.setAlpha(0.0f);
            }
            case QUESTION_IN, QUESTION_HOLD, QUESTION_OUT -> this.resetMainHUD();
            case TIMER_IN, ANSWERING -> {
                this.resetMainHUD();
                this.setListAlpha(this.answerWidgets, 1.0f);
                this.timerWidget.setAlpha(1.0f);
                this.timerWidget.reset(0.0f, this.timerTicksLeft);
            }
            case TIMER_OUT, ANSWER_PRE_QUIP -> {
                this.resetMainHUD();
                this.setListAlpha(this.answerWidgets, 1.0f);
                this.lockButtons();
                this.resetAnsweredPlayers();
            }
            case ANSWER_IN, ANSWER_HOLD, ANSWER_POST_QUIP -> {
                this.resetMainHUD();
                this.resetRevealedAnswer();
                this.resetAnsweredPlayers();
            }
            case REVEAL_CORRECT -> {
                this.resetMainHUD();
                this.resetRevealedAnswer();
                this.playerWidgets.forEach(widget -> {
                    if (this.isPlayerCorrect(widget)) {
                        widget.setAnswerState(QuestionPlayer.AnswerState.CORRECT);
                        widget.setBottomTextAlpha(0.0f);
                        QuestionImage tickWidget = widget.getTickWidget();
                        if (tickWidget == null) return;
                        tickWidget.setAlpha(1.0f);
                    } else {
                        widget.setAnswerState(QuestionPlayer.AnswerState.ANSWERED);
                    }
                });
                this.resetAnsweredPlayers();
            }
            case REVEAL_CORRECT_POINTS -> {
                this.resetMainHUD();
                this.resetRevealedAnswer();
                this.playerWidgets.forEach(widget -> {
                    if (this.isPlayerCorrect(widget)) {
                        widget.setAnswerState(QuestionPlayer.AnswerState.CORRECT);
                        widget.setBottomText("$" + QuestionScreenHandler.CORRECT_ANSWER_POINTS);
                    } else {
                        widget.setAnswerState(QuestionPlayer.AnswerState.ANSWERED);
                    }
                });
                this.resetAnsweredPlayers();
            }
            case REVEAL_INCORRECT -> {
                this.resetMainHUD();
                this.resetRevealedAnswer();
                this.playerWidgets.forEach(widget -> {
                    if (this.isPlayerCorrect(widget)) {
                        widget.setY(widget.getOriginalY() - widget.getHeight() * 2);
                    } else {
                        widget.setAnswerState(QuestionPlayer.AnswerState.ANSWERED);
                    }
                });
            }
            case REVEAL_INCORRECT_CROSSES, INCORRECT_QUIP -> {
                this.resetMainHUD();
                this.resetRevealedAnswer();
                this.playerWidgets.forEach(widget -> {
                    if (this.isPlayerCorrect(widget)) {
                        widget.setY(widget.getOriginalY() - widget.getHeight());
                    } else {
                        widget.setAnswerState(QuestionPlayer.AnswerState.INCORRECT);
                        QuestionImage crossWidget = widget.getCrossWidget();
                        if (crossWidget == null) return;
                        crossWidget.setAlpha(1.0f);
                    }
                });
            }
            case KILLING_ROOM_TRANSITION_LIGHTS -> {}
            case KILLING_ROOM_TRANSITION_MOVE, KILLING_ROOM_TRANSITION_HOLD -> this.resetBoxWidgets();
            case ALL_CORRECT_LOOP_BACK, ALL_CORRECT_LOOP_BACK_HOLD -> {
                this.resetMainHUD();
                this.playerWidgets.forEach(widget -> {
                    if (this.isPlayerCorrect(widget)) {
                        widget.setAnswerState(QuestionPlayer.AnswerState.CORRECT);
                        widget.setBottomText("$" + QuestionScreenHandler.CORRECT_ANSWER_POINTS);
                    } else {
                        widget.setAnswerState(QuestionPlayer.AnswerState.ANSWERED);
                    }
                });
            }
        }
    }

    private void nextState() {
        if (!this.firstState) this.state = this.state.next(this);
        if (this.state == State.CLOSING_SCREEN) {
            this.close();
            return;
        }
        this.firstState = false;
        float lastEndTime = this.stateEndTime;

        this.stateEndTime = this.uptimeSecs + switch (this.state) {
            case TITLE_IN, TITLE_OUT -> TITLE_FADE_TIME;
            case TITLE_HOLD -> TITLE_HOLD_TIME;
            case WELCOME_IN, WELCOME_OUT -> WELCOME_FADE_TIME;
            case WELCOME_QUIP -> this.getQuip(QuipType.WELCOME);
            case SCREEN_IN -> SCREEN_IN_TIME;
            case HUD_IN -> HUD_IN_TIME;
            case QUESTION_NUMBER_IN, QUESTION_NUMBER_OUT -> QUESTION_NUMBER_FADE_TIME;
            case QUESTION_NUMBER_HOLD -> QUESTION_NUMBER_HOLD_TIME;
            case QUESTION_IN, QUESTION_OUT -> QUESTION_ZOOM_TIME;
            case QUESTION_HOLD -> this.question.holdTime();
            case TIMER_IN, TIMER_OUT -> TIMER_MOVE_TIME;
            case ANSWERING -> this.answeringTimeSeconds;
            case ANSWER_PRE_QUIP -> this.getQuip(QuipType.PRE_ANSWER);
            case ANSWER_IN -> ANSWER_ZOOM_TIME;
            case ANSWER_HOLD -> ANSWER_HOLD_TIME;
            case ANSWER_POST_QUIP -> this.getQuip(QuipType.POST_ANSWER);
            case REVEAL_CORRECT -> CORRECT_REVEAL_TIME;
            case REVEAL_CORRECT_POINTS -> CORRECT_POINTS_TIME;
            case REVEAL_INCORRECT -> INCORRECT_REVEAL_TIME;
            case REVEAL_INCORRECT_CROSSES -> INCORRECT_CROSSES_TIME;
            case INCORRECT_QUIP -> this.getQuip(QuipType.INCORRECT);
            case KILLING_ROOM_TRANSITION_MOVE -> KILLING_ROOM_TRANSITION_MOVE_TIME;
            case KILLING_ROOM_TRANSITION_LIGHTS -> KILLING_ROOM_TRANSITION_LIGHTS_TIME;
            case KILLING_ROOM_TRANSITION_HOLD -> KILLING_ROOM_TRANSITION_HOLD_TIME;
            case ALL_CORRECT_LOOP_BACK -> ALL_CORRECT_LOOP_BACK_TIME;
            case ALL_CORRECT_LOOP_BACK_HOLD -> ALL_CORRECT_LOOP_BACK_HOLD_TIME;
            default -> throw new IllegalStateException("Unexpected value: " + this.state);
        };

        this.stateStartTime = lastEndTime;
        this.firstStateTick = true;
    }

    public enum State {
        TITLE_IN, // Entrypoint for a new quiz
        TITLE_HOLD,
        TITLE_OUT,
        WELCOME_IN,
        WELCOME_QUIP,
        WELCOME_OUT,
        SCREEN_IN, // Entrypoint for returning from a killing room
        HUD_IN,
        QUESTION_NUMBER_IN, // Entrypoint for a new question after all players answered correctly
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
        REVEAL_CORRECT_POINTS,
        REVEAL_INCORRECT,
        REVEAL_INCORRECT_CROSSES,
        INCORRECT_QUIP,
        KILLING_ROOM_TRANSITION_MOVE,
        KILLING_ROOM_TRANSITION_LIGHTS,
        KILLING_ROOM_TRANSITION_HOLD,
        ALL_CORRECT_LOOP_BACK,
        ALL_CORRECT_LOOP_BACK_HOLD,
        CLOSING_SCREEN;

        public State next(QuestionScreenOld screen) {
            return switch (this) {
                case ANSWER_POST_QUIP -> screen.allPlayersIncorrect() ? REVEAL_INCORRECT : REVEAL_CORRECT;
                case REVEAL_CORRECT_POINTS -> screen.allPlayersCorrect() ? ALL_CORRECT_LOOP_BACK : REVEAL_INCORRECT;
                case INCORRECT_QUIP -> Random.create().nextBoolean() ? KILLING_ROOM_TRANSITION_MOVE : KILLING_ROOM_TRANSITION_LIGHTS;
                case KILLING_ROOM_TRANSITION_MOVE -> KILLING_ROOM_TRANSITION_HOLD;
                case KILLING_ROOM_TRANSITION_HOLD, ALL_CORRECT_LOOP_BACK_HOLD, CLOSING_SCREEN -> CLOSING_SCREEN;
                default -> values()[this.ordinal() + 1];
            };
        }
    }

    public static void clientInit() {
        ModNetworking.clientReceive(TriviaMurderParty.NetworkIDs.QUESTION_SCREEN, clientReceiveInfo -> {
            PacketByteBuf buffer = clientReceiveInfo.buffer();
            int id = buffer.readInt();
            int questionNumber = buffer.readInt();
            int answeringSeconds = buffer.readInt();
            State state = buffer.readEnumConstant(State.class);

            if (MCTournament.client().currentScreen instanceof QuestionScreenOld questionScreen) questionScreen.close();
            Question question = Questions.getQuestionByID(id);
            MCTournament.client().setScreen(new QuestionScreenOld(question, questionNumber, answeringSeconds, state));
        });

        ModNetworking.clientReceive(TriviaMurderParty.NetworkIDs.QUESTION_ANSWERED, clientReceiveInfo -> {
            PacketByteBuf buffer = clientReceiveInfo.buffer();
            String playerName = buffer.readString();
            boolean isCaptain = buffer.readBoolean();
            int answerPosition = buffer.readInt();

            ClientPlayerEntity clientPlayer = MCTournament.client().player;
            PlayerEntity answeredPlayer = ModUtilClient.getPlayer(playerName);
            if (clientPlayer == null) return;

            if (MCTournament.client().currentScreen instanceof QuestionScreenOld questionScreen) {
//                See this: https://stackoverflow.com/questions/27482579/how-is-this-private-variable-accessible
//                Java be wildin'
                for (final var widget : questionScreen.playerWidgets) {
                    if (widget.getPlayer().getNameForScoreboard().equals(playerName)) {
                        questionScreen.captainAnswers.put(playerName, answerPosition);
                        break;
                    }
                }

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
                    questionScreen.answeredCount++;
                    questionScreen.answeredCountWidgets.get(1).setInt(questionScreen.answeredCount);

                } else if (clientPlayer.isTeammate(answeredPlayer)) {
                    for (final var answerWidget : questionScreen.answerWidgets) {
                        answerWidget.removePlayerHead(answeredPlayer);
                    }
                    questionScreen.answerWidgets.get(answerPosition).setPlayerHead(answeredPlayer);
                }
            }
        });

        ModNetworking.clientReceive(TriviaMurderParty.NetworkIDs.QUESTION_ANSWERING_END, clientReceiveInfo -> {
            if (MCTournament.client().currentScreen instanceof QuestionScreenOld questionScreen && questionScreen.state == State.ANSWERING) {
                questionScreen.forceStateEnd();
            }
        });
    }

    @Override
    public void close() {
        if (this.state == State.CLOSING_SCREEN && this.allPlayersCorrect()) {
            ModNetworking.sendToServer(TriviaMurderParty.NetworkIDs.QUESTION_ALL_CORRECT_LOOP_BACK);
        }
        MCTournament.client().setScreen(this.parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @SuppressWarnings("unused")
    float getQuip(QuipType quipType) {
        return 1.0f;
    }

    public void lockButtons() {
        this.answerWidgets.forEach(QuestionButton::lock);
    }

    private void forceStateEnd() {
        this.stateEndTime = this.uptimeSecs;
    }

    private void resetBoxWidgets() {
        this.leftBoxWidget.setPosition(0,0);
        this.rightBoxWidget.setPosition(this.width / 2, 0);
        this.leftBoxWidget.setAlpha(1.0f);
        this.rightBoxWidget.setAlpha(1.0f);
    }

    private void resetMainHUD() {
        this.setListAlpha(this.playerWidgets, 1.0f);
        this.setListAlpha(this.answeredCountWidgets, 1.0f);
        this.setListAlpha(this.roomCodeWidgets, 1.0f);
        this.questionWidget.setAlpha(1.0f);
    }

    private void resetRevealedAnswer() {
        PlayerEntity player = MCTournament.client().player;
        if (player == null) return;
        PlayerEntity captain = Tournament.inst().clientScoreboard().getTeamCaptain(player);
        int selectedAnswer = captain == null ? -1 : this.captainAnswers.getOrDefault(captain.getNameForScoreboard(), -1);

        for (final var widget : this.answerWidgets) {
            if (widget.isCorrect()) {
                widget.setAlpha(1.0f);
                widget.setWidth(widget.getOriginalWidth() * ANSWER_SIZE_MULTIPLIER);
                widget.setHeight(widget.getOriginalHeight() * ANSWER_SIZE_MULTIPLIER);
                widget.setX(this.getFinalAnswerX(widget));
                widget.setY(this.getFinalAnswerY(widget));
                if (widget.getAnswerPosition() == selectedAnswer) widget.setSelectedAnswer();
                break;
            }
        }
        this.lockButtons();
    }

    private void resetAnsweredPlayers() {
        this.playerWidgets.forEach(widget -> {
            if (widget.getAnswerState().equals(QuestionPlayer.AnswerState.UNANSWERED)) {
                widget.setAnswerState(QuestionPlayer.AnswerState.ANSWERED);
            }
        });
    }

    private int getFinalAnswerX(QuestionButton widget) {
        return (this.width / 2) - (widget.getWidth() / 2);
    }

    private int getFinalAnswerY(QuestionButton widget) {
        return this.height - widget.getHeight() - 10;
    }

    private boolean allPlayersIncorrect() {
        for (final var widget : this.playerWidgets) {
            if (this.isPlayerCorrect(widget)) return false;
        }
        return true;
    }

    private boolean allPlayersCorrect() {
        for (final var widget : this.playerWidgets) {
            if (!this.isPlayerCorrect(widget)) return false;
        }
        return true;
    }

    private boolean isPlayerCorrect(QuestionPlayer playerWidget) {
        return this.question.isCorrect(this.captainAnswers.getOrDefault(playerWidget.getPlayerName(), -1));
    }

    public enum QuipType {
        WELCOME,
        PRE_ANSWER,
        POST_ANSWER,
        INCORRECT
    }
}
