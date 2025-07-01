package com.ubunifu.toxicair;

import com.google.common.collect.ImmutableList;
import com.ubunifu.toxicair.blocks.ModBlocks;
import com.ubunifu.toxicair.effect.ModEffects;
import com.ubunifu.toxicair.potions.ModPotionRecipe;
import com.ubunifu.toxicair.potions.ModPotions;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToxicAir implements ModInitializer {
	public static final String MOD_ID = "toxic-air";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	@Override
	public void onInitialize() {
		ModEffects.Register();
		ModPotions.Register();
		ModPotionRecipe.Register();
		ModBlocks.Register();
		ModItemGroups.Register();
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerWorld world : server.getWorlds()) {
				ToxicAirPropagator.tick(world);
			}
		});
	}
}