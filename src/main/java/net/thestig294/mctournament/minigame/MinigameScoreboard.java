package net.thestig294.mctournament.minigame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.scoreboard.*;
import net.minecraft.text.Text;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.tournament.Tournament;
import net.thestig294.mctournament.tournament.TournamentScoreboard;
import org.jetbrains.annotations.Nullable;

public class MinigameScoreboard {
    private final Minigame minigame;
    private final Scoreboard scoreboard;

    public MinigameScoreboard(Minigame minigame, boolean isClient) {
        Tournament tournament = Tournament.inst();
        this.minigame = minigame;
        this.scoreboard = isClient ? tournament.clientScoreboard().getScoreboard() : tournament.scoreboard().getScoreboard();
    }

    public void serverBegin() {
        this.resetObjective(this.getObjectivePrefix(), this.getMainObjectiveDisplayName());
    }

    public String getObjectivePrefix() {
        return MCTournament.MOD_ID + ':' + this.minigame.getID() + ':';
    }

    public String getMainObjectiveDisplayName() {
        return this.minigame.getName().getString();
    }

    /**
     * Gets a nullable objective from the supplied basic name string,
     * without the minigame's scoreboard objective name prefix
     * @param name objective name
     * @return {@link ScoreboardObjective} from non-prefixed score name
     */
    public @Nullable ScoreboardObjective getObjective(String name) {
        return this.scoreboard.getNullableObjective(this.getObjectivePrefix() + name);
    }

    public ScoreboardObjective getOrCreateObjective(String name) {
        return this.getOrCreateObjective(name, name);
    }

    public ScoreboardObjective getOrCreateObjective(String name, String displayName) {
        ScoreboardObjective objective = this.getObjective(name);
        if (objective == null) {
            return this.addObjective(name, displayName);
        } else {
            return objective;
        }
    }

    public ScoreboardObjective addObjective(String name, String displayName) {
        return this.scoreboard.addObjective(this.getObjectivePrefix() + name, ScoreboardCriterion.DUMMY,
                Text.literal(displayName), ScoreboardCriterion.RenderType.INTEGER,
                false, null);
    }

    public void removeObjective(String name) {
        ScoreboardObjective objective = this.getObjective(name);
        if (objective != null) this.scoreboard.removeObjective(objective);
    }

    public void resetObjective(String name, String displayName) {
        this.removeObjective(name);
        this.addObjective(name, displayName);
    }

    // Score functions that do not specify a name use the minigame's "main" objective instead,
    // that has a blank name containing only the scoreboard objective prefix
    public int getScore(String playerName) {
        return this.getScore(playerName, "");
    }

    public int getScore(String playerName, String objectiveName) {
        ScoreboardObjective objective = this.getOrCreateObjective(objectiveName);
        return this.scoreboard.getOrCreateScore(ScoreHolder.fromName(playerName), objective).getScore();
    }

    public void setScore(String playerName, int score) {
        this.setScore(playerName, score, "");
    }

    public void setScore(String playerName, int score, String objectiveName) {
        ScoreboardObjective objective = this.getOrCreateObjective(objectiveName);
        this.scoreboard.getOrCreateScore(ScoreHolder.fromName(playerName), objective).setScore(score);
    }

    public void addScore(String playerName, int score) {
        this.addScore(playerName, score, "");
    }

    public void addScore(String playerName, int score, String objectiveName) {
        this.setScore(playerName, this.getScore(playerName) + score, objectiveName);
    }

    public void submitToTournamentScoreboard() {
        ScoreboardObjective tournamentObjective = this.scoreboard.getNullableObjective(TournamentScoreboard.OBJECTIVE_NAME);
        if (tournamentObjective == null) return;

        TournamentScoreboard tournamentScoreboard = Tournament.inst().scoreboard();
        float multiplier = this.minigame.getScoreMultiplier();

        for (int i = 0; i < TournamentScoreboard.MAX_TEAMS; i++) {
            Team team = tournamentScoreboard.getTeam(i);
            if (team == null) continue;

            for (final var playerName : team.getPlayerList()) {
                int minigameScore = (int) (this.getScore(playerName) * multiplier);
                this.scoreboard.getOrCreateScore(ScoreHolder.fromName(playerName), tournamentObjective).incrementScore(minigameScore);
            }
        }
    }

    public void clear() {
        this.scoreboard.getObjectives().stream()
                .filter(objective -> objective.getName().startsWith(this.getObjectivePrefix()))
                .forEach(this.scoreboard::removeObjective);
    }
}
