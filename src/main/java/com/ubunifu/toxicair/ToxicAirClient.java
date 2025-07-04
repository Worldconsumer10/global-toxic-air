package com.ubunifu.toxicair;

import com.ubunifu.toxicair.blocks.ModBlocks;
import com.ubunifu.toxicair.effect.ModEffects;
import com.ubunifu.toxicair.networking.ToxicAirPackets;
import com.ubunifu.toxicair.potions.ModPotionRecipe;
import com.ubunifu.toxicair.potions.ModPotions;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ToxicAirClient implements ClientModInitializer {
	public static final String MOD_ID = "toxic-air";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	int particleTick;
	int MAX_PARTICLES = 10;
	@Override
	public void onInitializeClient() {
		ToxicAirPackets.registerListener();
		ClientTickEvents.END_WORLD_TICK.register(server->{
			particleTick++;
			if (particleTick % 20 == 0) return;
			AirHandler.GetToxicityMap().forEach((world, map) -> {
				if (!world.isClient) return; // Safety check — do this on the server

				for (AbstractClientPlayerEntity player : ((ClientWorld) world).getPlayers()) {
					Vec3d playerPos = player.getPos();

					// Collect and sort nearest N blocks with non-zero toxicity
                    List<Long2FloatMap.Entry> nearest = map.long2FloatEntrySet().stream()
							.sorted(Comparator.comparingDouble(e -> BlockPos.fromLong(e.getLongKey()).getSquaredDistance(playerPos)))
							.limit(MAX_PARTICLES)
							.toList();

					for (Map.Entry<Long, Float> entry : nearest) {
						BlockPos blockPos = BlockPos.fromLong(entry.getKey());
						Vec3d blockCenter = blockPos.toCenterPos();

						DefaultParticleType particleType = ParticleTypes.BUBBLE;
						if (entry.getValue() > 0)
							particleType = ParticleTypes.SOUL;
						world.addImportantParticle(
								particleType,
								true,
								blockCenter.getX(),
								blockCenter.getY(),
								blockCenter.getZ(),
								0, 0.1, 0
						);
					}
				}
			});
		});
	}
}