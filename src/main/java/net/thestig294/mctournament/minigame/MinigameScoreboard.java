package net.thestig294.mctournament.minigame;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.*;
import net.minecraft.text.Text;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.tournament.Tournament;
import net.thestig294.mctournament.tournament.TournamentScoreboard;
import org.jetbrains.annotations.Nullable;

public class MinigameScoreboard {
    private final Minigame minigame;
    private final boolean isClient;
    private Scoreboard scoreboard;

    public MinigameScoreboard(Minigame minigame, boolean isClient) {
        this.minigame = minigame;
        this.isClient = isClient;
    }

    public void serverBegin() {
        Tournament tournament = Tournament.inst();
        TournamentScoreboard tournamentScoreboard = this.isClient ? tournament.clientScoreboard() : tournament.scoreboard();
        if (tournamentScoreboard == null) return;

        this.scoreboard = tournamentScoreboard.getScoreboard();
        this.resetObjective(this.getObjectivePrefix(), this.getMainObjectiveDisplayName());
    }

    public String getObjectivePrefix() {
        return MCTournament.MOD_ID + ':' + this.minigame.getID() + '_';
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

    /**
     * Score functions that do not specify a name use the minigame's "main" objective instead,
     * that has a blank name containing only the scoreboard objective prefix. <p>
     * The "main" objective is the one used to translate a player's score to their overall tournament score at the end of a round
     * @param playerName Player's scoreboard-safe name string (See: {@link PlayerEntity#getNameForScoreboard()})
     * @return Player's overall minigame score, or creates a score and returns 0 if it doesn't exist
     */
    public int getScore(String playerName) {
        return this.getScore(playerName, "");
    }

    public int getScore(PlayerEntity player, String objectiveName) {
        return this.getScore(player.getNameForScoreboard(), objectiveName);
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

    public void clear() {
        this.scoreboard.getObjectives().stream()
                .filter(objective -> objective.getName().startsWith(this.getObjectivePrefix()))
                .forEach(this.scoreboard::removeObjective);
    }

    public float getScoreMultiplier() {
        return this.minigame.getScoreMultiplier();
    }
}
