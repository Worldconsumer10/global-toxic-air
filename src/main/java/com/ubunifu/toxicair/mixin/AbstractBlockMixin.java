package com.ubunifu.toxicair.mixin;

import com.ubunifu.toxicair.AirHandler;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {

    @Inject(method = "randomTick",at=@At("HEAD"))
    public void tickInjection(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci){
        if (world.isClient) return;
        if (!AirHandler.isInMap(world,pos) && AirHandler.isEligablePosition(world,pos))
            AirHandler.CreateForPosition(world,pos);
    }
}
