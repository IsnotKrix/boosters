package com.boosters.client.mixin;

import com.boosters.BoostersConfig;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Tightens the distance at which nameplates/extra decoration are shown, independent
 * of the entity's own render-distance cutoff (EntityDistanceCullMixin).
 */
@Mixin(EntityRenderer.class)
public abstract class EntityNameTagCullMixin {

	@Inject(method = "shouldShowName", at = @At("HEAD"), cancellable = true)
	private void boosters$cullDistantNameTags(Entity entity, double distanceSq, CallbackInfoReturnable<Boolean> cir) {
		BoostersConfig config = BoostersConfig.get();
		if (!config.enableEntityCulling) {
			return;
		}

		double cullSq = config.entityDetailCullDistance * config.entityDetailCullDistance;
		if (distanceSq > cullSq) {
			cir.setReturnValue(false);
		}
	}
}
