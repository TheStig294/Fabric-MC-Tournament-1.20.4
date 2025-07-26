package net.thestig294.mctournament.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.thestig294.mctournament.font.ModFonts;
import net.thestig294.mctournament.util.ModUtil;
import net.thestig294.mctournament.widget.QuestionButton;
import net.thestig294.mctournament.widget.QuestionText;

public class QuestionScreen extends Screen {
    private final Screen parent;

    public QuestionScreen(Text title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        this.addDrawableChild(new QuestionText(this,this.width / 10, this.height / 3,
                "Approximately how many bridges are there in Venice?", ModFonts.QUESTION,20, Colors.WHITE, this.textRenderer));

        this.addDrawableChild(new QuestionButton(this, this.width * 2/3, this.height / 2 - 30, 120, 20, 1, "50", false));
        this.addDrawableChild(new QuestionButton(this, this.width * 2/3, this.height / 2, 120, 20, 2, "100", false));
        this.addDrawableChild(new QuestionButton(this, this.width * 2/3, this.height / 2 + 30, 120, 20, 3, "200", false));
        this.addDrawableChild(new QuestionButton(this, this.width * 2/3, this.height / 2 + 60, 120, 20, 4, "400", true));

        this.addDrawableChild(new QuestionText(this,10, this.height - 75,
                "ANSWER\nNOW!", ModFonts.QUESTION_ANSWER, 15, Colors.RED, this.textRenderer));
        this.addDrawableChild(new QuestionText(this, 25, this.height - 50,
                "0", ModFonts.QUESTION_NUMBER, 20, Colors.LIGHT_RED, this.textRenderer));
        this.addDrawableChild(new QuestionText(this, 0, this.height - 15,
                "ANSWERED", ModFonts.QUESTION_ANSWER, 10, Colors.GRAY, this.textRenderer));

        this.addDrawableChild(new QuestionText(this, this.width - 100, this.height - 30,
                "MINECRAFT.TV", ModFonts.QUESTION_ANSWER, 10, Colors.GRAY, this.textRenderer));
        this.addDrawableChild(new QuestionText(this, this.width - 50, this.height - 15,
                ModUtil.getRandomString(4, 2), ModFonts.QUESTION_ANSWER, 10, Colors.RED, this.textRenderer));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(this.parent);
    }
}
