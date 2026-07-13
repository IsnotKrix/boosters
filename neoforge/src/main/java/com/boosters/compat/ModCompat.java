package com.boosters.compat;

import com.boosters.BoostersConfig;
import com.boosters.BoostersMod;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects popular optimization mods at startup so Boosters can defer to them
 * instead of duplicating/fighting over the same behavior. Only entity culling is
 * currently deferred (a dedicated culling mod does shape-based occlusion, which
 * beats our simple distance cutoff); the rest are informational since they operate
 * on different subsystems (chunk generation, lighting, networking, ...) and stack
 * fine alongside Boosters's throttling.
 */
public final class ModCompat {

	/** How much tighter to pull in our own distance thresholds when Embeddium is present. */
	private static final double EMBEDDIUM_DISTANCE_MULTIPLIER = 0.75;

	private static boolean entityCullingModPresent;
	private static boolean embeddiumPresent;
	private static boolean initialized;

	// Built once during init() and never touched again - the F3 status line reads this
	// every frame while open, so it must be a plain cached string, not rebuilt per call.
	private static final List<String> detectedMods = new ArrayList<>();
	private static String detectedModsSummary = "none";

	private ModCompat() {
	}

	public static void init() {
		if (initialized) {
			return;
		}
		initialized = true;

		ModList mods = ModList.get();
		entityCullingModPresent = mods.isLoaded("entityculling");
		embeddiumPresent = mods.isLoaded("embeddium");

		checkMod(mods, "embeddium", "Embeddium");
		checkMod(mods, "entityculling", "EntityCulling");
		logIfPresent(mods, "ferritecore", "FerriteCore", "RAM usage reduction - complements Boosters's menu memory cleanup, no conflict");
		logIfPresent(mods, "krypton", "Krypton", "networking optimization - different subsystem, no conflict");
		logIfPresent(mods, "noisium", "Noisium", "terrain noise generation optimization - different subsystem, no conflict");
		logIfPresent(mods, "modernfix", "ModernFix", "mixed startup/memory/rendering optimization - different subsystem, no conflict");
		logIfPresent(mods, "vmp", "VMP", "server chunk/network throughput optimization - different subsystem, no conflict");
		logIfPresent(mods, "iris", "Iris", "shaders - different subsystem, no conflict");
		if (entityCullingModPresent) {
			BoostersMod.LOGGER.info(
					"Detected a dedicated entity-culling mod (entityculling) - Boosters is disabling its own entity culling to avoid duplicating it.");
		}
		if (embeddiumPresent) {
			BoostersMod.LOGGER.info(
					"Detected Embeddium - Boosters is pulling its own distance thresholds in by {}x. Embeddium already removes "
							+ "the GPU-side rendering bottleneck, so the CPU-side work Boosters targets (AI, entities, "
							+ "particles) becomes relatively more of the remaining cost; throttling it harder pays off more.",
					EMBEDDIUM_DISTANCE_MULTIPLIER);
		} else {
			BoostersMod.LOGGER.info(
					"Embeddium not detected - Boosters only throttles/culls, it doesn't replace the chunk renderer. "
							+ "For the biggest FPS gain from world rendering itself, pair Boosters with Embeddium.");
		}

		if (!detectedMods.isEmpty()) {
			detectedModsSummary = String.join(", ", detectedMods);
		}
	}

	private static void checkMod(ModList mods, String modId, String displayName) {
		if (mods.isLoaded(modId)) {
			detectedMods.add(displayName);
		}
	}

	private static void logIfPresent(ModList mods, String modId, String displayName, String note) {
		if (mods.isLoaded(modId)) {
			BoostersMod.LOGGER.info("Detected installed optimization mod '{}': {}.", modId, note);
			detectedMods.add(displayName);
		}
	}

	public static boolean shouldDeferEntityCulling() {
		return entityCullingModPresent && BoostersConfig.get().deferToDedicatedEntityCullingMods;
	}

	/**
	 * Multiplier applied to distance thresholds (culling/throttling start distances).
	 * Below 1.0 when Embeddium is present and the user hasn't opted out, pulling every
	 * threshold closer to the player for extra throttling on top of what Embeddium already
	 * buys on the rendering side.
	 */
	public static double distanceMultiplier() {
		if (embeddiumPresent && BoostersConfig.get().extraAggressiveWithEmbeddium) {
			return EMBEDDIUM_DISTANCE_MULTIPLIER;
		}
		return 1.0;
	}

	/** Comma-separated display names of every recognized optimization mod found installed, or "none". */
	public static String detectedModsSummary() {
		return detectedModsSummary;
	}
}
