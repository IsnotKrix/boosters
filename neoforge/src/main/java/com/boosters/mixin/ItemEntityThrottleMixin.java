package com.boosters.mixin;

import com.boosters.BoostersConfig;
import com.boosters.BoostersStats;
import com.boosters.compat.ModCompat;
import com.boosters.util.ThrottleUtil;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Throttles dropped item entities far from every player - the same "cache and
 * recheck periodically, tick less often at distance" pattern as
 * {@link MobAiThrottleMixin} and {@link BlockEntityThrottleMixin}, applied to a
 * common lag source neither of those cover (uncollected farm/mob-drop item piles).
 *
 * <p>This graduates the tick interval rather than freezing items outright, so
 * physics, stack merging and the despawn timer all keep progressing for distant
 * items - just less often - instead of items visibly hanging mid-air or never
 * despawning while unobserved.
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityThrottleMixin {

	private static final double SEARCH_RADIUS = 1_000_000.0;
	private static final int RECHECK_INTERVAL_TICKS = 10;

	@Unique
	private int boosters$cachedInterval = 1;

	@Unique
	private long boosters$nextRecheckTick = Long.MIN_VALUE;

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void boosters$throttleTick(CallbackInfo ci) {
		BoostersConfig config = BoostersConfig.get();
		if (!config.enableItemThrottle) {
			return;
		}

		ItemEntity self = (ItemEntity) (Object) this;

		if (self.tickCount >= boosters$nextRecheckTick) {
			double startDistance = config.itemThrottleStartDistance * ModCompat.distanceMultiplier();
			double maxDistance = startDistance * 3.0;

			Level level = self.level();
			Player nearest = level.getNearestPlayer(self, SEARCH_RADIUS);
			double distance = nearest == null ? maxDistance : Math.sqrt(nearest.distanceToSqr(self));

			boosters$cachedInterval = ThrottleUtil.intervalForDistance(distance, startDistance,
					maxDistance, 1, config.itemThrottleIntervalTicks);
			boosters$nextRecheckTick = self.tickCount + RECHECK_INTERVAL_TICKS;
		}

		if (!ThrottleUtil.shouldTick(self.tickCount, self.getId(), boosters$cachedInterval)) {
			BoostersStats.incrementItemTicksSkipped();
			ci.cancel();
		}
	}
}
