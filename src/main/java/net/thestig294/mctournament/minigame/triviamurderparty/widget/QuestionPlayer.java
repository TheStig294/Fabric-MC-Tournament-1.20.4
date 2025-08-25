package net.thestig294.mctournament.minigame.triviamurderparty.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class QuestionPlayer extends ClickableWidget {
    private final PlayerEntity player;
    private final int originalX;
    private final int originalY;
    private boolean forceLookForward;

    public QuestionPlayer(int x, int y, int width, int height, PlayerEntity player) {
        super(x, y, width, height, Text.empty());

        this.player = player;
        this.originalX = x;
        this.originalY = y;
        this.forceLookForward = false;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.forceLookForward) {
            mouseX = this.getX() + (this.getWidth() / 2);
            mouseY = this.getY() + (this.getHeight() / 2);
        }

        context.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        InventoryScreen.drawEntity(context, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(),
                30, 0.0625f, mouseX, mouseY, this.player);
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

    public void forceLookForward(boolean doForce) {
        this.forceLookForward = doForce;
    }

    public PlayerEntity getPlayer() {
        return this.player;
    }
}
