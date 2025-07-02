package com.ubunifu.toxicair;

import com.ubunifu.toxicair.blocks.ModBlocks;
import com.ubunifu.toxicair.effect.ModEffects;
import com.ubunifu.toxicair.networking.ToxicAirPackets;
import com.ubunifu.toxicair.potions.ModPotionRecipe;
import com.ubunifu.toxicair.potions.ModPotions;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
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
			for (ServerWorld world : server.getWorlds())
				ToxicAirPropagator.tick(world);
			AirHandler.ServerTick();
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			World world = handler.player.getWorld();
			Long2FloatOpenHashMap toxinMap = AirHandler.GetToxicityMap().get(world);
			if (toxinMap == null) return;

			Map<BlockPos, Float> visibleData = new HashMap<>();
			for (long key : toxinMap.keySet()) {
				visibleData.put(BlockPos.fromLong(key), toxinMap.get(key));
			}

			ToxicAirPackets.sendToxicitySync(handler.player, visibleData);
		});
	}
}