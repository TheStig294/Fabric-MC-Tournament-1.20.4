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

import java.awt.*;

public class QuestionPlayer extends ClickableWidget {
    private final PlayerEntity player;
    private final int originalY;
    private final TextRenderer textRenderer;
    private final Text playerNameText;
    private final Text teamCaptainText;
    private final boolean isCaptain;
    private AnswerState answerState;

    public QuestionPlayer(int x, int y, int width, int height, PlayerEntity player, TextRenderer textRenderer) {
        super(x, y, width, height, Text.empty());

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
        this.originalY = y;
        this.textRenderer = textRenderer;
        this.playerNameText = Text.literal(player.getNameForScoreboard())
                .styled(style -> style.withFont(TriviaMurderParty.Fonts.QUESTION_ANSWER));
        this.teamCaptainText = Text.translatable("widget.mctournament.question_player_team_captain")
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

        if (this.isCaptain) {
            context.drawCenteredTextWithShadow(this.textRenderer, this.teamCaptainText,
                    this.getX() + (this.getWidth() / 2), this.getY() + this.getHeight() - 5, this.getNameColor().getRGB());
        }

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
