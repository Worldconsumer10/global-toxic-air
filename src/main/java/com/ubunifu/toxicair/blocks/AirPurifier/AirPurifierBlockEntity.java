package com.ubunifu.toxicair.blocks.AirPurifier;

import com.ubunifu.toxicair.blocks.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class AirPurifierBlockEntity extends BlockEntity {
    int AIR_STRENGTH = 1000;
    public AirPurifierBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AIR_PURIFIER_BLOCK_ENTITY, pos, state);
    }

    public int getStrength() {
        return this.AIR_STRENGTH;
    }

}
