package com.ubunifu.toxicair.mixin;

import com.ubunifu.toxicair.toxins.WorldStaticNodeCache;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
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
        if (world == null || world.isClient) return;
        if (!(world instanceof ServerWorld serverWorld)) return;
        try{
            WorldStaticNodeCache map = WorldStaticNodeCache.worldWorldStaticNodeCacheHashMap.computeIfAbsent(serverWorld, w -> {
                try {
                    return new WorldStaticNodeCache(w);
                } catch (Exception e) {
                    e.printStackTrace(); // ← Don't just throw, log it
                    return null;
                }
            });

            if (map == null) return;

            BlockPos immutablePos = pos.toImmutable();
            map.NODE_TYPES.put(immutablePos, map.isAirConveyable(pos) ?
                    WorldStaticNodeCache.NODE_PATH_TYPE.AIR :
                    WorldStaticNodeCache.NODE_PATH_TYPE.BLOCKED);
        }catch (Exception ignored){}
    }
}
