package com.ubunifu.toxicair;

import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.util.*;
import java.util.concurrent.*;

public class ToxicAirPropagator {
    public static final float EMITTER_BOOST = 80f;
    public static final float VOID_REDUCTION = 50f;
    public static final float ASYNC_TICK_PROCESSES = 50;

    public static Map<World, PriorityQueue<BlockPos>> QUEUE = new HashMap<>();

    public static void tick(World world) {
        Comparator<BlockPos> disparityComparator = Comparator.comparingDouble(pos -> {
            if (!AirHandler.isEligablePosition(world,pos)) return 0;
            float self = AirHandler.getOrCompute(world, pos);
            float maxDelta = 0f;
            for (Direction dir : Direction.values()) {
                if (!AirHandler.isEligablePosition(world,pos.offset(dir))) continue;
                float neighbor = AirHandler.getOrCompute(world, pos.offset(dir));
                maxDelta = Math.max(maxDelta, Math.abs(self - neighbor));
            }

            // Sky brightness adjustment (0 = no sky light → more priority)
            int skyLight = world.getLightLevel(LightType.SKY, pos);
            float lightFactor = 1f - (skyLight / 15f); // 1 = no light, 0 = full light

            // Boost blocks with high disparity and low light
            return -(maxDelta + lightFactor * 5f); // Adjust weight as needed
        });
        Long2FloatOpenHashMap worldToxin = AirHandler.CreateForWorld(world); // Updated to return map

        Queue<BlockPos> blockQueue = QUEUE.computeIfAbsent(world, w -> new PriorityQueue<>(disparityComparator));

        if (blockQueue.isEmpty()) {
            // Clone keys to avoid concurrent modification
            for (long key : worldToxin.keySet().toLongArray()) {
                blockQueue.add(BlockPos.fromLong(key));
            }
            return;
        }

        int processed = 0;
        while (processed < ASYNC_TICK_PROCESSES && !blockQueue.isEmpty()) {
            BlockPos blockPos = blockQueue.poll();
            if (blockPos == null || !isChunkBlockLoaded(world, blockPos)) continue;
            if (!AirHandler.isEligablePosition(world,blockPos))return;

            float currentToxicity = 0;
            try {
                currentToxicity = AirHandler.getOrCompute(world, blockPos);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            float originalToxicity = currentToxicity;

            float totalTransfer = 0f;

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = blockPos.offset(dir);
                if (!AirHandler.isEligablePosition(world,neighbor)) continue;
                float neighborToxicity = Math.max(AirHandler.getOrCompute(world, neighbor), 0);

                // Boosts/penalties from emitters or voids
                if (AirHandler.isToxinEmitter(world, neighbor)) {
                    neighborToxicity += EMITTER_BOOST;
                } else if (AirHandler.isToxinVoid(world, neighbor)) {
                    neighborToxicity = Math.max(neighborToxicity - VOID_REDUCTION, 0);
                }

                float difference = currentToxicity - neighborToxicity;

                if (difference > 0) {
                    float transferAmount = difference * 0.25f; // Only move a fraction to simulate gradual spread
                    transferAmount = Math.min(transferAmount, currentToxicity); // Don't over-drain

                    // Pull toxins from current block to neighbor
                    AirHandler.SetToxicity(world, neighbor, neighborToxicity + transferAmount);
                    totalTransfer += transferAmount;
                }
            }

            currentToxicity -= totalTransfer;
            currentToxicity = MathHelper.clamp(currentToxicity, 0f, AirHandler.MAX_TOXICITY);

            AirHandler.SetToxicity(world, blockPos, currentToxicity);

            ToxicAir.LOGGER.info("Ticked {} → from {} to {}, Δ{}", blockPos, originalToxicity, currentToxicity, currentToxicity - originalToxicity);
            processed++;
        }
    }
    private static boolean isChunkBlockLoaded(World world,BlockPos blockPos){
        WorldChunk worldChunk = world.getWorldChunk(blockPos);
        ChunkPos chunkPos = worldChunk.getPos();
        return world.isChunkLoaded(chunkPos.x,chunkPos.z);
    }
}