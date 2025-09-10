package net.thestig294.mctournament.item.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.MinigameVariants;
import net.thestig294.mctournament.minigame.Minigames;
import net.thestig294.mctournament.tournament.Tournament;
import net.thestig294.mctournament.tournament.TournamentSettings;

public class RemoteItem extends Item {
    public RemoteItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        if (!world.isClient()) {
            Tournament.inst().serverSetup(new TournamentSettings()
                    .minigames(Minigames.TRIVIA_MURDER_PARTY)
                    .variants(MinigameVariants.DEFAULT)
            );
        }

        return TypedActionResult.success(itemStack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!context.getWorld().isClient()) {
            MCTournament.server().getPlayerManager().broadcast(Text.literal("Round ended!"), true);
            Tournament.inst().endCurrentMinigame(false);
        }
        return super.useOnBlock(context);
    }
}
