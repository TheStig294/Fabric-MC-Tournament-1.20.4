package net.thestig294.mctournament.minigame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.thestig294.mctournament.MCTournament;

import java.util.Objects;

public abstract class Minigame {
    private String variant;
    private Boolean variantSet;

    public Minigame() {
        this.variant = MinigameVariants.DEFAULT;
        this.variantSet = false;
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

    public void setVariant(String variant) {
        this.variant = variant;
        this.variantSet = true;
    }

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
}
