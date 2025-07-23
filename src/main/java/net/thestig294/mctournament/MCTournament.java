package net.thestig294.mctournament;

import net.fabricmc.api.ModInitializer;

import net.thestig294.mctournament.item.ModItemGroups;
import net.thestig294.mctournament.item.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MCTournament implements ModInitializer {
	public static final String MOD_ID = "mctournament";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("====Hello Server World!====");

		ModItems.registerItems();
		ModItemGroups.registerItemGroups();
	}
}