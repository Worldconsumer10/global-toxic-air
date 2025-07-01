package com.ubunifu.toxicair;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IToxinEmitter<T extends AbstractBlock> {
    void onToxinBlockPlaced(World world, BlockPos pos);
}
