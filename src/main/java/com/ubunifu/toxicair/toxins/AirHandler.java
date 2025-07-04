package com.ubunifu.toxicair.toxins;

import com.ubunifu.toxicair.blocks.AirPurifier.AirPurifierBlockEntity;
import it.unimi.dsi.fastutil.longs.Long2FloatArrayMap;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;

public class AirHandler {
    static int DEFAULT_TOXIN = 100;
    private static final HashMap<World, Long2FloatArrayMap> CLEAN_AIR_MAP = new HashMap<>();
    private static final HashMap<World, Set<BlockPos>> PURIFIER_LOCATIONS = new HashMap<>();

    public static boolean isPurifierSaved(World world, BlockPos blockPos)
    { return PURIFIER_LOCATIONS.computeIfAbsent(world, w->new HashSet<>()).contains(blockPos); }
    public static void AddPurifier(World world, BlockPos blockPos){
        PURIFIER_LOCATIONS.computeIfAbsent(world, w-> new HashSet<>())
                .add(blockPos);
    }
    public static void RemovePurifier(World world, BlockPos blockPos){
        PURIFIER_LOCATIONS.computeIfAbsent(world, w-> new HashSet<>())
                .remove(blockPos);
    }

    private static int getPurifierStrength(World world, BlockPos blockPos, boolean denyRecursion){
        if (!(world.getBlockEntity(blockPos) instanceof AirPurifierBlockEntity airPurifierBlockEntity)) return 0;
        int strength = airPurifierBlockEntity.getStrength();
        if (denyRecursion) return strength;
        for (Direction dir : Direction.values()) {
            strength += getPurifierStrength(world,blockPos.offset(dir),true);
        }
        return strength;
    }
    private static int getToxicity(BlockPos blockPos, World world, int depth){
        if (depth > 5) return 0;
        if (world.getBlockState(blockPos).isSolidBlock(world,blockPos)) return 0;
        int toxicity = DEFAULT_TOXIN;
        for (Direction dir : Direction.values()) {
            BlockPos test = blockPos.offset(dir);
            toxicity += getToxicity(test, world, depth + 1);
        }
        return toxicity;
    }
    private static int getNeighbourToxicity(BlockPos blockPos, World world){
        int toxicity = 0;
        for (Direction dir : Direction.values()) {
            toxicity += getToxicity(blockPos.offset(dir),world,0);
        }
        return toxicity;
    }

    public static void tick(MinecraftServer server, World world){
        Set<BlockPos> worldPurifiers = PURIFIER_LOCATIONS.computeIfAbsent(world,w->new HashSet<>());
        Long2FloatArrayMap cleanAirMap = CLEAN_AIR_MAP.computeIfAbsent(world, w -> new Long2FloatArrayMap());

        for (BlockPos purifierLocation : worldPurifiers) {
            int strength = getPurifierStrength(world, purifierLocation, false);
            if (strength <= 0) continue;

            int maxRadius = strength / 10 + 1; // Adjust this scaling as needed
            Queue<BlockPos> queue = new ArrayDeque<>();
            Set<BlockPos> visited = new HashSet<>();

            queue.add(purifierLocation);
            visited.add(purifierLocation);

            while (!queue.isEmpty()) {
                BlockPos current = queue.poll();
                int distance = (int) Math.sqrt(current.getSquaredDistance(purifierLocation));
                if (distance > maxRadius) continue;

                float decay = 1f - ((float) distance / maxRadius); // Linearly decay effectiveness
                float strengthAtPos = Math.max(0f, strength * decay);

                // Only update if it's stronger than previous value at that position
                long key = current.toImmutable().asLong();
                float previous = cleanAirMap.getOrDefault(key, 0f);
                if (strengthAtPos > previous) {
                    cleanAirMap.put(key, strengthAtPos);
                }

                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = current.offset(dir);
                    if (!visited.contains(neighbor) && !world.getBlockState(neighbor).isSolidBlock(world, neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }
    }

    public static float getToxicity(BlockPos blockPos, World world) {
        Long2FloatArrayMap toxicityMap = CLEAN_AIR_MAP.computeIfAbsent(world, w->new Long2FloatArrayMap());
        if (toxicityMap.containsKey(blockPos.toImmutable().asLong()))
            return Math.max(toxicityMap.get(blockPos.toImmutable().asLong()),0);
        return 100;
    }
}
