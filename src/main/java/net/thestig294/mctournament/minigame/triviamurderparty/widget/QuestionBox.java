package net.thestig294.mctournament.minigame.triviamurderparty.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;

import java.awt.*;

public class QuestionBox extends ClickableWidget {
    private final int color;
    private final int originalX;
    private final int originalY;

    public QuestionBox(int x, int y, int width, int height, Color color) {
        super(x, y, width, height, Text.empty());
        this.color = color.getRGB();
        this.originalX = x;
        this.originalY = y;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        context.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), this.color);
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public void playDownSound(SoundManager soundManager) {

    }

    public int getOriginalX() {
        return this.originalX;
    }

    public int getOriginalY() {
        return this.originalY;
    }
}
