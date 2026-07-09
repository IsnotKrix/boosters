package com.boosters.mixin;

import com.boosters.BoostersConfig;
import com.boosters.BoostersStats;
import com.boosters.compat.ModCompat;
import com.boosters.util.ThrottleUtil;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Extends vanilla's existing "every other tick" goal-selector throttle (see
 * Mob#serverAiStep) with an additional distance-based throttle: mobs far from
 * every player skip sensing/goalSelector/targetSelector/navigation entirely
 * on most ticks instead of merely alternating full/partial goal updates.
 *
 * <p>The nearest-player search + distance math only runs once every
 * {@link #RECHECK_INTERVAL_TICKS} ticks per mob, with the resulting throttle
 * interval cached in between (same "don't poll every tick" pattern Lithium uses
 * for its AI tracking). A mob's distance to the player rarely changes enough in
 * half a second to matter, and re-deriving it every single tick for every single
 * mob was adding more overhead than the throttling itself was saving back.
 */
@Mixin(Mob.class)
public abstract class MobAiThrottleMixin {

	private static final double SEARCH_RADIUS = 1_000_000.0;
	private static final int RECHECK_INTERVAL_TICKS = 10;

	@Unique
	private int boosters$cachedInterval = 1;

	@Unique
	private long boosters$nextRecheckTick = Long.MIN_VALUE;

	@Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true)
	private void boosters$throttleAiStep(CallbackInfo ci) {
		BoostersConfig config = BoostersConfig.get();
		if (!config.enableAiThrottle) {
			return;
		}

		Mob self = (Mob) (Object) this;

		if (self.tickCount >= boosters$nextRecheckTick) {
			double distanceMultiplier = ModCompat.distanceMultiplier();
			double startDistance = config.aiThrottleStartDistance * distanceMultiplier;
			double maxDistance = config.aiThrottleMaxDistance * distanceMultiplier;

			Level level = self.level();
			Player nearest = level.getNearestPlayer(self, SEARCH_RADIUS);
			double distance = nearest == null ? maxDistance : Math.sqrt(nearest.distanceToSqr(self));

			boosters$cachedInterval = ThrottleUtil.intervalForDistance(distance, startDistance,
					maxDistance, config.aiThrottleIntervalTicks, config.aiThrottleMaxIntervalTicks);
			boosters$nextRecheckTick = self.tickCount + RECHECK_INTERVAL_TICKS;
		}

		if (!ThrottleUtil.shouldTick(self.tickCount, self.getId(), boosters$cachedInterval)) {
			BoostersStats.incrementAiStepsSkipped();
			ci.cancel();
		}
	}
}
