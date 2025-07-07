package com.ubunifu.toxicair.blocks.AirSampler;

import com.ubunifu.toxicair.toxins.AStarAirAlgorithm;
import com.ubunifu.toxicair.toxins.ToxinHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AirSamplerBlockItem extends BlockItem {
    public AirSamplerBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if (!selected || !world.isClient) return;
        if (!(entity instanceof PlayerEntity player)) return;
        BlockPos eyeBlockPos = BlockPos.ofFloored(player.getEyePos());
        player.sendMessage(Text.of(ToxinHandler.isToxicAir(world,eyeBlockPos,true) ? "Is in toxic air!" : "Is not in toxic air."),true);
    }
}
