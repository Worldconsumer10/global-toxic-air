package com.ubunifu.toxicair.blocks;

import com.ubunifu.toxicair.ToxicAir;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static final Block AIR_PURIFIER = registerBlock("air_purifier",
            new AirPurifierBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)));
    public static final Block AIR_SAMPLER =  Registry.register(Registries.BLOCK,new Identifier(ToxicAir.MOD_ID,"air_sampler"),
            new AirSamplerBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)));
    public static final Item AIR_SAMPLER_BLOCK_ITEM = Registry.register(Registries.ITEM, Identifier.of(ToxicAir.MOD_ID,"air_sampler"),new AirSamplerBlockItem(AIR_SAMPLER,new FabricItemSettings()));

    private static Item registerBlockItem(String name, Block block){
        return Registry.register(Registries.ITEM, Identifier.of(ToxicAir.MOD_ID,name),new BlockItem(block,new FabricItemSettings()));
    }
    private static Block registerBlock(String name, Block block){
        registerBlockItem(name,block);
        return Registry.register(Registries.BLOCK,new Identifier(ToxicAir.MOD_ID,name),block);
    }
    public static void Register(){}
}
