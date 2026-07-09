package com.boosters.mixin;

import com.boosters.BoostersConfig;
import com.boosters.BoostersStats;
import com.boosters.util.ThrottleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Throttles ticking of block entities far from every player. Types that are
 * timing-sensitive or visibly animated (hoppers, pistons, beacons, ...) are
 * excluded via {@link BoostersConfig#blockEntityThrottleExcludeTypes}.
 */
@Mixin(targets = "net.minecraft.world.level.chunk.LevelChunk$BoundTickingBlockEntity")
public abstract class BlockEntityThrottleMixin {

	private static final double SEARCH_RADIUS = 1_000_000.0;

	@Shadow
	@Final
	private BlockEntity blockEntity;

	@Shadow
	public abstract BlockPos getPos();

	@Shadow
	public abstract String getType();

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

		BlockPos pos = getPos();
		double cx = pos.getX() + 0.5;
		double cy = pos.getY() + 0.5;
		double cz = pos.getZ() + 0.5;

		Player nearest = level.getNearestPlayer(cx, cy, cz, SEARCH_RADIUS, false);
		if (nearest == null) {
			return;
		}

		double distance = Math.sqrt(nearest.distanceToSqr(cx, cy, cz));
		int interval = ThrottleUtil.intervalForDistance(distance, config.blockEntityThrottleStartDistance,
				config.blockEntityThrottleStartDistance * 3L, 1, config.blockEntityThrottleIntervalTicks);

		if (!ThrottleUtil.shouldTick(level.getGameTime(), pos.hashCode(), interval)) {
			BoostersStats.blockEntityTicksSkipped.incrementAndGet();
			ci.cancel();
		}
	}
}
