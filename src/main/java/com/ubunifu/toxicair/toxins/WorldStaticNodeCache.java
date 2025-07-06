package com.ubunifu.toxicair.toxins;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;

public class WorldStaticNodeCache {
    public static HashMap<World,WorldStaticNodeCache> worldWorldStaticNodeCacheHashMap = new HashMap<>();
    World world;
    public HashMap<BlockPos,NODE_PATH_TYPE> NODE_TYPES;
    public WorldStaticNodeCache(World world) throws Exception {
        if (world.isClient) throw new Exception("Cannot be created on the client");
        this.world = world;
        this.NODE_TYPES = new HashMap<>();
        worldWorldStaticNodeCacheHashMap.putIfAbsent(world, this);
    }

    public boolean isAirConveyable(World world, BlockPos blockPos){
        if (world.isClient) return false;
        BlockState blockState = world.getBlockState(blockPos);
        return !blockState.isSolidBlock(world,blockPos);
    }

    public NODE_PATH_TYPE GetOrCompute(BlockPos blockPos){
        return NODE_TYPES.computeIfAbsent(blockPos.toImmutable(),w->{
            if (isAirConveyable(world,w))
                return NODE_PATH_TYPE.AIR;
            return NODE_PATH_TYPE.BLOCKED;
        });
    }

    public static NODE_PATH_TYPE GetOrCompute(World world, BlockPos blockPos){
        if (world.isClient) return NODE_PATH_TYPE.BLOCKED;
        WorldStaticNodeCache nodeTypes = worldWorldStaticNodeCacheHashMap.computeIfAbsent(world, world1 -> {
            try {
                return new WorldStaticNodeCache(world1);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return nodeTypes.GetOrCompute(blockPos.toImmutable());
    }

    public enum NODE_PATH_TYPE {
        BLOCKED,
        AIR
    }
}
