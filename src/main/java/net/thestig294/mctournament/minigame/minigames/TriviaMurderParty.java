package net.thestig294.mctournament.minigame.minigames;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
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
        ModNetworking.serverReceive(ModNetworking.QUESTION_SCREEN_OPEN, serverReceiveInfo -> {
            String recievedString = serverReceiveInfo.buf().readString();
            String playerName = serverReceiveInfo.player().getName().getString();
            MCTournament.LOGGER.info("Received string from client: {} from player: {}", recievedString, playerName);
        });
    }

    @Override
    public void serverBegin() {
        MCTournament.LOGGER.info("Running server begin");
        ModNetworking.broadcast(ModNetworking.QUESTION_SCREEN_OPEN, PacketByteBufs.create().writeString("Networked string!!!"));
//        ModNetworking.send(ModNetworking.TOURNAMENT_END_ROUND, PacketByteBufs.create().writeString("Player-specific net string"),
//                MCTournament.SERVER.getPlayerManager().getPlayerList().get(0));
    }

    @Override
    public void serverEnd() {
        MCTournament.LOGGER.info("Running server end");
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientInit() {
        MCTournament.LOGGER.info("Running client init");
        ModNetworking.clientReceive(ModNetworking.QUESTION_SCREEN_OPEN, clientReceiveInfo -> {
            String networkedString = clientReceiveInfo.buffer().readString();
            MCTournament.LOGGER.info("Client message received: {}", networkedString);
            this.openQuestionScreen();
        });
//        ClientPlayNetworking.registerGlobalReceiver(ModNetworking.QUESTION_SCREEN_OPEN,
//                (client, handler, buf, responseSender)
//                        -> client.execute(() -> {
//                            MCTournament.LOGGER.info("Client message received");
//                            this.openQuestionScreen();
//                }));
//        ModNetworking.clientReceive(ModNetworking.TOURNAMENT_END_ROUND, clientReceiveInfo -> {
//            String netString = clientReceiveInfo.buffer().readString();
//            MCTournament.LOGGER.info("Received player-exclusive message: {}", netString);
//        });
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
        ModNetworking.sendToServer(ModNetworking.QUESTION_SCREEN_OPEN, PacketByteBufs.create().writeString("Client networked string!!!"));
    }
}
