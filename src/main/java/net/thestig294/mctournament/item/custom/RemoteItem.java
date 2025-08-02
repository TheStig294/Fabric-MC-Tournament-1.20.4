package net.thestig294.mctournament.item.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.thestig294.mctournament.minigame.Minigames;
import net.thestig294.mctournament.tournament.Tournament;
import net.thestig294.mctournament.tournament.TournamentSettings;

import java.util.List;

public class RemoteItem extends Item {
    public RemoteItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

//        if (!world.isClient()) {
            Tournament tournament = Tournament.getInstance();
            tournament.setup(new TournamentSettings().minigames(List.of(Minigames.TRIVIA_MURDER_PARTY)), world.isClient());
//        }

        return TypedActionResult.success(itemStack);
    }
}
