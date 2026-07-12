package com.boosters.client.mixin;

import com.boosters.BoostersConfig;
import com.boosters.BoostersStats;
import com.boosters.compat.ModCompat;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds a hard distance cutoff on top of vanilla's frustum-only check, so entities
 * far beyond normal interaction range are never submitted for rendering at all.
 * No-ops when a dedicated entity-culling mod is present (see ModCompat) to avoid
 * two mods fighting over the same shouldRender() decision.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityDistanceCullMixin {

	@Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
	private void boosters$cullDistantEntities(Entity entity, Frustum frustum, double camX, double camY, double camZ,
			CallbackInfoReturnable<Boolean> cir) {
		BoostersConfig config = BoostersConfig.get();
		if (!config.enableEntityCulling || ModCompat.shouldDeferEntityCulling()) {
			return;
		}

		double dx = entity.getX() - camX;
		double dy = entity.getY() - camY;
		double dz = entity.getZ() - camZ;
		double distSq = dx * dx + dy * dy + dz * dz;
		double maxDist = config.entityCullingMaxDistance * ModCompat.distanceMultiplier();
		double maxDistSq = maxDist * maxDist;

		if (distSq > maxDistSq) {
			BoostersStats.incrementEntitiesCulled();
			cir.setReturnValue(false);
		}
	}
}
