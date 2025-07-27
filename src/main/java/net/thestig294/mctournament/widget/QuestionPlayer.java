package net.thestig294.mctournament.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;
import net.thestig294.mctournament.font.ModFonts;

import java.awt.*;

public class QuestionPlayer extends ClickableWidget {
    private final ClientPlayerEntity player;
    private final TextRenderer textRenderer;
    private final int color;
    private final Text playerName;

    public QuestionPlayer(int x, int y, int width, int height, ClientPlayerEntity player, TextRenderer textRenderer, Color color) {
        super(x, y, width, height, Text.empty());

        this.player = player;
        this.textRenderer = textRenderer;
        this.color = color.getRGB();
        this.playerName = Text.literal(this.player.getName().getString()).styled(style -> style.withFont(ModFonts.QUESTION_ANSWER));
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawCenteredTextWithShadow(this.textRenderer, this.playerName, this.getX() + this.getWidth() / 2, this.getY(), this.color);
        InventoryScreen.drawEntity(context, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(),
                30, 0.0f, mouseX, mouseY, this.player);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public void playDownSound(SoundManager soundManager) {

    }
}
