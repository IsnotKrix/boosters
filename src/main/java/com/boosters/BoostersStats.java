package com.boosters;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Live counters incremented by the throttling/culling mixins, snapshotted once per
 * second into "per second" rates for display (e.g. the F3 status line). Shared
 * between the main and client source sets since, in singleplayer, both run in the
 * same JVM (integrated server) - this does not reflect a remote dedicated server.
 */
public final class BoostersStats {
	private BoostersStats() {
	}

	public static final AtomicInteger aiStepsSkipped = new AtomicInteger();
	public static final AtomicInteger blockEntityTicksSkipped = new AtomicInteger();
	public static final AtomicInteger entitiesCulled = new AtomicInteger();
	public static final AtomicInteger particlesDropped = new AtomicInteger();

	private static volatile int aiStepsSkippedPerSecond;
	private static volatile int blockEntityTicksSkippedPerSecond;
	private static volatile int entitiesCulledPerSecond;
	private static volatile int particlesDroppedPerSecond;

	public static void snapshotAndReset() {
		aiStepsSkippedPerSecond = aiStepsSkipped.getAndSet(0);
		blockEntityTicksSkippedPerSecond = blockEntityTicksSkipped.getAndSet(0);
		entitiesCulledPerSecond = entitiesCulled.getAndSet(0);
		particlesDroppedPerSecond = particlesDropped.getAndSet(0);
	}

	public static int aiStepsSkippedPerSecond() {
		return aiStepsSkippedPerSecond;
	}

	public static int blockEntityTicksSkippedPerSecond() {
		return blockEntityTicksSkippedPerSecond;
	}

	public static int entitiesCulledPerSecond() {
		return entitiesCulledPerSecond;
	}

	public static int particlesDroppedPerSecond() {
		return particlesDroppedPerSecond;
	}
}
