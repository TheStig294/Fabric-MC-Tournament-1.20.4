package net.thestig294.mctournament.minigame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.tournament.Tournament;
import org.jetbrains.annotations.Nullable;

public class MinigameScoreboard {
    private final Minigame minigame;
    private final boolean isClient;
    private final Scoreboard scoreboard;
    private ScoreboardObjective mainScoreObjective;

    public MinigameScoreboard(Minigame minigame, boolean isClient) {
        Tournament tournament = Tournament.inst();
        this.minigame = minigame;
        this.isClient = isClient;
        this.scoreboard = this.isClient ? tournament.clientScoreboard().getScoreboard() : tournament.scoreboard().getScoreboard();
    }

    public void serverBegin() {
        this.mainScoreObjective = this.resetObjective(this.getMainObjectiveName(), this.getMainObjectiveDisplayName());
    }

    public String getMainObjectiveName() {
        return MCTournament.MOD_ID + ':' + this.minigame.getID();
    }

    public String getMainObjectiveDisplayName() {
        return this.minigame.getName().getString() + " score";
    }

    public @Nullable ScoreboardObjective getObjective(String name) {
        return this.scoreboard.getNullableObjective(name);
    }

    public ScoreboardObjective addObjective(String name, String displayName) {
        return this.scoreboard.addObjective(name, ScoreboardCriterion.DUMMY,
                Text.literal(displayName), ScoreboardCriterion.RenderType.INTEGER,
                false, null);
    }

    public void removeObjective(String name) {
        ScoreboardObjective objective = this.getObjective(name);
        if (objective != null) this.scoreboard.removeObjective(objective);
    }

    public ScoreboardObjective resetObjective(String name, String displayName) {
        this.removeObjective(name);
        return this.addObjective(name, displayName);
    }

    public int getScore(String playerName) {
        return this.scoreboard.getOrCreateScore(ScoreHolder.fromName(playerName), this.mainScoreObjective).getScore();
    }

    public int getScore(String playerName, String objectiveName) {

    }

    public void setScore(String playerName, int score, String objectiveName) {
        ScoreboardObjective objective = this.getObjective(objectiveName);
        if (objective == null) this.scoreboard.addObjective()
    }

    public void setScore(String playerName, int score) {
        this.scoreboard.getOrCreateScore(ScoreHolder.fromName(playerName), this.mainScoreObjective).setScore(score);
    }

    public void addScore(String playerName, int score) {
        this.setScore(playerName, this.getScore(playerName) + score);
    }

    @Environment(EnvType.CLIENT)
    public void clientBegin() {

    }
}
