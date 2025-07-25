package net.thestig294.mctournament.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
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

        this.addDrawableChild(new QuestionText(this,this.width / 10, this.height / 3, "Approximately how many bridges are there in Venice?", this.textRenderer));

        this.addDrawableChild(new QuestionButton(this, this.width * 2/3, this.height / 2, 120, 20, 1, "50"));
        this.addDrawableChild(new QuestionButton(this, this.width * 2/3, this.height / 2 + 30, 120, 20, 2, "100"));
        this.addDrawableChild(new QuestionButton(this, this.width * 2/3, this.height / 2 + 60, 120, 20, 3, "200"));
        this.addDrawableChild(new QuestionButton(this, this.width * 2/3, this.height / 2 + 90, 120, 20, 4, "400"));
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
