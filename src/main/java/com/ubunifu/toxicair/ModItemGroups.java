package com.ubunifu.toxicair;

import com.ubunifu.toxicair.blocks.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    public static final ItemGroup TOXIC_AIR = Registry.register(Registries.ITEM_GROUP, Identifier.of(ToxicAir.MOD_ID,"item_group_ta"), FabricItemGroup.builder()
            .displayName(Text.translatable("itemgroup.title"))
            .icon(()->new ItemStack(Items.BARRIER))
            .entries((displayContext,entries)->{
                entries.add(ModBlocks.AIR_PURIFIER);
                entries.add(ModBlocks.AIR_SAMPLER);
            })
            .build());

    public static void Register(){}
}
