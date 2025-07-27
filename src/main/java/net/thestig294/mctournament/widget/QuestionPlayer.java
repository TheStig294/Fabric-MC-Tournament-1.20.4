package net.thestig294.mctournament.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class QuestionPlayer extends ClickableWidget {
    private final PlayerEntity player;

    public QuestionPlayer(int x, int y, int width, int height, PlayerEntity player) {
        super(x, y, width, height, Text.empty());

        this.player = player;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        InventoryScreen.drawEntity(context, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(),
                30, 0.0625f, mouseX, mouseY, this.player);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public void playDownSound(SoundManager soundManager) {

    }
}
