package com.boosters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public final class BoostersConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FMLPaths.CONFIGDIR.get().resolve("boosters.json");

	private static BoostersConfig instance = new BoostersConfig();

	/** Master switch. When off, every throttle/cull mixin below is a no-op, regardless of its own toggle. */
	public boolean enabled = true;

	// --- Active preset (Quality/Balanced/Performance/Extreme, or Custom) ---
	public BoostersPreset preset = BoostersPreset.PERFORMANCE;

	// --- Mob AI throttling (server/logic side, works in singleplayer too) ---
	public boolean enableAiThrottle = true;
	/** Distance from the nearest player (blocks) at which AI throttling starts kicking in. */
	public int aiThrottleStartDistance = 24;
	/** Distance at which AI throttling reaches its maximum (slowest) interval. */
	public int aiThrottleMaxDistance = 96;
	/** How many ticks between goal-selector updates for the closest throttled mobs. */
	public int aiThrottleIntervalTicks = 3;
	/** How many ticks between goal-selector updates for the farthest mobs (at/beyond max distance). */
	public int aiThrottleMaxIntervalTicks = 16;

	// --- Client-side entity render culling ---
	public boolean enableEntityCulling = true;
	/** Entities farther than this from the camera are never rendered, regardless of visibility. */
	public double entityCullingMaxDistance = 72.0;
	/** Beyond this distance, skip nameplates/extra decoration rendering even if the entity itself is drawn. */
	public double entityDetailCullDistance = 32.0;

	// --- Block entity (chest, furnace, sign, etc.) tick throttling ---
	public boolean enableBlockEntityThrottle = true;
	public int blockEntityThrottleStartDistance = 32;
	public int blockEntityThrottleIntervalTicks = 6;
	/**
	 * Block entity type names (substring match) that are never throttled because
	 * players notice their timing directly (item movement, redstone, animations).
	 */
	public List<String> blockEntityThrottleExcludeTypes = Arrays.asList(
			"hopper", "piston", "beacon", "conduit", "brewing_stand", "sculk_sensor", "sculk_shrieker");

	// --- Dropped item / XP orb tick throttling (server/logic side) ---
	public boolean enableItemThrottle = true;
	/** Distance from the nearest player (blocks) at which item/orb throttling starts kicking in. */
	public int itemThrottleStartDistance = 32;
	/** How many ticks between updates for the farthest items/orbs (at/beyond 3x the start distance). */
	public int itemThrottleIntervalTicks = 6;

	// --- Particle reduction (client-side) ---
	public boolean enableParticleReduction = true;
	/** Fraction of particles that are actually allowed to spawn, 0.0-1.0. */
	public double particleDensityMultiplier = 0.35;
	/** Particles requested farther than this from the camera are always dropped. */
	public double particleCullDistance = 24.0;

	// --- Block entity renderer (chest lids, sign text, skulls, ...) view distance ---
	public boolean enableBlockEntityRendererCulling = true;
	/** Multiplier applied to vanilla's default 64-block BER view distance (0.1-1.0). */
	public double blockEntityRendererDistanceMultiplier = 0.35;

	// --- Compatibility with other optimization mods ---
	/** When a dedicated entity-culling mod is detected, let it own culling instead of fighting it. */
	public boolean deferToDedicatedEntityCullingMods = true;
	/**
	 * When Embeddium (the NeoForge port of Sodium) is detected, pull all distance thresholds
	 * in further (see {@code ModCompat#distanceMultiplier()}). Embeddium removes the GPU-side
	 * rendering bottleneck, so throttling the CPU-side work harder pays off more with it installed.
	 */
	public boolean extraAggressiveWithEmbeddium = true;

	// --- F3 debug screen status line ---
	public boolean showF3Status = true;

	public static BoostersConfig get() {
		return instance;
	}

	public static void load() {
		if (Files.exists(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
				BoostersConfig loaded = GSON.fromJson(reader, BoostersConfig.class);
				if (loaded != null) {
					instance = loaded;
				}
			} catch (IOException | RuntimeException e) {
				BoostersMod.LOGGER.warn("Failed to load boosters.json, using default values", e);
			}
		}
		save();
	}

	public static void save() {
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
				GSON.toJson(instance, writer);
			}
		} catch (IOException e) {
			BoostersMod.LOGGER.warn("Failed to save boosters.json", e);
		}
	}
}
