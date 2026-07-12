package com.boosters.mixin;

import com.boosters.BoostersConfig;
import com.boosters.BoostersStats;
import com.boosters.compat.ModCompat;
import com.boosters.util.ThrottleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Throttles ticking of block entities far from every player. Types that are
 * timing-sensitive or visibly animated (hoppers, pistons, beacons, ...) are
 * excluded via {@link BoostersConfig#blockEntityThrottleExcludeTypes}.
 *
 * <p>Like {@link MobAiThrottleMixin}, the nearest-player search only runs once every
 * {@link #RECHECK_INTERVAL_TICKS} ticks per block entity, not on every single tick -
 * a block entity's distance to the player doesn't change fast enough for per-tick
 * precision to matter, and checking it that often was costing more than it saved.
 */
@Mixin(targets = "net.minecraft.world.level.chunk.LevelChunk$BoundTickingBlockEntity")
public abstract class BlockEntityThrottleMixin {

	private static final double SEARCH_RADIUS = 1_000_000.0;
	private static final int RECHECK_INTERVAL_TICKS = 10;

	@Shadow
	@Final
	private BlockEntity blockEntity;

	@Shadow
	public abstract BlockPos getPos();

	@Shadow
	public abstract String getType();

	@Unique
	private int boosters$cachedInterval = 1;

	@Unique
	private long boosters$nextRecheckTick = Long.MIN_VALUE;

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void boosters$throttleTick(CallbackInfo ci) {
		BoostersConfig config = BoostersConfig.get();
		if (!config.enableBlockEntityThrottle) {
			return;
		}

		String type = getType();
		for (String excluded : config.blockEntityThrottleExcludeTypes) {
			if (type.contains(excluded)) {
				return;
			}
		}

		Level level = blockEntity.getLevel();
		if (level == null) {
			return;
		}

		long gameTime = level.getGameTime();
		BlockPos pos = getPos();

		if (gameTime >= boosters$nextRecheckTick) {
			double startDistance = config.blockEntityThrottleStartDistance * ModCompat.distanceMultiplier();
			double maxDistance = startDistance * 3.0;

			double cx = pos.getX() + 0.5;
			double cy = pos.getY() + 0.5;
			double cz = pos.getZ() + 0.5;

			Player nearest = level.getNearestPlayer(cx, cy, cz, SEARCH_RADIUS, false);
			double distance = nearest == null ? maxDistance : Math.sqrt(nearest.distanceToSqr(cx, cy, cz));

			boosters$cachedInterval = ThrottleUtil.intervalForDistance(distance, startDistance,
					maxDistance, 1, config.blockEntityThrottleIntervalTicks);
			boosters$nextRecheckTick = gameTime + RECHECK_INTERVAL_TICKS;
		}

		if (!ThrottleUtil.shouldTick(gameTime, pos.hashCode(), boosters$cachedInterval)) {
			BoostersStats.incrementBlockEntityTicksSkipped();
			ci.cancel();
		}
	}
}
