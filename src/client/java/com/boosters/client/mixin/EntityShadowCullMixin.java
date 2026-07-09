package com.boosters.client.mixin;

import com.boosters.BoostersConfig;
import com.boosters.compat.ModCompat;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Skips ground-shadow rendering for entities beyond {@link BoostersConfig#entityDetailCullDistance}
 * (the same distance nametags already disappear at). A shadow radius of 0 makes vanilla's own
 * shadow code short-circuit before it does any chunk/block-state lookups for the shadow shape -
 * this only removes the decorative blob under distant entities, it never touches their model,
 * pose, or animation, so there's no risk of stutter or desync from this.
 */
@Mixin(EntityRenderer.class)
public abstract class EntityShadowCullMixin {

	@Inject(method = "getShadowRadius", at = @At("HEAD"), cancellable = true)
	private void boosters$cullDistantShadows(EntityRenderState state, CallbackInfoReturnable<Float> cir) {
		BoostersConfig config = BoostersConfig.get();
		if (!config.enableEntityCulling) {
			return;
		}

		double cullDist = config.entityDetailCullDistance * ModCompat.distanceMultiplier();
		double cullDistSq = cullDist * cullDist;
		if (state.distanceToCameraSq > cullDistSq) {
			cir.setReturnValue(0.0f);
		}
	}
}
