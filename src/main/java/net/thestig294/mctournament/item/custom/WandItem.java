package net.thestig294.mctournament.item.custom;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.thestig294.mctournament.util.ModUtil;

import java.util.List;

@SuppressWarnings("unused")
public class WandItem extends Item {
    public WandItem(Settings settings) {
        super(settings);
    }

    private static BlockPos START;

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient()) {
            START = player.getBlockPos();
        }

        return super.use(world, player, hand);
    }

    public ActionResult onAttackBlock(PlayerEntity player, World world, BlockPos pos) {
        if (!world.isClient()) {

            if (START != null) {
                List<ServerPlayerEntity> players = ModUtil.getPlayersWithinBound(START, pos);
                StringBuilder stringBuilder = new StringBuilder();

                for (final var ply : players) {
                    stringBuilder.append(ply.getNameForScoreboard()).append(", ");
                }

                player.sendMessage(Text.literal(stringBuilder.toString()));
            }
        }

        return ActionResult.PASS;
    }

    public static void init() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (player.getStackInHand(hand).getItem() instanceof WandItem wand){
                return wand.onAttackBlock(player, world, pos);
            } else {
                return ActionResult.PASS;
            }
        });
    }
}
