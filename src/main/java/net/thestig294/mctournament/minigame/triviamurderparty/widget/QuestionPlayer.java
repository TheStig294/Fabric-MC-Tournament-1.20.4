package net.thestig294.mctournament.minigame.triviamurderparty.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.tournament.Tournament;
import net.thestig294.mctournament.util.ModColors;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class QuestionPlayer extends ClickableWidget implements QuestionWidget {
    private final boolean isCaptain;
    private final PlayerEntity player;
    private final int originalX;
    private final int originalY;
    private final int originalWidth;
    private final int originalHeight;
    private final TextRenderer textRenderer;
    private Text bottomText;
    private float bottomTextAlpha;
    private AnswerState answerState;
    private boolean isPlayerCorrect;
    private boolean alwaysDrawBottomText;
    private QuestionImage tickWidget;
    private QuestionImage crossWidget;

    public QuestionPlayer(int x, int y, int width, int height, PlayerEntity player, TextRenderer textRenderer) {
        super(x, y, width, height, Text.literal(player.getNameForScoreboard())
                .styled(style -> style.withFont(TriviaMurderParty.Fonts.QUESTION_ANSWER)));

        PlayerEntity clientPlayer = MCTournament.CLIENT.player;
        if (clientPlayer == null) {
            this.isCaptain = false;
        } else {
            Team team = clientPlayer.getScoreboardTeam();
            if (team == null) {
                this.isCaptain = false;
            } else {
                int teamSize = team.getPlayerList().size();
                PlayerEntity captain = Tournament.inst().clientScoreboard().getTeamCaptain(clientPlayer);
//                Don't show the "Your Team" text if the player is in a "team" of 1!
                this.isCaptain = teamSize > 1 && captain != null && captain.equals(player);
            }
        }

        this.player = player;
        this.originalX = x;
        this.originalY = y;
        this.originalWidth = width;
        this.originalHeight = height;
        this.textRenderer = textRenderer;
        this.bottomText = Text.translatable("widget.mctournament.question_player_your_team")
                .styled(style -> style.withFont(TriviaMurderParty.Fonts.QUESTION_ANSWER));
        this.bottomTextAlpha = 1.0f;
        this.answerState = AnswerState.UNANSWERED;
        this.isPlayerCorrect = false;
        this.alwaysDrawBottomText = false;
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
        context.drawCenteredTextWithShadow(this.textRenderer, this.getMessage(),
                this.getX() + (this.getWidth() / 2), this.getY() + 3, this.getTextColor());

        if (this.isCaptain || this.alwaysDrawBottomText) {
            context.setShaderColor(1.0f, 1.0f, 1.0f, this.bottomTextAlpha);
            context.drawCenteredTextWithShadow(this.textRenderer, this.bottomText,
                    this.getX() + (this.getWidth() / 2), this.getY() + this.getHeight() - 5, this.getTextColor());
            context.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        }

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

    @Override
    public int getOriginalWidth() {
        return this.originalWidth;
    }

    @Override
    public int getOriginalHeight() {
        return this.originalHeight;
    }

    public PlayerEntity getPlayer() {
        return this.player;
    }

    public int getTextColor() {
        Color color = switch (this.answerState) {
            case UNANSWERED -> ModColors.WHITE;
            case ANSWERED -> ModColors.YELLOW;
            case CORRECT -> ModColors.GREEN;
            case INCORRECT -> ModColors.RED;
        };
        return color.getRGB();
    }

    public void setAnswerState(AnswerState answerState) {
        this.answerState = answerState;
    }

    public void setPlayerCorrect(boolean isPlayerCorrect) {
        this.isPlayerCorrect = isPlayerCorrect;
    }

    public boolean isPlayerCorrect() {
        return this.isPlayerCorrect;
    }

    public void setBottomText(String textString) {
        this.bottomText = Text.literal(textString).styled(style -> style.withFont(TriviaMurderParty.Fonts.QUESTION_ANSWER));
        this.alwaysDrawBottomText = true;
    }

    public void setBottomTextAlpha(float alpha) {
        this.bottomTextAlpha = alpha;
    }

    public void setTickWidget(QuestionImage widget) {
        this.tickWidget = widget;
    }

    public void setCrossWidget(QuestionImage widget) {
        this.crossWidget = widget;
    }

    public @Nullable QuestionImage getTickWidget() {
        return this.tickWidget;
    }

    public @Nullable QuestionImage getCrossWidget() {
        return this.crossWidget;
    }

    @Override
    public float getAlpha() {
        return this.alpha;
    }

    public enum AnswerState {
        UNANSWERED,
        ANSWERED,
        CORRECT,
        INCORRECT
    }
}
