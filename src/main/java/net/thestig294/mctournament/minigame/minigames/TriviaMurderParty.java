package net.thestig294.mctournament.minigame.minigames;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.Minigame;
import net.thestig294.mctournament.network.ModNetworking;
import net.thestig294.mctournament.screen.QuestionScreen;

public class TriviaMurderParty extends Minigame {
    @Override
    public Text getName() {
        return Text.translatable("minigame.trivia_murder_party.name");
    }

    @Override
    public void serverInit() {
        MCTournament.LOGGER.info("Running server init");
    }

    @Override
    public void serverBegin() {
        MCTournament.LOGGER.info("Running server begin");
        ModNetworking.broadcast(ModNetworking.OPEN_QUESTION_SCREEN);
    }

    @Override
    public void serverEnd() {
        MCTournament.LOGGER.info("Running server end");
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientInit() {
        MCTournament.LOGGER.info("Running client init");
        ClientPlayNetworking.registerGlobalReceiver(ModNetworking.OPEN_QUESTION_SCREEN,
                (client, handler, buf, responseSender)
                        -> client.execute(() -> {
                            MCTournament.LOGGER.info("Client message received");
                            this.openQuestionScreen();
                }));
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientBegin() {
        MCTournament.LOGGER.info("Running client begin");
//        this.openQuestionScreen();
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientEnd() {
        MCTournament.LOGGER.info("Running client end");
    }

    @Environment(EnvType.CLIENT)
    private void openQuestionScreen() {
        MCTournament.CLIENT.setScreen(new QuestionScreen(Text.translatable("screen.mctournament.question"), MCTournament.CLIENT.currentScreen));
    }
}
