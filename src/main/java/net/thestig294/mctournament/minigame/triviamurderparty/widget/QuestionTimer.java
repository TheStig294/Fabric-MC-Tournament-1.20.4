package net.thestig294.mctournament.minigame.triviamurderparty.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.util.ModUtilClient;

public class QuestionTimer extends ClickableWidget {
    private static final int MIN_LENGTH = 0;
    private static final int MAX_LENGTH = TriviaMurderParty.Textures.QUESTION_TIMER_HAND_COUNT - 1;

    private int length;
    private final float tickFrequency;
    private final float startDelay;
    private float totalDelta;
    private float nextDeltaTick;
    private int ticksLeft;
    private final int originalY;

    public QuestionTimer(int x, int y, int width, int height, int length, float tickFrequency, float startDelay) {
        super(x, y, width, height, Text.empty());

        this.length = length;
        this.tickFrequency = tickFrequency;
        this.startDelay = startDelay;
        this.reset();
        this.originalY = y;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        RenderSystem.enableBlend();
        this.totalDelta += delta;

        if (this.totalDelta / ModUtilClient.getTicksPerSecond() >= this.nextDeltaTick && this.ticksLeft > 0) {
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
        this.totalDelta = 0.0f;
        this.nextDeltaTick = this.tickFrequency + startDelay;
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

    public int getOriginalY() {
        return this.originalY;
    }

    public int getTicksLeft() {
        return this.ticksLeft;
    }
}
