package net.thestig294.mctournament.minigame.triviamurderparty.widget;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.minigame.triviamurderparty.question.Question;
import net.thestig294.mctournament.minigame.triviamurderparty.screen.QuestionScreen;
import net.thestig294.mctournament.network.ModNetworking;
import net.thestig294.mctournament.tournament.Tournament;
import net.thestig294.mctournament.util.ModColors;

public class QuestionButton extends PressableWidget {
    private final QuestionScreen screen;
    private final int answerNumber;
    private final TextRenderer textRenderer;
    private final boolean isCorrect;
    private final int originalX;
    private final int originalY;
    private boolean locked;
    private boolean pressed;

    public QuestionButton(QuestionScreen screen, int x, int y, int width, int height, TextRenderer textRenderer,
                          int answerNumber, Question question) {
        super(x, y, width, height, Text.literal(question.getAnswer(answerNumber))
                .styled(style -> style.withFont(TriviaMurderParty.Fonts.QUESTION_ANSWER)));

        this.screen = screen;
        this.textRenderer = textRenderer;
        this.answerNumber = answerNumber;
        this.isCorrect = question.isCorrect(answerNumber);
        this.originalX = x;
        this.originalY = y;
        this.locked = false;
        this.pressed = false;
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        if (this.locked) return;
        this.pressed = false;
        ClientPlayerEntity player = MCTournament.CLIENT.player;
        if (player == null || !Tournament.inst().clientScoreboard().isTeamCaptain(player)) return;

        ModNetworking.sendToServer(TriviaMurderParty.NetworkIDs.QUESTION_ANSWERED, PacketByteBufs.create()
                .writeString(player.getNameForScoreboard())
                .writeBoolean(this.isCorrect)
        );

        this.screen.lockButtons();
    }

    @Override
    public void onPress() {
        if (this.locked) return;
        this.pressed = true;
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        if (!this.locked) super.playDownSound(soundManager);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        context.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(),
                0, this.getColor());
        this.drawMessage(context, this.textRenderer, ModColors.WHITE.getRGB());

        ClickableWidget.drawScrollableText(context, this.textRenderer, Text.literal(Integer.toString(this.answerNumber))
                        .styled(style -> style
                        .withFont(TriviaMurderParty.Fonts.QUESTION_NUMBER)),
                this.getX() - this.getWidth(), this.getY() - this.getHeight(),
                this.getX() + this.getWidth(), this.getY() + this.getHeight(), ModColors.YELLOW.getRGB());
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
//        Not going to bother with the narrator... Sorry screen-reader users!
    }

    public int getOriginalX() {
        return this.originalX;
    }

    public int getOriginalY() {
        return this.originalY;
    }

    public void lock() {
        this.locked = true;
    }

    public int getColor() {
        if (this.locked) {
            return ModColors.BLACK.getRGB();
        } else if (this.pressed) {
            return ModColors.WHITE.getRGB();
        } else if (this.isHovered()) {
            return ModColors.GREY.getRGB();
        } else {
            return ModColors.BLACK.getRGB();
        }
    }
}
