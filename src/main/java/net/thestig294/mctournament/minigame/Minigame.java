package net.thestig294.mctournament.minigame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

public abstract class Minigame {
    private final MinigameScoreboard scoreboard;

    public Minigame() {
        this.scoreboard = new MinigameScoreboard();
    }

    public abstract Text getName();

    public abstract void serverInit();

    public abstract void serverBegin();

    public abstract void serverEnd();

    @Environment(EnvType.CLIENT)
    public abstract void clientInit();

    @Environment(EnvType.CLIENT)
    public abstract void clientBegin();

    @Environment(EnvType.CLIENT)
    public abstract void clientEnd();

    public MinigameScoreboard getScoreboard() {
        return this.scoreboard;
    }
}
