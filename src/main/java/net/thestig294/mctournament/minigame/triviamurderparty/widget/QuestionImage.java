package net.thestig294.mctournament.minigame.triviamurderparty.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class QuestionImage extends ClickableWidget implements QuestionWidget {
    private final int originalX;
    private final int originalY;
    private final int originalWidth;
    private final int originalHeight;
    private final Identifier texture;

    public QuestionImage(int x, int y, int width, int height, Identifier textureID) {
        super(x, y, width, height, Text.empty());

        this.originalX = x;
        this.originalY = y;
        this.originalWidth = width;
        this.originalHeight = height;
        this.texture = textureID;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);

        context.drawTexture(this.texture, this.getX(), this.getY(), 0,0,
                this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());

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

    public int getOriginalWidth() {
        return this.originalWidth;
    }

    public int getOriginalHeight() {
        return this.originalHeight;
    }

    @Override
    public float getAlpha() {
        return this.alpha;
    }
}
