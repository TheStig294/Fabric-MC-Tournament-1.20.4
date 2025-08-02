package net.thestig294.mctournament.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.thestig294.mctournament.font.ModFonts;
import net.thestig294.mctournament.util.ModUtil;
import net.thestig294.mctournament.widget.QuestionButton;
import net.thestig294.mctournament.widget.QuestionPlayer;
import net.thestig294.mctournament.widget.QuestionText;
import net.thestig294.mctournament.widget.QuestionTimer;

import java.awt.*;
import java.util.List;

public class QuestionScreen extends Screen {
    private final Screen parent;

    public QuestionScreen(Text title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) {
            return;
        }
        List<? extends PlayerEntity> players = world.getPlayers();

        this.addDrawableChild(new QuestionText(this.width / 2, this.height / 2, "1", ModFonts.QUESTION_NUMBER, 20, Color.YELLOW,this.width / 2, this.textRenderer));

        for (int i = 0; i < players.size(); i++) {
            PlayerEntity player = players.get(i);
            player.setCustomNameVisible(false);
            this.addDrawableChild(new QuestionPlayer(i * this.width / 8, 5, this.width / 8, this.height / 3, player));
        }

        this.addDrawableChild(new QuestionText(this.width / 30, this.height / 3,
                "In what Daniel Day-Lewis film does he say the line “Stay alive! No matter what occurs. I will find you!”?",
                ModFonts.QUESTION,25, Color.WHITE, this.width * 3 / 5, this.textRenderer));

        this.addDrawableChild(new QuestionButton(this, this.width * 2/3, this.height / 2 - 30, 140, 20, 1, "There Will Be Blood", false));
        this.addDrawableChild(new QuestionButton(this, this.width * 2/3, this.height / 2, 140, 20, 2, "In the Name of the Father", false));
        this.addDrawableChild(new QuestionButton(this, this.width * 2/3, this.height / 2 + 30, 140, 20, 3, "My Beautiful Laundrette", false));
        this.addDrawableChild(new QuestionButton(this, this.width * 2/3, this.height / 2 + 60, 140, 20, 4, "The Last of the Mohicans", true));

        this.addDrawableChild(new QuestionText(5, this.height - 65,
                "ANSWER\nNOW!", ModFonts.QUESTION_ANSWER, 15, Color.RED, 100, this.textRenderer));
        this.addDrawableChild(new QuestionText( 15, this.height - 40,
                "0", ModFonts.QUESTION_NUMBER, 20, Color.ORANGE, 100, this.textRenderer));
        this.addDrawableChild(new QuestionText( 0, this.height - 15,
                "ANSWERED", ModFonts.QUESTION_ANSWER, 10, Color.GRAY, 100, this.textRenderer));

        this.addDrawableChild(new QuestionTimer(this.width / 3, this.height - 64, 64, 64, 20, 1.0f, 0));

        this.addDrawableChild(new QuestionText( this.width - 75, this.height - 30,
                "MINECRAFT.TV", ModFonts.QUESTION_ANSWER, 10, Color.GRAY, 100, this.textRenderer));
        this.addDrawableChild(new QuestionText( this.width - 40, this.height - 15,
                ModUtil.getRandomString(4, 2), ModFonts.QUESTION_ANSWER, 10, Color.RED, 100, this.textRenderer));
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
