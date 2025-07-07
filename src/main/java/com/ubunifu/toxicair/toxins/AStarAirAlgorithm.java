package com.ubunifu.toxicair.toxins;

import com.ubunifu.toxicair.ToxicAir;
import com.ubunifu.toxicair.blocks.AirPurifier.AirPurifierBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class AStarAirAlgorithm {
    private static final Map<World, Map<Pair<BlockPos, BlockPos>, Double>> PATH_CACHE = new ConcurrentHashMap<>();

    public static double ComputeDistance(World world, BlockPos startPos, BlockPos endPos){
        startPos = startPos.toImmutable();
        endPos = endPos.toImmutable();

        Pair<BlockPos, BlockPos> key = new Pair<>(startPos, endPos);
        Map<Pair<BlockPos, BlockPos>, Double> worldCache = PATH_CACHE.computeIfAbsent(world, w -> new ConcurrentHashMap<>());

        BlockPos[] spos = {startPos};
        BlockPos[] epos = {endPos};

        // Immediate cache return
        Double cachedResult = worldCache.get(key);
        if (cachedResult != null) {
            // Recompute it async for next time
            CompletableFuture.runAsync(() -> {
                double freshValue = internalCompute(world, spos[0], epos[0]);
                worldCache.put(key, freshValue);
            });
            return cachedResult;
        }

        // No cached result, compute it sync this time
        double result = internalCompute(world, startPos, endPos);
        worldCache.put(key, result);
        if (worldCache.size() > 1000)
            try{//noinspection SuspiciousMethodCalls
                worldCache.remove(worldCache.keySet().stream().findFirst());
            }catch (Exception ignored){}
        return result;
    }

    static double internalCompute(World world, BlockPos startPos, BlockPos endPos) {
        startPos = startPos.toImmutable();
        endPos = endPos.toImmutable();
        if (startPos.equals(endPos)) return 0;
        if (GetNodeType(world, startPos) != WorldStaticNodeCache.NODE_PATH_TYPE.AIR ||
                GetNodeType(world, endPos) != WorldStaticNodeCache.NODE_PATH_TYPE.BLOCKED)
            return Double.MAX_VALUE;
        float maxNodes = ComputeMaxSteps(startPos,endPos);
        float expandedNodes = 0;

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<BlockPos> closedSet = new HashSet<>();
        Map<BlockPos, Float> gScore = new HashMap<>();
        gScore.put(startPos,0f);
        openSet.add(new Node(world,startPos, 0f, heuristic(startPos, endPos)));
        while (!openSet.isEmpty()) {
            if (++expandedNodes > maxNodes) {
                //System.out.println("[A*] Aborted: Node limit exceeded (" + expandedNodes + " > " + maxNodes + ")");
                return Double.MAX_VALUE;
            }
            Node current = openSet.poll();
            BlockPos blockPos = current.pos;
            if (isClose(blockPos,endPos)) {
                return gScore.get(blockPos); // Or reconstruct path from cameFrom
            }
            closedSet.add(blockPos);

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = blockPos.offset(dir).toImmutable();
                if (!isClose(neighbor,endPos) && GetNodeType(world, neighbor) != WorldStaticNodeCache.NODE_PATH_TYPE.AIR) continue;
                if (closedSet.contains(neighbor)) continue;

                float tentativeG = gScore.getOrDefault(blockPos, Float.MAX_VALUE) + GetNodeMalice(world, neighbor);

                if (tentativeG < gScore.getOrDefault(neighbor, Float.MAX_VALUE)) {
                    gScore.put(neighbor, tentativeG);

                    float h = heuristic(neighbor, endPos);
                    //debugNode(world,neighbor,h);
                    openSet.add(new Node(world,neighbor, tentativeG, h));
                }
            }
        }
        return gScore.getOrDefault(endPos,Float.MAX_VALUE);
    }
    private static void debugNode(World world, BlockPos pos, float fCost) {
        if (!(world instanceof ServerWorld)) return;
        ((ServerWorld) world).spawnParticles(
                fCost < 10 ? ParticleTypes.END_ROD : ParticleTypes.SMOKE,
                pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                1, 0, 0, 0, 0.0
        );
    }
    private static boolean isClose(BlockPos a, BlockPos b)
    {return a.isWithinDistance(b,1.5f);}

    static float ComputeMaxSteps(BlockPos startPos, BlockPos endPos){
        int estimatedSteps = Math.abs(startPos.getX() - endPos.getX())
                + Math.abs(startPos.getY() - endPos.getY())
                + Math.abs(startPos.getZ() - endPos.getZ());
        return estimatedSteps * 2;
    }
    static float heuristic(BlockPos a, BlockPos b) {
        return Math.abs(a.getX() - b.getX()) +
                Math.abs(a.getY() - b.getY()) +
                Math.abs(a.getZ() - b.getZ());
    }
    public static WorldStaticNodeCache.NODE_PATH_TYPE GetNodeType(World world,BlockPos blockPos){
        return WorldStaticNodeCache.GetOrCompute(world,blockPos); // Either AIR or BLOCKED
    }
    public static int GetNodeMalice(World world, BlockPos blockPos){
        WorldStaticNodeCache.NODE_PATH_TYPE type = GetNodeType(world, blockPos);
        return switch (type) {
            case AIR -> 1; // normal air
            case AIR_SUNLIT -> 2; // sun-exposed air, cost amplified to simulate harder purifier reach
            default -> 100; // blocked or toxic, basically impassable
        };
    }

    private static class Node implements Comparable<Node> {
        BlockPos pos;
        float gCost; // Cost from start
        float hCost; // Heuristic to goal
        float fCost; // gCost + hCost
        WorldStaticNodeCache.NODE_PATH_TYPE nodeType;

        public Node(World world,BlockPos pos, float gCost, float hCost) {
            this.pos = pos;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = gCost + hCost;
            this.nodeType = GetNodeType(world,pos);
        }

        @Override
        public int compareTo(Node other) {
            return Float.compare(this.fCost, other.fCost);
        }
    }
}
