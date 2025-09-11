package net.thestig294.mctournament;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.thestig294.mctournament.font.ModFonts;
import net.thestig294.mctournament.item.ModItemGroups;
import net.thestig294.mctournament.item.ModItems;
import net.thestig294.mctournament.item.custom.RemoteItem;
import net.thestig294.mctournament.item.custom.WandItem;
import net.thestig294.mctournament.minigame.Minigames;
import net.thestig294.mctournament.network.ModNetworking;
import net.thestig294.mctournament.structure.ModStructures;
import net.thestig294.mctournament.texture.ModTextures;
import net.thestig294.mctournament.tournament.Tournament;
import net.thestig294.mctournament.util.ModTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MCTournament implements ModInitializer {
	public static final String MOD_ID = "mctournament";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static MinecraftServer SERVER;
	private static MinecraftClient CLIENT;

	@Override
	public void onInitialize() {
		LOGGER.info("====Hello Server World!====");

		ServerLifecycleEvents.SERVER_STARTED.register(server -> SERVER = server);

		ModNetworking.registerNetworkIDs();

		ModItems.registerItems();
		ModItemGroups.registerItemGroups();
        RemoteItem.init();
        WandItem.init();

		ModFonts.registerFonts();
		ModTextures.registerTextures();
        ModStructures.registerStructures();

		Minigames.registerMinigames(false);
		Tournament.inst().serverInit();

        ModTimer.init(false);
	}

    public static MinecraftServer server() {
        if (SERVER == null) LOGGER.error("Server instance null, very likely trying to access the server instance on the client!");
        return SERVER;
    }

    public static MinecraftClient client() {
        if (CLIENT == null) LOGGER.error("Client instance null, very likely trying to access the client instance on the server!");
        return CLIENT;
    }

    public static void setClient(MinecraftClient client) {
        CLIENT = client;
    }
}