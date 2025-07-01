package com.ubunifu.toxicair;

import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class ToxicAirPropagator {
    private static final float MAX_TOXICITY = 100f;
    static final float MAX_UPDATE_PER_TICK = 2;

    public static void tick(World world) {
        Long2FloatOpenHashMap updated = new Long2FloatOpenHashMap();
        updated.defaultReturnValue(0f);

        float toUpdate = MAX_UPDATE_PER_TICK;
        for (long key : AirHandler.TOXICITY_MAP.keySet()) {
            if (toUpdate <= 0) break;
            toUpdate--;
            BlockPos pos = BlockPos.fromLong(key);
            float currentToxicity = AirHandler.getToxicity(pos);

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.offset(dir);
                long neighborKey = neighbor.asLong();

                if (isSkyExposed(world, neighbor)) {
                    updated.put(neighborKey, MAX_TOXICITY);
                } else {
                    float neighborTox = AirHandler.getToxicity(neighbor);
                    if (currentToxicity > neighborTox) {
                        updated.put(neighborKey, currentToxicity); // Or some function of it
                    }
                }
            }
        }

        updated.forEach((key, value) -> {
            if (value > AirHandler.getToxicity(BlockPos.fromLong(key))) {
                AirHandler.setToxicity(BlockPos.fromLong(key), value);
            }
        });
    }

    private static boolean isSkyExposed(World world, BlockPos pos) {
        return world.getLightLevel(LightType.SKY, pos) > 0;
    }
}