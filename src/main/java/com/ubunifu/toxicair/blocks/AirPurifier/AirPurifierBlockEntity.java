package com.ubunifu.toxicair.blocks.AirPurifier;

import com.ubunifu.toxicair.blocks.ModBlockEntities;
import com.ubunifu.toxicair.toxins.AStarAirAlgorithm;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.ConcurrentSkipListSet;

public class AirPurifierBlockEntity extends BlockEntity {
    public AirPurifierBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AIR_PURIFIER_BLOCK_ENTITY, pos, state);
    }

    public int getEffectiveDistance() {
        return 1000;
    }

}
