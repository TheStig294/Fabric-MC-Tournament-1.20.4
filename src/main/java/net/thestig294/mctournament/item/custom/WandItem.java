package net.thestig294.mctournament.item.custom;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.thestig294.mctournament.minigame.triviamurderparty.TriviaMurderParty;
import net.thestig294.mctournament.structure.ModStructures;

public class WandItem extends Item {
    public WandItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        return super.use(world, player, hand);
    }

    public ActionResult onAttackBlock(World world, PlayerEntity player) {
        if (!world.isClient()) {
            ModStructures.jigsawPlace(TriviaMurderParty.Structures.CORRIDOR, player.getBlockPos(), player.getYaw());
        }

        return ActionResult.PASS;
    }

    public static void init() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (player.getStackInHand(hand).getItem() instanceof WandItem wand){
                return wand.onAttackBlock(world, player);
            } else {
                return ActionResult.PASS;
            }
        });
    }
}
