package net.thestig294.mctournament.minigame.minigames;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.Minigame;
import net.thestig294.mctournament.network.ModNetworking;
import net.thestig294.mctournament.screen.QuestionScreen;
import net.thestig294.mctournament.util.ModUtil;

import java.util.List;

public class TriviaMurderParty extends Minigame {
    @Override
    public Text getName() {
        return Text.translatable("minigame.trivia_murder_party.name");
    }

    @Override
    public void sharedInit() {

    }

    @Override
    public void sharedBegin() {
        MCTournament.LOGGER.info("Running begin");
        if (!ModUtil.isClient()) {
            List<ServerPlayerEntity> players = ModUtil.SERVER.getPlayerManager().getPlayerList();
            for (final var player : players) {
                ServerPlayNetworking.send(player, ModNetworking.OPEN_QUESTION_SCREEN, PacketByteBufs.empty());
            }
        }
    }

    @Override
    public void sharedCleanup() {

    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientInit() {
        ClientPlayNetworking.registerGlobalReceiver(ModNetworking.OPEN_QUESTION_SCREEN,
                (client, handler, buf, responseSender)
                        -> client.execute(this::openQuestionScreen));
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientBegin() {
        MCTournament.LOGGER.info("Running client begin");
        this.openQuestionScreen();
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientCleanup() {

    }

    @Environment(EnvType.CLIENT)
    private void openQuestionScreen() {
        ModUtil.CLIENT.setScreen(new QuestionScreen(Text.translatable("screen.mctournament.question"), ModUtil.CLIENT.currentScreen));
    }
}
