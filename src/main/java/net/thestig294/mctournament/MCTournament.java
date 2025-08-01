package net.thestig294.mctournament;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.minecraft.server.MinecraftServer;
import net.thestig294.mctournament.font.ModFonts;
import net.thestig294.mctournament.item.ModItemGroups;
import net.thestig294.mctournament.item.ModItems;
import net.thestig294.mctournament.minigame.Minigames;
import net.thestig294.mctournament.texture.ModTextures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MCTournament implements ModInitializer {
	public static final String MOD_ID = "mctournament";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static MinecraftServer SERVER;

	@Override
	public void onInitialize() {
		LOGGER.info("====Hello Server World!====");

		ModItems.registerItems();
		ModItemGroups.registerItemGroups();

		ModFonts.registerFonts();
		ModTextures.registerTextures();

		Minigames.registerMinigames();

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			SERVER = server;
		});
	}
}