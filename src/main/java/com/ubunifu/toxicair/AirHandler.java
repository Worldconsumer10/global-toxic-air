package com.ubunifu.toxicair;

import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AirHandler {
    public static final float MAX_TOXICITY = 100f;
    public static final HashMap<World,Long2FloatOpenHashMap> TOXICITY_MAP = new HashMap<>();
    public static final HashMap<World,Set<BlockPos>> TOXIN_VOID = new HashMap<>();
    public static final HashMap<World,Set<BlockPos>> TOXIN_EMITTER = new HashMap<>();
    public static boolean isToxinVoid(World world, BlockPos pos){
        return TOXIN_VOID.containsKey(world) && TOXIN_VOID.get(world).contains(pos.toImmutable());
    }
    public static boolean isToxinEmitter(World world, BlockPos pos){
        return TOXIN_EMITTER.containsKey(world) && TOXIN_EMITTER.get(world).contains(pos.toImmutable());
    }
    public static Set<BlockPos> getToxinVoids(World world) {
        return TOXIN_VOID.containsKey(world) ? TOXIN_VOID.get(world) : Set.of();
    }

    public static Set<BlockPos> getToxinEmitters(World world) {
        return TOXIN_EMITTER.containsKey(world) ? TOXIN_EMITTER.get(world) : Set.of();
    }

    public static void registerToxinVoid(World world, BlockPos pos) {
        ToxicAirPropagator.markDirty(world,pos);
        TOXIN_VOID.computeIfAbsent(world, w -> ConcurrentHashMap.newKeySet()).add(pos.toImmutable());
    }

    public static void registerToxinEmitter(World world, BlockPos pos) {
        ToxicAirPropagator.markDirty(world,pos);
        TOXIN_EMITTER.computeIfAbsent(world, w -> ConcurrentHashMap.newKeySet()).add(pos.toImmutable());
    }
    public static void unregisterToxinVoid(World world, BlockPos pos) {
        ToxicAirPropagator.markDirty(world,pos);
        TOXIN_VOID.computeIfAbsent(world, w -> ConcurrentHashMap.newKeySet()).remove(pos.toImmutable());
    }

    public static void unregisterToxinEmitter(World world, BlockPos pos) {
        ToxicAirPropagator.markDirty(world,pos);
        TOXIN_EMITTER.computeIfAbsent(world, w -> ConcurrentHashMap.newKeySet()).remove(pos.toImmutable());
    }
    public static boolean isEligablePosition(World world, BlockPos blockPos){
        BlockState blockState = world.getBlockState(blockPos);
        return blockState.isSolidBlock(world,blockPos)
                || blockState.canPathfindThrough(world,blockPos, NavigationType.AIR)
                || blockState.canPathfindThrough(world,blockPos,NavigationType.LAND)
                || blockState.canPathfindThrough(world,blockPos,NavigationType.WATER);
    }
    public static boolean isInMap(World world, BlockPos blockPos){
        if (TOXICITY_MAP.containsKey(world))
            return TOXICITY_MAP.get(world).containsKey(blockPos.asLong());
        return false;
    }

    public static void CreateForWorld(World world){
        synchronized (TOXICITY_MAP){
            if (TOXICITY_MAP.containsKey(world)) return;
            Long2FloatOpenHashMap map = new Long2FloatOpenHashMap();
            map.defaultReturnValue(-1);
            TOXICITY_MAP.put(world,map);
        }
    }

    public static float getOrCompute(World world, BlockPos blockPos){
        float existingCache = GetToxicity(world,blockPos);
        if (existingCache == -1)
            return CreateForPosition(world, blockPos);
        return existingCache;
    }

    public static float GetToxicity(World world,BlockPos blockPos){
        Long2FloatOpenHashMap map = TOXICITY_MAP.computeIfAbsent(world, w -> {
            Long2FloatOpenHashMap m = new Long2FloatOpenHashMap();
            m.defaultReturnValue(-1f);
            return m;
        });
        return map.get(blockPos.asLong());
    }

    public static void SetToxicity(World world, BlockPos blockPos, float toxicity){
        Long2FloatOpenHashMap map = TOXICITY_MAP.computeIfAbsent(world, w -> {
            Long2FloatOpenHashMap m = new Long2FloatOpenHashMap();
            m.defaultReturnValue(-1f);
            return m;
        });
        map.put(blockPos.asLong(),toxicity);
        ToxicAirPropagator.markDirty(world,blockPos);
    }

    public static float CreateForPosition(World world, BlockPos blockPos){
        if (world.getLightLevel(LightType.SKY,blockPos)>0){
            SetToxicity(world,blockPos,MAX_TOXICITY);
            return MAX_TOXICITY;
        }
        for (Direction direction : Direction.values()) {
            BlockPos testPosition = blockPos.offset(direction);
            if (world.getLightLevel(LightType.SKY,testPosition)>0)
            {
                SetToxicity(world,blockPos,MAX_TOXICITY);
                return MAX_TOXICITY;
            }
        }
        SetToxicity(world, blockPos, -1f);
        return -1f;
    }
}
