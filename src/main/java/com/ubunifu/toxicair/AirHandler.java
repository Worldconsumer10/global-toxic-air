package com.ubunifu.toxicair;

import com.ubunifu.toxicair.annotations.ToxinEmitter;
import com.ubunifu.toxicair.annotations.ToxinVoid;
import com.ubunifu.toxicair.networking.ToxicAirPackets;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AirHandler {
    public static final float MAX_TOXICITY = 100f;
    private static final HashMap<World,Long2FloatOpenHashMap> TOXICITY_MAP = new HashMap<>();
    static int clientUpdateCooldown;

    public static void ServerTick(){
        clientUpdateCooldown = Math.min(clientUpdateCooldown - 1,0);
    }

    private static void ToxicityMapUpdated(World world) throws Exception {
        if (clientUpdateCooldown > 0) return;
        if (!(world instanceof ServerWorld serverWorld)){
            throw new Exception("Toxicity map updated on client. Do not do this.");
        }
        Long2FloatOpenHashMap toxinMap = TOXICITY_MAP.get(world);
        if (toxinMap == null) return;
        clientUpdateCooldown = 5;
        Map<BlockPos, Float> visibleData = new HashMap<>();
        for (long key : toxinMap.keySet()) {
            visibleData.put(BlockPos.fromLong(key), toxinMap.get(key));
        }

        // Broadcast to all players in this world
        for (ServerPlayerEntity player : serverWorld.getPlayers()) {
            ToxicAirPackets.sendToxicitySync(player, visibleData);
        }
    }

    @SuppressWarnings("unchecked")
    public static HashMap<World,Long2FloatOpenHashMap> GetToxicityMap(){
        return (HashMap<World,Long2FloatOpenHashMap>)TOXICITY_MAP.clone();
    }

    public static boolean isToxinVoid(World world, BlockPos pos){
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        return block.getClass().isAnnotationPresent(ToxinVoid.class);
    }
    public static boolean isToxinEmitter(World world, BlockPos pos){
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        return block.getClass().isAnnotationPresent(ToxinEmitter.class);
    }
    public static boolean isEligablePosition(World world, BlockPos blockPos){
        BlockState blockState = world.getBlockState(blockPos);
        return !blockState.isSolidBlock(world,blockPos)
                || blockState.canPathfindThrough(world,blockPos, NavigationType.AIR)
                || blockState.canPathfindThrough(world,blockPos,NavigationType.LAND)
                || blockState.canPathfindThrough(world,blockPos,NavigationType.WATER);
    }
    public static boolean isInMap(World world, BlockPos blockPos){
        if (TOXICITY_MAP.containsKey(world))
            return TOXICITY_MAP.get(world).containsKey(blockPos.asLong());
        return false;
    }

    public static Long2FloatOpenHashMap CreateForWorld(World world){
        if (TOXICITY_MAP.containsKey(world)) return TOXICITY_MAP.get(world);
        Long2FloatOpenHashMap long2FloatOpenHashMap = new Long2FloatOpenHashMap();
        long2FloatOpenHashMap.put(world.getSpawnPos().toImmutable().asLong(),CreateForPosition(world,world.getSpawnPos()));
        return long2FloatOpenHashMap;
    }

    public static float getOrCompute(World world, BlockPos blockPos){
        if (!isEligablePosition(world,blockPos)){
            ToxicAir.LOGGER.warn("\n\nAttempted to get toxicity of illegible block. Returned 0.\n\n");
            return 0;
        }
        float existingCache = GetToxicity(world,blockPos);
        if (existingCache == -1)
            return CreateForPosition(world, blockPos);
        return existingCache;
    }

    public static float GetToxicity(World world,BlockPos blockPos){
        Long2FloatOpenHashMap map = TOXICITY_MAP.get(world);
        if (map == null) return -1f;
        return map.get(blockPos.asLong());
    }

    public static void SetToxicity(World world, BlockPos blockPos, float toxicity){
        Long2FloatOpenHashMap map = TOXICITY_MAP.computeIfAbsent(world, w -> CreateForWorld(world));
        map.put(blockPos.asLong(),toxicity);
        try {
            ToxicityMapUpdated(world);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static float CreateForPosition(World world, BlockPos blockPos) {
        Long2FloatOpenHashMap map = TOXICITY_MAP.computeIfAbsent(world, w -> {
            Long2FloatOpenHashMap m = new Long2FloatOpenHashMap();
            m.defaultReturnValue(-1f);
            return m;
        });

        float toxicity = computeAtPosition(world, blockPos.toImmutable());
        map.put(blockPos.toImmutable().asLong(), toxicity);
        try {
            ToxicityMapUpdated(world);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return toxicity;
    }
    private static float computeAtPosition(World world, BlockPos blockPos){
        if (world.getLightLevel(LightType.SKY,blockPos)>0)
            return MAX_TOXICITY;
        float toxicity = -1;
        for (Direction dir : Direction.values()) {
            if (world.getLightLevel(LightType.SKY,blockPos.offset(dir))>0)
                return MAX_TOXICITY;
            toxicity += Math.max(TOXICITY_MAP.getOrDefault(world,new Long2FloatOpenHashMap()).get(blockPos.offset(dir).toImmutable().asLong()),0);
        }
        return Math.min(toxicity,MAX_TOXICITY);
    }

    public static void addWorldMap(World clientWorld, Long2FloatOpenHashMap map) {
        TOXICITY_MAP.put(clientWorld,map);
        if (clientWorld.isClient) return;
        try {
            ToxicityMapUpdated(clientWorld);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeWorldMap(World clientWorld) {
        TOXICITY_MAP.remove(clientWorld);
        if (clientWorld.isClient) return;
        try {
            ToxicityMapUpdated(clientWorld);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
