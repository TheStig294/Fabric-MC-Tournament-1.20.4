package net.thestig294.mctournament.minigame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.tournament.Tournament;

public abstract class Minigame {
    private String variant = MinigameVariants.DEFAULT;
    private Boolean variantSet = false;
    private MinigameScoreboard scoreboard;
    private MinigameScoreboard clientScoreboard;
    private BlockPos position;

    public abstract String getID();

    /**
     * Used for translating a minigame's player scores to a scaled value suitable for adding to the player's
     * overall tournament score
     * @return Multiplier int for translating a minigame's score to a tournament score
     */
    public abstract float getScoreMultiplier();

    public abstract boolean ignoreTeamCaptainScoreTransfer();

    public void serverPreInit() {
        this.scoreboard = new MinigameScoreboard(this, false);
    }

    public abstract void serverInit();

    public void serverPreBegin() {
        this.scoreboard.serverBegin();
    }

    /**
     * Called after a small delay after the last round ends to allow for packets to be sent between the server and client.
     * Also called after a player joins mid-round.
     */
    public abstract void serverBegin();

    public void serverPreEnd() {
        Tournament.inst().scoreboard().updateOverallScores();
        this.scoreboard.clear();
    }

    public abstract void serverEnd();

    @Environment(EnvType.CLIENT)
    public void clientPreInit() {
        this.clientScoreboard = new MinigameScoreboard(this, true);
    }

    @Environment(EnvType.CLIENT)
    public abstract void clientInit();

    /**
     * Called after a small delay after the last round ends to allow for packets to be sent between the server and client.
     * Also called after a player joins mid-round.
     */
    @Environment(EnvType.CLIENT)
    public abstract void clientBegin();

    @Environment(EnvType.CLIENT)
    public abstract void clientEnd();

    public Text getName() {
        return Text.translatable("minigame." + this.getID() + ".name");
    }

    public void setVariant(String variant) {
        this.variant = variant;
        this.variantSet = true;
    }

    public String getVariant() {
        if (!this.variantSet) {
            MCTournament.LOGGER.error("""
                    Trying to call Minigame.getVariant() of {} before the variant is set!
                    This is not set until serverBegin()/clientBegin() is called!
                    Returning default variant.
                    """, this.getName().getString());
            return MinigameVariants.DEFAULT;
        }
        return this.variant;
    }

    @Override
    public String toString() {
        return this.getName().getString();
    }

    public MinigameScoreboard scoreboard() {
        return this.scoreboard;
    }

    @SuppressWarnings("unused")
    @Environment(EnvType.CLIENT)
    public MinigameScoreboard clientScoreboard() {
        return this.clientScoreboard;
    }

    public void hideNametags() {
        Tournament.inst().scoreboard().setGlobalNametagVisibility(false);
    }

    public void setPosition(BlockPos pos) {
        this.position = pos;
    }

    public BlockPos getPosition() {
        return this.position;
    }
}
