package net.thestig294.mctournament.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.util.ModUtil;

public class ModItemGroups {
    public static void registerItemGroup(String name, ItemGroup.Builder itemGroup) {
        Registry.register(Registries.ITEM_GROUP, new Identifier(MCTournament.MOD_ID, name), itemGroup.build());
    }

    public static void registerItemGroups(){
        ModUtil.logRegistration("item groups");

        registerItemGroup("mctournament", FabricItemGroup.builder()
                .displayName(Text.translatable("itemGroup." + MCTournament.MOD_ID))
                .icon(() -> new ItemStack(ModItems.TROPHY))
                .entries((displayContext, entries) -> {
                    entries.add(ModItems.TROPHY);
                    entries.add(ModItems.REMOTE);
                    entries.add(ModItems.WAND);
                })
        );
    }
}
