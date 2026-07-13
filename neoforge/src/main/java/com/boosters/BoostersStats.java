package com.boosters;

/**
 * Live counters incremented by the throttling/culling mixins, snapshotted once per
 * second into "per second" rates for display (e.g. the F3 status line). Shared
 * between the main and client source sets since, in singleplayer, both run in the
 * same JVM (integrated server) - this does not reflect a remote dedicated server.
 *
 * <p>Each counter has exactly one writer thread (the AI/block-entity ones are only
 * ever incremented from the world tick thread, the entity/particle ones only from
 * the render thread) and is only ever reset from {@link #snapshotAndReset()} on the
 * render thread. That single-writer/single-resetter pattern means a plain
 * {@code volatile int} is enough for correct visibility - an {@code AtomicInteger}'s
 * compare-and-swap retry loop buys nothing here and was measurably adding overhead
 * at the tens-of-thousands-of-increments-per-second range this mod can hit.
 */
public final class BoostersStats {
	private BoostersStats() {
	}

	private static volatile int aiStepsSkipped;
	private static volatile int blockEntityTicksSkipped;
	private static volatile int itemTicksSkipped;
	private static volatile int entitiesCulled;
	private static volatile int particlesDropped;

	private static volatile int aiStepsSkippedPerSecond;
	private static volatile int blockEntityTicksSkippedPerSecond;
	private static volatile int itemTicksSkippedPerSecond;
	private static volatile int entitiesCulledPerSecond;
	private static volatile int particlesDroppedPerSecond;

	public static void incrementAiStepsSkipped() {
		aiStepsSkipped++;
	}

	public static void incrementBlockEntityTicksSkipped() {
		blockEntityTicksSkipped++;
	}

	public static void incrementItemTicksSkipped() {
		itemTicksSkipped++;
	}

	public static void incrementEntitiesCulled() {
		entitiesCulled++;
	}

	public static void incrementParticlesDropped() {
		particlesDropped++;
	}

	public static void snapshotAndReset() {
		aiStepsSkippedPerSecond = aiStepsSkipped;
		blockEntityTicksSkippedPerSecond = blockEntityTicksSkipped;
		itemTicksSkippedPerSecond = itemTicksSkipped;
		entitiesCulledPerSecond = entitiesCulled;
		particlesDroppedPerSecond = particlesDropped;
		aiStepsSkipped = 0;
		blockEntityTicksSkipped = 0;
		itemTicksSkipped = 0;
		entitiesCulled = 0;
		particlesDropped = 0;
	}

	public static int aiStepsSkippedPerSecond() {
		return aiStepsSkippedPerSecond;
	}

	public static int blockEntityTicksSkippedPerSecond() {
		return blockEntityTicksSkippedPerSecond;
	}

	public static int itemTicksSkippedPerSecond() {
		return itemTicksSkippedPerSecond;
	}

	public static int entitiesCulledPerSecond() {
		return entitiesCulledPerSecond;
	}

	public static int particlesDroppedPerSecond() {
		return particlesDroppedPerSecond;
	}
}
