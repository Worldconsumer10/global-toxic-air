package com.ubunifu.toxicair.toxins;

import com.ubunifu.toxicair.ToxicAir;
import com.ubunifu.toxicair.blocks.AirPurifier.AirPurifierBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

public class AStarAirAlgorithm {
    public static HashMap<ServerWorld,Set<BlockPos>> AIR_PURIFIER_BLOCKS = new HashMap<>();
    static double GetPurifierStrength(World world, BlockPos blockPos){
        BlockEntity blockEntity = world.getBlockEntity(blockPos);
        if (!(blockEntity instanceof AirPurifierBlockEntity airPurifierBlock)) return 0;
        return airPurifierBlock.getEffectiveDistance();
    }
    private static final int SCAN_RADIUS = 4;
    private static final int TICK_INTERVAL = 80;
    private static final Map<UUID, Integer> entityTickCounters = new HashMap<>();

    public static void EntityTick(LivingEntity entity) {
        UUID entityId = entity.getUuid();
        int tickCount = entityTickCounters.getOrDefault(entityId, 0) + 1;

        if (tickCount % TICK_INTERVAL != 0) {
            entityTickCounters.put(entityId, tickCount);
            return;
        }
        entityTickCounters.put(entityId, 0);

        World world = entity.getWorld();
        if (world.isClient || !(world instanceof ServerWorld serverWorld)) return;

        BlockPos origin = entity.getBlockPos();
        BlockBox scanBox = BlockBox.create(origin.add(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS),
                origin.add(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS));

        Set<BlockPos> purifiers = AStarAirAlgorithm.AIR_PURIFIER_BLOCKS
                .computeIfAbsent(serverWorld, w -> new ConcurrentSkipListSet<>());

        BlockPos.stream(scanBox)
                .filter(pos -> serverWorld.getBlockEntity(pos) instanceof AirPurifierBlockEntity)
                .forEach(pos -> purifiers.add(pos.toImmutable()));
    }

    public static boolean isToxicAir(World world, BlockPos blockPos) {
        if (world.isClient) return true;
        Set<BlockPos> purifierPositions = AIR_PURIFIER_BLOCKS.getOrDefault(world, Collections.emptySet());

        ToxicAir.LOGGER.info("Purifiers: "+purifierPositions.size());
        for (BlockPos purifierPos : purifierPositions) {
            double maxDistance = GetPurifierStrength(world, purifierPos);
            ToxicAir.LOGGER.info("Max Strength of purifier: "+maxDistance);
            if (maxDistance <= 0) continue;

            double pathDistance = GetDistance(world, blockPos, purifierPos);
            ToxicAir.LOGGER.info("Distance to purifier: "+pathDistance);
            if (pathDistance < 0) continue; // Unreachable

            if (pathDistance <= maxDistance) {
                ToxicAir.LOGGER.info("Close enough");
                return false; // Within reach of at least one purifier
            }
        }
        ToxicAir.LOGGER.info("Too far");
        return true; // No reachable purifiers = toxic
    }
    public static double GetDistance(World world, BlockPos startPos, BlockPos endPos) {
        if (startPos.equals(endPos)) return 0;
        if (GetNodeType(world, startPos) != WorldStaticNodeCache.NODE_PATH_TYPE.AIR ||
                GetNodeType(world, endPos) != WorldStaticNodeCache.NODE_PATH_TYPE.AIR)
            return -1;
        Queue<BlockPos> queue = new ArrayDeque<>();
        Map<BlockPos, Integer> visited = new HashMap<>();
        queue.add(startPos);
        visited.put(startPos, 0);
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            int currentDist = visited.get(current);
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.offset(dir);
                if (visited.containsKey(neighbor)) continue;
                if (GetNodeType(world, neighbor) != WorldStaticNodeCache.NODE_PATH_TYPE.AIR) continue;
                visited.put(neighbor, currentDist + 1);
                if (neighbor.equals(endPos))
                    return currentDist + 1;
                queue.add(neighbor);
            }
        }
        return -1; // No path found
    }
    public static WorldStaticNodeCache.NODE_PATH_TYPE GetNodeType(World world,BlockPos blockPos){
        return WorldStaticNodeCache.GetOrCompute(world,blockPos); // Either AIR or BLOCKED
    }

    private static class Node {
        BlockPos pos;
        double gScore;  // Cost from start
        double fScore;  // gScore + heuristic

        Node(BlockPos pos, double gScore, double fScore) {
            this.pos = pos;
            this.gScore = gScore;
            this.fScore = fScore;
        }
    }
}
