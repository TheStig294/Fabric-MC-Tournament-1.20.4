package net.thestig294.mctournament.minigame.minigames;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.thestig294.mctournament.minigame.Minigame;

// A special minigame that gets automatically played at the end of every tournament
public class TournamentEnd extends Minigame {
    @Override
    public Text getName() {
        return Text.translatable("minigame.tournament_end.name");
    }

    @Override
    public void sharedInit() {

    }

    @Override
    public void sharedBegin() {

    }

    @Override
    public void sharedCleanup() {

    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientInit() {

    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientBegin() {

    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientCleanup() {

    }
}
