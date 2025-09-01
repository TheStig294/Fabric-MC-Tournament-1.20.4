package net.thestig294.mctournament.minigame.tournamentbegin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.thestig294.mctournament.minigame.Minigame;

/**
 * A special minigame that gets automatically played at the start of every tournament
 */
public class TournamentBegin extends Minigame {
    @Override
    public String getID() {
        return "tournament_begin";
    }

    @Override
    public float getScoreMultiplier() {
        return 1.0f;
    }

    @Override
    public boolean ignoreTeamCaptainScoreTransfer() {
        return false;
    }

    @Override
    public void serverInit() {

    }

    @Override
    public void serverBegin() {

    }

    @Override
    public void serverPreEnd() {

    }

    @Override
    public void serverEnd() {

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
    public void clientEnd() {

    }
}
