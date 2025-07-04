package com.ubunifu.toxicair.blocks.AirPurifier;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.EitherMapCodec;
import com.ubunifu.toxicair.toxins.AirHandler;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class AirPurifierBlock extends Block implements BlockEntityProvider {
    private static final VoxelShape SHAPE = AirPurifierBlock.createCuboidShape(0,0,0,16,16,16);

    public AirPurifierBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AirPurifierBlockEntity(pos,state);
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);
        if (!AirHandler.isPurifierSaved(world,pos)) AirHandler.AddPurifier(world,pos);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        AirHandler.RemovePurifier(world,pos);
        return super.onBreak(world, pos, state, player);
    }
}
