package net.thestig294.mctournament.minigame.triviamurderparty.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.thestig294.mctournament.font.ModFonts;
import net.thestig294.mctournament.minigame.triviamurderparty.question.Question;

public class QuestionButton extends PressableWidget {
    private final int answerNumber;
    private final Screen screen;
    private final TextRenderer textRenderer;
    private final boolean isCorrect;

    public QuestionButton(Screen screen, int x, int y, int width, int height, TextRenderer textRenderer, int answerNumber, Question question) {
        super(x, y, width, height, Text.literal(question.getAnswer(answerNumber)).styled(style -> style.withFont(ModFonts.QUESTION_ANSWER)));

        this.screen = screen;
        this.textRenderer = textRenderer;
        this.answerNumber = answerNumber;
        this.isCorrect = question.isCorrect(answerNumber);
    }

    @Override
    public void onPress() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (player != null) {
            player.sendMessage(Text.literal("You chose answer: " + this.answerNumber), true);

            if (this.isCorrect()) {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.ambient(SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK));
            } else {
                this.screen.close();
            }
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        context.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0, this.isHovered() ? Colors.GRAY : Colors.BLACK);
        this.drawMessage(context, this.textRenderer, Colors.WHITE);

        ClickableWidget.drawScrollableText(context, this.textRenderer, Text.literal(Integer.toString(this.answerNumber)).styled(style -> style
                        .withFont(ModFonts.QUESTION_NUMBER)),
                this.getX() - this.getWidth(), this.getY() - this.getHeight(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), Colors.YELLOW);
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
//        Not going to bother with the narrator... Sorry screen-reader users!
    }

    public boolean isCorrect(){
        return this.isCorrect;
    }
}
