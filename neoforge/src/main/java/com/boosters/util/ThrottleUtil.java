package com.boosters.util;

public final class ThrottleUtil {
	private ThrottleUtil() {
	}

	/**
	 * Linearly scales the tick interval between minInterval (at/below startDistance)
	 * and maxInterval (at/above maxDistance).
	 */
	public static int intervalForDistance(double distance, double startDistance, double maxDistance,
			int minInterval, int maxInterval) {
		if (distance <= startDistance) {
			return 1;
		}
		if (distance >= maxDistance || maxDistance <= startDistance) {
			return maxInterval;
		}
		double t = (distance - startDistance) / (maxDistance - startDistance);
		return (int) Math.round(minInterval + t * (maxInterval - minInterval));
	}

	/**
	 * Spreads throttled updates across ticks using a per-object salt (e.g. entity id)
	 * so that every throttled entity doesn't recompute on the same world tick.
	 */
	public static boolean shouldTick(long gameTime, int salt, int interval) {
		if (interval <= 1) {
			return true;
		}
		return Math.floorMod(gameTime + salt, interval) == 0;
	}
}
