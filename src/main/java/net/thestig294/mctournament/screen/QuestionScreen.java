package net.thestig294.mctournament.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

public class QuestionScreen extends Screen {
    public Screen parent;

    public QuestionScreen(Text title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

//        Literally just derma from Gmod all over again lol (bugs included!)

        ButtonWidget button1 = ButtonWidget.builder(Text.translatable("widget.mctournament.question.button1"), but -> this.clickAnswer(1))
                .position(this.width / 2 - 100, this.height / 2 - 100)
                .build();
        ButtonWidget button2 = ButtonWidget.builder(Text.translatable("widget.mctournament.question.button2"), but -> this.clickAnswer(2))
                .position(this.width / 2 + 100, this.height / 2 - 100)
                .build();
        ButtonWidget button3 = ButtonWidget.builder(Text.translatable("widget.mctournament.question.button3"), but -> this.clickAnswer(3))
                .position(this.width / 2 - 100, this.height / 2 + 100)
                .build();
        ButtonWidget button4 = ButtonWidget.builder(Text.translatable("widget.mctournament.question.button4"), but -> this.clickAnswer(4))
                .position(this.width / 2 + 100, this.height / 2 + 100)
                .build();

        this.addDrawableChild(button1);
        this.addDrawableChild(button2);
        this.addDrawableChild(button3);
        this.addDrawableChild(button4);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(this.parent);
    }

    private void clickAnswer(int answer) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;


        if (player != null) {
            player.sendMessage(Text.literal("You answered question: " + answer), true);
        }

        this.close();
    }
}
