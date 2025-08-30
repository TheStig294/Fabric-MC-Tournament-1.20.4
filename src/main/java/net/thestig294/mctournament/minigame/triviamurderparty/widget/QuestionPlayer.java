package net.thestig294.mctournament.minigame.triviamurderparty.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.util.ModColors;

import java.awt.*;

public class QuestionPlayer extends ClickableWidget {
    private final PlayerEntity player;
    private final int originalY;
    private final TextRenderer textRenderer;
    private final Text playerNameText;
    private AnswerState answerState;

    public QuestionPlayer(int x, int y, int width, int height, PlayerEntity player, TextRenderer textRenderer) {
        super(x, y, width, height, Text.empty());

        this.player = player;
        this.originalY = y;
        this.textRenderer = textRenderer;
        this.playerNameText = Text.literal(player.getNameForScoreboard())
                .styled(style -> style.withFont(TriviaMurderParty.Fonts.QUESTION_ANSWER));
        this.answerState = AnswerState.UNANSWERED;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);

        if (this.answerState != AnswerState.UNANSWERED) {
            mouseX = this.getX() + (this.getWidth() / 2);
            mouseY = this.getY() + (this.getHeight() / 2);
        }
        InventoryScreen.drawEntity(context, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(),
                30, 0.0625f, mouseX, mouseY, this.player);
        context.drawCenteredTextWithShadow(this.textRenderer, this.playerNameText,
                this.getX() + (this.getWidth() / 2), this.getY() + 3, this.getNameColor().getRGB());

        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
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

    public PlayerEntity getPlayer() {
        return this.player;
    }

    public Color getNameColor() {
        return switch (this.answerState) {
            case UNANSWERED -> ModColors.WHITE;
            case ANSWERED -> ModColors.YELLOW;
            case CORRECT -> ModColors.GREEN;
            case INCORRECT -> ModColors.RED;
        };
    }

    public void setAnswerState(AnswerState answerState) {
        this.answerState = answerState;
    }

    public enum AnswerState {
        UNANSWERED,
        ANSWERED,
        CORRECT,
        INCORRECT
    }
}
