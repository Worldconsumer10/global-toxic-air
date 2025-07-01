package com.ubunifu.toxicair.mixin;

import com.ubunifu.toxicair.AirHandler;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {
    @Inject(method = "onBlockAdded",at=@At("HEAD"))
    public void addBlockInjection(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify, CallbackInfo ci){
        if (state.isSolidBlock(world,pos)) return; // Ignore any solid block.
        AirHandler.createIfPossible(world,pos);
    }
}
