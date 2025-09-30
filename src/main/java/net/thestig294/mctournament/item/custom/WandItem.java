package net.thestig294.mctournament.item.custom;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.minigame.triviamurderparty.screen.KillingRoomScreen;
import net.thestig294.mctournament.structure.ModStructures;

public class WandItem extends Item {
    public WandItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient()) {
            ModStructures.jigsawPlace(TriviaMurderParty.Structures.CORRIDOR, player);
        }

        return super.use(world, player, hand);
    }

    @SuppressWarnings("unused")
    public ActionResult onAttackBlock(PlayerEntity player, World world) {
        if (world.isClient()) {
            MCTournament.client().setScreen(new KillingRoomScreen(KillingRoomScreen.State.TIMER_IN, "tattoos", 1));
        }

        return ActionResult.PASS;
    }

    public static void init() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (player.getStackInHand(hand).getItem() instanceof WandItem wand){
                return wand.onAttackBlock(player, world);
            } else {
                return ActionResult.PASS;
            }
        });
    }
}
