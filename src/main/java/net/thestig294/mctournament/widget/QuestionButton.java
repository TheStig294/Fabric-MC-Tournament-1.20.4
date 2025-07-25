package net.thestig294.mctournament.widget;

import net.minecraft.client.MinecraftClient;
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

public class QuestionButton extends PressableWidget {
    private final int questionNumber;
    private final Screen screen;

    public QuestionButton(Screen screen, int x, int y, int width, int height, int questionNumber, String text) {
        super(x, y, width, height, Text.literal(text).styled(style -> style.withFont(ModFonts.QUESTION_ANSWER)));

        this.questionNumber = questionNumber;
        this.screen = screen;
    }

    @Override
    public void onPress() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (player != null) {
            player.sendMessage(Text.literal("You chose answer: " + this.questionNumber), true);
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK, 1.0f));
            this.screen.close();
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();

        context.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0, this.isHovered() ? Colors.GRAY : Colors.BLACK);
        this.drawMessage(context, client.textRenderer, Colors.WHITE);

        ClickableWidget.drawScrollableText(context, client.textRenderer, Text.literal(Integer.toString(this.questionNumber)).styled(style -> style
                        .withFont(ModFonts.QUESTION_NUMBER)),
                this.getX() - this.getWidth(), this.getY() - this.getHeight(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), Colors.YELLOW);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
//        Not going to bother with the narrator... Sorry screen-reader users!
    }
}
