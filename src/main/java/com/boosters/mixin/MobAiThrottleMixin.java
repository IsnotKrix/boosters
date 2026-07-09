package com.boosters.mixin;

import com.boosters.BoostersConfig;
import com.boosters.BoostersStats;
import com.boosters.util.ThrottleUtil;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Extends vanilla's existing "every other tick" goal-selector throttle (see
 * Mob#serverAiStep) with an additional distance-based throttle: mobs far from
 * every player skip sensing/goalSelector/targetSelector/navigation entirely
 * on most ticks instead of merely alternating full/partial goal updates.
 */
@Mixin(Mob.class)
public abstract class MobAiThrottleMixin {

	private static final double SEARCH_RADIUS = 1_000_000.0;

	@Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true)
	private void boosters$throttleAiStep(CallbackInfo ci) {
		BoostersConfig config = BoostersConfig.get();
		if (!config.enableAiThrottle) {
			return;
		}

		Mob self = (Mob) (Object) this;
		Level level = self.level();
		Player nearest = level.getNearestPlayer(self, SEARCH_RADIUS);
		if (nearest == null) {
			return;
		}

		double distance = Math.sqrt(nearest.distanceToSqr(self));
		int interval = ThrottleUtil.intervalForDistance(distance, config.aiThrottleStartDistance,
				config.aiThrottleMaxDistance, config.aiThrottleIntervalTicks, config.aiThrottleMaxIntervalTicks);

		if (!ThrottleUtil.shouldTick(self.tickCount, self.getId(), interval)) {
			BoostersStats.aiStepsSkipped.incrementAndGet();
			ci.cancel();
		}
	}
}
