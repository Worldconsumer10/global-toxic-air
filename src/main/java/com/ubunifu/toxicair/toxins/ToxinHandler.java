package com.ubunifu.toxicair.toxins;

import com.ubunifu.toxicair.ToxicAir;
import com.ubunifu.toxicair.blocks.AirPurifier.AirPurifierBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

public class ToxinHandler {
    public static HashMap<PlayerEntity, Boolean> PLAYER_IS_IN_TOXIN = new HashMap<>();
    static HashMap<ServerWorld, Set<BlockPos>> AIR_PURIFIER_BLOCKS = new HashMap<>();
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

        if (tickCount != 1 && tickCount % TICK_INTERVAL != 0) {
            entityTickCounters.put(entityId, tickCount);
            return;
        }
        entityTickCounters.put(entityId, 0);

        World world = entity.getWorld();
        if (world.isClient || !(world instanceof ServerWorld serverWorld)) return;

        BlockPos origin = entity.getBlockPos();
        BlockBox scanBox = BlockBox.create(origin.add(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS),
                origin.add(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS));

        Set<BlockPos> purifiers = ToxinHandler.AIR_PURIFIER_BLOCKS
                .computeIfAbsent(serverWorld, w -> new ConcurrentSkipListSet<>());

        BlockPos.stream(scanBox)
                .filter(pos -> serverWorld.getBlockEntity(pos) instanceof AirPurifierBlockEntity)
                .forEach(pos -> purifiers.add(pos.toImmutable()));
    }

    public static boolean isToxicAir(World world, BlockPos blockPos, boolean logging) {
        if (world.isClient) return true;
        Set<BlockPos> purifierPositions = AIR_PURIFIER_BLOCKS.getOrDefault(world, Collections.emptySet());

        if (logging) ToxicAir.LOGGER.info("Purifiers: "+purifierPositions.size());
        for (BlockPos purifierPos : purifierPositions) {
            double maxDistance = GetPurifierStrength(world, purifierPos);
            if (maxDistance <= 0) continue;
            double distance = AStarAirAlgorithm.ComputeDistance(world,blockPos,purifierPos);
            if (distance < maxDistance)
            {
                if (logging) ToxicAir.LOGGER.info("Nearest Purifier: "+distance);
                return false;
            }
            if (logging) ToxicAir.LOGGER.info("Purifier too far.");
        }
        return true; // No reachable purifiers = toxic
    }
}
