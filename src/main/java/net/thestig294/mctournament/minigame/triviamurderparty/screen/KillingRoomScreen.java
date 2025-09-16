package net.thestig294.mctournament.minigame.triviamurderparty.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.minigame.triviamurderparty.killingroom.KillingRoom;

import java.util.Arrays;
import java.util.List;

@Environment(EnvType.CLIENT)
public class KillingRoomScreen extends Screen {
    private final String roomID;
    private final Text name;
    private final List<KillingRoom.Description> descriptionLines;
    private int descriptionLineIndex;
    private final Screen parent;
    private State state;

    public KillingRoomScreen(KillingRoom room) {
        super(Text.translatable(room.properties().name()));
        this.roomID = room.properties().id();
        this.name = Text.translatable(room.properties().name()).styled(style -> style.withFont(TriviaMurderParty.Fonts.QUESTION));
        this.descriptionLines = Arrays.stream(room.properties().descriptionLines()).toList();
        this.descriptionLineIndex = 0;
        this.parent = MCTournament.client().currentScreen;
        this.state = State.NAME_IN;

        HudRenderCallback.EVENT.register(this::hudRender);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }

    private void hudRender(DrawContext context, float tickDelta) {

    }

    @Override
    public void close() {
        MCTournament.client().setScreen(this.parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public enum State {
        NAME_IN,
        NAME_HOLD,
        NAME_OUT,
        DESCRIPTION_IN,
        DESCRIPTION_HOLD,
        DESCRIPTION_OUT,
        HUD_RENDER
    }
}
