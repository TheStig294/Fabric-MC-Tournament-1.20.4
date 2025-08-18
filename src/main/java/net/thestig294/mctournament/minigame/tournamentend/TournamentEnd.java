package net.thestig294.mctournament.minigame.tournamentend;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.Minigame;

/**
 * A special minigame that gets automatically played at the end of every tournament
 */
public class TournamentEnd extends Minigame {
    @Override
    public String getID() {
        return "tournament_end";
    }

    @Override
    public void serverInit() {

    }

    @Override
    public void serverBegin() {
        MCTournament.SERVER.getPlayerManager().broadcast(Text.translatable("tournament.mctournament.end_message"), true);
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

//    The tournament scoreboard is used directly instead
    @Override
    public void translateScores() {
    }
}
