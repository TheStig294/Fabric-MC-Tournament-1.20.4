package net.thestig294.mctournament.mixin;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Scoreboard.class)
public class ScoreboardMixin {
    @Shadow
    @Final
    private Object2ObjectMap<String, ScoreboardObjective> objectives;

    @Inject(method = "addObjective", at = @At("HEAD"), cancellable = true)
    void onAddObjective(String name, ScoreboardCriterion criterion, Text displayName, ScoreboardCriterion.RenderType renderType,
                        boolean displayAutoUpdate, NumberFormat numberFormat, CallbackInfoReturnable<ScoreboardObjective> cir){
        if (this.objectives.containsKey(name)) cir.cancel();
    }
}
