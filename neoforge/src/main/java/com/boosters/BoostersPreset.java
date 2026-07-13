package com.boosters;

/**
 * Ready-made intensity presets so users don't have to understand every slider.
 * Each named preset writes a full set of the numeric distance/interval/density
 * fields into a {@link BoostersConfig}; the on/off toggles, exclusion list and
 * compatibility flags are intentionally left alone (a preset changes how hard we
 * throttle, not which features exist).
 *
 * <p>{@link #CUSTOM} is the "none of the above" marker used once a user edits any
 * individual value away from a named preset.
 */
public enum BoostersPreset {
	// name              aiStart aiMax aiInt aiMaxInt  entCull entDetail  beStart beInt  itemStart itemInt  partDensity partCull  berMult
	QUALITY("Quality", 64, 192, 2, 6, 160.0, 64.0, 64, 3, 64, 3, 0.80, 48.0, 0.75),
	BALANCED("Balanced", 40, 128, 2, 10, 112.0, 48.0, 48, 4, 48, 4, 0.60, 32.0, 0.60),
	PERFORMANCE("Performance", 24, 96, 3, 16, 72.0, 32.0, 32, 6, 32, 6, 0.35, 24.0, 0.35),
	EXTREME("Extreme", 16, 64, 4, 24, 48.0, 20.0, 20, 10, 20, 10, 0.15, 16.0, 0.20),
	CUSTOM("Custom", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

	private static final double EPSILON = 1.0e-6;

	private final String displayName;
	private final int aiStart;
	private final int aiMax;
	private final int aiInterval;
	private final int aiMaxInterval;
	private final double entityCull;
	private final double entityDetail;
	private final int beStart;
	private final int beInterval;
	private final int itemStart;
	private final int itemInterval;
	private final double particleDensity;
	private final double particleCull;
	private final double berMultiplier;

	BoostersPreset(String displayName, int aiStart, int aiMax, int aiInterval, int aiMaxInterval,
			double entityCull, double entityDetail, int beStart, int beInterval, int itemStart, int itemInterval,
			double particleDensity, double particleCull, double berMultiplier) {
		this.displayName = displayName;
		this.aiStart = aiStart;
		this.aiMax = aiMax;
		this.aiInterval = aiInterval;
		this.aiMaxInterval = aiMaxInterval;
		this.entityCull = entityCull;
		this.entityDetail = entityDetail;
		this.beStart = beStart;
		this.beInterval = beInterval;
		this.itemStart = itemStart;
		this.itemInterval = itemInterval;
		this.particleDensity = particleDensity;
		this.particleCull = particleCull;
		this.berMultiplier = berMultiplier;
	}

	public String displayName() {
		return displayName;
	}

	/** Writes this preset's numeric values into the given config. No-op for CUSTOM. */
	public void applyTo(BoostersConfig config) {
		if (this == CUSTOM) {
			return;
		}
		config.aiThrottleStartDistance = aiStart;
		config.aiThrottleMaxDistance = aiMax;
		config.aiThrottleIntervalTicks = aiInterval;
		config.aiThrottleMaxIntervalTicks = aiMaxInterval;
		config.entityCullingMaxDistance = entityCull;
		config.entityDetailCullDistance = entityDetail;
		config.blockEntityThrottleStartDistance = beStart;
		config.blockEntityThrottleIntervalTicks = beInterval;
		config.itemThrottleStartDistance = itemStart;
		config.itemThrottleIntervalTicks = itemInterval;
		config.particleDensityMultiplier = particleDensity;
		config.particleCullDistance = particleCull;
		config.blockEntityRendererDistanceMultiplier = berMultiplier;
	}

	private boolean matches(BoostersConfig config) {
		return this != CUSTOM
				&& config.aiThrottleStartDistance == aiStart
				&& config.aiThrottleMaxDistance == aiMax
				&& config.aiThrottleIntervalTicks == aiInterval
				&& config.aiThrottleMaxIntervalTicks == aiMaxInterval
				&& config.blockEntityThrottleStartDistance == beStart
				&& config.blockEntityThrottleIntervalTicks == beInterval
				&& config.itemThrottleStartDistance == itemStart
				&& config.itemThrottleIntervalTicks == itemInterval
				&& near(config.entityCullingMaxDistance, entityCull)
				&& near(config.entityDetailCullDistance, entityDetail)
				&& near(config.particleDensityMultiplier, particleDensity)
				&& near(config.particleCullDistance, particleCull)
				&& near(config.blockEntityRendererDistanceMultiplier, berMultiplier);
	}

	private static boolean near(double a, double b) {
		return Math.abs(a - b) < EPSILON;
	}

	/** Returns the named preset whose values the config currently matches, or {@link #CUSTOM}. */
	public static BoostersPreset matching(BoostersConfig config) {
		for (BoostersPreset preset : values()) {
			if (preset.matches(config)) {
				return preset;
			}
		}
		return CUSTOM;
	}
}
