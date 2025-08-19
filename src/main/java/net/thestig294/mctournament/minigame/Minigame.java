package net.thestig294.mctournament.minigame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.thestig294.mctournament.MCTournament;

import java.util.Objects;

public abstract class Minigame {
    private String variant = MinigameVariants.DEFAULT;
    private Boolean variantSet = false;
    private MinigameScoreboard scoreboard;
    private MinigameScoreboard clientScoreboard;

    public abstract String getID();

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

    /**
     * Called just before serverEnd() at the end of the round.
     * Mostly here as a reminder to manipulate the scoreboard so it's ready for the next minigame
     */
    public abstract void translateScores();

    public abstract void serverEnd();

    @Environment(EnvType.CLIENT)
    public void clientPreInit() {
        this.clientScoreboard = new MinigameScoreboard(this, true);
    }

    @Environment(EnvType.CLIENT)
    public abstract void clientInit();

    @Environment(EnvType.CLIENT)
    public void clientPreBegin() {
        this.clientScoreboard.clientBegin();
    }

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

    @SuppressWarnings("unused")
    public boolean isVariant(String variant) {
        if (!this.variantSet) {
            MCTournament.LOGGER.error("""
                    Trying to call Minigame.isVariant() of {} before the variant is set!
                    This is not set until serverBegin()/clientBegin() is called!
                    Returning false.
                    """, this.getName().getString());
            return false;
        }
        return Objects.equals(this.variant, variant);
    }

    @Override
    public String toString() {
        return this.getName().getString();
    }

    public MinigameScoreboard scoreboard() {
        return this.scoreboard;
    }

    @Environment(EnvType.CLIENT)
    public MinigameScoreboard clientScoreboard() {
        return this.clientScoreboard;
    }
}
