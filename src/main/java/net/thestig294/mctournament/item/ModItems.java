package net.thestig294.mctournament.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.item.custom.RemoteItem;

public class ModItems {
    public static final Item TROPHY = register("trophy", new Item(new FabricItemSettings()));
    public static final Item REMOTE = register("remote", new RemoteItem(new FabricItemSettings().maxCount(1)));

    private static Item register(String name, Item item){
        return Registry.register(Registries.ITEM, new Identifier(MCTournament.MOD_ID, name), item);
    }

    public static void registerItems() {
        MCTournament.LOGGER.info("Registering items for " + MCTournament.MOD_ID);
    }
}
