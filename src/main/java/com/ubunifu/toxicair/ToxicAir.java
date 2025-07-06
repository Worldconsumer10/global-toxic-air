package com.ubunifu.toxicair;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.ubunifu.toxicair.blocks.ModBlockEntities;
import com.ubunifu.toxicair.blocks.ModBlocks;
import com.ubunifu.toxicair.effect.ModEffects;
import com.ubunifu.toxicair.itemgroups.ModItemGroups;
import com.ubunifu.toxicair.potions.ModPotionRecipe;
import com.ubunifu.toxicair.potions.ModPotions;
import com.ubunifu.toxicair.toxins.AStarAirAlgorithm;
import com.ubunifu.toxicair.toxins.WorldStaticNodeCache;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToxicAir implements ModInitializer {
	public static final String MOD_ID = "toxic-air";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	@Override
	public void onInitialize() {
		ModEffects.Register();
		ModPotions.Register();
		ModPotionRecipe.Register();
		ModBlocks.Register();
		ModBlockEntities.Register();
		ModItemGroups.Register();
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerWorld world : server.getWorlds())
			{
                try {
                    new WorldStaticNodeCache(world);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
		});
	}
}