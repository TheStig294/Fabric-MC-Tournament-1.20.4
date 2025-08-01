package net.thestig294.mctournament.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.thestig294.mctournament.MCTournament;

public class ModItemGroups {
    public static ItemGroup MC_TOURNAMENT = register("mctournament",
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemGroup.mctournament"))
                    .icon(() -> new ItemStack(ModItems.TROPHY))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.TROPHY);
                        entries.add(ModItems.REMOTE);
                    })
    );
    public static ItemGroup MINIGAME_TRIVIA_MURDER_PARTY = register("minigame_trivia_murder_party",
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemGroup.minigame_trivia_murder_party"))
                    .icon(() -> new ItemStack(ModItems.TROPHY))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.TROPHY);
                        entries.add(ModItems.REMOTE);
                    })
    );

    private static ItemGroup register(String name, ItemGroup.Builder itemGroup) {
        return Registry.register(Registries.ITEM_GROUP, new Identifier(MCTournament.MOD_ID, name), itemGroup.build());
    }

    private static void registerToIngredients(FabricItemGroupEntries entries){
        entries.add(ModItems.TROPHY);
        entries.add(ModItems.REMOTE);
    }

    public static void registerItemGroups(){
        MCTournament.LOGGER.info("Registering item group {} and {} for {}", MC_TOURNAMENT.getDisplayName().getString(),
                MINIGAME_TRIVIA_MURDER_PARTY.getDisplayName().getString(), MCTournament.MOD_ID);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(ModItemGroups::registerToIngredients);
    }
}
