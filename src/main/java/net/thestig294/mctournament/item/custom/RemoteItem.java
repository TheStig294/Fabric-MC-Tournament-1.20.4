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
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.Minigames;
import net.thestig294.mctournament.screen.QuestionScreen;
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

//        if (world.isClient()) {
//            this.openQuestionScreen();
//        }
        if (!world.isClient()) {
            Tournament tournament = Tournament.getInstance();
//            tournament.setup(new TournamentSettings().minigames(List.of(Minigames.TRIVIA_MURDER_PARTY, Minigames.TOWERFALL, Minigames.MARIO_KART)));

            tournament.setup(new TournamentSettings().minigames(2));
            tournament.endRound();

            MCTournament.LOGGER.info(tournament.getMinigame().getName().getString());
            MCTournament.LOGGER.info(tournament.getScoreboard().toString());
        }

        return TypedActionResult.success(itemStack);
    }

    @Environment(EnvType.CLIENT)
    private void openQuestionScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.setScreen(new QuestionScreen(Text.translatable("screen.mctournament.question"), client.currentScreen));
    }
}
