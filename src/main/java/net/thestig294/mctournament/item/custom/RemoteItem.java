package net.thestig294.mctournament.item.custom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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

        if (world.isClient()) {
            this.openQuestionScreen();
        }

        return TypedActionResult.success(itemStack);
    }

    @Environment(EnvType.CLIENT)
    private void openQuestionScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.setScreen(new QuestionScreen(Text.translatable("screen.mctournament.question"), client.currentScreen));
    }
}
