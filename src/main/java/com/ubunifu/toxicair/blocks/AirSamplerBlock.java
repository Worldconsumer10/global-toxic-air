package com.ubunifu.toxicair.blocks;

import com.ubunifu.toxicair.AirHandler;
import com.ubunifu.toxicair.ToxicAirPropagator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class AirSamplerBlock extends Block {
    public AirSamplerBlock(Settings settings) {
        super(settings);
    }
    int cooldown;

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getStackInHand(hand);
        cooldown--;
        if (heldItem.isEmpty() && cooldown <= 0)
        {
            cooldown = 2;
            double airQuality = Math.floor(100 - sampleAirQuality(pos,world));
            float toxinReduction = sampleAirCleaning(pos,world);
            float toxinIncrease = sampleAirPolluting(pos,world);
            String extraText = airQuality < 100 ? "Unbreathable":"Breathable";
            player.sendMessage(Text.of("Sample Collected:" +
                    "\nAir Quality: "+airQuality + "%" +
                    "\nNearby Air Cleaning: "+toxinReduction+"p/t"+
                    "\nNearby Air Polluting: "+(toxinIncrease == Float.MAX_VALUE ? "Infinite":toxinIncrease)+"p/t"+
                    "\nAir Status: "+extraText));
            return ActionResult.SUCCESS;
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }
    private float sampleAirPolluting(BlockPos blockPos,World world){
        BlockPos startPos = blockPos.add(-1, -1, -1);
        BlockPos endPos = blockPos.add(1, 1, 1);

        float toxicityLoss = 0;

        for (BlockPos pos : BlockPos.iterate(startPos, endPos)) {
            if (world.getLightLevel(LightType.SKY,pos)>0)
                return Float.MAX_VALUE;
            toxicityLoss += AirHandler.isToxinEmitter(world, pos) ? ToxicAirPropagator.EMITTER_BOOST : 0;
        }
        return toxicityLoss;
    }
    private float sampleAirCleaning(BlockPos blockPos,World world){
        BlockPos startPos = blockPos.add(-1, -1, -1);
        BlockPos endPos = blockPos.add(1, 1, 1);

        float toxicityLoss = 0;

        for (BlockPos pos : BlockPos.iterate(startPos, endPos)) {
            toxicityLoss += AirHandler.isToxinVoid(world, pos) ? ToxicAirPropagator.VOID_REDUCTION : 0;
        }
        return toxicityLoss;
    }
    private float sampleAirQuality(BlockPos blockPos,World world){
        BlockPos startPos = blockPos.add(-1, -1, -1);
        BlockPos endPos = blockPos.add(1, 1, 1);

        float minToxicity = 0;

        for (BlockPos pos : BlockPos.iterate(startPos, endPos)) {
            float toxicity = AirHandler.getOrCompute(world, pos);
            if (minToxicity < toxicity)
                minToxicity = toxicity;
        }
        return minToxicity;
    }
}
