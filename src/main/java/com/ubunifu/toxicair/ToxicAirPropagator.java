package com.ubunifu.toxicair;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.util.*;
import java.util.concurrent.*;

public class ToxicAirPropagator {
    private static final float MAX_TOXICITY = 100f;
    public static final float EMITTER_BOOST = 10f;
    public static final float VOID_REDUCTION = 10f;
    private static final int MAX_SKY_UPDATES_PER_TICK = 5;

    private static final Queue<BlockPos> skyQueue = new ConcurrentLinkedQueue<>();
    private static final Set<BlockPos> dirtyBlocks = ConcurrentHashMap.newKeySet();
    private static final Set<BlockPos> queued = ConcurrentHashMap.newKeySet();

    private static final ExecutorService executor = Executors.newFixedThreadPool(2);

    public static void markDirty(World world, BlockPos pos) {
        if (pos != null && world != null) {
            dirtyBlocks.add(pos.toImmutable());
        }
    }
    public static void markSkySensitive(BlockPos pos) {
        if (queued.add(pos)) skyQueue.add(pos);
    }

    static float recentPropagation;
    static int processedTicks;
    public static void tick(World world) {
        if (!AirHandler.TOXICITY_MAP.containsKey(world)) return;
        processedTicks++;
        if (processedTicks % 20 == 1){
            ToxicAir.LOGGER.info("Propergation Checkup: "+recentPropagation+" toxins have been moved around");
            recentPropagation=0;
        }
        Set<BlockPos> toProcess = new HashSet<>();
        Iterator<BlockPos> it = dirtyBlocks.iterator();
        int limit = 50; // don't overload a single tick

        while (it.hasNext() && limit-- > 0) {
            BlockPos pos = it.next();
            it.remove(); // consume it now

            toProcess.add(pos);
        }

        for (BlockPos pos : toProcess) {
            // Run it directly or asynchronously based on conditions
            if (isSkyExposed(world, pos)) {
                // run on main thread
                propagateBlock(world, pos);
            } else {
                // run async
                CompletableFuture.runAsync(() -> propagateBlock(world, pos));
            }
        }
    }
    private static void propagateBlock(World world, BlockPos pos) {
        float currentToxicity = AirHandler.GetToxicity(world, pos);
        if (currentToxicity <= 0) return;

        boolean changed = false;

        // Handle voids (purifiers)
        for (BlockPos voidPos : AirHandler.getToxinVoids(world)) {
            if (pos.isWithinDistance(voidPos, 6)) {
                float newToxicity = Math.max(currentToxicity - 10f, 0f);
                if (newToxicity < currentToxicity) {
                    recentPropagation += newToxicity;
                    AirHandler.SetToxicity(world, pos, newToxicity);
                    changed = true;
                }
            }
        }

        // Handle emitters
        for (BlockPos emitterPos : AirHandler.getToxinEmitters(world)) {
            if (pos.isWithinDistance(emitterPos, 6)) {
                float newToxicity = Math.min(currentToxicity + 10f, MAX_TOXICITY);
                if (newToxicity > currentToxicity) {
                    recentPropagation += newToxicity;
                    AirHandler.SetToxicity(world, pos, newToxicity);
                    changed = true;
                }
            }
        }

        // Spread to neighbors
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.offset(dir);
            float neighborToxicity = AirHandler.GetToxicity(world, neighbor);
            if (neighborToxicity == -1) continue;

            float avg = (currentToxicity + neighborToxicity) / 2f;
            if (Math.abs(neighborToxicity - currentToxicity) > 1f) {
                recentPropagation += avg;
                AirHandler.SetToxicity(world, neighbor, avg);
                markDirty(world, neighbor); // <-- Mark neighbors as dirty for future ticks
                changed = true;
            }
        }

        // If this block's toxicity was reduced, re-mark it
        if (changed) {
            markDirty(world, pos);
        }
    }
    private static boolean isSkyExposed(World world, BlockPos pos) {
        return world.getLightLevel(LightType.SKY, pos) > 0;
    }
}