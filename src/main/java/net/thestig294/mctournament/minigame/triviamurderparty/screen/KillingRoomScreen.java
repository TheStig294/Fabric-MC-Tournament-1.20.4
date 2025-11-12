package net.thestig294.mctournament.minigame.triviamurderparty.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.minigame.triviamurderparty.killingroom.KillingRoom;
import net.thestig294.mctournament.minigame.triviamurderparty.killingroom.KillingRooms;
import net.thestig294.mctournament.minigame.triviamurderparty.widget.QuestionText;
import net.thestig294.mctournament.minigame.triviamurderparty.widget.QuestionTimer;
import net.thestig294.mctournament.network.ModNetworking;
import net.thestig294.mctournament.screen.AnimatedScreen;
import net.thestig294.mctournament.util.ModColors;

import java.util.List;

@Environment(EnvType.CLIENT)
public class KillingRoomScreen extends AnimatedScreen<KillingRoomScreen, KillingRoomScreen.State> {
    private static final float TIMER_MOVE_TIME = 1.0f;

    private final String id;
    private final KillingRoom room;
    private final int timerIndex;
    private int descriptionIndex;
    private final List<Float> descriptionLengths;
    private final Text title;
    private final Text description;
    private final Text timerDescription;
    private final int timerLength;
    private final boolean onTrial;
    private final Text onTrialText;

    private QuestionText titleWidget;
    private QuestionText descriptionWidget;
    private QuestionTimer timerWidget;
    private QuestionText timerDescriptionWidget;
    private QuestionText onTrialTextWidget;

    public KillingRoomScreen(State startingState, String id, int timerIndex) {
        super(startingState, KillingRoomScreen.class, State.class);
        this.id = id;
        this.room = KillingRooms.getKillingRoom(id);
        this.timerIndex = timerIndex;
        this.descriptionIndex = 0;
        this.descriptionLengths = room.getDescriptionLengths();
        this.title = Text.translatable("screen." + TriviaMurderParty.ID + ".killing_room")
                .styled(style -> style.withFont(TriviaMurderParty.Fonts.QUESTION_NUMBER));
        this.description = Text.translatable(this.getDescriptionString())
                .styled(style -> style.withFont(TriviaMurderParty.Fonts.QUESTION));
        this.timerDescription = Text.translatable(this.getTimerDescriptionString())
                .styled(style -> style.withFont(TriviaMurderParty.Fonts.QUESTION));
        List<KillingRoom.Timer> timers = this.room.getTimers();
        this.timerLength = timerIndex >= 0 && timerIndex < timers.size() ? timers.get(timerIndex).length() : 0;
        this.onTrial = KillingRoom.isOnTrial(true, MCTournament.client().player);
        String translationString = this.onTrial ? "on_trial" : "not_on_trial";
        this.onTrialText = Text.translatable("screen." + TriviaMurderParty.ID + ".killing_room." + translationString)
                .styled(style -> style.withFont(TriviaMurderParty.Fonts.QUESTION_ANSWER));
    }

    @Override
    protected void createWidgets() {
        this.titleWidget = this.addDrawableChild(new QuestionText(this.width / 2, this.height / 2, this.title,
                40, ModColors.RED, this.width / 2, this.textRenderer));

        this.descriptionWidget = this.addDrawableChild(new QuestionText(this.width / 2, this.height / 2, this.description,
                20, ModColors.WHITE, this.width / 2, this.textRenderer));

        this.timerWidget = this.addDrawableChild(new QuestionTimer(0, 0, 64, 64, this.timerLength,
                1.0f, TIMER_MOVE_TIME, this));
        this.timerDescriptionWidget = this.addDrawableChild(new QuestionText(this.hudWidth() / 2, 30, this.timerDescription,
                20, ModColors.WHITE, this.hudWidth() * 2 / 3, this.textRenderer));

        this.onTrialTextWidget = this.addDrawableChild(new QuestionText(this.width / 2, 15, this.onTrialText,
                20, this.onTrial ? ModColors.RED : ModColors.GREEN, this.width / 2, this.textRenderer));
    }

    @SuppressWarnings("unused")
    public enum State implements AnimatedScreen.State<KillingRoomScreen> {
        TITLE_IN {
            public void begin(KillingRoomScreen screen) {screen.onTrialTextWidget.setAlpha(1.0f);}
            public void render(KillingRoomScreen screen) {
                QuestionText title = screen.titleWidget;
                screen.animate(title::setAlpha, 0.0f, 1.0f);
                screen.animate(title::setX, title.getOriginalX() - (title.getOriginalWidth() / 2), title.getOriginalX());
                screen.animate(title::setY, title.getOriginalY() - (title.getOriginalHeight() / 2), title.getOriginalY());
                screen.animate(title::setWidth, title.getOriginalWidth() * 2, title.getOriginalWidth());
                screen.animate(title::setHeight, title.getOriginalHeight() * 2, title.getOriginalHeight());
            }
            public void refresh(KillingRoomScreen screen) {
                screen.titleWidget.setAlpha(1.0f);
                screen.onTrialTextWidget.setAlpha(1.0f);
            }
            public float duration(KillingRoomScreen screen) {return 0.5f;}
        },
        TITLE_HOLD {
            public void render(KillingRoomScreen screen) {}
            public void refresh(KillingRoomScreen screen) {
                screen.titleWidget.setAlpha(1.0f);
                screen.onTrialTextWidget.setAlpha(1.0f);
            }
            public float duration(KillingRoomScreen screen) {return 1.0f;}
        },
        TITLE_OUT {
            public void render(KillingRoomScreen screen) {
                QuestionText title = screen.titleWidget;
                screen.animate(title::setAlpha, 1.0f, 0.0f);
                screen.animate(title::setX, title.getOriginalX(), title.getOriginalX() - (title.getOriginalWidth() / 2));
                screen.animate(title::setY, title.getOriginalY(), title.getOriginalY() - (title.getOriginalHeight() / 2));
                screen.animate(title::setWidth, title.getOriginalWidth(), title.getOriginalWidth() * 2);
                screen.animate(title::setHeight, title.getOriginalHeight(), title.getOriginalHeight() * 2);
            }
            public void refresh(KillingRoomScreen screen) {screen.onTrialTextWidget.setAlpha(1.0f);}
            public float duration(KillingRoomScreen screen) {return 1.0f;}
        },
        NAME_IN {
            public void begin(KillingRoomScreen screen) {
                screen.titleWidget.setText(screen.getNameString(), ModColors.GREY);
            }
            public void render(KillingRoomScreen screen) {
                screen.animate(screen.titleWidget::setAlpha, 0.0f, 1.0f);
                screen.animate(screen.titleWidget::setY, 0, screen.titleWidget.getOriginalY());
            }
            public void refresh(KillingRoomScreen screen) {
                screen.titleWidget.setAlpha(1.0f);
                screen.onTrialTextWidget.setAlpha(1.0f);
            }
            public float duration(KillingRoomScreen screen) {return 1.0f;}
        },
        NAME_HOLD {
            public void render(KillingRoomScreen screen) {}
            public void refresh(KillingRoomScreen screen) {
                screen.titleWidget.setAlpha(1.0f);
                screen.onTrialTextWidget.setAlpha(1.0f);
            }
            public float duration(KillingRoomScreen screen) {return 1.0f;}
        },
        NAME_OUT {
            public void render(KillingRoomScreen screen) {screen.animate(screen.titleWidget::setAlpha, 1.0f, 0.0f);}
            public void refresh(KillingRoomScreen screen) {screen.onTrialTextWidget.setAlpha(1.0f);}
            public float duration(KillingRoomScreen screen) {return 1.0f;}
        },
        DESCRIPTION_IN {
            public void begin(KillingRoomScreen screen) {screen.descriptionWidget.setText(screen.getDescriptionString());}
            public void render(KillingRoomScreen screen) {screen.animate(screen.descriptionWidget::setAlpha, 0.0f, 1.0f);}
            public void refresh(KillingRoomScreen screen) {
                screen.descriptionWidget.setAlpha(1.0f);
                screen.onTrialTextWidget.setAlpha(1.0f);
            }
            public float duration(KillingRoomScreen screen) {return 1.0f;}
        },
        DESCRIPTION_HOLD {
            public void render(KillingRoomScreen screen) {}
            public void refresh(KillingRoomScreen screen) {
                screen.descriptionWidget.setAlpha(1.0f);
                screen.onTrialTextWidget.setAlpha(1.0f);
            }
            public float duration(KillingRoomScreen screen) {return screen.descriptionLengths.get(screen.descriptionIndex);}
        },
        DESCRIPTION_OUT {
            public void render(KillingRoomScreen screen) {screen.animate(screen.descriptionWidget::setAlpha, 1.0f, 0.0f);}
            public void refresh(KillingRoomScreen screen) {
                screen.descriptionWidget.setAlpha(0.0f);
                screen.onTrialTextWidget.setAlpha(1.0f);
            }
            public float duration(KillingRoomScreen screen) {return 1.0f;}
            public State next(KillingRoomScreen screen) {
                screen.descriptionIndex++;
                if (screen.descriptionIndex >= screen.descriptionLengths.size()) {
                    screen.room.clientBegin();
                    ModNetworking.sendToServer(TriviaMurderParty.NetworkIDs.KILLING_ROOM_BEGIN);
                    return null;
                } else {
                    return DESCRIPTION_IN;
                }
            }
        },
        TIMER_IN {
            public boolean isHudState(KillingRoomScreen screen) {return true;}
            public void begin(KillingRoomScreen screen) {
                screen.timerWidget.reset();
                screen.timerDescriptionWidget.setText(screen.getTimerDescriptionString());
            }
            public void render(KillingRoomScreen screen) {
                screen.animate(screen.timerWidget::setAlpha, 0.0f, 1.0f);
                screen.animate(screen.timerWidget::setY, -screen.timerWidget.getOriginalY(), screen.timerWidget.getOriginalY());
                screen.animate(screen.timerDescriptionWidget::setAlpha, 0.0f, 1.0f);
                screen.animate(screen.timerDescriptionWidget::setY, -screen.timerDescriptionWidget.getOriginalY(),
                        screen.timerDescriptionWidget.getOriginalY());
            }
            public void refresh(KillingRoomScreen screen) {
                screen.timerWidget.setAlpha(1.0f);
                screen.onTrialTextWidget.setAlpha(1.0f);
            }
            public float duration(KillingRoomScreen screen) {return TIMER_MOVE_TIME;}
        },
        TIMER_HOLD {
            public boolean isHudState(KillingRoomScreen screen) {return true;}
            public void render(KillingRoomScreen screen) {}
            public void refresh(KillingRoomScreen screen) {
                screen.timerWidget.setAlpha(1.0f);
                screen.timerDescriptionWidget.setAlpha(1.0f);
                screen.onTrialTextWidget.setAlpha(1.0f);
            }
            public float duration(KillingRoomScreen screen) {return screen.timerLength;}
        },
        TIMER_OUT {
            public boolean isHudState(KillingRoomScreen screen) {return true;}
            public void render(KillingRoomScreen screen) {
                screen.animate(screen.timerWidget::setAlpha, 1.0f, 0.0f);
                screen.animate(screen.timerWidget::setY, screen.timerWidget.getOriginalY(), -screen.timerWidget.getOriginalY());
                screen.animate(screen.timerDescriptionWidget::setAlpha, 1.0f, 0.0f);
                screen.animate(screen.timerDescriptionWidget::setY, screen.timerDescriptionWidget.getOriginalY(),
                        -screen.timerDescriptionWidget.getOriginalY());
            }
            public void end(KillingRoomScreen screen) {
                ModNetworking.sendToServer(TriviaMurderParty.NetworkIDs.KILLING_ROOM_TIMER_UP, PacketByteBufs.create()
                        .writeInt(screen.timerIndex));
            }
            public void refresh(KillingRoomScreen screen) {screen.onTrialTextWidget.setAlpha(1.0f);}
            public float duration(KillingRoomScreen screen) {return TIMER_MOVE_TIME;}
        }
    }

    public static void clientInit() {
        ModNetworking.clientReceive(TriviaMurderParty.NetworkIDs.KILLING_ROOM_SCREEN, clientReceiveInfo -> {
            PacketByteBuf buffer = clientReceiveInfo.buffer();

            KillingRoomScreenHandler.Entrypoint entrypoint = buffer.readEnumConstant(KillingRoomScreenHandler.Entrypoint.class);
            State startingState = switch (entrypoint) {
                case TITLE_IN -> State.TITLE_IN;
                case TIMER_IN -> State.TIMER_IN;
            };

            String roomID = buffer.readString();
            int timerIndex = buffer.readInt();

            MCTournament.client().setScreen(new KillingRoomScreen(startingState, roomID, timerIndex));
        });
    }

    private String getNameString() {
        return "screen." + TriviaMurderParty.ID + ".killing_room_" + this.id;
    }

    private String getDescriptionString() {
        return "screen." + TriviaMurderParty.ID + ".killing_room_" + this.id + "_description_" + this.descriptionIndex;
    }

    private String getTimerDescriptionString() {
        return "screen." + TriviaMurderParty.ID + ".killing_room_" + this.id + "_description_timer_" + this.timerIndex;
    }
}
