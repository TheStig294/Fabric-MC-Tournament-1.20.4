package net.thestig294.mctournament.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

public class QuestionScreen extends Screen {

    public QuestionScreen(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();

        GridWidget grid = new GridWidget().setColumnSpacing(10);
        GridWidget.Adder adder = grid.createAdder(2);

        for (int i = 1; i <= 4; i++) {
            int finalI = i;
            ButtonWidget button = ButtonWidget.builder(Text.translatable("widget.mctournament.question.button" + i),
                    but -> this.clickAnswer(finalI)).build();
            adder.add(button);
            this.addDrawableChild(button);
        }

        grid.forEachChild(child -> {
            child.setNavigationOrder(1);
            this.addDrawableChild(child);
        });
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }

    private void clickAnswer(int answer) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player != null) {
            player.sendMessage(Text.literal("You answered question: " + answer), true);
        }

        client.setScreen(null);
    }
}
