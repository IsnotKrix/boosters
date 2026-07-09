package com.boosters;

import com.boosters.compat.ModCompat;
import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoostersMod implements ModInitializer {
	public static final String MOD_ID = "boosters";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		BoostersConfig.load();
		ModCompat.init();
		LOGGER.info("Boosters loaded - AI throttle interval={} ticks, block entity throttle interval={} ticks",
				BoostersConfig.get().aiThrottleIntervalTicks,
				BoostersConfig.get().blockEntityThrottleIntervalTicks);
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
