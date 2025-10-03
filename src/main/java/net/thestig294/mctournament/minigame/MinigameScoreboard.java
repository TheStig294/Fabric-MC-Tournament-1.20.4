package net.thestig294.mctournament.minigame;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.*;
import net.minecraft.text.Text;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.tournament.Teams;
import net.thestig294.mctournament.tournament.Tournament;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class MinigameScoreboard {
    public static final String MAIN_OBJECTIVE_NAME = "main_score";

    private final Minigame minigame;
    private final boolean isClient;
    private Scoreboard scoreboard;
    private final Teams teams;

    public MinigameScoreboard(Minigame minigame, boolean isClient) {
        this.minigame = minigame;
        this.isClient = isClient;
        this.teams = isClient ? Tournament.inst().clientTeams() : Tournament.inst().teams();
    }

    private boolean initScoreboard() {
        if (this.scoreboard != null) return false;

        if (this.isClient) {
            if (MCTournament.client().world == null) return true;
            this.scoreboard = Objects.requireNonNull(MCTournament.client().world).getScoreboard();
        } else {
            this.scoreboard = MCTournament.server().getScoreboard();
        }

        return false;
    }

    public void begin() {
        if (this.initScoreboard()) return;

        if (!this.isClient) this.clear();
    }

    public String getObjectivePrefix() {
        return this.minigame.getID() + '_';
    }

    /**
     * Gets a nullable objective from the supplied basic name string,
     * without the minigame's teams objective name prefix
     * @param name objective name
     * @return {@link ScoreboardObjective} from non-prefixed score name
     */
    public @Nullable ScoreboardObjective getObjective(String name) {
        if (this.initScoreboard()) return null;

        return this.scoreboard.getNullableObjective(this.getObjectivePrefix() + name);
    }

    public @Nullable ScoreboardObjective getOrCreateObjective(String name) {
        return this.getOrCreateObjective(name, name);
    }

    public @Nullable ScoreboardObjective getOrCreateObjective(String name, String displayName) {
        ScoreboardObjective objective = this.getObjective(name);
        if (objective == null) {
            return this.addObjective(name, displayName);
        } else {
            return objective;
        }
    }

    public @Nullable ScoreboardObjective addObjective(String name, String displayName) {
        if (this.initScoreboard()) return null;

        return this.scoreboard.addObjective(this.getObjectivePrefix() + name, ScoreboardCriterion.DUMMY,
                Text.literal(displayName), ScoreboardCriterion.RenderType.INTEGER,
                false, null);
    }

    public int getScore(Team team) {
        PlayerEntity captain = this.teams.getTeamCaptain(team);
        if (captain == null) return 0;
        return this.getScore(captain);
    }

    public int getScore(PlayerEntity player) {
        return this.getScore(player.getNameForScoreboard());
    }

    /**
     * Score functions that do not specify a name use the minigame's "main" objective instead,
     * that has a blank name containing only the teams objective prefix. <p>
     * The "main" objective is the one used to translate a player's score to their overall tournament score at the end of a round
     * @param playerName Player's teams-safe name string (See: {@link PlayerEntity#getNameForScoreboard()})
     * @return Player's overall minigame score, or creates a score and returns 0 if it doesn't exist
     */
    public int getScore(String playerName) {
        return this.getScore(playerName, MAIN_OBJECTIVE_NAME);
    }

    public boolean getBoolean(PlayerEntity player, String objectiveName) {
        return this.getBoolean(player.getNameForScoreboard(), objectiveName);
    }

    public boolean getBoolean(String playerName, String objectiveName) {
        return this.getScore(playerName, objectiveName) != 0;
    }

    public int getScore(String playerName, String objectiveName) {
        if (this.initScoreboard()) return 0;

        ScoreboardObjective objective = this.getOrCreateObjective(objectiveName);
        return this.scoreboard.getOrCreateScore(ScoreHolder.fromName(playerName), objective).getScore();
    }

    public void setBoolean(PlayerEntity player, String objectiveName, boolean value) {
        this.setBoolean(player.getNameForScoreboard(), objectiveName, value);
    }

    public void setBoolean(String playerName, String objectiveName, boolean value) {
        this.setScore(playerName, objectiveName, value ? 1 : 0);
    }

    @SuppressWarnings("unused")
    public void setScore(Team team, int score) {
        PlayerEntity captain = this.teams.getTeamCaptain(team);
        if (captain == null) return;
        this.setScore(captain, score);
    }

    public void setScore(PlayerEntity player, int score) {
        this.setScore(player.getNameForScoreboard(), score);
    }

    public void setScore(String playerName, int score) {
        this.setScore(playerName, MAIN_OBJECTIVE_NAME, score);
    }

    public void setScore(String playerName, String objectiveName, int score) {
        if (this.initScoreboard()) return;

        ScoreboardObjective objective = this.getOrCreateObjective(objectiveName);
        this.scoreboard.getOrCreateScore(ScoreHolder.fromName(playerName), objective).setScore(score);
    }

    public void addScore(Team team, int score) {
        PlayerEntity captain = this.teams.getTeamCaptain(team);
        if (captain == null) return;
        this.addScore(captain, score);
    }

    public void addScore(PlayerEntity player, int score) {
        this.addScore(player.getNameForScoreboard(), score);
    }

    public void addScore(String playerName, int score) {
        this.addScore(playerName, MAIN_OBJECTIVE_NAME, score);
    }

    public void addScore(String playerName, String objectiveName, int score) {
        this.setScore(playerName, objectiveName, this.getScore(playerName) + score);
    }

    public void clear() {
        if (this.initScoreboard()) return;

        this.scoreboard.getObjectives().stream()
                .filter(objective -> objective.getName().startsWith(this.getObjectivePrefix()))
                .forEach(this.scoreboard::removeObjective);
    }

    public float getScoreMultiplier() {
        return this.minigame.getScoreMultiplier();
    }

    public void setDisplay(ScoreboardDisplaySlot slot) {
        this.setDisplay(MAIN_OBJECTIVE_NAME, slot);
    }

    public void setDisplay(String objectiveName, ScoreboardDisplaySlot slot) {
        ScoreboardObjective objective = this.getObjective(objectiveName);
        if (objective == null) return;
        this.scoreboard.setObjectiveSlot(slot, objective);
    }

    @SuppressWarnings("unused")
    public void clearDisplay(ScoreboardDisplaySlot slot) {
        this.scoreboard.setObjectiveSlot(slot, null);
    }

    public @Nullable Scoreboard getScoreboard() {
        this.initScoreboard();
        return this.scoreboard;
    }

    @SuppressWarnings("unused")
    public ServerScoreboard getServerScoreboard() {
        return MCTournament.server().getScoreboard();
    }
}
