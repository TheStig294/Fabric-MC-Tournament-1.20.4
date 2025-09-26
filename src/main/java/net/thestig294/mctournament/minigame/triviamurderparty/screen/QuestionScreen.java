package net.thestig294.mctournament.minigame.triviamurderparty.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.random.Random;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.minigame.triviamurderparty.question.Question;
import net.thestig294.mctournament.minigame.triviamurderparty.question.Questions;
import net.thestig294.mctournament.network.ModNetworking;
import net.thestig294.mctournament.screen.AnimatedScreen;
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
public class QuestionScreen extends AnimatedScreen<QuestionScreen, QuestionScreen.State> {
    private static final int TITLE_SHAKE_PIXEL_AMPLITUDE = 5;
    private static final int TITLE_SHAKE_FREQUENCY_PERCENT = 5;
    private static final float TIMER_MOVE_TIME = 1.0f;
    private static final int ANSWER_SIZE_MULTIPLIER = 2;

    private final Question question;
    private final int questionNumber;
    private final int answeringTimeSeconds;
    private final Map<String, Integer> captainAnswers;
    private int answeredCount;
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

    public QuestionScreen(Question question, int questionNumber, int answeringTimeSeconds, State startingState) {
        super(startingState);
        this.question = question;
        this.questionNumber = questionNumber;
        this.answeringTimeSeconds = answeringTimeSeconds;
        this.answeredCount = 0;
        this.captainAnswers = new HashMap<>();

        this.titleWidgets = new ArrayList<>();
        this.playerWidgets = new ArrayList<>();
        this.answerWidgets = new ArrayList<>();
        this.answeredCountWidgets = new ArrayList<>();
        this.roomCodeWidgets = new ArrayList<>();
    }

    @Override
    protected void createWidgets() {
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
        List<PlayerEntity> teamCaptains = Tournament.inst().clientScoreboard().getValidTeamCaptains(true);
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
    }

    @SuppressWarnings("unused")
    public enum State implements AnimatedScreen.State<QuestionScreen> {
        TITLE_IN { // Entrypoint for a new quiz
            public void begin(QuestionScreen screen) {screen.resetBoxWidgets();}
            public void render(QuestionScreen screen) {screen.listAnimateAlpha(screen.titleWidgets, 0.0f, 1.0f);}
            public void refresh(QuestionScreen screen) {
                screen.resetBoxWidgets();
                screen.setListAlpha(screen.titleWidgets, 1.0f);
            }
            public float duration(QuestionScreen screen) {return 0.5f;}
        },
        TITLE_HOLD {
            public void render(QuestionScreen screen) {
                screen.everyStatePercent(TITLE_SHAKE_FREQUENCY_PERCENT, () -> screen.titleWidgets.forEach(widget -> {
                    widget.setX(widget.getOriginalX() + Random.create().nextBetween(-TITLE_SHAKE_PIXEL_AMPLITUDE, TITLE_SHAKE_PIXEL_AMPLITUDE));
                    widget.setY(widget.getOriginalY() + Random.create().nextBetween(-TITLE_SHAKE_PIXEL_AMPLITUDE, TITLE_SHAKE_PIXEL_AMPLITUDE));
                }));
            }
            public void refresh(QuestionScreen screen) {TITLE_IN.refresh(screen);}
            public float duration(QuestionScreen screen) {return 4.0f;}
        },
        TITLE_OUT {
            public void begin(QuestionScreen screen) {
                screen.titleWidgets.forEach(widget -> {
                    widget.setX(widget.getOriginalX());
                    widget.setY(widget.getOriginalY());
                });
            }
            public void render(QuestionScreen screen) {screen.listAnimateAlpha(screen.titleWidgets, 1.0f, 0.0f);}
            public void refresh(QuestionScreen screen) {screen.resetBoxWidgets();}
            public float duration(QuestionScreen screen) {return 0.5f;}
        },
        WELCOME_IN {
            public void begin(QuestionScreen screen) {screen.questionNumberWidget.setText("screen.mctournament.question_welcome");}
            public void render(QuestionScreen screen) {screen.animate(screen.questionNumberWidget::setAlpha, 0.0f, 1.0f);}
            public void refresh(QuestionScreen screen) {
                screen.questionNumberWidget.setAlpha(1.0f);
                screen.questionNumberWidget.setText("screen.mctournament.question_welcome");
            }
            public float duration(QuestionScreen screen) {return 1.0f;}
        },
        WELCOME_QUIP {
            public void render(QuestionScreen screen) {}
            public void refresh(QuestionScreen screen) {WELCOME_IN.refresh(screen);}
            public float duration(QuestionScreen screen) {return screen.getQuip(QuipType.WELCOME);}
        },
        WELCOME_OUT {
            public void render(QuestionScreen screen) {screen.animate(screen.questionNumberWidget::setAlpha, 1.0f, 0.0f);}
            public void refresh(QuestionScreen screen) {screen.questionNumberWidget.setText("screen.mctournament.question_welcome");}
            public float duration(QuestionScreen screen) {return 1.0f;}
        },
        SCREEN_IN { // Entrypoint for returning from a killing room
            public void begin(QuestionScreen screen) {screen.resetBoxWidgets();}
            public void render(QuestionScreen screen) {
                screen.animate(screen.leftBoxWidget::setX, 0, screen.leftBoxWidget.getOriginalX());
                screen.animate(screen.rightBoxWidget::setX, screen.width / 2, screen.rightBoxWidget.getOriginalX());
            }
            public void refresh(QuestionScreen screen) {}
            public float duration(QuestionScreen screen) {return 1.0f;}
        },
        HUD_IN {
            public void render(QuestionScreen screen) {
                screen.playerWidgets.forEach(widget -> {
                    screen.animate(widget::setY, -widget.getOriginalY() - widget.getHeight(), widget.getOriginalY());
                    screen.animate(widget::setAlpha, 0.0f, 1.0f);
                });
                screen.answeredCountWidgets.forEach(widget -> {
                    screen.animate(widget::setY, screen.height, widget.getOriginalY());
                    screen.animate(widget::setAlpha, 0.0f, 1.0f);
                });
                screen.roomCodeWidgets.forEach(widget -> {
                    screen.animate(widget::setY, screen.height, widget.getOriginalY());
                    screen.animate(widget::setAlpha, 0.0f, 1.0f);
                });
            }
            public void refresh(QuestionScreen screen) {
                screen.resetMainHUD();
                screen.questionWidget.setAlpha(0.0f);
            }
            public float duration(QuestionScreen screen) {return 1.0f;}
        },
        QUESTION_NUMBER_IN { // Entrypoint for a new question after all players answered correctly
            public void begin(QuestionScreen screen) {screen.questionNumberWidget.setInt(screen.questionNumber);}
            public void render(QuestionScreen screen) {screen.animate(screen.questionNumberWidget::setAlpha, 0.0f, 1.0f);}
            public void refresh(QuestionScreen screen) {HUD_IN.refresh(screen);}
            public float duration(QuestionScreen screen) {return 1.0f;}
        },
        QUESTION_NUMBER_HOLD {
            public void render(QuestionScreen screen) {}
            public void refresh(QuestionScreen screen) {HUD_IN.refresh(screen);}
            public float duration(QuestionScreen screen) {return 1.0f;}
        },
        QUESTION_NUMBER_OUT {
            public void render(QuestionScreen screen) {screen.animate(screen.questionNumberWidget::setAlpha, 1.0f, 0.0f);}
            public void refresh(QuestionScreen screen) {HUD_IN.refresh(screen);}
            public float duration(QuestionScreen screen) {return 1.0f;}
        },
        QUESTION_IN {
            public void begin(QuestionScreen screen) {screen.questionWidget.setPosition(screen.width / 2, screen.height);}
            public void render(QuestionScreen screen) {
                screen.animate(screen.questionWidget::setY, screen.height, screen.questionWidget.getOriginalY());
                screen.animate(screen.questionWidget::setAlpha, 0.0f, 1.0f);
            }
            public void refresh(QuestionScreen screen) {screen.resetMainHUD();}
            public float duration(QuestionScreen screen) {return 1.0f;}
        },
        QUESTION_HOLD {
            public void render(QuestionScreen screen) {}
            public void refresh(QuestionScreen screen) {QUESTION_IN.refresh(screen);}
            public float duration(QuestionScreen screen) {return screen.question.holdTime();}
        },
        QUESTION_OUT {
            public void render(QuestionScreen screen) {screen.animate(screen.questionWidget::setX, screen.width / 2, screen.questionWidget.getOriginalX());}
            public void refresh(QuestionScreen screen) {QUESTION_IN.refresh(screen);}
            public float duration(QuestionScreen screen) {return 1.0f;}
        },
        TIMER_IN {
            public void render(QuestionScreen screen) {
                screen.answerWidgets.forEach(widget -> {
                    screen.animate(widget::setX, screen.width + widget.getWidth(), widget.getOriginalX());
                    screen.animate(widget::setAlpha, 0.0f, 1.0f);
                });
                screen.animate(screen.timerWidget::setY, screen.height, screen.timerWidget.getOriginalY());
                screen.animate(screen.timerWidget::setAlpha, 0.0f, 1.0f);
                screen.timerWidget.reset();
            }
            public void refresh(QuestionScreen screen) {
                screen.resetMainHUD();
                screen.setListAlpha(screen.answerWidgets, 1.0f);
                screen.timerWidget.setAlpha(1.0f);
                screen.timerWidget.reset(0.0f, screen.timerTicksLeft);
            }
            public float duration(QuestionScreen screen) {return TIMER_MOVE_TIME;}
        },
        ANSWERING {
            public void begin(QuestionScreen screen) {ModNetworking.sendToServer(TriviaMurderParty.NetworkIDs.QUESTION_ANSWERING_BEGIN);}
            public void render(QuestionScreen screen) {screen.timerTicksLeft = screen.timerWidget.getTicksLeft();}
            public void refresh(QuestionScreen screen) {TIMER_IN.refresh(screen);}
            public float duration(QuestionScreen screen) {return screen.answeringTimeSeconds;}
        },
        TIMER_OUT {
            public void begin(QuestionScreen screen) {
                screen.lockButtons();
                screen.resetAnsweredPlayers();
                ModUtilClient.playSound(SoundEvents.BLOCK_BELL_USE);
            }
            public void render(QuestionScreen screen) {
                screen.animate(screen.timerWidget::setY, screen.timerWidget.getOriginalY(), screen.height);
                screen.animate(screen.timerWidget::setAlpha, 1.0f, 0.0f);
                screen.timerWidget.reset(0.0f, 0);
            }
            public void refresh(QuestionScreen screen) {
                screen.resetMainHUD();
                screen.setListAlpha(screen.answerWidgets, 1.0f);
                screen.lockButtons();
                screen.resetAnsweredPlayers();
            }
            public float duration(QuestionScreen screen) {return TIMER_MOVE_TIME;}
        },
        ANSWER_PRE_QUIP {
            public void render(QuestionScreen screen) {}
            public void refresh(QuestionScreen screen) {TIMER_OUT.refresh(screen);}
            public float duration(QuestionScreen screen) {return screen.getQuip(QuipType.PRE_ANSWER);}
        },
        ANSWER_IN {
            public void render(QuestionScreen screen) {
                screen.answerWidgets.forEach(widget -> {
                    if (widget.isCorrect()) {
                        screen.animate(widget::setX, widget.getOriginalX(), screen.getFinalAnswerX(widget));
                        screen.animate(widget::setY, widget.getOriginalY(), screen.getFinalAnswerY(widget));
                        screen.animate(widget::setWidth, widget.getOriginalWidth(), widget.getOriginalWidth() * ANSWER_SIZE_MULTIPLIER);
                        screen.animate(widget::setHeight, widget.getOriginalHeight(), widget.getOriginalHeight() * ANSWER_SIZE_MULTIPLIER);
                    } else {
                        screen.animate(widget::setAlpha, 1.0f, 0.0f);
                    }
                });
            }
            public void refresh(QuestionScreen screen) {
                screen.resetMainHUD();
                screen.resetRevealedAnswer();
                screen.resetAnsweredPlayers();
            }
            public float duration(QuestionScreen screen) {return 0.2f;}
        },
        ANSWER_HOLD {
            public void render(QuestionScreen screen) {}
            public void refresh(QuestionScreen screen) {ANSWER_IN.refresh(screen);}
            public float duration(QuestionScreen screen) {return 3.0f;}
        },
        ANSWER_POST_QUIP {
            public void render(QuestionScreen screen) {}
            public void refresh(QuestionScreen screen) {ANSWER_IN.refresh(screen);}
            public float duration(QuestionScreen screen) {return screen.getQuip(QuipType.POST_ANSWER);}
            public State next(QuestionScreen screen) {return screen.allPlayersIncorrect() ? REVEAL_INCORRECT : REVEAL_CORRECT;}
        },
        REVEAL_CORRECT {
            public void begin(QuestionScreen screen) {
                screen.playerWidgets.forEach(widget -> {
                    if (!screen.isPlayerCorrect(widget)) return;
                    widget.setAnswerState(QuestionPlayer.AnswerState.CORRECT);
                    widget.setBottomText("$" + QuestionScreenHandler.CORRECT_ANSWER_POINTS);
                    widget.setBottomTextAlpha(0.0f);
                });
            }
            public void render(QuestionScreen screen) {
                screen.playerWidgets.forEach(widget -> {
                    if (!screen.isPlayerCorrect(widget)) return;
                    screen.animate(widget::setBottomTextAlpha, 0.0f, 1.0f);
                    QuestionImage tick = widget.getTickWidget();
                    if (tick == null) return;
                    screen.animate(tick::setX, tick.getOriginalX() - (tick.getOriginalWidth() / 2), tick.getOriginalX());
                    screen.animate(tick::setY, tick.getOriginalY() - (tick.getOriginalHeight() / 2), tick.getOriginalY());
                    screen.animate(tick::setWidth, tick.getOriginalWidth() * 2, tick.getOriginalWidth());
                    screen.animate(tick::setHeight, tick.getOriginalHeight() * 2, tick.getOriginalHeight());
                    screen.animate(tick::setAlpha, 0.0f, 1.0f);
                });
            }
            public void refresh(QuestionScreen screen) {
                screen.resetMainHUD();
                screen.resetRevealedAnswer();
                screen.playerWidgets.forEach(widget -> {
                    if (screen.isPlayerCorrect(widget)) {
                        widget.setAnswerState(QuestionPlayer.AnswerState.CORRECT);
                        widget.setBottomTextAlpha(0.0f);
                        QuestionImage tickWidget = widget.getTickWidget();
                        if (tickWidget == null) return;
                        tickWidget.setAlpha(1.0f);
                    } else {
                        widget.setAnswerState(QuestionPlayer.AnswerState.ANSWERED);
                    }
                });
                screen.resetAnsweredPlayers();
            }
            public float duration(QuestionScreen screen) {return 0.5f;}
        },
        REVEAL_CORRECT_POINTS {
            public void render(QuestionScreen screen) {
                screen.playerWidgets.forEach(widget -> {
                    if (!screen.isPlayerCorrect(widget)) return;
                    QuestionImage tick = widget.getTickWidget();
                    if (tick == null) return;
                    screen.animate(tick::setAlpha, 1.0f, 0.0f);
                });
            }
            public void refresh(QuestionScreen screen) {
                screen.resetMainHUD();
                screen.resetRevealedAnswer();
                screen.playerWidgets.forEach(widget -> {
                    if (screen.isPlayerCorrect(widget)) {
                        widget.setAnswerState(QuestionPlayer.AnswerState.CORRECT);
                        widget.setBottomText("$" + QuestionScreenHandler.CORRECT_ANSWER_POINTS);
                    } else {
                        widget.setAnswerState(QuestionPlayer.AnswerState.ANSWERED);
                    }
                });
                screen.resetAnsweredPlayers();
            }
            public float duration(QuestionScreen screen) {return 1.0f;}
            public State next(QuestionScreen screen) {return screen.allPlayersCorrect() ? ALL_CORRECT_LOOP_BACK : REVEAL_INCORRECT;}
        },
        REVEAL_INCORRECT {
            public void render(QuestionScreen screen) {
                screen.playerWidgets.forEach(widget -> {
                    if (!screen.isPlayerCorrect(widget)) return;
                    screen.animate(widget::setY, widget.getOriginalY(), -widget.getHeight() * 2);
                    screen.animate(widget::setBottomTextAlpha, 1.0f, 0.0f);
                });
            }
            public void refresh(QuestionScreen screen) {
                screen.resetMainHUD();
                screen.resetRevealedAnswer();
                screen.playerWidgets.forEach(widget -> {
                    if (screen.isPlayerCorrect(widget)) {
                        widget.setY(widget.getOriginalY() - widget.getHeight() * 2);
                    } else {
                        widget.setAnswerState(QuestionPlayer.AnswerState.ANSWERED);
                    }
                });
            }
            public float duration(QuestionScreen screen) {return 0.5f;}
        },
        REVEAL_INCORRECT_CROSSES {
            public void begin(QuestionScreen screen) {screen.playerWidgets.forEach(widget -> widget.setAnswerState(QuestionPlayer.AnswerState.INCORRECT));}
            public void render(QuestionScreen screen) {
                screen.playerWidgets.forEach(widget -> {
                    if (screen.isPlayerCorrect(widget)) return;
                    QuestionImage cross = widget.getCrossWidget();
                    if (cross == null) return;
                    screen.animate(cross::setX, cross.getOriginalX() - (cross.getOriginalWidth() / 2), cross.getOriginalX());
                    screen.animate(cross::setY, cross.getOriginalY() - (cross.getOriginalHeight() / 2), cross.getOriginalY());
                    screen.animate(cross::setWidth, cross.getOriginalWidth() * 2, cross.getOriginalWidth());
                    screen.animate(cross::setHeight, cross.getOriginalHeight() * 2, cross.getOriginalHeight());
                    screen.animate(cross::setAlpha, 0.0f, 1.0f);
                });
            }
            public void refresh(QuestionScreen screen) {
                screen.resetMainHUD();
                screen.resetRevealedAnswer();
                screen.playerWidgets.forEach(widget -> {
                    if (screen.isPlayerCorrect(widget)) {
                        widget.setY(widget.getOriginalY() - widget.getHeight());
                    } else {
                        widget.setAnswerState(QuestionPlayer.AnswerState.INCORRECT);
                        QuestionImage crossWidget = widget.getCrossWidget();
                        if (crossWidget == null) return;
                        crossWidget.setAlpha(1.0f);
                    }
                });
            }
            public float duration(QuestionScreen screen) {return 0.5f;}
        },
        INCORRECT_QUIP {
            public void render(QuestionScreen screen) {}
            public void refresh(QuestionScreen screen) {REVEAL_INCORRECT_CROSSES.refresh(screen);}
            public float duration(QuestionScreen screen) {return screen.getQuip(QuipType.INCORRECT);}
            public State next(QuestionScreen screen) {return Random.create().nextBoolean() ? KILLING_ROOM_TRANSITION_MOVE : KILLING_ROOM_TRANSITION_LIGHTS;}
        },
        KILLING_ROOM_TRANSITION_MOVE {
            public void begin(QuestionScreen screen) {ModNetworking.sendToServer(TriviaMurderParty.NetworkIDs.QUESTION_MOVE_PLAYER);}
            public void render(QuestionScreen screen) {
                screen.children().forEach(child -> {
                    if (child instanceof QuestionBox widget) {
                        screen.animate(widget::setAlpha, 0.0f, 1.0f);
                        screen.animate(widget::setX, screen.width / 2, 0);
                        screen.animate(widget::setY, screen.height / 2, 0);
                        screen.animate(widget::setWidth, 0, screen.width);
                        screen.animate(widget::setHeight, 0, screen.height);
                    } else if ((child instanceof QuestionWidget widget) && widget.getAlpha() > 0.0f) {
                        screen.animate(widget::setAlpha, 1.0f, 0.0f);
                    }
                });
            }
            public void refresh(QuestionScreen screen) {screen.resetBoxWidgets();}
            public float duration(QuestionScreen screen) {return 1.0f;}
            public State next(QuestionScreen screen) {return KILLING_ROOM_TRANSITION_HOLD;}
        },
        KILLING_ROOM_TRANSITION_LIGHTS {
            public void begin(QuestionScreen screen) {ModNetworking.sendToServer(TriviaMurderParty.NetworkIDs.QUESTION_TRIGGER_LIGHTS_OFF);}
            public void render(QuestionScreen screen) {
                screen.children().forEach(child -> {
                    if ((child instanceof QuestionWidget widget) && widget.getAlpha() > 0.0f) {
                        screen.animate(widget::setAlpha, 1.0f, 0.0f);
                    }
                });
            }
            public void refresh(QuestionScreen screen) {}
            public float duration(QuestionScreen screen) {return 1.0f;}
        },
        KILLING_ROOM_TRANSITION_HOLD {
            public void render(QuestionScreen screen) {}
            public void end(QuestionScreen screen) {ModNetworking.sendToServer(TriviaMurderParty.NetworkIDs.QUESTION_SCREEN_START_KILLING_ROOM);}
            public void refresh(QuestionScreen screen) {KILLING_ROOM_TRANSITION_MOVE.refresh(screen);}
            public float duration(QuestionScreen screen) {return 3.0f;}
            public State next(QuestionScreen screen) {return null;}
        },
        ALL_CORRECT_LOOP_BACK {
            public void render(QuestionScreen screen) {
                screen.animate(screen.questionWidget::setX, screen.questionWidget.getOriginalX(), -screen.questionWidget.getWidth());
                for (final var widget : screen.answerWidgets) {
                    if (widget.isCorrect()) {
                        screen.animate(widget::setX, screen.getFinalAnswerX(widget), screen.width + (widget.getWidth() / 4));
                        break;
                    }
                }
            }
            public void refresh(QuestionScreen screen) {
                screen.resetMainHUD();
                screen.playerWidgets.forEach(widget -> {
                    if (screen.isPlayerCorrect(widget)) {
                        widget.setAnswerState(QuestionPlayer.AnswerState.CORRECT);
                        widget.setBottomText("$" + QuestionScreenHandler.CORRECT_ANSWER_POINTS);
                    } else {
                        widget.setAnswerState(QuestionPlayer.AnswerState.ANSWERED);
                    }
                });
            }
            public float duration(QuestionScreen screen) {return 1.0f;}
        },
        ALL_CORRECT_LOOP_BACK_HOLD {
            public void begin(QuestionScreen screen) {ModNetworking.sendToServer(TriviaMurderParty.NetworkIDs.QUESTION_MOVE_PLAYER);}
            public void render(QuestionScreen screen) {}
            public void end(QuestionScreen screen) {ModNetworking.sendToServer(TriviaMurderParty.NetworkIDs.QUESTION_ALL_CORRECT_LOOP_BACK);}
            public void refresh(QuestionScreen screen) {ALL_CORRECT_LOOP_BACK.refresh(screen);}
            public float duration(QuestionScreen screen) {return 1.0f;}
        }
    }

    public static void clientInit() {
        ModNetworking.clientReceive(TriviaMurderParty.NetworkIDs.QUESTION_SCREEN, clientReceiveInfo -> {
            PacketByteBuf buffer = clientReceiveInfo.buffer();
            int id = buffer.readInt();
            int questionNumber = buffer.readInt();
            int answeringSeconds = buffer.readInt();

            QuestionScreenHandler.Entrypoint entrypoint = buffer.readEnumConstant(QuestionScreenHandler.Entrypoint.class);
            State state = switch (entrypoint) {
                case TITLE_IN -> State.TITLE_IN;
                case SCREEN_IN -> State.SCREEN_IN;
                case QUESTION_NUMBER_IN -> State.QUESTION_NUMBER_IN;
            };

            Question question = Questions.getQuestionByID(id);
            MCTournament.client().setScreen(new QuestionScreen(question, questionNumber, answeringSeconds, state));
        });

        ModNetworking.clientReceive(TriviaMurderParty.NetworkIDs.QUESTION_ANSWERED, clientReceiveInfo -> {
            PacketByteBuf buffer = clientReceiveInfo.buffer();
            String playerName = buffer.readString();
            boolean isCaptain = buffer.readBoolean();
            int answerPosition = buffer.readInt();

            ClientPlayerEntity clientPlayer = MCTournament.client().player;
            PlayerEntity answeredPlayer = ModUtilClient.getPlayer(playerName);
            if (clientPlayer == null || answeredPlayer == null) return;

            if (MCTournament.client().currentScreen instanceof QuestionScreen questionScreen) {
//                See this: https://stackoverflow.com/questions/27482579/how-is-this-private-variable-accessible
//                Java be wildin'
                for (final var widget : questionScreen.playerWidgets) {
                    if (widget.getPlayerName().equals(playerName)) {
                        questionScreen.captainAnswers.put(playerName, answerPosition);
                        break;
                    }
                }

                if (isCaptain) {
                    for (final var playerWidget : questionScreen.playerWidgets) {
                        if (playerWidget.getPlayerName().equals(playerName)) {
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
            if (MCTournament.client().currentScreen instanceof QuestionScreen questionScreen && questionScreen.isState(State.ANSWERING)) {
                questionScreen.forceStateEnd();
            }
        });
    }

    @SuppressWarnings("unused")
    private float getQuip(QuipType quipType) {
        return 1.0f;
    }

    public void lockButtons() {
        this.answerWidgets.forEach(QuestionButton::lock);
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
        PlayerEntity captain = Tournament.inst().clientScoreboard().getTeamCaptain(true, player);
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
