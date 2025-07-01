package com.ubunifu.toxicair;

import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class AirHandler {
    public static final float MAX_TOXICITY = 100f;
    public static final Long2FloatOpenHashMap TOXICITY_MAP = new Long2FloatOpenHashMap();


    public static float getToxicity(BlockPos pos) {
        return TOXICITY_MAP.get(pos.asLong());
    }

    public static void setToxicity(BlockPos pos, float toxicity) {
        TOXICITY_MAP.put(pos.asLong(), toxicity);
    }

    public static void createIfPossible(World world, BlockPos pos)
    {
        long key = pos.asLong();
        float cached = TOXICITY_MAP.get(key);
        if (cached != -1f) return;
        float computed = computeToxicity(world, pos);
        TOXICITY_MAP.put(key, computed);
    }

    public static float getOrComputeToxicity(World world, BlockPos pos) {
        long key = pos.asLong();
        float cached = TOXICITY_MAP.get(key);
        if (cached != -1f) return cached;

        float computed = computeToxicity(world, pos);
        TOXICITY_MAP.put(key, computed);
        return computed;
    }

    private static float computeToxicity(World world, BlockPos blockPos) {
        int skyLight = world.getLightLevel(LightType.SKY, blockPos);
        if (skyLight > 0) return MAX_TOXICITY;

        for (Direction dir : Direction.values()) {
            BlockPos neighbor = blockPos.offset(dir);
            if (world.getLightLevel(LightType.SKY, neighbor) > 0) {
                return MAX_TOXICITY;
            }
        }

        return 0f;
    }

    static {
        TOXICITY_MAP.defaultReturnValue(-1f); // fallback if not cached
    }
}
