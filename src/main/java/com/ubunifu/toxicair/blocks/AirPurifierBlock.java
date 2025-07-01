package com.ubunifu.toxicair.blocks;

import com.ubunifu.toxicair.AirHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AirPurifierBlock extends Block {
    public AirPurifierBlock(Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);
        if (!AirHandler.isToxinVoid(world,pos)) AirHandler.registerToxinVoid(world,pos);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        AirHandler.registerToxinVoid(world,pos);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        AirHandler.unregisterToxinVoid(world,pos);
        return super.onBreak(world, pos, state, player);
    }
}
