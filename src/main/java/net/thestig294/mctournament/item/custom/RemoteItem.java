package net.thestig294.mctournament.item.custom;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.thestig294.mctournament.screen.QuestionScreen;

public class RemoteItem extends Item {
    public RemoteItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        
        user.playSound(SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.NEUTRAL, 1.0f, 1.0f);

        if (world.isClient()) {
            MinecraftClient client = MinecraftClient.getInstance();
            client.setScreen(new QuestionScreen(Text.translatable("screen.mctournament.question"), client.currentScreen));
        }

        return TypedActionResult.success(itemStack);
    }
}
