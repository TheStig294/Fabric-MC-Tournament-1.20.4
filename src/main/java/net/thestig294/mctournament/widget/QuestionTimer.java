package net.thestig294.mctournament.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

public class QuestionTimer extends ClickableWidget {
    private final Identifier frontTexture;
    private final Identifier backTexture;
    private final float tickFrequency;
    private final TextRenderer textRenderer;
    private float totalDelta;
    private float nextDeltaTick;
    private int ticksLeft;

    public QuestionTimer(int x, int y, int width, int height, Identifier frontTexture, Identifier backTexture,
                         int length, float tickFrequency, int startDelay, TextRenderer textRenderer) {
        super(x, y, width, height, Text.empty());
        this.frontTexture = frontTexture;
        this.backTexture = backTexture;
        this.ticksLeft = length;
        this.tickFrequency = tickFrequency;
        this.textRenderer = textRenderer;
        this.totalDelta = 0.0f;
        this.nextDeltaTick = tickFrequency + startDelay;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawGuiTexture(this.backTexture, this.getX(), this.getY(), this.getWidth(), this.getHeight());

        this.totalDelta += delta;
        if (this.totalDelta / 20 >= this.nextDeltaTick && this.ticksLeft > 0) {
            this.ticksLeft--;
            this.nextDeltaTick += this.tickFrequency;
        }
        context.drawText(this.textRenderer, Integer.toString(ticksLeft), this.getX(),this.getY(), Colors.WHITE, true);

        context.drawGuiTexture(this.frontTexture, this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}
