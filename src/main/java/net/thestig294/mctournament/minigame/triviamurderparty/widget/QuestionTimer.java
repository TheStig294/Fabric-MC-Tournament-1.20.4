package net.thestig294.mctournament.minigame.triviamurderparty.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.screen.AnimatedScreen;

public class QuestionTimer extends ClickableWidget implements QuestionWidget {
    private static final int MIN_LENGTH = 0;
    private static final int MAX_LENGTH = TriviaMurderParty.Textures.QUESTION_TIMER_HAND_COUNT - 1;

    private int length;
    private final float tickFrequency;
    private final float startDelay;
    private final AnimatedScreen<?,?> screen;
    private float nextDeltaTick;
    private int ticksLeft;
    private final int originalX;
    private final int originalY;
    private final int originalWidth;
    private final int originalHeight;

    public QuestionTimer(int x, int y, int width, int height, int length, float tickFrequency, float startDelay, AnimatedScreen<?,?> screen) {
        super(x, y, width, height, Text.empty());

        this.length = length;
        this.tickFrequency = tickFrequency;
        this.startDelay = startDelay;
        this.screen = screen;
        this.reset();
        this.originalX = x;
        this.originalY = y;
        this.originalWidth = width;
        this.originalHeight = height;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        RenderSystem.enableBlend();

        if (this.screen.getUptimeSecs() >= this.nextDeltaTick && this.ticksLeft > 0) {
            this.ticksLeft--;
            this.nextDeltaTick += this.tickFrequency;
        }

        context.drawTexture(TriviaMurderParty.Textures.QUESTION_TIMER_BACK, this.getX(), this.getY(), 0, 0,
                this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
        context.drawTexture(TriviaMurderParty.Textures.QUESTION_TIMER_HANDS[this.ticksLeft], this.getX(), this.getY(),
                0, 0, this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
        RenderSystem.disableBlend();
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public void reset() {
        this.reset(this.startDelay);
    }

    public void reset(float startDelay) {
        this.nextDeltaTick = this.tickFrequency + startDelay + this.screen.getUptimeSecs();
        this.ticksLeft = Math.max(MIN_LENGTH, Math.min(length, MAX_LENGTH));
    }

    public void reset(float startDelay, int length) {
        this.length = length;
        this.reset(startDelay);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public void playDownSound(SoundManager soundManager) {

    }

    @Override
    public int getOriginalX() {
        return this.originalX;
    }

    public int getOriginalY() {
        return this.originalY;
    }

    @Override
    public int getOriginalWidth() {
        return this.originalWidth;
    }

    @Override
    public int getOriginalHeight() {
        return this.originalHeight;
    }

    public int getTicksLeft() {
        return this.ticksLeft;
    }

    @Override
    public float getAlpha() {
        return this.alpha;
    }

    @Override
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
    }
}
