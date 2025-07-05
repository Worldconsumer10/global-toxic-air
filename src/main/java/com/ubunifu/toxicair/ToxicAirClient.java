package com.ubunifu.toxicair;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToxicAirClient implements ClientModInitializer {
	public static final String MOD_ID = "toxic-air";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	int particleTick;
	int MAX_PARTICLES = 10;
	@Override
	public void onInitializeClient() {
	}
}