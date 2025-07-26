package net.thestig294.mctournament.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.RotationAxis;
import net.thestig294.mctournament.texture.ModTextures;

public class QuestionTimer extends ClickableWidget {
    private final TextRenderer textRenderer;
    private final float tickFrequency;
    private float totalDelta;
    private float nextDeltaTick;
    private int ticksLeft;
    private int ticksPassed;

    public QuestionTimer(int x, int y, int width, int height,
                         int length, float tickFrequency, int startDelay, TextRenderer textRenderer) {
        super(x, y, width, height, Text.empty());
        this.textRenderer = textRenderer;
        this.tickFrequency = tickFrequency;
        this.totalDelta = 0.0f;
        this.nextDeltaTick = tickFrequency + startDelay;
        this.ticksLeft = length;
        this.ticksPassed = 0;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawTexture(ModTextures.QUESTION_TIMER_BACK, this.getX(), this.getY(), this.getWidth(),this.getHeight(), this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
        context.drawTexture(ModTextures.QUESTION_TIMER_FRONT_RIGHT, this.getX(), this.getY(), this.getWidth(),this.getHeight(), this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());

        this.totalDelta += delta;
        if (this.totalDelta / 20 >= this.nextDeltaTick && this.ticksLeft > 0) {
            this.ticksLeft--;
            this.ticksPassed++;
            this.nextDeltaTick += this.tickFrequency;
        }
        context.drawText(this.textRenderer, Integer.toString(ticksLeft), this.getX(),this.getY(), Colors.WHITE, true);

        MatrixStack matrices = context.getMatrices();
        matrices.push();
//        A clock rotates its hand by 6 degrees each second. 360 degrees/60 seconds = 6 degrees per second!
        float degreesRotation = (float) (this.ticksPassed * 6);
//        float degreesRotation = totalDelta / 50.0f % 360;
        matrices.translate(-(this.getX() + this.getWidth() * 0.5), -(this.getY() + this.getHeight() * 0.5), 0.0f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotation(degreesRotation));
//        matrices.translate(this.getX(), this.getY(), 0.0f);
        context.drawTexture(ModTextures.QUESTION_TIMER_HAND, this.getX(), this.getY(), this.getWidth(),this.getHeight(), this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
        matrices.pop();

        context.drawTexture(ModTextures.QUESTION_TIMER_FRONT_LEFT, this.getX(), this.getY(), this.getWidth(),this.getHeight(), this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}
