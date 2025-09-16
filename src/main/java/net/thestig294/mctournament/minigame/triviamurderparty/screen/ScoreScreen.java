package net.thestig294.mctournament.minigame.triviamurderparty.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.thestig294.mctournament.MCTournament;

public class ScoreScreen extends Screen {
    private final Screen parent;

    protected ScoreScreen(Text title) {
        super(title);
        this.parent = MCTournament.client().currentScreen;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        MCTournament.client().setScreen(this.parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
