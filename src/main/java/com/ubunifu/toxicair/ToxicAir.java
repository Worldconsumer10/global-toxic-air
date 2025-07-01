package com.ubunifu.toxicair;

import com.google.common.collect.ImmutableList;
import com.ubunifu.toxicair.effect.ModEffects;
import com.ubunifu.toxicair.potions.ModPotionRecipe;
import com.ubunifu.toxicair.potions.ModPotions;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToxicAir implements ModInitializer {
	public static final String MOD_ID = "toxic-air";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static int tickCounter = 0;
	@Override
	public void onInitialize() {
		ModEffects.Register();
		ModPotions.Register();
		ModPotionRecipe.Register();
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			tickCounter++;
			if (tickCounter % 5 == 0) { // Once per second
				for (ServerWorld world : server.getWorlds()) {
					ToxicAirPropagator.tick(world);
				}
			}
		});
	}
}