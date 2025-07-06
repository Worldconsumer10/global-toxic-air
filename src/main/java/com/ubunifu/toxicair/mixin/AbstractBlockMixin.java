package com.ubunifu.toxicair.mixin;

import com.ubunifu.toxicair.toxins.WorldStaticNodeCache;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {

    @Inject(method = "neighborUpdate",at=@At("HEAD"))
    public void neighbourInject(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify, CallbackInfo ci){
        if (world.isClient) return;
        WorldStaticNodeCache map = WorldStaticNodeCache.worldWorldStaticNodeCacheHashMap.computeIfAbsent(world, world1 -> {
            try {
                return new WorldStaticNodeCache(world1);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        if (map.isAirConveyable(world,pos))
            map.NODE_TYPES.put(pos.toImmutable(), WorldStaticNodeCache.NODE_PATH_TYPE.AIR);
        else
            map.NODE_TYPES.put(pos.toImmutable(), WorldStaticNodeCache.NODE_PATH_TYPE.BLOCKED);
    }
}
