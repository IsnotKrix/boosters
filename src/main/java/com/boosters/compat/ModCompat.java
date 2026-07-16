package com.boosters.compat;

import com.boosters.BoostersConfig;
import com.boosters.BoostersMod;
import net.fabricmc.loader.api.FabricLoader;

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

	/** How much tighter to pull in our own distance thresholds when Sodium is present. */
	private static final double SODIUM_DISTANCE_MULTIPLIER = 0.75;

	private static boolean entityCullingModPresent;
	private static boolean sodiumPresent;
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

		FabricLoader loader = FabricLoader.getInstance();
		entityCullingModPresent = loader.isModLoaded("entityculling");
		sodiumPresent = loader.isModLoaded("sodium");

		checkMod(loader, "sodium", "Sodium");
		checkMod(loader, "entityculling", "EntityCulling");
		logIfPresent(loader, "lithium", "Lithium", "game logic optimization - Boosters's AI throttle is additive, not a duplicate");
		logIfPresent(loader, "c2me", "C2ME", "chunk generation/loading - different subsystem, no conflict");
		logIfPresent(loader, "starlight", "Starlight", "lighting engine - different subsystem, no conflict");
		logIfPresent(loader, "ferritecore", "FerriteCore", "RAM usage reduction - different subsystem, no conflict");
		logIfPresent(loader, "krypton", "Krypton", "networking optimization - different subsystem, no conflict");
		logIfPresent(loader, "noisium", "Noisium", "terrain noise generation optimization - different subsystem, no conflict");
		logIfPresent(loader, "modernfix", "ModernFix", "mixed startup/memory/rendering optimization - different subsystem, no conflict");
		logIfPresent(loader, "immediatelyfast", "ImmediatelyFast", "HUD/text/immediate-mode rendering optimization - different subsystem, no conflict");
		logIfPresent(loader, "vmp", "VMP", "server chunk/network throughput optimization - different subsystem, no conflict");
		logIfPresent(loader, "bobby", "Bobby", "distant chunk caching - different subsystem, no conflict");
		logIfPresent(loader, "iris", "Iris", "shaders - different subsystem, no conflict");
		if (entityCullingModPresent) {
			BoostersMod.LOGGER.info(
					"Detected a dedicated entity-culling mod (entityculling) - Boosters is disabling its own entity culling to avoid duplicating it.");
		}
		if (sodiumPresent) {
			BoostersMod.LOGGER.info(
					"Detected Sodium - Boosters is pulling its own distance thresholds in by {}x. Sodium already removes "
							+ "the GPU-side rendering bottleneck, so the CPU-side work Boosters targets (AI, entities, "
							+ "particles) becomes relatively more of the remaining cost; throttling it harder pays off more.",
					SODIUM_DISTANCE_MULTIPLIER);
		} else {
			BoostersMod.LOGGER.info(
					"Sodium not detected - Boosters only throttles/culls, it doesn't replace the chunk renderer. "
							+ "For the biggest FPS gain from world rendering itself, pair Boosters with Sodium.");
		}

		if (!detectedMods.isEmpty()) {
			detectedModsSummary = String.join(", ", detectedMods);
		}
	}

	private static void checkMod(FabricLoader loader, String modId, String displayName) {
		if (loader.isModLoaded(modId)) {
			detectedMods.add(displayName);
		}
	}

	private static void logIfPresent(FabricLoader loader, String modId, String displayName, String note) {
		if (loader.isModLoaded(modId)) {
			BoostersMod.LOGGER.info("Detected installed optimization mod '{}': {}.", modId, note);
			detectedMods.add(displayName);
		}
	}

	public static boolean shouldDeferEntityCulling() {
		return entityCullingModPresent && BoostersConfig.get().deferToDedicatedEntityCullingMods;
	}

	/**
	 * Multiplier applied to distance thresholds (culling/throttling start distances).
	 * Below 1.0 when Sodium is present and the user hasn't opted out, pulling every
	 * threshold closer to the player for extra throttling on top of what Sodium already
	 * buys on the rendering side.
	 */
	public static double distanceMultiplier() {
		if (sodiumPresent && BoostersConfig.get().extraAggressiveWithSodium) {
			return SODIUM_DISTANCE_MULTIPLIER;
		}
		return 1.0;
	}

	/** Comma-separated display names of every recognized optimization mod found installed, or "none". */
	public static String detectedModsSummary() {
		return detectedModsSummary;
	}
}
