package com.boosters.compat;

import com.boosters.BoostersConfig;
import com.boosters.BoostersMod;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Detects popular optimization mods at startup so Boosters can defer to them
 * instead of duplicating/fighting over the same behavior. Only entity culling is
 * currently deferred (a dedicated culling mod does shape-based occlusion, which
 * beats our simple distance cutoff); the rest are informational since they operate
 * on different subsystems (chunk generation, lighting, networking, ...) and stack
 * fine alongside Boosters's throttling.
 */
public final class ModCompat {

	private static boolean entityCullingModPresent;
	private static boolean initialized;

	private ModCompat() {
	}

	public static void init() {
		if (initialized) {
			return;
		}
		initialized = true;

		FabricLoader loader = FabricLoader.getInstance();
		entityCullingModPresent = loader.isModLoaded("entityculling");

		logIfPresent(loader, "sodium", "renderer - no conflict with our mixins");
		logIfPresent(loader, "lithium", "game logic optimization - Boosters's AI throttle is additive, not a duplicate");
		logIfPresent(loader, "c2me", "chunk generation/loading - different subsystem, no conflict");
		logIfPresent(loader, "starlight", "lighting engine - different subsystem, no conflict");
		logIfPresent(loader, "ferritecore", "RAM usage reduction - different subsystem, no conflict");
		logIfPresent(loader, "krypton", "networking optimization - different subsystem, no conflict");
		logIfPresent(loader, "noisium", "terrain noise generation optimization - different subsystem, no conflict");
		if (entityCullingModPresent) {
			BoostersMod.LOGGER.info(
					"Detected a dedicated entity-culling mod (entityculling) - Boosters is disabling its own entity culling to avoid duplicating it.");
		}
	}

	private static void logIfPresent(FabricLoader loader, String modId, String note) {
		if (loader.isModLoaded(modId)) {
			BoostersMod.LOGGER.info("Detected installed optimization mod '{}': {}.", modId, note);
		}
	}

	public static boolean shouldDeferEntityCulling() {
		return entityCullingModPresent && BoostersConfig.get().deferToDedicatedEntityCullingMods;
	}
}
