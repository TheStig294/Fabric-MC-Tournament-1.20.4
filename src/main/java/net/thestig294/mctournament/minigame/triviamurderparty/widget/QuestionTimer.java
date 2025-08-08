package net.thestig294.mctournament.minigame.triviamurderparty.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;
import net.thestig294.mctournament.texture.ModTextures;

public class QuestionTimer extends ClickableWidget {
    private final float tickFrequency;
    private float totalDelta;
    private float nextDeltaTick;
    private int ticksLeft;

    public QuestionTimer(int x, int y, int width, int height,
                         int length, float tickFrequency, int startDelay) {
        super(x, y, width, height, Text.empty());

        final int MIN_LENGTH = 0;
        final int MAX_LENGTH = ModTextures.QUESTION_TIMER_HAND_COUNT - 1;

        this.tickFrequency = tickFrequency;
        this.totalDelta = 0.0f;
        this.nextDeltaTick = tickFrequency + startDelay;
        this.ticksLeft = Math.max(MIN_LENGTH, Math.min(length, MAX_LENGTH));
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        RenderSystem.enableBlend();
        this.totalDelta += delta;
//        Minecraft runs at 20 ticks/second
        if (this.totalDelta / 20 >= this.nextDeltaTick && this.ticksLeft > 0) {
            this.ticksLeft--;
            this.nextDeltaTick += this.tickFrequency;
        }

        context.drawTexture(ModTextures.QUESTION_TIMER_BACK, this.getX(), this.getY(), 0, 0, this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
        context.drawTexture(ModTextures.QUESTION_TIMER_HANDS[this.ticksLeft], this.getX(), this.getY(), 0, 0, this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
        RenderSystem.disableBlend();
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public void playDownSound(SoundManager soundManager) {

    }
}
