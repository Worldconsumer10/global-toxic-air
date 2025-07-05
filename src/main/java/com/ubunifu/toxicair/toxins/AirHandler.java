package com.ubunifu.toxicair.toxins;

import com.ubunifu.toxicair.ToxicAir;
import com.ubunifu.toxicair.blocks.AirPurifier.AirPurifierBlockEntity;
import com.ubunifu.toxicair.blocks.ModBlockEntities;
import it.unimi.dsi.fastutil.longs.Long2FloatArrayMap;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.*;

public class AirHandler {
    static int DEFAULT_TOXIN = 100;
    private static final HashMap<World, Long2FloatArrayMap> CLEAN_AIR_MAP = new HashMap<>();
    private static final HashMap<World, Set<BlockPos>> PURIFIER_LOCATIONS = new HashMap<>();

    public static boolean isPurifierSaved(World world, BlockPos blockPos)
    { return PURIFIER_LOCATIONS.computeIfAbsent(world, w->new HashSet<>()).contains(blockPos); }
    public static void AddPurifier(World world, BlockPos blockPos){
        Set<BlockPos> blistList = PURIFIER_LOCATIONS.computeIfAbsent(world,w->new HashSet<>());
        blistList.add(blockPos.toImmutable());
        PURIFIER_LOCATIONS.put(world,blistList);
    }
    public static void RemovePurifier(World world, BlockPos blockPos){
        Set<BlockPos> blistList = PURIFIER_LOCATIONS.computeIfAbsent(world,w->new HashSet<>());
        blistList.remove(blockPos.toImmutable());
        PURIFIER_LOCATIONS.put(world,blistList);
    }

    private static boolean canHoldToxicity(World world, BlockPos blockPos){
        BlockState blockState = world.getBlockState(blockPos);
        return !blockState.isSolidBlock(world,blockPos)
                || blockState.canPathfindThrough(world,blockPos, NavigationType.AIR)
                || blockState.canPathfindThrough(world,blockPos, NavigationType.LAND)
                || blockState.canPathfindThrough(world,blockPos, NavigationType.WATER);
    }

    private static int getToxicity(BlockPos blockPos, World world, int depth, int maxDepth){
        if (depth > maxDepth) return 0;
        if (world.getBlockState(blockPos).isSolidBlock(world,blockPos)) return 0;
        int toxicity = DEFAULT_TOXIN;
        for (Direction dir : Direction.values()) {
            BlockPos test = blockPos.offset(dir);
            toxicity += getToxicity(test, world, depth + 1,maxDepth);
        }
        return toxicity;
    }

    public static void tick(MinecraftServer server, World world) {
        if (world.isClient) return;

        Set<BlockPos> purifiers = PURIFIER_LOCATIONS.computeIfAbsent(world, w -> new HashSet<>());
        Long2FloatArrayMap cleanAir = CLEAN_AIR_MAP.computeIfAbsent(world, w -> {
            Long2FloatArrayMap m = new Long2FloatArrayMap();
            m.defaultReturnValue(100f);
            return m;
        });

        Set<BlockPos> affectedBlockPos = new HashSet<>();

        for (BlockPos origin : purifiers) {
            AirPurifierBlockEntity airPurifierBlockEntity = world.getBlockEntity(origin, ModBlockEntities.AIR_PURIFIER_BLOCK_ENTITY).orElse(null);
            if (airPurifierBlockEntity == null) {
                ToxicAir.LOGGER.info("No purifier block entity at {}", origin);
                continue;
            }

            int budget = airPurifierBlockEntity.getStrength();
            if (budget <= 0) {
                ToxicAir.LOGGER.info("Purifier at {} has no budget", origin);
                continue;
            }

            ToxicAir.LOGGER.info("Purifier at {} starts with strength {}", origin, budget);

            Queue<BlockPos> queue = new ArrayDeque<>();
            Set<BlockPos> visited = new HashSet<>();
            queue.add(origin.toImmutable());
            visited.add(origin.toImmutable());

            while (budget > 0 && !queue.isEmpty()) {
                BlockPos pos = queue.poll().toImmutable();

                float toxicity = cleanAir.getOrDefault(pos.asLong(), 100);
                ToxicAir.LOGGER.debug("Checking {}: toxicity={}, budget={}", pos, toxicity, budget);

                if (toxicity <= 0) {
                    affectedBlockPos.add(pos);
                    continue;
                }

                if (budget >= toxicity) {
                    cleanAir.put(pos.asLong(), 0);
                    affectedBlockPos.add(pos);
                    budget -= (int) toxicity;
                    ToxicAir.LOGGER.info("Cleaned {} fully. Remaining budget: {}", pos, budget);
                } else {
                    float leftoverTox = toxicity - budget;
                    cleanAir.put(pos.asLong(), leftoverTox);
                    affectedBlockPos.add(pos);
                    ToxicAir.LOGGER.info("Cleaned {} partially. New toxicity: {}. Budget used up.", pos, leftoverTox);
                    budget = 0;
                }

                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = pos.offset(dir).toImmutable();
                    if (!visited.contains(neighbor) && canHoldToxicity(world, neighbor)) {
                        queue.add(neighbor);
                        // (intentionally not modifying visited — as per original logic)
                    }
                }
            }
        }

        cleanAir.forEach((blockLong, toxicity) -> {
            BlockPos blockPos = BlockPos.fromLong(blockLong);
            if (affectedBlockPos.contains(blockPos.toImmutable())) return;
            if (!canHoldToxicity(world, blockPos)) return;

            float newToxicity = toxicity + 1;
            if (newToxicity >= 100) {
                cleanAir.remove((long) blockLong);
                ToxicAir.LOGGER.debug("Toxicity at {} reverted to default and removed", blockPos);
            } else {
                cleanAir.put((long) blockLong, newToxicity);
                affectedBlockPos.add(blockPos.toImmutable());
                ToxicAir.LOGGER.debug("Toxicity at {} increased from {} to {}", blockPos, toxicity, newToxicity);
            }
        });
    }

    public static float getToxicity(BlockPos blockPos, World world) {
        if (world.isClient) return 100;
        Long2FloatArrayMap toxicityMap = CLEAN_AIR_MAP.computeIfAbsent(world, w->new Long2FloatArrayMap());
        return toxicityMap.getOrDefault(blockPos.toImmutable().asLong(),100);
    }
}
