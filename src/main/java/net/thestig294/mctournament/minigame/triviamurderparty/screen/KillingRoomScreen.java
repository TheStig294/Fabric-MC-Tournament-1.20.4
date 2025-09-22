package net.thestig294.mctournament.minigame.triviamurderparty.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.minigame.triviamurderparty.killingroom.KillingRoom;
import net.thestig294.mctournament.minigame.triviamurderparty.killingroom.KillingRooms;
import net.thestig294.mctournament.minigame.triviamurderparty.widget.QuestionText;
import net.thestig294.mctournament.network.ModNetworking;
import net.thestig294.mctournament.screen.AnimatedScreen;
import net.thestig294.mctournament.util.ModColors;
import net.thestig294.mctournament.util.ModUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
public class KillingRoomScreen extends AnimatedScreen<KillingRoomScreen, KillingRoomScreen.State> {
    private final KillingRoom room;
    private final String id;
    private final List<Float> descriptionLengths;
    private final Text title;
    private Text description;
    private int descriptionIndex;

    private QuestionText titleWidget;
    private QuestionText descriptionWidget;

    public KillingRoomScreen(KillingRoom room) {
        super(State.NAME_IN);
        this.room = room;
        this.id = room.properties().id();
        this.descriptionLengths = Arrays.stream(room.properties().descriptionLengths());
        this.title = Text.translatable("screen.mctournament.killing_room")
                .styled(style -> style.withFont(TriviaMurderParty.Fonts.QUESTION_NUMBER));
        this.description = Text.translatable("screen.mctournament.killing_room_" + this.id +"_description_0")
                .styled(style -> style.withFont(TriviaMurderParty.Fonts.QUESTION));
        this.descriptionIndex = 0;
    }

    @Override
    protected void createWidgets() {
        this.titleWidget = this.addDrawableChild(new QuestionText(this.width / 2, this.height / 2, this.title,
                20, ModColors.RED, this.width / 2, this.textRenderer));

        this.descriptionWidget = this.addDrawableChild(new QuestionText(this.width / 2, this.height / 2, this.description,
                20, ModColors.WHITE, this.width / 2, this.textRenderer));
    }

    public enum State implements AnimatedScreen.State<KillingRoomScreen> {
        TITLE_IN {
            @Override
            public void render(KillingRoomScreen screen) {
                QuestionText title = screen.titleWidget;
                screen.animate(title::setAlpha, 0.0f, 1.0f);
                screen.animate(title::setX, title.getOriginalX() - (title.getOriginalWidth() / 2), title.getOriginalX());
                screen.animate(title::setY, title.getOriginalY() - (title.getOriginalHeight() / 2), title.getOriginalY());
                screen.animate(title::setWidth, title.getOriginalWidth() * 2, title.getOriginalWidth());
                screen.animate(title::setHeight, title.getOriginalHeight() * 2, title.getOriginalHeight());
            }

            @Override
            public void refresh(KillingRoomScreen screen) {
                screen.titleWidget.setAlpha(1.0f);
            }

            @Override
            public float duration(KillingRoomScreen screen) {
                return 0.5f;
            }
        },
        TITLE_HOLD {
            @Override
            public void render(KillingRoomScreen screen) {

            }

            @Override
            public void refresh(KillingRoomScreen screen) {
                screen.titleWidget.setAlpha(1.0f);
            }

            @Override
            public float duration(KillingRoomScreen screen) {
                return 1.0f;
            }
        },
        TITLE_OUT {
            @Override
            public void render(KillingRoomScreen screen) {
                QuestionText title = screen.titleWidget;
                screen.animate(title::setAlpha, 1.0f, 0.0f);
                screen.animate(title::setX, title.getOriginalX(), title.getOriginalX() - (title.getOriginalWidth() / 2));
                screen.animate(title::setY, title.getOriginalY(), title.getOriginalY() - (title.getOriginalHeight() / 2));
                screen.animate(title::setWidth, title.getOriginalWidth(), title.getOriginalWidth() * 2);
                screen.animate(title::setHeight, title.getOriginalHeight(), title.getOriginalHeight() * 2);
            }

            @Override
            public void refresh(KillingRoomScreen screen) {

            }

            @Override
            public float duration(KillingRoomScreen screen) {
                return 1.0f;
            }
        },
        NAME_IN {
            @Override
            public void begin(KillingRoomScreen screen) {
                screen.titleWidget.setText("screen.mctournament.killing_room_" + screen.id + "_name", ModColors.GREY);
            }

            @Override
            public void render(KillingRoomScreen screen) {
                screen.animate(screen.titleWidget::setAlpha, 0.0f, 1.0f);
                screen.animate(screen.titleWidget::setY, 0, screen.titleWidget.getOriginalY());
            }

            @Override
            public void refresh(KillingRoomScreen screen) {
                screen.titleWidget.setAlpha(1.0f);
            }

            @Override
            public float duration(KillingRoomScreen screen) {
                return 1.0f;
            }
        },
        NAME_HOLD {
            @Override
            public void render(KillingRoomScreen screen) {

            }

            @Override
            public void refresh(KillingRoomScreen screen) {
                screen.titleWidget.setAlpha(1.0f);
            }

            @Override
            public float duration(KillingRoomScreen screen) {
                return 1.0f;
            }
        },
        NAME_OUT {
            @Override
            public void render(KillingRoomScreen screen) {
                screen.animate(screen.titleWidget::setAlpha, 1.0f, 0.0f);
            }

            @Override
            public void refresh(KillingRoomScreen screen) {

            }

            @Override
            public float duration(KillingRoomScreen screen) {
                return 1.0f;
            }
        },
        DESCRIPTION_IN {
            @Override
            public void begin(KillingRoomScreen screen) {
                screen.descriptionWidget.setText(screen.getDescriptionString());
            }

            @Override
            public void render(KillingRoomScreen screen) {
                screen.animate(screen.descriptionWidget::setAlpha, 0.0f, 1.0f);
            }

            @Override
            public void refresh(KillingRoomScreen screen) {
                screen.descriptionWidget.setAlpha(1.0f);
            }

            @Override
            public float duration(KillingRoomScreen screen) {
                return 1.0f;
            }
        },
        DESCRIPTION_HOLD {
            @Override
            public void render(KillingRoomScreen screen) {

            }

            @Override
            public void refresh(KillingRoomScreen screen) {
                screen.descriptionWidget.setAlpha(1.0f);
            }

            @Override
            public float duration(KillingRoomScreen screen) {
                return screen.getDescription().duration();
            }
        },
        DESCRIPTION_OUT {
            @Override
            public void render(KillingRoomScreen screen) {
                screen.animate(screen.descriptionWidget::setAlpha, 1.0f, 0.0f);
            }

            @Override
            public void refresh(KillingRoomScreen screen) {
                screen.descriptionWidget.setAlpha(0.0f);
            }

            @Override
            public float duration(KillingRoomScreen screen) {
                return 1.0f;
            }

            @Override
            public State next(KillingRoomScreen screen) {
                screen.descriptionIndex++;
                if (screen.descriptionIndex >= screen.descriptionLengths.size()) {
                    return HUD_RENDER;
                } else {
                    return DESCRIPTION_IN;
                }
            }
        },
        HUD_RENDER {
            @Override
            public void render(KillingRoomScreen screen) {

            }

            @Override
            public void refresh(KillingRoomScreen screen) {

            }

            @Override
            public float duration(KillingRoomScreen screen) {
                return 0;
            }
        }
    }

    public static void clientInit() {
        ModNetworking.clientReceive(TriviaMurderParty.NetworkIDs.KILLING_ROOM_SCREEN, clientReceiveInfo -> {
            PacketByteBuf buffer = clientReceiveInfo.buffer();
            String id = buffer.readString();
            KillingRoom room = KillingRooms.get(id);
            MCTournament.client().setScreen(new KillingRoomScreen(room));
        });
    }

    private String getDescriptionString() {
        return "screen.mctournament.killing_room_" + this.id + "_description_" + this.descriptionIndex;
    }
}
